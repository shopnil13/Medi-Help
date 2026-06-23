import uuid
from datetime import datetime

from pydantic import BaseModel, ConfigDict, EmailStr



class UserResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    full_name: str
    email: EmailStr
    is_active: bool
    is_verified: bool
    created_at: datetime

    