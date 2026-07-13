"""add extraction confirmation fields

Revision ID: 52c42b1a8ef4
Revises: c6e34031a4c2
Create Date: 2026-07-13 21:20:00

"""

from collections.abc import Sequence

import sqlalchemy as sa

from alembic import op

revision: str = "52c42b1a8ef4"
down_revision: str | Sequence[str] | None = "c6e34031a4c2"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column("processing_jobs", sa.Column("confirmed_result_json", sa.JSON(), nullable=True))
    op.add_column(
        "processing_jobs",
        sa.Column("confirmed_at", sa.DateTime(timezone=True), nullable=True),
    )


def downgrade() -> None:
    op.drop_column("processing_jobs", "confirmed_at")
    op.drop_column("processing_jobs", "confirmed_result_json")
