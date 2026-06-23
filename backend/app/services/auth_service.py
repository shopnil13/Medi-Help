from datetime import datetime, timedelta, timezone
from uuid import UUID

from fastapi import HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import get_settings
from app.core.security import (
    create_access_token,
    create_plain_refresh_token,
    get_password_hash,
    hash_refresh_token,
    verify_password,
)

from app.models.refresh_token import RefreshToken
from app.models.user import User
from app.schemas.auth import AuthTokenResponse, LoginRequest, RegisterRequest


settings = get_settings()

async def get_user_by_email(db: AsyncSession, email: str) -> User | None:
    stmt= select(User).where(User.email == email.lower())
    result = await db.execute(stmt)
    return result.scalar_one_or_none()


async def get_user_by_id(db: AsyncSession, user_id: UUID) -> User | None:
    return await db.get(User, user_id)


async def create_refresh_token_record(
        db: AsyncSession,
        user_id: UUID,
) -> str:
    plain_refresh_token = create_plain_refresh_token()
    token_hash = hash_refresh_token(plain_refresh_token)

    expires_at = datetime.now(timezone.utc) + timedelta(
        days=settings.refresh_token_expire_days
    )

    refresh_token = RefreshToken(
        user_id=user_id,
        token_hash=token_hash,
        expires_at=expires_at,
    )

    db.add(refresh_token)

    return plain_refresh_token


async def register_user(
        db: AsyncSession,
        payload: RegisterRequest,
) -> AuthTokenResponse:
    existing_user = await get_user_by_email(db, payload.email)

    if existing_user is not None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="An account with this emial already exists."
        )
    
    user = User(
        full_name=payload.full_name.strip(),
        email=payload.email.lower(),
        password_hash=get_password_hash(payload.password),
    )

    db.add(user)
    await db.flush()


    access_token = create_access_token(str(user.id))
    refresh_token = await create_refresh_token_record(db, user.id)

    await db.commit()

    return AuthTokenResponse(
        access_token=access_token,
        refresh_token=refresh_token,
    )


async def login_user(
        db: AsyncSession,
        payload: LoginRequest,
) -> AuthTokenResponse:
    
    user = await get_user_by_email(db, payload.email)

    if user is None or not verify_password(payload.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password.",
        )
    
    if not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="This account is disabled",
        )
    

    acces_token = create_access_token(str(user.id))
    refresh_token = await create_refresh_token_record(db, user.id)

    await db.commit()

    return AuthTokenResponse(
        access_token=create_access_token,
        refresh_token=refresh_token,
    )



async def refresh_access_token(
        db: AsyncSession,
        plain_refresh_token: str,
) -> AuthTokenResponse:
    token_hash = hash_refresh_token(plain_refresh_token)
    now = datetime.now(timezone.utc)

    stmt = select(RefreshToken).where(
        RefreshToken.token_hash == token_hash,
        RefreshToken.revoked_at.is_(None),
        RefreshToken.expires_at > now,
    )
    result = await db.execute(stmt)
    existing_token = result.scalar_one_or_none()

    if existing_token is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired refresh token",
        )
    
    user = await get_user_by_id(db, existing_token.user_id)

    if user is None or not user.is_active:
        raise HTTPException(
            status_code= status.HTTP_401_UNAUTHORIZED,
            detail="Invalid refresh token.",
        )
    
    existing_token.revoked_at = now


    access_token = create_access_token(str(user.id))
    new_refresh_token = await create_refresh_token_record(db, user.id)

    await db.commit()

    return AuthTokenResponse(
        access_token=access_token,
        refresh_token=new_refresh_token,
    )



async def logout_user(
        db: AsyncSession,
        plain_refresh_token: str,
) -> None:
    token_hash = hash_refresh_token(plain_refresh_token)

    stmt = select(RefreshToken).where(
        RefreshToken.token_hash == token_hash,
        RefreshToken.revoked_at.is_(None),
    )
    result = await db.execute(stmt)
    existing_token = result.scalar_one_or_none()

    if existing_token is not None:
        existing_token.revoked_at = datetime.now(timezone.utc)
        await db.commit()
        