from collections import defaultdict
from datetime import datetime
from uuid import UUID

from fastapi import HTTPException, status
from sqlalchemy import select, tuple_
from sqlalchemy.dialects.postgresql import insert as postgresql_insert
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.document import Document
from app.models.vital_record import VitalRecord
from app.schemas.vital import VitalCreate, VitalMetricType, VitalSource, VitalTrend

METRIC_NAMES = {
    "heart_rate": "Heart rate",
    "blood_pressure_systolic": "Blood pressure (systolic)",
    "blood_pressure_diastolic": "Blood pressure (diastolic)",
    "blood_glucose": "Blood glucose",
    "weight": "Weight",
}


async def create_vital(db: AsyncSession, user_id: UUID, payload: VitalCreate) -> VitalRecord:
    records = await create_vitals(db, user_id, [payload])
    return records[0]


async def create_vitals(
    db: AsyncSession,
    user_id: UUID,
    payloads: list[VitalCreate],
) -> list[VitalRecord]:
    document_ids = {
        payload.source_document_id for payload in payloads if payload.source_document_id is not None
    }
    if document_ids:
        result = await db.execute(
            select(Document.id).where(
                Document.user_id == user_id,
                Document.id.in_(document_ids),
            )
        )
        owned_ids = set(result.scalars().all())
        if owned_ids != document_ids:
            raise HTTPException(status.HTTP_404_NOT_FOUND, "Source document not found.")

    regular_records: dict[int, VitalRecord] = {}
    health_connect_payloads: dict[tuple[str, datetime], VitalCreate] = {}
    for index, payload in enumerate(payloads):
        if payload.source == "health_connect":
            health_connect_payloads.setdefault((payload.metric_type, payload.recorded_at), payload)
            continue
        record = _record_from_payload(user_id, payload)
        regular_records[index] = record
        db.add(record)

    if health_connect_payloads:
        values = [_record_values(user_id, payload) for payload in health_connect_payloads.values()]
        statement = postgresql_insert(VitalRecord).values(values)
        await db.execute(
            statement.on_conflict_do_nothing(
                index_elements=["user_id", "metric_type", "source", "recorded_at"],
                index_where=VitalRecord.source == "health_connect",
            )
        )

    await db.commit()
    for record in regular_records.values():
        await db.refresh(record)

    health_connect_records: dict[tuple[str, datetime], VitalRecord] = {}
    if health_connect_payloads:
        result = await db.execute(
            select(VitalRecord).where(
                VitalRecord.user_id == user_id,
                VitalRecord.source == "health_connect",
                tuple_(VitalRecord.metric_type, VitalRecord.recorded_at).in_(
                    health_connect_payloads.keys()
                ),
            )
        )
        health_connect_records = {
            (record.metric_type, record.recorded_at): record for record in result.scalars().all()
        }

    records: list[VitalRecord] = []
    included_health_connect: set[tuple[str, datetime]] = set()
    for index, payload in enumerate(payloads):
        if payload.source != "health_connect":
            records.append(regular_records[index])
            continue
        key = (payload.metric_type, payload.recorded_at)
        if key not in included_health_connect:
            records.append(health_connect_records[key])
            included_health_connect.add(key)
    return records


async def list_vitals(
    db: AsyncSession,
    user_id: UUID,
    metric_type: VitalMetricType | None = None,
    start_date: datetime | None = None,
    end_date: datetime | None = None,
    source: VitalSource | None = None,
) -> list[VitalRecord]:
    _validate_range(start_date, end_date)
    stmt = select(VitalRecord).where(VitalRecord.user_id == user_id)
    if metric_type is not None:
        stmt = stmt.where(VitalRecord.metric_type == metric_type)
    if start_date is not None:
        stmt = stmt.where(VitalRecord.recorded_at >= start_date)
    if end_date is not None:
        stmt = stmt.where(VitalRecord.recorded_at <= end_date)
    if source is not None:
        stmt = stmt.where(VitalRecord.source == source)
    result = await db.execute(stmt.order_by(VitalRecord.recorded_at.asc()))
    return list(result.scalars().all())


async def get_vital_trends(
    db: AsyncSession,
    user_id: UUID,
    metric_type: VitalMetricType | None = None,
    start_date: datetime | None = None,
    end_date: datetime | None = None,
    source: VitalSource | None = None,
) -> list[VitalTrend]:
    records = await list_vitals(db, user_id, metric_type, start_date, end_date, source)
    grouped: dict[tuple[str, str, str], list[VitalRecord]] = defaultdict(list)
    for record in records:
        grouped[(record.metric_type, record.metric_name, record.unit)].append(record)

    trends = []
    for (record_type, name, unit), points in grouped.items():
        values = [float(point.value_numeric) for point in points]
        direction = "stable"
        if values[-1] > values[0]:
            direction = "up"
        elif values[-1] < values[0]:
            direction = "down"
        trends.append(
            VitalTrend(
                metric_type=record_type,
                metric_name=name,
                unit=unit,
                count=len(values),
                minimum=min(values),
                maximum=max(values),
                average=sum(values) / len(values),
                latest=values[-1],
                direction=direction,
                points=points,
            )
        )
    return trends


def _metric_name(payload: VitalCreate) -> str:
    if payload.metric_type == "custom":
        if payload.metric_name is None or not payload.metric_name.strip():
            raise HTTPException(
                status.HTTP_422_UNPROCESSABLE_CONTENT,
                "Custom metrics require a metric name.",
            )
        return payload.metric_name.strip()
    return METRIC_NAMES[payload.metric_type]


def _record_values(user_id: UUID, payload: VitalCreate) -> dict[str, object]:
    return {
        "user_id": user_id,
        "metric_type": payload.metric_type,
        "metric_name": _metric_name(payload),
        "value_numeric": payload.value_numeric,
        "unit": payload.unit.strip(),
        "recorded_at": payload.recorded_at,
        "source": payload.source,
        "source_document_id": payload.source_document_id,
        "notes": payload.notes,
    }


def _record_from_payload(user_id: UUID, payload: VitalCreate) -> VitalRecord:
    return VitalRecord(**_record_values(user_id, payload))


def _validate_range(start_date: datetime | None, end_date: datetime | None) -> None:
    if start_date is not None and end_date is not None and start_date > end_date:
        raise HTTPException(
            status.HTTP_422_UNPROCESSABLE_CONTENT,
            "start_date must be before or equal to end_date.",
        )
