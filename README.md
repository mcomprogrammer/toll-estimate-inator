# Toll Estimate Inator

Spring Boot API scaffold based on the three-page **Backend Assignment - Toll Plazas Between Two Pincodes** PDF. This stage establishes the build, API records, controller/service separation, and an exception-handling entry point. It does not implement toll lookup.

Java 21 language target, Spring Boot 4.1.1, Maven Wrapper. No preview features, Lombok, database, or geographic libraries. Spring MVC, validation, and cache starters are included; validation and caching behavior are deliberately deferred.

## Run and verify

Install JDK 21 or a compatible newer JDK and set `JAVA_HOME` to its installation directory. Initial builds need internet access to download Maven and dependencies.

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

On macOS/Linux use `./mvnw` instead. Import `pom.xml` as a Maven project in IntelliJ. The service listens on port 8080. No Google key or CSV is required for the scaffold.

In Postman, send `POST http://localhost:8080/api/v1/toll-plazas` with `Content-Type: application/json`:

```json
{"sourcePincode":"110001","destinationPincode":"560001"}
```

Current scaffold response is **501 Not Implemented**:

```json
{"error":"Toll plaza lookup is not implemented in this scaffold"}
```

This is a temporary scaffold response, not an assignment success response. There are no fabricated routes or toll results. Step 1 request validation is active: missing, blank, malformed, and equal pincodes return HTTP 400 with the PDF's specified error body. Framework-level errors outside this request contract still use Spring's default behavior.

## Assignment contract

The eventual success response has the exact field names from the PDF:

```json
{
  "route": {
    "sourcePincode": "110001",
    "destinationPincode": "560001",
    "distanceInKm": 2100
  },
  "tollPlazas": [
    {"name":"Toll Plaza 1","latitude":28.7041,"longitude":77.1025,"distanceFromSource":200}
  ]
}
```

These are illustrative PDF values, not calculated results. No tolls must retain the route object and return `"tollPlazas": []`. The PDF defines these validation error bodies:

```json
{"error":"Invalid source or destination pincode"}
```

```json
{"error":"Source and destination pincodes cannot be the same"}
```

## Requirements to resolve before implementation

- The PDF gives no HTTP statuses and calls the empty-list response an error despite using the success shape. Proposed: 200 for a found route with zero tolls and 400 for invalid or equal pincodes.
- Request validation now requires JSON strings matching `[1-9][0-9]{5}`, rejects missing/null/blank values, and returns HTTP 400. Pincodes Google cannot resolve will be handled separately during Step 3. Input is not trimmed or normalized.
- `distanceFromSource` has no stated unit or rounding rule. Proposed: kilometres, consistent with `distanceInKm`; determine precision before implementation.
- The evaluation mentions database operations/upserts, but the functional requirements only specify a CSV and caching. No persistence is scaffolded; clarify only if a database is expected later.
- Inspect the actual supplied CSV before defining its schema, duplicate handling, or malformed-row policy. No sample toll data has been invented.
- Cache duration, size, and external API failure/no-route behavior are unspecified and deferred.

The user's scope explicitly overrides the PDF's Mappls 80–90% matching target. That accuracy target is outside this project's intended implementation scope.

## Planned implementation, not included yet

1. Load the supplied toll CSV once at startup.
2. Resolve the two Indian pincodes with Google Geocoding.
3. Request a driving route with detailed geometry from Google Routes.
4. Use a small Haversine helper to match each toll within 500 metres of any returned route point.
5. Estimate distance from the source by summing consecutive route-point distances up to the closest point, then sort tolls by that estimate.
6. Return the assignment JSON and cache successful results by the ordered source/destination pair.

The point-based approximation can miss tolls between widely spaced points and include tolls on nearby roads. Detailed geometry helps but does not guarantee accuracy. Cumulative Haversine distance is also an approximation. This approach does not claim the PDF's 80–90% agreement with Mappls and needs no JTS or coordinate projection.

Future implementation should add Google configuration via environment variables, timeouts and external error handling, CSV loading, validation, caching, and focused tests. Keep API keys out of source control. None of those integrations or algorithms runs in this scaffold.

## Structure

- `api`: controller and request/response records matching the PDF.
- `service`: lookup entry point, currently throwing an explicit not-implemented exception.
- `exception`: scaffold exception and JSON error mapping.
- `src/test`: application startup and endpoint wiring checks, with no external API calls.

Reference for the selected framework: [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html).
