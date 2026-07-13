import asyncio
import base64
from abc import ABC, abstractmethod
from io import BytesIO

import httpx
import pytesseract
from PIL import Image

from app.core.config import get_settings


class OCRProvider(ABC):
    @abstractmethod
    async def extract_text(self, images: list[bytes]) -> str:
        raise NotImplementedError


class TesseractOCRProvider(OCRProvider):
    def __init__(self, command: str | None = None) -> None:
        self.command = command

    async def extract_text(self, images: list[bytes]) -> str:
        def run() -> str:
            if self.command:
                pytesseract.pytesseract.tesseract_cmd = self.command
            pages = [pytesseract.image_to_string(Image.open(BytesIO(image))) for image in images]
            return "\n\n".join(page.strip() for page in pages if page.strip())

        return await asyncio.to_thread(run)


class GoogleVisionOCRProvider(OCRProvider):
    def __init__(self, api_key: str) -> None:
        if not api_key:
            raise ValueError("GOOGLE_VISION_API_KEY is required for Google Vision OCR.")
        self.api_key = api_key

    async def extract_text(self, images: list[bytes]) -> str:
        requests = [
            {
                "image": {"content": base64.b64encode(image).decode("ascii")},
                "features": [{"type": "DOCUMENT_TEXT_DETECTION"}],
            }
            for image in images
        ]
        async with httpx.AsyncClient(timeout=60) as client:
            response = await client.post(
                "https://vision.googleapis.com/v1/images:annotate",
                params={"key": self.api_key},
                json={"requests": requests},
            )
            response.raise_for_status()
        pages = []
        for result in response.json().get("responses", []):
            error = result.get("error")
            if error:
                raise RuntimeError(error.get("message", "Google Vision OCR failed."))
            pages.append(result.get("fullTextAnnotation", {}).get("text", ""))
        return "\n\n".join(page.strip() for page in pages if page.strip())


def get_ocr_provider() -> OCRProvider:
    settings = get_settings()
    if settings.ocr_backend == "tesseract":
        return TesseractOCRProvider(settings.tesseract_command)
    if settings.ocr_backend == "google_vision":
        return GoogleVisionOCRProvider(settings.google_vision_api_key or "")
    raise ValueError(f"Unsupported OCR backend: {settings.ocr_backend}")
