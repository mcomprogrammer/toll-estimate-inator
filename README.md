# Toll Estimate Inator

Spring Boot API based on the three-page **Backend Assignment - Toll Plazas Between Two Pincodes** PDF. It provides the lookup workflow, startup loading of the supplied toll catalog, and approximate toll matching.

Java 21 language target, Spring Boot 4.1.1, Maven Wrapper. No preview features, Lombok, database, or geographic libraries. Spring MVC, validation, cache, and Apache Commons CSV dependencies are included; successful lookups use a simple in-memory cache.

## Run and verify

Install JDK 21 or a compatible newer JDK and set `JAVA_HOME` to its installation directory. Initial builds need internet access to download Maven and dependencies.

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
.\mvnw.cmd spring-boot:run
```

On macOS/Linux use `./mvnw` instead. Import `pom.xml` as a Maven project in IntelliJ. The service listens on port 8080. The bundled toll CSV is loaded at startup. A `GOOGLE_MAPS_API_KEY` is required for a successful lookup.

In Postman, send `POST http://localhost:8080/api/v1/toll-plazas` with `Content-Type: application/json`:

```json
{"sourcePincode":"110001","destinationPincode":"560001"}
```

The endpoint geocodes both pincodes, requests a driving route, matches nearby tolls, estimates their distance from the source, and returns the assignment response shape. Step 1 request validation is active: missing, blank, malformed, and equal pincodes return HTTP 400 with the PDF's specified error body. A valid route with no matching tolls returns HTTP 200 with an empty `tollPlazas` list. Framework-level errors outside this request contract still use Spring's default behavior.

## Google geocoding (Step 3)

`GoogleMapsClient.geocode(pincode)` sends one request and reads the first result's coordinates. It uses a 3-second connection timeout and a 5-second read timeout. A failed request or unsuccessful lookup throws `GoogleMapsException`. For this take-home, we trust Google's successful response: no postal-code rechecking, coordinate-range checks, or error-category hierarchy. Raw HTTP exceptions are not propagated because their URLs can contain the API key. Google client tests are omitted to keep this assignment small.

To supply the key on Windows, open the application's IntelliJ Run Configuration and add `GOOGLE_MAPS_API_KEY` under Environment variables. Alternatively set it through Windows' user environment-variable settings, then restart IntelliJ/your terminal before running Maven. Never paste the key into chat or commit it. No key is needed for the remaining automated tests; they do not call Google.

The endpoint calls geocoding separately for source and destination, then connects the returned coordinates to routing and toll matching. The client checks for a missing key when invoked, so the application can still start without credentials.

Reference: [Google Geocoding request and response](https://developers.google.com/maps/documentation/geocoding/requests-geocoding).

## Google driving route (Step 4)

`GoogleMapsClient.route(origin, destination)` calls the Routes API with a high-quality GeoJSON polyline. It reads the first route's distance and ordered geometry points. Google returns GeoJSON coordinates as `[longitude, latitude]`; the client converts them to its `Coordinates(latitude, longitude)` record for toll matching. An empty routes response raises `GoogleMapsException`; successful Google response fields are otherwise trusted to keep the client small.

Reference: [Google Routes computeRoutes](https://developers.google.com/maps/documentation/routes/reference/rest/v2/TopLevel/computeRoutes).

## Approximate toll matching (Step 5)

`TollMatcher` checks each toll against every route point using the Haversine formula, which estimates straight-line distance over the Earth's surface. A toll is included once when its closest route point is within `toll.matching-threshold-metres`, which defaults to 500 metres. This simple point-based approach can miss tolls between widely spaced route points and can include tolls on nearby roads; it does not claim the PDF's Mappls accuracy target.

## Caching (Step 7)

Successful lookup responses use Spring's default process-local in-memory cache with the ordered key `sourcePincode:destinationPincode`. Reversing the pincodes creates a different cache entry. Exceptions are not cached, and the cache is cleared when the application restarts.

Successful empty toll lists are cached too. There is no expiration or size limit. This demonstrates Spring caching for the assignment; it is not a verified retention policy for live Google data. [Google's Routes policies](https://developers.google.com/maps/documentation/routes/policies) restrict caching of Routes content. Review the applicable agreement before using this full-response cache with live Google results; clearing memory on restart does not resolve that restriction.

## Automated checks

Run `./mvnw.cmd verify` on Windows to run the tests and package the executable JAR in `target/`. The focused suite covers request validation, bundled CSV loading, tolls inside/outside the matching threshold, cumulative distance and sorting, no matching tolls, application startup, and cache reuse through Spring's proxy. Google calls are mocked in workflow and caching tests; the suite does not spend API calls or verify live credentials. Google client response tests are intentionally omitted for the simplified scope.

## Bundled toll data

The source file is bundled at `src/main/resources/data/toll_plaza_india_cleaned.csv` and is loaded from the classpath using Apache Commons CSV. Its inspected headers are `longitude,latitude,toll_name,geo_state`, with 1,536 data rows. The loader stores `toll_name`, `latitude`, and `longitude`; `geo_state` is accepted as extra metadata and ignored. Quoted CSV values are parsed correctly.

The catalog reads this fixed file once at startup and keeps an immutable list. Basic checks require a non-blank name and finite coordinates within latitude/longitude ranges. Invalid values stop startup with a record-specific message. The parser handles quoting; there is no configurable import system or separate loader abstraction. One focused test checks that the bundled file loads and latitude/longitude are mapped correctly.

## Assignment contract

The success response uses the exact field names from the PDF:

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

## Remaining implementation notes

- The PDF gives no HTTP statuses and calls the empty-list response an error despite using the success shape. This implementation uses 200 for a found route with zero tolls and 400 for invalid or equal pincodes.
- Request validation requires JSON strings matching `[1-9][0-9]{5}`, rejects missing/null/blank values, and returns HTTP 400. Pincodes Google cannot resolve raise `GoogleMapsException`. Input is not trimmed or normalized.
- `distanceFromSource` and `distanceInKm` are returned in kilometres without additional rounding.
- The evaluation mentions database operations/upserts, but the functional requirements only specify a CSV and caching. No persistence is scaffolded; clarify only if a database is expected later.
- The actual CSV schema is now fixed by the bundled file: `longitude`, `latitude`, `toll_name`, and extra `geo_state`. Duplicate rows are preserved because the assignment does not require deduplication.
- The cache has no expiration or size bound; see the caching limitation above.

The user's scope explicitly overrides the PDF's Mappls 80–90% matching target. That accuracy target is outside this project's intended implementation scope.

## Implementation notes

1. Load the supplied toll CSV once at startup.
2. Resolve the two Indian pincodes with Google Geocoding.
3. Request a driving route with detailed geometry from Google Routes.
4. Use a small Haversine helper to match each toll within 500 metres of any returned route point.
5. Estimate distance from the source by summing consecutive route-point distances up to the closest point, then sort tolls by that estimate.
6. Return the assignment JSON and cache successful results by ordered source/destination pair.

The point-based approximation can miss tolls between widely spaced points and include tolls on nearby roads. Detailed geometry helps but does not guarantee accuracy. Cumulative Haversine distance is also an approximation. This approach does not claim the PDF's 80–90% agreement with Mappls and needs no JTS or coordinate projection.

The endpoint uses a simple process-local cache. Google configuration, timeouts, CSV loading, validation, matching, and focused tests are already present. Keep API keys out of source control.

## Structure

- `api`: controller and request/response records matching the PDF.
- `data`: startup catalog and in-memory toll record.
- `service`: lookup workflow, Haversine helper, and approximate toll matcher.
- `exception`: validation and same-pincode error mapping.
- `src/test`: controller validation, catalog loading, matching, workflow, and cache checks, with no external API calls.

Reference for the selected framework: [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html).
