"""deduplicate health connect vitals

Revision ID: f81a2c7d9e44
Revises: d4f37c90a126
Create Date: 2026-07-19 22:00:00

"""

from collections.abc import Sequence

import sqlalchemy as sa

from alembic import op

revision: str = "f81a2c7d9e44"
down_revision: str | Sequence[str] | None = "d4f37c90a126"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.execute("""
        DELETE FROM vital_records
        WHERE id IN (
            SELECT id
            FROM (
                SELECT
                    id,
                    ROW_NUMBER() OVER (
                        PARTITION BY user_id, metric_type, source, recorded_at
                        ORDER BY created_at, id
                    ) AS duplicate_number
                FROM vital_records
                WHERE source = 'health_connect'
            ) AS duplicate_records
            WHERE duplicate_number > 1
        )
        """)
    op.create_index(
        "uq_vital_records_health_connect_identity",
        "vital_records",
        ["user_id", "metric_type", "source", "recorded_at"],
        unique=True,
        postgresql_where=sa.text("source = 'health_connect'"),
        sqlite_where=sa.text("source = 'health_connect'"),
    )


def downgrade() -> None:
    op.drop_index(
        "uq_vital_records_health_connect_identity",
        table_name="vital_records",
        postgresql_where=sa.text("source = 'health_connect'"),
    )
