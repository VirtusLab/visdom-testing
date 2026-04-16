# PBT Pricing Calculator Demo

Demonstrates: 90% line coverage, 0 computation bugs caught.

A pricing module with two planted rounding bugs that 10 traditional JUnit tests
(achieving high coverage) completely miss, but property-based tests (jqwik) find
within seconds.

## The bugs

1. **Early rounding of discount rate** -- `discountPercent / 100` is rounded to
   2 decimal places before multiplying by subtotal. Lossless for integer
   discounts (10%, 20%), but loses precision for fractional discounts (1.05%).

2. **Wrong rounding mode on VAT** -- uses `FLOOR` instead of `HALF_UP`. Agrees
   with correct rounding for most inputs, diverges on small prices with odd VAT
   rates.

## Prerequisites

- Java 21+
- Maven 3.9+

## Run the demo

```bash
./run-demo.sh
```

## Or step by step

```bash
# 1. Traditional tests -- all 10 pass
mvn test -Dtest=PriceCalculatorTraditionalTest

# 2. Coverage report -- high line coverage
mvn jacoco:report
open target/site/jacoco/index.html

# 3. Property-based tests -- 2 oracle properties fail
mvn test -Dtest=PriceCalculatorPropertyTest

# 4. Mutation testing -- reveals the gap
mvn pitest:mutationCoverage
open target/pit-reports/index.html
```

## What you will see

| Step | Result |
|------|--------|
| Traditional tests | 10/10 pass |
| JaCoCo coverage | ~90% line coverage on PriceCalculator |
| Property tests (structural) | 6/6 pass |
| Property tests (oracle) | 2/2 FAIL -- both bugs caught |
| PIT mutation testing | Surviving mutants in discount and VAT logic |
