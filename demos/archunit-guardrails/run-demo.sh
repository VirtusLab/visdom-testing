#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

CONTROLLER_SRC=src/main/java/com/example/store/controller/ProductController.java
SERVICE_SRC=src/main/java/com/example/store/service/ProductService.java

# Save clean copies for restoration
TMPDIR_CLEAN=$(mktemp -d)
cp "$CONTROLLER_SRC" "$TMPDIR_CLEAN/ProductController.java"
cp "$SERVICE_SRC" "$TMPDIR_CLEAN/ProductService.java"

restore_clean() {
    cp -f "$TMPDIR_CLEAN/ProductController.java" "$CONTROLLER_SRC"
    cp -f "$TMPDIR_CLEAN/ProductService.java" "$SERVICE_SRC"
}
trap "restore_clean; rm -rf $TMPDIR_CLEAN" EXIT

echo "============================================"
echo "  ArchUnit Architecture Guardrails Demo"
echo "============================================"

echo ""
echo "=== Step 1: Clean code — all rules pass ==="
mvn test -Dtest=ArchitectureTest -q
echo "All architecture rules pass"

echo ""
echo "=== Step 2: Introduce AI-generated violation ==="
echo "Simulating: AI bypasses service layer, uses field injection"
cp -f violations/01-controller-calls-repo/ProductController.java "$CONTROLLER_SRC"

echo ""
echo "=== Step 3: ArchUnit catches it ==="
mvn test -Dtest=ArchitectureTest 2>&1 || true
echo ""
echo "^ ArchUnit detected: controller->repository bypass, field injection"

echo ""
echo "=== Step 4: Restore clean code ==="
restore_clean

echo ""
echo "=== Step 5: Show RestTemplate violation ==="
cp -f violations/02-rest-template/ProductService.java "$SERVICE_SRC"
mvn test -Dtest=ArchitectureTest 2>&1 || true
echo ""
echo "^ ArchUnit detected: RestTemplate usage (deprecated since Spring 6.1), field injection"

echo ""
echo "=== Step 6: Restore and show cyclic dependency ==="
restore_clean
cp -f violations/03-cyclic-dependency/ProductService.java "$SERVICE_SRC"
mvn test -Dtest=ArchitectureTest 2>&1 || true
echo ""
echo "^ ArchUnit detected: service->controller dependency (cyclic)"

restore_clean
echo ""
echo "Demo complete. Clean code restored."
