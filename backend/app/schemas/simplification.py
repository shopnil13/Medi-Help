from pydantic import BaseModel, Field


class MedicineSimplification(BaseModel):
    purpose: str = Field(min_length=1, max_length=500)
    how_to_take: str = Field(min_length=1, max_length=500)


class BiomarkerSimplification(BaseModel):
    explanation: str = Field(min_length=1, max_length=500)
    status_meaning: str = Field(min_length=1, max_length=500)
    more_details: str = Field(min_length=1, max_length=1000)
