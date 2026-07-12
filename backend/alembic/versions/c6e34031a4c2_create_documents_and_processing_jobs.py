"""create documents and processing jobs

Revision ID: c6e34031a4c2
Revises: 87e9723e56da
Create Date: 2026-07-12 20:30:00

"""

from collections.abc import Sequence

import sqlalchemy as sa

from alembic import op

revision: str = "c6e34031a4c2"
down_revision: str | Sequence[str] | None = "87e9723e56da"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "documents",
        sa.Column("id", sa.UUID(), nullable=False),
        sa.Column("user_id", sa.UUID(), nullable=False),
        sa.Column("document_type", sa.String(length=20), nullable=False),
        sa.Column("original_filename", sa.String(length=255), nullable=False),
        sa.Column("content_type", sa.String(length=100), nullable=False),
        sa.Column("file_size_bytes", sa.BigInteger(), nullable=False),
        sa.Column("storage_provider", sa.String(length=20), nullable=False),
        sa.Column("storage_key", sa.String(length=500), nullable=False),
        sa.Column("status", sa.String(length=20), nullable=False),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.Column(
            "updated_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("storage_key"),
    )
    op.create_index(op.f("ix_documents_user_id"), "documents", ["user_id"], unique=False)
    op.create_table(
        "processing_jobs",
        sa.Column("id", sa.UUID(), nullable=False),
        sa.Column("document_id", sa.UUID(), nullable=False),
        sa.Column("user_id", sa.UUID(), nullable=False),
        sa.Column("status", sa.String(length=20), nullable=False),
        sa.Column("progress_percent", sa.Integer(), nullable=False),
        sa.Column("raw_text", sa.Text(), nullable=True),
        sa.Column("structured_result_json", sa.JSON(), nullable=True),
        sa.Column("error_message", sa.String(length=500), nullable=True),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.Column(
            "updated_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.Column("completed_at", sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(["document_id"], ["documents.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index(
        op.f("ix_processing_jobs_document_id"), "processing_jobs", ["document_id"], unique=False
    )
    op.create_index(
        op.f("ix_processing_jobs_user_id"), "processing_jobs", ["user_id"], unique=False
    )


def downgrade() -> None:
    op.drop_index(op.f("ix_processing_jobs_user_id"), table_name="processing_jobs")
    op.drop_index(op.f("ix_processing_jobs_document_id"), table_name="processing_jobs")
    op.drop_table("processing_jobs")
    op.drop_index(op.f("ix_documents_user_id"), table_name="documents")
    op.drop_table("documents")
