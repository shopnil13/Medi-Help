"""create vital records and biomarkers

Revision ID: a31f46d8c902
Revises: 90ee81fb2b47
Create Date: 2026-07-13 23:00:00

"""

from collections.abc import Sequence

import sqlalchemy as sa

from alembic import op

revision: str = "a31f46d8c902"
down_revision: str | Sequence[str] | None = "90ee81fb2b47"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "vital_records",
        sa.Column("id", sa.UUID(), nullable=False),
        sa.Column("user_id", sa.UUID(), nullable=False),
        sa.Column("metric_type", sa.String(length=40), nullable=False),
        sa.Column("metric_name", sa.String(length=120), nullable=False),
        sa.Column("value_numeric", sa.Numeric(precision=14, scale=4), nullable=False),
        sa.Column("unit", sa.String(length=30), nullable=False),
        sa.Column("recorded_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("source", sa.String(length=30), nullable=False),
        sa.Column("source_document_id", sa.UUID(), nullable=True),
        sa.Column("notes", sa.Text(), nullable=True),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.ForeignKeyConstraint(["source_document_id"], ["documents.id"], ondelete="SET NULL"),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index(op.f("ix_vital_records_metric_type"), "vital_records", ["metric_type"])
    op.create_index(op.f("ix_vital_records_recorded_at"), "vital_records", ["recorded_at"])
    op.create_index(op.f("ix_vital_records_source"), "vital_records", ["source"])
    op.create_index(
        op.f("ix_vital_records_source_document_id"), "vital_records", ["source_document_id"]
    )
    op.create_index(op.f("ix_vital_records_user_id"), "vital_records", ["user_id"])

    op.create_table(
        "biomarkers",
        sa.Column("id", sa.UUID(), nullable=False),
        sa.Column("user_id", sa.UUID(), nullable=False),
        sa.Column("source_document_id", sa.UUID(), nullable=False),
        sa.Column("name", sa.String(length=120), nullable=False),
        sa.Column("normalized_name", sa.String(length=120), nullable=False),
        sa.Column("value_numeric", sa.Numeric(precision=14, scale=4), nullable=True),
        sa.Column("value_text", sa.String(length=200), nullable=True),
        sa.Column("unit", sa.String(length=30), nullable=True),
        sa.Column("reference_range_text", sa.String(length=120), nullable=True),
        sa.Column("status", sa.String(length=20), nullable=False),
        sa.Column("recorded_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("confidence_score", sa.Numeric(precision=4, scale=3), nullable=True),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.ForeignKeyConstraint(["source_document_id"], ["documents.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index(op.f("ix_biomarkers_normalized_name"), "biomarkers", ["normalized_name"])
    op.create_index(op.f("ix_biomarkers_recorded_at"), "biomarkers", ["recorded_at"])
    op.create_index(op.f("ix_biomarkers_source_document_id"), "biomarkers", ["source_document_id"])
    op.create_index(op.f("ix_biomarkers_user_id"), "biomarkers", ["user_id"])


def downgrade() -> None:
    op.drop_index(op.f("ix_biomarkers_user_id"), table_name="biomarkers")
    op.drop_index(op.f("ix_biomarkers_source_document_id"), table_name="biomarkers")
    op.drop_index(op.f("ix_biomarkers_recorded_at"), table_name="biomarkers")
    op.drop_index(op.f("ix_biomarkers_normalized_name"), table_name="biomarkers")
    op.drop_table("biomarkers")
    op.drop_index(op.f("ix_vital_records_user_id"), table_name="vital_records")
    op.drop_index(op.f("ix_vital_records_source_document_id"), table_name="vital_records")
    op.drop_index(op.f("ix_vital_records_source"), table_name="vital_records")
    op.drop_index(op.f("ix_vital_records_recorded_at"), table_name="vital_records")
    op.drop_index(op.f("ix_vital_records_metric_type"), table_name="vital_records")
    op.drop_table("vital_records")
