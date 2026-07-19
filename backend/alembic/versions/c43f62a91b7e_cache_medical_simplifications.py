"""cache medical simplifications

Revision ID: c43f62a91b7e
Revises: f81a2c7d9e44
Create Date: 2026-07-19 23:20:00

"""

from collections.abc import Sequence

import sqlalchemy as sa

from alembic import op

revision: str = "c43f62a91b7e"
down_revision: str | Sequence[str] | None = "f81a2c7d9e44"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "biomarkers",
        sa.Column("explanation_simplified", sa.String(length=500), nullable=True),
    )
    op.add_column(
        "biomarkers",
        sa.Column("status_explanation", sa.String(length=500), nullable=True),
    )
    op.add_column(
        "biomarkers",
        sa.Column("details_simplified", sa.Text(), nullable=True),
    )
    op.add_column(
        "biomarkers",
        sa.Column("ask_doctor", sa.Boolean(), server_default=sa.false(), nullable=False),
    )
    op.add_column(
        "vital_records",
        sa.Column("source_biomarker_id", sa.UUID(), nullable=True),
    )
    op.create_foreign_key(
        "fk_vital_records_source_biomarker_id",
        "vital_records",
        "biomarkers",
        ["source_biomarker_id"],
        ["id"],
        ondelete="SET NULL",
    )
    op.create_index(
        "ix_vital_records_source_biomarker_id",
        "vital_records",
        ["source_biomarker_id"],
    )


def downgrade() -> None:
    op.drop_index("ix_vital_records_source_biomarker_id", table_name="vital_records")
    op.drop_constraint(
        "fk_vital_records_source_biomarker_id",
        "vital_records",
        type_="foreignkey",
    )
    op.drop_column("vital_records", "source_biomarker_id")
    op.drop_column("biomarkers", "ask_doctor")
    op.drop_column("biomarkers", "details_simplified")
    op.drop_column("biomarkers", "status_explanation")
    op.drop_column("biomarkers", "explanation_simplified")
