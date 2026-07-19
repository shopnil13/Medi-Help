"""link lab records to processing jobs

Revision ID: d4f37c90a126
Revises: a31f46d8c902
Create Date: 2026-07-15 18:00:00

"""

from collections.abc import Sequence

import sqlalchemy as sa

from alembic import op

revision: str = "d4f37c90a126"
down_revision: str | Sequence[str] | None = "a31f46d8c902"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column("biomarkers", sa.Column("source_job_id", sa.UUID(), nullable=True))
    op.create_foreign_key(
        "fk_biomarkers_source_job_id",
        "biomarkers",
        "processing_jobs",
        ["source_job_id"],
        ["id"],
        ondelete="SET NULL",
    )
    op.create_index(
        op.f("ix_biomarkers_source_job_id"),
        "biomarkers",
        ["source_job_id"],
        unique=False,
    )
    op.add_column("vital_records", sa.Column("source_job_id", sa.UUID(), nullable=True))
    op.create_foreign_key(
        "fk_vital_records_source_job_id",
        "vital_records",
        "processing_jobs",
        ["source_job_id"],
        ["id"],
        ondelete="SET NULL",
    )
    op.create_index(
        op.f("ix_vital_records_source_job_id"),
        "vital_records",
        ["source_job_id"],
        unique=False,
    )


def downgrade() -> None:
    op.drop_index(op.f("ix_vital_records_source_job_id"), table_name="vital_records")
    op.drop_constraint(
        "fk_vital_records_source_job_id",
        "vital_records",
        type_="foreignkey",
    )
    op.drop_column("vital_records", "source_job_id")
    op.drop_index(op.f("ix_biomarkers_source_job_id"), table_name="biomarkers")
    op.drop_constraint("fk_biomarkers_source_job_id", "biomarkers", type_="foreignkey")
    op.drop_column("biomarkers", "source_job_id")
