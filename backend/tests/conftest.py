from collections.abc import AsyncGenerator

import pytest_asyncio
from httpx import ASGITransport, AsyncClient
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine
from sqlalchemy.pool import NullPool

from app.db.base import Base
from app.db.session import get_db_session
from app.main import app
from app.worker.dispatcher import JobDispatcher, get_job_dispatcher

TEST_DATABASE_URL = "postgresql+asyncpg://health_user:health_password@localhost:5433/medi_help_test"

# NullPool avoids reusing an asyncpg connection across the different event
# loops that pytest-asyncio spins up per test function.
test_engine = create_async_engine(TEST_DATABASE_URL, echo=False, poolclass=NullPool)
TestSessionLocal = async_sessionmaker(bind=test_engine, autoflush=False, expire_on_commit=False)


@pytest_asyncio.fixture(autouse=True)
async def prepare_database() -> AsyncGenerator[None, None]:
    async with test_engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield
    async with test_engine.begin() as conn:
        await conn.run_sync(Base.metadata.drop_all)


async def _override_get_db_session() -> AsyncGenerator[AsyncSession, None]:
    async with TestSessionLocal() as session:
        yield session


app.dependency_overrides[get_db_session] = _override_get_db_session


class NoopJobDispatcher(JobDispatcher):
    def enqueue(self, job_id) -> None:
        del job_id


app.dependency_overrides[get_job_dispatcher] = NoopJobDispatcher


@pytest_asyncio.fixture
async def client() -> AsyncGenerator[AsyncClient, None]:
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        yield ac


@pytest_asyncio.fixture
async def db_session() -> AsyncGenerator[AsyncSession, None]:
    async with TestSessionLocal() as session:
        yield session
