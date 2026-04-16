# Visdom Testing: Claim Audit, Benchmarks & Demo Plan

## Part 1: Where People Will Push Back

### RED FLAGS — Claims That Need Defense or Revision

#### 1. "90% line coverage while missing every computation bug"
**Risk: HIGH** — This is the headline claim and the most scrutinized.

- **What's solid**: The case study is real (pricing module, jqwik, 2 bugs found). The PDF documents it with specific counterexamples (`unitPrice=0.01, qty=48, discount=1.05%`).
- **What's attackable**: "every computation bug" is absolute language. A critic will ask: "How do you know there weren't other computation bugs that traditional tests DID catch?" The claim conflates "every computation bug *in our case study*" with a universal statement.
- **Fix**: Say "90% line coverage while missing both computation bugs in our pricing module" — specific, defensible, still powerful.

#### 2. The ArchUnit Experiment — "10/10 violations"
**Risk: MEDIUM** — The experiment is real but has methodology gaps a researcher will find.

- **What's solid**: 10 runs, 3 variants, same CRUD task, same model (Claude Haiku 4.5). Results are deterministic for the layer bypass (10/10 without, 0/10 with).
- **What's attackable**:
  - **Sample size**: n=10 per variant is tiny. A statistician will say this lacks statistical power.
  - **Single task**: Only one CRUD endpoint (`GET /api/products/{sku}`). Would results hold for non-CRUD tasks?
  - **Single model**: Claude Haiku 4.5 only. GPT-4, Gemini, or a different Claude tier might behave differently.
  - **No randomization details**: What prompts? What temperature? Were runs independent?
- **Defense**: Acknowledge limitations upfront. The 10/10 vs 0/10 split is so extreme it's significant even at n=10 (Fisher's exact test: p=0.0000017). But generalizability is honestly limited.
- **Recommendation**: Run at least 3 more models (GPT-4o, Gemini 2.5, Claude Sonnet) and 2 more task types. This turns "one experiment" into "consistent pattern."

#### 3. "84% of CI test failures are flaky" — Google attribution
**Risk: MEDIUM** — Accurately cited but easily miscontextualized.

- **What's solid**: The figure comes from John Micco's ICST 2017 keynote ("The State of Continuous Integration Testing @Google"). The exact phrasing is "84% of transitions from Pass -> Fail are from flaky tests."
- **What's attackable**:
  - **It's from 2016/2017** — 9 years old. Google hasn't published an updated figure.
  - **Google-specific**: Google runs 4.2M tests, 150M executions/day. Their scale is not typical. A 50-person startup will have a fundamentally different flaky test profile.
  - **We cite it as "Source: Google"** — should cite "Micco, ICST 2017" for academic credibility.
  - **The stat is about *transitions*, not all failures** — subtle but important distinction.
- **Fix**: Update attribution to "Micco, ICST 2017" and add caveat about Google scale. Consider adding more recent data (e.g., Google's 2020 "De-Flake Your Tests" paper which confirms ~1.5% flakiness rate remains stable).

#### 4. "73% acceptance rate for AI-generated mutation tests" — Meta ACH
**Risk: LOW** — This is accurately cited and well-documented.

- **Source**: Meta Engineering Blog (Feb 2025) + arXiv paper (2501.12862). 191 tests reviewed during test-a-thons on WhatsApp/Messenger, Oct-Dec 2024.
- **What's attackable**: The 73% is for *privacy-focused* mutation tests, not general mutation tests. The scope is narrower than it appears on our site.
- **Bonus stat to add**: "49% of ACH tests didn't add line coverage but caught previously undetected faults" — this directly supports our "coverage lies" narrative.

#### 5. "56% to 80% mutation coverage" — Atlassian
**Risk: LOW** — Directly from Atlassian's blog with 5 named projects.

- **Source**: Atlassian Blog "Automating Mutation Coverage with AI" (2025).
- **What's attackable**: These are Atlassian's own projects with their own tooling (Rovo Dev CLI). External reproducibility is limited.
- **The full data** (useful to have on hand):

  | Project | Before | After |
  |---------|--------|-------|
  | jira-project-a | 56% | 80% |
  | jira-project-b | 70% | 88% |
  | jira-project-c | 83% | 96% |
  | jira-project-d | 71% | 80% |
  | jira-project-e | 84% | 90% |

#### 6. "30-50% reduction in integration issues with consumer-driven contracts"
**Risk: HIGH** — No source attributed on the site. This is floating as fact.

- **Problem**: We couldn't find the original source for this stat. It reads like consultant-speak.
- **Fix**: Either find the original source or remove the specific percentage. Replace with eBay case study: "Time to detect compatibility issues dropped from days to minutes" (sourced from eBay Innovation blog).

#### 7. "$600K/year wasted on flaky tests" calculation (leaders page)
**Risk: MEDIUM** — The math is transparent but the assumptions are soft.

- The formula: `8% of dev time × $150K salary × 50 engineers = $600K`
- **What's attackable**: Where does "8%" come from? Is $150K an accurate average? Why 50 engineers? These feel chosen to produce a dramatic number.
- **Fix**: Show the formula explicitly and let readers plug in their own numbers. Frame as "calculator" not "fact."

#### 8. "Testing hours per sprint reduced 40-60%"
**Risk: HIGH** — No source, no case study, no methodology.

- This appears on the leaders page as a benefit claim with no backing.
- **Fix**: Either tie this to a specific engagement/case study or remove it.

#### 9. "PBT failures are NOT flaky"
**Risk: MEDIUM** — Technically true but misleading.

- **What's solid**: If a PBT failure is reproducible with the same seed, it's deterministic. jqwik logs seeds by default.
- **What's attackable**: If you run PBT with *different* random seeds and the test intermittently fails, that looks exactly like a flaky test to a developer who doesn't understand PBT. The input that triggers the failure may be rare, causing intermittent CI failures.
- **Fix**: The claim is on the FAQ page already with the seed explanation. Consider adding: "If a property fails on one seed and passes on another, that means the bug only manifests on specific inputs — which is exactly the kind of bug PBT is designed to find."

#### 10. "RestTemplate is deprecated since Spring 6.1"
**Risk: LOW but pedantic** — Spring's own messaging is "deprecated for removal" since Spring 6.1 / Spring Boot 3.2 (late 2023). It's technically marked `@Deprecated(since = "6.1", forRemoval = true)`. People who still use Spring 5 will push back.

---

### YELLOW FLAGS — Reasonable but Needs Context

| Claim | Issue | Fix |
|-------|-------|-----|
| "jqwik generates 1000+ inputs per property" | Default is 1000 but configurable | Add "by default" |
| "ArchUnit rules run in under 10 seconds" | True for focused suites, not guaranteed for 50+ rules on large codebases | Already has caveat on site ("Even with 50+ rules, stays under 30s") |
| "Mutation testing in 2-5 minutes for 50 classes" | Depends heavily on test speed and mutant count | Add "typical" qualifier |
| Structural vs oracle properties distinction | The PDF says "6 structural properties all passed on buggy code" | This is honest and actually strengthens the argument |
| "PBT finds ~50x as many mutations as average unit test" | From OOPSLA 2025 paper (Python/Hypothesis) | Great benchmark to ADD to the site |

---

## Part 2: Benchmarks That Fit Our Case

### Directly Supporting Benchmarks

| Benchmark | Source | Key Finding | Fit |
|-----------|--------|-------------|-----|
| **PBT finds 50x more mutations** | OOPSLA 2025, UC San Diego | Each PBT finds ~50x as many mutations as average unit test. 76% of mutations found within first 20 inputs. | Perfect for "why PBT" argument |
| **AI code has 1.7x more issues** | CodeRabbit Dec 2025 (470 PRs) | AI PRs: 10.83 issues vs 6.45 human. 1.4x more critical issues. | Validates "AI code needs extra testing" |
| **DORA 2025: 9% bug rate increase** | Google DORA Report 2025 | 90% AI adoption → 9% more bugs, 91% more code review time, 154% larger PRs | Enterprise-credible source |
| **Faros 2026: 54% more bugs/dev** | Faros AI analysis | Bugs per developer up 54%. Incidents per PR up 242.7%. | Updated/stronger version of DORA |
| **ChatGPT test oracle precision: 6.3%** | arXiv 2404.10304 | 92.2% of test failures due to incorrect test oracles | Directly validates "circular testing" claim |
| **Stanford: AI → less secure code** | Perry et al., CCS 2023 | Developers with AI assistants wrote significantly less secure code AND were more confident in it | Overconfidence angle |
| **Endor Labs: 15/20 AI completions have design flaws** | Endor Labs 2025 | 12/20 exhibited "design pattern drift", 6/20 invisible to static analysis | Validates architecture testing need |
| **GitClear: code duplication 8.3% → 12.3%** | GitClear 2025 | AI adoption correlated with declining code quality metrics | General AI quality concern |
| **Equivalent mutants: 10-40%** | Gruen et al. (Jaxen study) | 40% equivalent in XPath engine, 15min to verify each | Honest limitation to acknowledge |
| **Meta ACH: 49% of tests had no coverage impact** | Meta Engineering 2025 | Almost half of generated tests caught faults invisible to coverage | Perfect for "coverage lies" |

### Benchmarks to ADD to the Site

1. **OOPSLA 2025 PBT paper** — "Each property-based test finds ~50x as many mutations as the average unit test" (UC San Diego). This is the strongest external validation of PBT effectiveness.

2. **CodeRabbit AI vs Human report** — 470 PRs analyzed, AI code has 1.7x more issues. Enterprise-friendly source.

3. **DORA 2025** — Google's own DevOps report showing AI adoption increases bug rates. Nobody argues with DORA.

4. **Meta ACH 49% stat** — We already cite Meta for the 73% acceptance rate, but the 49% "no coverage gain but caught faults" stat is even more powerful for our narrative.

5. **Stanford CCS 2023** — The overconfidence finding ("developers believed their AI-assisted code was more secure when it was actually less secure") is a compelling addition to the "who tests the tests" framing.

---

## Part 3: Ready-to-Run Demos

### Demo 1: PBT Pricing Calculator (Existing Case Study)
**Purpose**: Prove the "90% coverage, 0 bugs found" claim live.

```
visdom-testing-demo-pbt/
├── pom.xml                           # Spring Boot + jqwik + PIT
├── src/main/java/
│   └── com/example/pricing/
│       ├── PriceCalculator.java      # The buggy implementation
│       └── PriceCalculatorFixed.java  # The fixed version
├── src/test/java/
│   └── com/example/pricing/
│       ├── PriceCalculatorTraditionalTest.java  # 10 tests, 90% coverage, 0 bugs caught
│       ├── PriceCalculatorPropertyTest.java     # 8 properties, 2 bugs found
│       └── PriceCalculatorReferenceFormula.java # Oracle for comparison
├── README.md
└── run-demo.sh                       # One command to demonstrate
```

**Demo script**:
```bash
# Step 1: Run traditional tests — all pass
mvn test -Dtest=PriceCalculatorTraditionalTest
# Output: Tests run: 10, Failures: 0

# Step 2: Check coverage — 90%
mvn jacoco:report
# Output: 90% line coverage

# Step 3: Run PBT — 2 failures
mvn test -Dtest=PriceCalculatorPropertyTest
# Output: Tests run: 8, Failures: 2
# Shrunk counterexample: unitPrice=0.01, qty=48, discount=1.05%

# Step 4: Run mutation testing — see the gap
mvn pitest:mutationCoverage
# Output: Mutation score 73% (traditional) — mutants survive on computation paths

# Step 5: Fix bugs, re-run everything
# All green, mutation score improves
```

**Bugs to plant**:
1. `discountPercent / 100` rounded to 2dp before multiplication (early rounding)
2. `RoundingMode.FLOOR` instead of `HALF_UP` on VAT calculation

### Demo 2: ArchUnit Architecture Guardrails
**Purpose**: Show 10/10 violations without ArchUnit, 0/10 with.

```
visdom-testing-demo-archunit/
├── pom.xml
├── src/main/java/
│   └── com/example/store/
│       ├── controller/ProductController.java  # Clean: calls service
│       ├── service/ProductService.java
│       ├── repository/ProductRepository.java
│       └── config/AppConfig.java
├── src/test/java/
│   └── com/example/store/
│       └── ArchitectureTest.java              # The ArchUnit rules
├── violations/                                # Pre-generated AI violations
│   ├── violation-controller-calls-repo.java   # Controller → Repository
│   ├── violation-field-injection.java         # @Autowired field
│   └── violation-rest-template.java           # RestTemplate usage
├── README.md
└── run-demo.sh
```

**Demo script**:
```bash
# Step 1: Show the rules
cat src/test/java/com/example/store/ArchitectureTest.java

# Step 2: Run tests — all pass (clean code)
mvn test

# Step 3: Introduce a violation (copy pre-generated AI output)
cp violations/violation-controller-calls-repo.java \
   src/main/java/com/example/store/controller/ProductController.java

# Step 4: Run tests — ArchUnit catches it
mvn test
# Output: Architecture Violation: controller depends on repository

# Step 5: Show RestTemplate ban
cp violations/violation-rest-template.java \
   src/main/java/com/example/store/service/ProductService.java
mvn test
# Output: RestTemplate is deprecated; use RestClient
```

**Rules to include**:
```java
@ArchTest
void controllersShouldNotAccessRepositories(JavaClasses classes) { ... }

@ArchTest
void noFieldInjection(JavaClasses classes) { ... }

@ArchTest
void noRestTemplate(JavaClasses classes) { ... }

@ArchTest
void noGenericExceptions(JavaClasses classes) { ... }

@ArchTest
void noCyclicDependencies(JavaClasses classes) { ... }
```

### Demo 3: Mutation Testing Reality Check
**Purpose**: Show mutation score vs coverage gap.

```
visdom-testing-demo-mutation/
├── pom.xml                           # PIT configured
├── src/main/java/
│   └── com/example/tax/
│       └── TaxCalculator.java        # Multiple tax brackets
├── src/test/java/
│   └── com/example/tax/
│       ├── TaxCalculatorWeakTest.java    # High coverage, low mutation score
│       └── TaxCalculatorStrongTest.java  # Same coverage, high mutation score
├── README.md
└── run-demo.sh
```

**Demo script**:
```bash
# Step 1: Run weak tests — 95% coverage
mvn test -Dtest=TaxCalculatorWeakTest jacoco:report
# All pass, 95% line coverage

# Step 2: Mutation testing reveals the truth
mvn pitest:mutationCoverage -DtargetTests=TaxCalculatorWeakTest
# Mutation score: ~55% — many boundary mutations survive

# Step 3: Run strong tests — same coverage, better mutation score
mvn pitest:mutationCoverage -DtargetTests=TaxCalculatorStrongTest
# Mutation score: ~85%

# The difference: weak tests check "is there output?"
# Strong tests check "is the output correct for this boundary?"
```

### Demo 4: Integrated Pipeline (Full Visdom Testing Layer Cake)
**Purpose**: Show all 4 layers running in sequence in a real CI-like flow.

```
visdom-testing-demo-full/
├── pom.xml
├── src/main/java/com/example/ecommerce/
│   ├── controller/OrderController.java
│   ├── service/OrderService.java
│   ├── service/PricingService.java
│   ├── repository/OrderRepository.java
│   └── client/InventoryClient.java
├── src/test/java/com/example/ecommerce/
│   ├── architecture/ArchitectureTest.java    # L0: <10s
│   ├── properties/PricingPropertyTest.java   # L1: ~2s
│   ├── mutation/pitest-config.xml            # L2: ~5min
│   └── contracts/InventoryContractTest.java  # L3: ~10s
├── pact/                                     # Contract definitions
├── README.md
└── run-all-layers.sh
```

**Demo script**:
```bash
#!/bin/bash
echo "=== L0: Architecture Guardrails ==="
time mvn test -Dtest=ArchitectureTest
# Expected: <10s, catches layer violations

echo "=== L1: Property-Based Verification ==="
time mvn test -Dtest=PricingPropertyTest
# Expected: ~2s, catches computation bugs

echo "=== L2: Mutation-Guided Quality ==="
time mvn pitest:mutationCoverage
# Expected: ~5min, shows mutation score

echo "=== L3: Contract Verification ==="
time mvn test -Dtest=InventoryContractTest
# Expected: ~10s, verifies API contract

echo "=== Dashboard: Quality Metrics ==="
# Generate combined report
mvn jacoco:report
cat target/pit-reports/*/mutations.csv | wc -l
```

---

## Part 4: Action Items

### Immediate Fixes (site content)
- [ ] "90% line coverage while missing every computation bug" → add "both computation bugs in our pricing module"
- [ ] Google 84% attribution → "Micco, ICST 2017" not just "Google"
- [ ] "30-50% reduction in integration issues" → find source or replace with eBay case study
- [ ] "Testing hours per sprint reduced 40-60%" → needs source or removal
- [ ] "$600K/year" calculation → make it an interactive calculator, not a fact
- [ ] Meta 73% → add context: "for privacy-focused mutation tests"

### New Benchmarks to Add
- [ ] OOPSLA 2025: PBT finds 50x more mutations than average unit test
- [ ] CodeRabbit 2025: AI PRs have 1.7x more issues than human PRs
- [ ] DORA 2025: AI adoption → 9% bug rate increase
- [ ] Meta ACH: 49% of tests caught faults invisible to coverage
- [ ] Stanford CCS 2023: AI assistants → less secure code + overconfidence

### Demos to Build
- [ ] Demo 1: PBT Pricing Calculator (highest priority — proves headline claim)
- [ ] Demo 2: ArchUnit Architecture Guardrails (second priority — visual)
- [ ] Demo 3: Mutation Testing Reality Check (third — shows coverage vs mutation gap)
- [ ] Demo 4: Integrated Pipeline (stretch — full layer cake)

### Pre-emptive Defenses to Write
- [ ] FAQ entry: "Your sample size is only 10" → Fisher's exact test, p<0.000002
- [ ] FAQ entry: "This only works for Java" → language equivalents table (already on FAQ page)
- [ ] FAQ entry: "Isn't this too much testing overhead?" → layer execution times + only L0/L1 on every PR, L2/L3 on critical paths
- [ ] FAQ entry: "Why not just use better prompts?" → Endor Labs 12/20 design drift regardless of prompt quality
- [ ] Blog post: "What the critics get right" — acknowledge equivalent mutants, PBT learning curve, Java-centricity

---

## External Source URLs

| Source | URL |
|--------|-----|
| Google Flaky Tests (Micco, ICST 2017) | https://research.google.com/pubs/archive/45880.pdf |
| Meta ACH Engineering Blog | https://engineering.fb.com/2025/02/05/security/revolutionizing-software-testing-llm-powered-bug-catchers-meta-ach/ |
| Meta ACH arXiv Paper | https://arxiv.org/html/2501.12862v1 |
| Atlassian Mutation Coverage Blog | https://www.atlassian.com/blog/developer/automating-mutation-coverage-with-ai |
| OOPSLA 2025 PBT Paper | https://cseweb.ucsd.edu/~mcoblenz/assets/pdf/OOPSLA_2025_PBT.pdf |
| CodeRabbit AI vs Human Report | https://www.coderabbit.ai/blog/state-of-ai-vs-human-code-generation-report |
| DORA 2025 Report | https://dora.dev/research/2025/dora-report/ |
| Stanford CCS 2023 (AI Security) | https://dl.acm.org/doi/10.1145/3576915.3623157 |
| Endor Labs Design Flaws | https://www.endorlabs.com/learn/design-flaws-in-ai-generated-code |
| GitClear Code Quality 2025 | https://www.gitclear.com/ai_assistant_code_quality_2025_research |
| eBay Contract Testing | https://innovation.ebayinc.com/stories/api-evolution-with-confidence-a-case-study-of-contract-testing-adoption-at-ebay/ |
| PBT in Practice (ICSE 2024) | https://harrisongoldste.in/papers/icse24-pbt-in-practice.pdf |
| Equivalent Mutants Study | https://www.st.cs.uni-saarland.de/publications/files/gruen-mutation-2009.pdf |
| ChatGPT Test Oracle Quality | https://arxiv.org/html/2404.10304v1 |
| Qodo AI Code Quality Survey | https://www.qodo.ai/reports/state-of-ai-code-quality/ |
| Sonar AI Trust Gap | https://www.sonarsource.com/company/press-releases/sonar-data-reveals-critical-verification-gap-in-ai-coding/ |
| Faros AI DORA Analysis | https://www.faros.ai/blog/key-takeaways-from-the-dora-report-2025 |
