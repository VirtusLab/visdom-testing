#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

BOLD='\033[1m'
GREEN='\033[0;32m'
RED='\033[0;31m'
CYAN='\033[0;36m'
RESET='\033[0m'

separator() {
    echo ""
    echo -e "${CYAN}================================================================${RESET}"
    echo -e "${BOLD}$1${RESET}"
    echo -e "${CYAN}================================================================${RESET}"
    echo ""
}

# ------------------------------------------------------------------
separator "STEP 1: Traditional tests (expect ALL 10 to PASS)"
# ------------------------------------------------------------------
mvn -q test -Dtest=PriceCalculatorTraditionalTest && \
    echo -e "\n${GREEN}Result: All traditional tests passed.${RESET}" || \
    echo -e "\n${RED}Unexpected: some traditional tests failed!${RESET}"

# ------------------------------------------------------------------
separator "STEP 2: Coverage report (JaCoCo)"
# ------------------------------------------------------------------
mvn -q test -Dtest=PriceCalculatorTraditionalTest jacoco:report
echo -e "${GREEN}Coverage report generated.${RESET}"
echo "Open: target/site/jacoco/index.html"

# ------------------------------------------------------------------
separator "STEP 3: Property-based tests (expect 2 oracle properties to FAIL)"
# ------------------------------------------------------------------
mvn test -Dtest=PriceCalculatorPropertyTest && \
    echo -e "\n${RED}Unexpected: all property tests passed — bugs not caught!${RESET}" || \
    echo -e "\n${GREEN}Expected: oracle property tests failed, catching the planted bugs.${RESET}"

# ------------------------------------------------------------------
separator "STEP 4: Mutation testing (PIT)"
# ------------------------------------------------------------------
mvn -q test -Dtest=PriceCalculatorTraditionalTest pitest:mutationCoverage && \
    echo -e "\n${GREEN}Mutation report generated.${RESET}" || \
    echo -e "\n${RED}PIT encountered an error.${RESET}"
echo "Open: target/pit-reports/index.html"

# ------------------------------------------------------------------
separator "DEMO COMPLETE"
# ------------------------------------------------------------------
echo "Summary:"
echo "  - Traditional tests: 10/10 PASSED (bugs invisible)"
echo "  - Coverage:          ~90% line coverage"
echo "  - Property tests:    6 structural PASSED, 2 oracle FAILED (bugs caught!)"
echo "  - Mutation testing:  surviving mutants reveal weak test suite"
echo ""
echo "Conclusion: high coverage alone does not guarantee correctness."
echo "Property-based testing finds the bugs that example-based tests miss."
