# Have used agents thoroughly in making this. if you want something created by hand, please have a look at the simple chat app.

# Toll Estimate Inator

Small Spring Boot API that finds approximate toll plazas between two Indian
pincodes using Google Geocoding and Routes APIs.

## Requirements

- Java 21
- Internet access for the first Maven build
- `GOOGLE_MAPS_API_KEY` with Geocoding and Routes APIs enabled

On Windows, set the key in IntelliJ's run configuration or as a Windows user
environment variable, then restart IntelliJ or the terminal.

## Run

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

## Request

```http
POST http://localhost:8080/api/v1/toll-plazas
Content-Type: application/json
```

```json
{
  "sourcePincode": "110001",
  "destinationPincode": "560001"
}
```

The response contains the route distance and matching toll plazas:

```json
{
  "route": {
    "sourcePincode": "110001",
    "destinationPincode": "560001",
    "distanceInKm": 2134.449
  },
  "tollPlazas": []
}
```

Missing, malformed, or equal pincodes return HTTP 400. A pincode that Google
cannot resolve also returns HTTP 400.

## Notes

- Toll data is bundled at `src/main/resources/data/toll_plaza_india_cleaned.csv`
  and loaded once at startup.
- Matching uses a configurable 500 m nearest-route-point Haversine check.
  It is intentionally approximate and may miss tolls between sparse route
  points or include tolls on nearby roads. It does not claim the PDF's
  80-90% Mappls accuracy target.
- Successful lookups use a simple in-memory cache keyed by the ordered pair
  `sourcePincode:destinationPincode`. The cache is cleared on restart.
- Run `.\mvnw.cmd test` to execute the focused automated tests; Google calls
  are mocked.
