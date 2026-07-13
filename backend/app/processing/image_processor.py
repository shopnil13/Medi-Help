from io import BytesIO

import cv2
import fitz
import numpy as np
from PIL import Image, ImageOps


def document_to_preprocessed_images(data: bytes, content_type: str) -> list[bytes]:
    if content_type == "application/pdf":
        document = fitz.open(stream=data, filetype="pdf")
        try:
            source_images = [
                Image.open(BytesIO(page.get_pixmap(dpi=200, alpha=False).tobytes("png"))).copy()
                for page in document
            ]
        finally:
            document.close()
    else:
        source_images = [Image.open(BytesIO(data)).copy()]

    return [_preprocess_image(image) for image in source_images]


def _preprocess_image(image: Image.Image) -> bytes:
    oriented = ImageOps.exif_transpose(image).convert("RGB")
    rgb = np.array(oriented)
    gray = cv2.cvtColor(rgb, cv2.COLOR_RGB2GRAY)
    deskewed = _deskew(gray)
    denoised = cv2.medianBlur(deskewed, 3)
    contrasted = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8)).apply(denoised)
    _, thresholded = cv2.threshold(
        contrasted,
        0,
        255,
        cv2.THRESH_BINARY + cv2.THRESH_OTSU,
    )
    success, encoded = cv2.imencode(".png", thresholded)
    if not success:
        raise ValueError("Image preprocessing failed.")
    return encoded.tobytes()


def _deskew(gray: np.ndarray) -> np.ndarray:
    coordinates = np.column_stack(np.where(gray < 220))
    if len(coordinates) < 20:
        return gray
    angle = cv2.minAreaRect(coordinates)[-1]
    angle = -(90 + angle) if angle < -45 else -angle
    if abs(angle) < 0.1 or abs(angle) > 15:
        return gray
    height, width = gray.shape
    matrix = cv2.getRotationMatrix2D((width / 2, height / 2), angle, 1.0)
    return cv2.warpAffine(
        gray,
        matrix,
        (width, height),
        flags=cv2.INTER_CUBIC,
        borderMode=cv2.BORDER_REPLICATE,
    )
