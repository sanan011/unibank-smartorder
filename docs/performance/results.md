# Performance Testing Results

This document summarizes the performance testing strategy and results for the `unibank-smartorder` project.

## Expected Thresholds

For all performance tests, the following thresholds must be met:

| Metric | Expected Threshold | Description |
|---|---|---|
| **Response Time (p95)** | `< 500ms` | 95% of all requests must complete within 500 milliseconds. |
| **Error Rate** | `< 1%` | Less than 1% of all requests can fail (e.g. HTTP 5xx responses or timeouts). |

## Test Scenarios

The tests are located in `docs/performance/k6/`.

1. **Smoke Test (`smoke-test.js`)**: Runs with 1 Virtual User (VU) for 30 seconds to verify basic functionality.
2. **Load Test (`load-test.js`)**: Ramps up to 50 VUs and holds for 2 minutes to evaluate normal peak traffic.
3. **Stress Test (`stress-test.js`)**: Ramps up to 100 VUs and holds to determine the system's breaking point under extreme load.

## Results Summary
*(Update this section after running tests against the environment)*

| Test Run Date | Test Type | VUs | p95 Response Time | Error Rate | Passed? |
|---|---|---|---|---|---|
| TBD | Smoke | 1 | TBD ms | TBD % | ⬜ |
| TBD | Load | 50 | TBD ms | TBD % | ⬜ |
| TBD | Stress | 100 | TBD ms | TBD % | ⬜ |
