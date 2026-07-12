import uuid
from datetime import datetime, time

from sqlalchemy import DateTime, ForeignKey, String, Time, func
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base


class MedicationSchedule(Base):
    __tablename__ = "medication_schedules"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
    )

    medication_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("medications.id", ondelete="CASCADE"),
        index=True,
        nullable=False,
    )

    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        index=True,
        nullable=False,
    )

    time_of_day: Mapped[time] = mapped_column(Time, nullable=False)
    frequency_type: Mapped[str] = mapped_column(String(20), nullable=False, default="daily")
    days_of_week: Mapped[str | None] = mapped_column(String(20), nullable=True)
    meal_relation: Mapped[str] = mapped_column(String(20), nullable=False, default="unknown")
    dose_amount: Mapped[str | None] = mapped_column(String(50), nullable=True)
    notes: Mapped[str | None] = mapped_column(String(300), nullable=True)

    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False,
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False,
    )

    medication = relationship("Medication", back_populates="schedules")
