# MarkItDown Web

This subproject is an older Flask-based web application path for document-to-Markdown conversion.

## Status

- Not the primary delivery path of this repository
- Kept for historical and experimental web usage
- The recommended end-user project is still the Java CLI in [`../java/README.md`](../java/README.md)

## What It Provides

- Browser-based file upload and conversion flow
- Batch upload support
- Preview and download of Markdown results
- A separate Python web stack from the Java CLI

## Run Locally

```bash
cd conveter
pip install -r requirements.txt
python app.py
```

Then open [http://localhost:5000](http://localhost:5000).

## Notes

- OCR and format support in this path may differ from the Java CLI
- This path is lower priority and may not receive the same level of maintenance
- For public releases, prefer the Java CLI artifacts
