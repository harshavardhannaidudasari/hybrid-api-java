# Hybrid API Automation Framework (Java)

[![CI](https://github.com/harshavardhannaidudasari/hybrid-api-java/actions/workflows/ci.yml/badge.svg)](https://github.com/harshavardhannaidudasari/hybrid-api-java/actions/workflows/ci.yml)

A small, reusable REST API test client (RestAssured + TestNG under the hood)
built to be **imported by other projects**, not just run standalone. It's the
API-layer sibling to this account's `hybrid-web-mobile-*` (browser + native
mobile UI automation) and `hybrid-selfheal-*` (self-healing UI locators)
project families - see "Why this exists" below.

## Why this exists

UI automation projects regularly need to check or set up backend state
without driving the browser/app through every step (e.g. "assert the cart
total via the API instead of reading it off the page", or "create a test
user via the API before a UI login test"). `ApiClient` is meant to be added
as a dependency to those projects for exactly that, rather than each project
reinventing its own HTTP client.

It is **not** tied to one specific API. Every setting - base URL, timeouts,
retry behavior, auth credentials - is overridable via `HYBRID_API_*`
environment variables (same convention as the other two project families'
`HYBRID_*` overrides), so pointing this at a different backend is a config
change, not a code change.

## Using this as a library

```java
ApiClient api = new ApiClient(); // reads HYBRID_API_BASE_URL, defaults to https://dummyjson.com

// e.g. inside a UI test, verify backend state instead of scraping the DOM:
Response product = api.get("/products/1");
assertEquals(product.jsonPath().getInt("id"), 1);

// or authenticate once and reuse the token on every subsequent call:
String token = api.post("/auth/login", Map.of("username", "emilys", "password", "emilyspass"))
                   .jsonPath().getString("accessToken");
ApiClient authed = new ApiClient().withBearerToken(token);
authed.get("/auth/me");
```

## What's in the box

| File | Purpose |
|---|---|
| `ApiClient` | `get`/`post`/`put`/`patch`/`delete`, optional bearer-token auth, request/response logging, every call routed through `RetryPolicy` |
| `RetryPolicy` | Retries a request up to `HYBRID_API_RETRY_ATTEMPTS` times (default 3, 300ms apart) on a 5xx or connection exception - smooths over transient blips against a real public API, doesn't mask real failures (4xx and assertion failures are never retried) |
| `ApiConfig` | Every setting + its env var override, in one place |

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `HYBRID_API_BASE_URL` | `https://dummyjson.com` | Target API |
| `HYBRID_API_TIMEOUT_MS` | `10000` | Connect/socket timeout |
| `HYBRID_API_RETRY_ATTEMPTS` | `3` | Max attempts per request |
| `HYBRID_API_RETRY_BACKOFF_MS` | `300` | Delay between retries |
| `HYBRID_API_AUTH_USERNAME` / `HYBRID_API_AUTH_PASSWORD` | `emilys` / `emilyspass` | Credentials the auth tests log in with |

## Target API used for this repo's own tests

[dummyjson.com](https://dummyjson.com) - a free, no-signup-required fake
REST API with real CRUD semantics and a working JWT auth flow, used here
purely to prove the client works end-to-end. (`reqres.in`, the other common
choice for this, now requires a paid API key for every endpoint - confirmed
by `curl` returning 401 on a plain `GET /api/users/2` - so it wasn't used.)

## Setup

```bash
cd hybrid-api-java
mvn -DskipTests compile
```

## Running

```bash
mvn test
```

## What's actually been verified (last real run)

`mvn test` -> **7/7 passed** against the live `dummyjson.com`:

| Test | What it proves |
|---|---|
| `getSingleProductReturnsExpectedFields` | `GET` + JSON path parsing |
| `getProductListRespectsLimitParam` | Query params |
| `addProductReturnsCreatedIdAndEchoesTitle` | `POST` with a JSON body (`201`) |
| `updateProductReturnsUpdatedTitle` | `PUT` with a JSON body |
| `deleteProductMarksIsDeletedTrue` | `DELETE` |
| `meEndpointRejectsRequestWithNoToken` | Protected endpoint correctly `401`s with no auth |
| `loginThenMeEndpointReturnsAuthenticatedUser` | Full auth flow: login for a real JWT, then use `withBearerToken` on a second client instance to hit a protected endpoint |

## Bug found by actually running this (and how it was fixed)

RestAssured's `.body(Object)` needs a JSON serializer on the classpath to
turn a `Map` into a request body - without one it throws
`IllegalStateException: Cannot serialize object because no JSON serializer
found in classpath` at request time, not at compile time. The `POST`/`PUT`
tests only failed once actually run; `mvn compile` had no way to catch it.
Fixed by adding `jackson-databind` as a dependency.

## CI

`.github/workflows/ci.yml` runs the full suite against the live API on every
push/PR to `master`. No browser/emulator needed, so unlike the
`hybrid-web-mobile-*` CI jobs (which skip mobile), this one runs everything.
