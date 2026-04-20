# Contract: Merchant Location

**Feature Branch**: `001-merchant-onboarding`
**Date**: 2026-04-20 (updated)
**Scope**: Location management and geofence-based merchant discovery

---

## Data Model Note

Location fields are stored as columns embedded directly in `merchant_applications` (not a separate table). The implementation uses `DOUBLE PRECISION` for coordinates to support Haversine proximity queries efficiently.

---

## Endpoints

### 1. PUT /v1/merchants/{merchantId}/location

Set or replace the merchant's location and geofence radius. Idempotent — always returns `200 OK` whether creating or updating.

**Request**

```http
PUT /v1/merchants/{merchantId}/location
Content-Type: application/json
```

```json
{
  "latitude": 53.0793,
  "longitude": 8.8017,
  "geofenceRadiusMeters": 100,
  "googlePlaceId": "ChIJuXHBrNcOrkcRrRpMbW97Uus"
}
```

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `latitude` | number | YES | `DOUBLE` in range `[-90, 90]` |
| `longitude` | number | YES | `DOUBLE` in range `[-180, 180]` |
| `geofenceRadiusMeters` | integer | YES | > 0 |
| `googlePlaceId` | string | NO | Stored as-is without Maps API validation; null = no Place ID |

**Response `200 OK`**

```json
{
  "merchantId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "latitude": 53.0793,
  "longitude": 8.8017,
  "geofenceRadiusMeters": 100,
  "googlePlaceId": "ChIJuXHBrNcOrkcRrRpMbW97Uus"
}
```

**Error responses**

| Status | Condition |
|--------|-----------|
| `400 Bad Request` | `latitude` out of range, `longitude` out of range, `geofenceRadiusMeters` ≤ 0 |
| `404 Not Found` | `{merchantId}` does not exist |

---

### 2. GET /v1/merchants/{merchantId}/location

Retrieve the location record for a merchant.

**Response `200 OK`**

```json
{
  "merchantId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "latitude": 53.0793,
  "longitude": 8.8017,
  "geofenceRadiusMeters": 100,
  "googlePlaceId": "ChIJuXHBrNcOrkcRrRpMbW97Uus"
}
```

`googlePlaceId` is `null` when not set.

**Error responses**

| Status | Condition |
|--------|-----------|
| `404 Not Found` | Merchant does not exist, or merchant has no location set |

---

### 3. DELETE /v1/merchants/{merchantId}/location

Removes all location data from the merchant (latitude, longitude, geofenceRadiusMeters, googlePlaceId).

**Response `204 No Content`**

**Error responses**

| Status | Condition |
|--------|-----------|
| `404 Not Found` | `{merchantId}` does not exist |

---

### 4. GET /v1/merchants/nearby

Find merchants whose store location falls within `radiusMeters` of the given coordinates. Intended for mobile geofence-enter events. Only merchants with a location set are returned.

**Request**

```http
GET /v1/merchants/nearby?lat=53.0793&lng=8.8017&radiusMeters=500
```

| Parameter | Type | Required | Default |
|-----------|------|----------|---------|
| `lat` | number | YES | — |
| `lng` | number | YES | — |
| `radiusMeters` | number | NO | 500 |

**Response `200 OK`**

```json
{
  "merchants": [
    {
      "merchantId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "legalBusinessName": "Acme Coffee GmbH",
      "latitude": 53.0793,
      "longitude": 8.8017,
      "geofenceRadiusMeters": 100,
      "googlePlaceId": "ChIJuXHBrNcOrkcRrRpMbW97Uus",
      "distanceMeters": 12.4
    }
  ]
}
```

Results are ordered by ascending `distanceMeters`. Empty array (not 404) when no merchants are found nearby.

---

## Validation Rules

| Field | Rule |
|-------|------|
| `latitude` | `DOUBLE` in range `[-90, 90]` |
| `longitude` | `DOUBLE` in range `[-180, 180]` |
| `geofenceRadiusMeters` | Integer > 0 |
| `googlePlaceId` | Nullable `VARCHAR(255)`; stored as-is without Maps API validation |
| `radiusMeters` (nearby) | Must be > 0 |

---

## Tenant Isolation

- The `{merchantId}` path variable identifies both the target merchant and the tenant scope.
- The authenticated principal must be the merchant identified by `{merchantId}` or hold an internal admin role.
- `GET /v1/merchants/nearby` is intentionally unauthenticated or broadly accessible — it returns only public merchant presence data (name, coordinates, geofence).
