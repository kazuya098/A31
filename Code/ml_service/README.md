# Mandrillus ML service

This directory contains the inference part of the animal face
re-identification system. It is intentionally isolated from the existing
`Code/Dataprepare.py` and from the A31 frontend/backend implementation.

## API contract

The service is compatible with the A31 backend's existing algorithm client:

- `POST /predict`: multipart field `file`
- `POST /predict_batch`: repeated multipart field `files`
- `GET /health`: runtime and artifact status

An individual result has this shape:

```json
{
  "identity_id": 123,
  "confidence": 0.91,
  "heatmap_url": "/static/heat_xxx.jpg",
  "gallery_images": [
    {"year": "2020", "date": "2020-01-02", "image_url": "/static/gallery_xxx.jpg"}
  ]
}
```

The batch response wraps the same result objects in `{"results": [...]}`.
The `gallery_images` fields match the DTO expected by the A31 backend.

## External artifacts

The checkpoint and gallery are deliberately not committed. Configure them
with environment variables before starting the service:

```powershell
$env:MODEL_PATH = 'D:\path\to\clean_model.pth'
$env:GALLERY_DIR = 'D:\path\to\gallery'
$env:GALLERY_EMB_PATH = 'D:\path\to\gallery\gallery_emb.npy'
$env:GALLERY_LABEL_PATH = 'D:\path\to\gallery\gallery_labels (1).npy'
python -m uvicorn main:app --host 0.0.0.0 --port 8000
```

Run the command from this directory. `STATIC_DIR` and
`CONFIDENCE_THRESHOLD` are optional overrides. A missing artifact does not
make the module import fail; `/health` reports `degraded` and prediction
requests return HTTP 503 until the paths are configured.

## Connecting A31

The existing Spring backend already contains the client and does not need a
source change for this service. In a local deployment, configure the backend
through environment variables or an uncommitted local properties override:

```powershell
$env:ALGORITHM_SERVICE_URL = 'http://127.0.0.1:8000/predict'
$env:ALGORITHM_MOCK_ENABLED = 'false'
```

For a deployed service, use the reachable `/predict` URL instead. Keep the
backend URL and database credentials out of committed `.env` files.

## Scope and provenance

The service contains the Swin-Tiny embedding model and the gallery nearest-
neighbour matching path used by the existing ML API. The full Mandrillus
gallery, trained weights, and generated heatmaps are deployment data rather
than source files. The repository's small `Data/Mandrillus` sample remains
separate and is not used as a replacement for the production gallery.

The dataset and method references are recorded in
[`REFERENCES.md`](REFERENCES.md). The training scripts and experiment history
are not included here, so claims about the exact training loss should be
checked against the original experiment records before publication.
