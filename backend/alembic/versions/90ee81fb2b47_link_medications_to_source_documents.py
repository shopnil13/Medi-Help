"""link medications to source documents

Revision ID: 90ee81fb2b47
Revises: 52c42b1a8ef4
Create Date: 2026-07-13 22:00:00

"""

from collections.abc import Sequence

import sqlalchemy as sa

from alembic import op

revision: str = "90ee81fb2b47"
down_revision: str | Sequence[str] | None = "52c42b1a8ef4"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column("medications", sa.Column("source_document_id", sa.UUID(), nullable=True))
    op.add_column("medications", sa.Column("source_job_id", sa.UUID(), nullable=True))
    op.create_foreign_key(
        "fk_medications_source_document_id",
        "medications",
        "documents",
        ["source_document_id"],
        ["id"],
        ondelete="SET NULL",
    )
    op.create_foreign_key(
        "fk_medications_source_job_id",
        "medications",
        "processing_jobs",
        ["source_job_id"],
        ["id"],
        ondelete="SET NULL",
    )
    op.create_index(
        op.f("ix_medications_source_document_id"),
        "medications",
        ["source_document_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_medications_source_job_id"),
        "medications",
        ["source_job_id"],
        unique=False,
    )


def downgrade() -> None:
    op.drop_index(op.f("ix_medications_source_job_id"), table_name="medications")
    op.drop_index(op.f("ix_medications_source_document_id"), table_name="medications")
    op.drop_constraint("fk_medications_source_job_id", "medications", type_="foreignkey")
    op.drop_constraint("fk_medications_source_document_id", "medications", type_="foreignkey")
    op.drop_column("medications", "source_job_id")
    op.drop_column("medications", "source_document_id")
