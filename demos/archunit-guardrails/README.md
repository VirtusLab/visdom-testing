# ArchUnit Architecture Guardrails Demo

Demonstrates how ArchUnit catches architectural violations in AI-generated code.

## What This Proves

AI code generators often produce code that "works" but violates architectural rules:
- Bypassing the service layer (controller calls repository directly)
- Using field injection (`@Autowired`) instead of constructor injection
- Using deprecated APIs (`RestTemplate` instead of `RestClient`)
- Throwing generic exceptions instead of domain-specific ones
- Creating cyclic dependencies between layers

ArchUnit encodes these rules as tests that run in CI, catching violations before they reach production.

## Project Structure

```
src/main/java/com/example/store/
  controller/   ProductController.java     (clean — uses service layer)
  service/      ProductService.java        (clean — constructor injection, RestClient)
  repository/   ProductRepository.java     (JPA interface)
  model/        Product.java               (JPA entity)
  exception/    ProductNotFoundException.java

src/test/java/com/example/store/
  ArchitectureTest.java                    (5 ArchUnit rules)

violations/
  01-controller-calls-repo/                (bypasses service, field injection, generic exception)
  02-rest-template/                        (deprecated API, field injection)
  03-cyclic-dependency/                    (service depends on controller)
```

## Running

```bash
# Run the full demo (clean -> violate -> detect -> restore)
./run-demo.sh

# Or run just the architecture tests
mvn test -Dtest=ArchitectureTest
```

## Architecture Rules

| Rule | What It Catches |
|------|----------------|
| `controllers_should_not_access_repositories` | Controller bypassing service layer |
| `no_field_injection` | `@Autowired` on fields instead of constructor injection |
| `no_rest_template` | Using deprecated `RestTemplate` |
| `no_generic_exceptions` | Throwing `RuntimeException` from service code |
| `services_should_not_depend_on_controllers` | Cyclic layer dependencies |

## Requirements

- Java 21+
- Maven 3.9+
