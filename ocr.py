"""
PaddleOCR cloud job example.

Usage:
  set PADDLE_OCR_TOKEN=your-token
  python ocr.py C:\\path\\to\\file.pdf

Optional env vars:
  PADDLE_OCR_JOB_URL=https://paddleocr.aistudio-app.com/api/v2/ocr/jobs
  PADDLE_OCR_MODEL=PaddleOCR-VL-1.5
  PADDLE_OCR_POLL_INTERVAL=5
"""

import json
import os
import sys
import time

import requests

JOB_URL = os.getenv("PADDLE_OCR_JOB_URL", "https://paddleocr.aistudio-app.com/api/v2/ocr/jobs")
TOKEN = os.getenv("PADDLE_OCR_TOKEN")
MODEL = os.getenv("PADDLE_OCR_MODEL", "PaddleOCR-VL-1.5")
POLL_INTERVAL = int(os.getenv("PADDLE_OCR_POLL_INTERVAL", "5"))

OPTIONAL_PAYLOAD = {
    "useDocOrientationClassify": False,
    "useDocUnwarping": False,
    "useChartRecognition": False,
}


def require_token() -> str:
    if TOKEN:
        return TOKEN
    raise SystemExit("PADDLE_OCR_TOKEN is not set")


def submit_job(file_path: str) -> str:
    headers = {"Authorization": f"bearer {require_token()}"}
    print(f"Processing file: {file_path}")

    if file_path.startswith("http://") or file_path.startswith("https://"):
        headers["Content-Type"] = "application/json"
        payload = {
            "fileUrl": file_path,
            "model": MODEL,
            "optionalPayload": OPTIONAL_PAYLOAD,
        }
        response = requests.post(JOB_URL, json=payload, headers=headers, timeout=60)
    else:
        if not os.path.exists(file_path):
            raise SystemExit(f"File not found: {file_path}")

        with open(file_path, "rb") as file_obj:
            response = requests.post(
                JOB_URL,
                headers=headers,
                data={"model": MODEL, "optionalPayload": json.dumps(OPTIONAL_PAYLOAD)},
                files={"file": file_obj},
                timeout=60,
            )

    response.raise_for_status()
    payload = response.json()
    return payload["data"]["jobId"]


def wait_for_jsonl_url(job_id: str) -> str:
    headers = {"Authorization": f"bearer {require_token()}"}
    while True:
        response = requests.get(f"{JOB_URL}/{job_id}", headers=headers, timeout=60)
        response.raise_for_status()
        payload = response.json()["data"]
        state = payload["state"]

        if state == "done":
            return payload["resultUrl"]["jsonUrl"]
        if state == "failed":
            raise SystemExit(f"PaddleOCR job failed: {payload.get('errorMsg', 'unknown error')}")

        print(f"Current state: {state}")
        time.sleep(POLL_INTERVAL)


def download_markdown(jsonl_url: str) -> str:
    response = requests.get(jsonl_url, timeout=60)
    response.raise_for_status()

    markdown_blocks = []
    for line in response.text.splitlines():
        if not line.strip():
            continue
        item = json.loads(line)
        for result in item.get("result", {}).get("layoutParsingResults", []):
            markdown_text = result.get("markdown", {}).get("text")
            if markdown_text:
                markdown_blocks.append(markdown_text.strip())

    return "\n\n".join(markdown_blocks)


def main() -> None:
    if len(sys.argv) < 2:
        raise SystemExit("Usage: python ocr.py <local-file-or-url>")

    file_path = sys.argv[1]
    job_id = submit_job(file_path)
    print(f"Job submitted successfully. job id: {job_id}")
    jsonl_url = wait_for_jsonl_url(job_id)
    markdown = download_markdown(jsonl_url)
    print(markdown)


if __name__ == "__main__":
    main()
