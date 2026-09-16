# Visdom Testing — Suite Alignment

A companion to `VISDOM_QA_REVIEW_ARCHITECTURE.md`. That document specifies an engine. This one
specifies how Visdom Testing becomes a *Visdom* component: where its surface lives, what it owes the
rest of the suite, and which existing patterns it must not reinvent.

Backlog: **VIS-177** (epic) in the `Testing` component of the `VIS` project.

---

## 1. What the pasted architecture gets right, and what it leaves out

`VISDOM_QA_REVIEW_ARCHITECTURE.md` is the strongest design artefact the module has. The step model,
the matcher chains evaluated cheap-first, one sandbox per review, artifacts by reference, snapshot
explainability, schedule-and-read MCP — all of it is settled and none of it should be reopened.

What it does not contain is anything that makes a component part of Visdom:

| Layer | Status in the doc |
| --- | --- |
| Engine — steps, matchers, sandbox, findings, MCP | Fully specified |
| UI surface | Absent. MCP is stated as the only interface |
| Control Plane contract | Absent, though the CP roadmap already declares what Testing owes it |
| Orchestrator flow node and its config panel | Absent. Explicitly left open and unowned on 20.08 |
| Reviewer / tester separation | Collapsed. The doc describes a reviewer |

The last row is the one that matters most, because it decides everything else.

---

## 2. The line: reviewer finds problems, tester produces evidence

From the 10.08 design call, and asked for architecturally, not rhetorically:

> A reviewer finds problems. A tester produces evidence. The tester answers *can a human trust this
> enough to ship*, so the human need not walk the whole flow.

The pasted document answers the reviewer's question — *is this change good enough, and if not, why?*
— and returns `approved` / `changes_requested` plus findings. That is Visdom Code Review.

Visdom Testing answers a different question with a different output shape: artefacts a human opens
(a recorded click-through with narration, a mutation report, an architecture-drift diff), signals
that trend over time, and a filled-in release checklist. Its failure mode is not "missed a bug" but
"the proof was not convincing".

**Recommendation: one runtime, two products.**

| Shared | Distinct per product |
| --- | --- |
| Step model and matcher chains | Step catalog |
| One sandbox per run | Output contract — verdict+findings vs evidence+signals |
| Artifact store and roles | Control Plane surface |
| Tracing, cost ledger, auth, project config | Orchestrator node and its configuration |
| Snapshot explainability | Persistence needs — Testing keeps history, Review does not |

Building a second sandbox/matcher/artifact stack duplicates the hardest part of the system and buys
nothing. Building Testing as *only* a set of steps inside QA Review loses the product — the audience
differs (release manager and PM, not reviewer), the persistence differs (trends and baselines), and
the surface differs. The boundary sits between engine and product, not between two engines.

This settles §11.5 of the architecture doc and is the subject of **VIS-178**.

---

## 3. What Testing owes the rest of the suite

`visdom-control-plane/roadmap.md` §4.1 already names Visdom Testing as the evidence source for the
Quality layer: **architecture drift, test trust, mutation score, flaky rate**. Nothing emits them
today, so the Quality score has no drivers and the *explainable scores* product principle cannot hold.

The signal contract (**VIS-179**) needs to fix, per signal: unit, direction, producing step, grain
(run / commit / repository / window), and provenance — which run and which artifact settles it, so a
number in the Control Plane can always be opened into the evidence behind it. TraceVault's existing
CP wiring is the reference for push-vs-pull.

Two consequences the architecture doc does not currently support:

- **History.** Mutation score and flaky rate are only meaningful against a baseline, and "coverage
  must not drop" is open question §11.4. Testing owns the raw signal history; the Control Plane owns
  rollups and scores. An append-only signal record is cheaper than measuring the common ancestor by
  running every step twice (**VIS-180**).
- **A `recording` artifact role.** The doc has `interpret` and `evidence`. A recorded walkthrough is
  neither — it is the artefact a human opens *first*, and the UI needs to know where to put a player.
  It is also the largest thing the system produces, so retention belongs in the same decision
  (**VIS-181**). This is the most client-pulled feature the module has: two clients on 10.08, Corify
  on 11.08, efinti on 18.08, all unprompted.

---

## 4. The surface: Control Plane, not a third frontend

`apps/web/app/testing/page.tsx` renders `StubView` today. The route, the sidebar entry and the icon
(`FlaskConical`) already exist. Everything below is built from primitives already in that repository,
so Testing looks and behaves exactly like `/code-review` rather than like a separate tool.

**Page composition — `/code-review` is the template:**

| Testing panel | Mirrors | Contents |
| --- | --- | --- |
| `TestingHero` | `VcrHero` | Module thesis, baseline-vs-deployed comparison |
| `KpiRail` | reused as-is | `endpoint="/api/testing/kpis"` — test trust, mutation score, architecture drift, flaky rate, spec coverage, evidence freshness |
| `StrategyRibbon` | `PipelineRibbon` | Step families as stations: deterministic → architecture → property → mutation → generated E2E → evidence; each with budget, p50, runs, findings |
| `EvidenceQueue` | `ActivePrQueue` | One row per change: evidence badges, recording link, checklist state |
| `ReleaseChecklist` | new | The evidence a human needs before "you can merge" |
| `RunDrawer` | `AgentDrawer` | 480px right drawer: trust radar, step trace, inline recording |

(**VIS-182**, **VIS-184**, **VIS-185**.)

**Backend follows the same pattern** — `dashboard/api/TestingController.kt` with
`dashboard/fake/Testing*Faker.kt` behind it, DTOs in `ApiDtos.kt` mirrored into `apps/web/lib/types.ts`
(**VIS-183**). The faker is not scaffolding: every dashboard panel in that repo has one, and that is
what keeps the product demoable while the real pipeline is mid-change — the concern raised on 19.08
about returning from leave into an untested demo.

**Consistency rules, taken from the existing codebase rather than invented:**

- Tokens from `visdom-design-system/src/styles/globals.css`. Never redefined locally.
- Montserrat headings / Inter body / JetBrains Mono for data, timestamps, hashes and eyebrow labels.
- Cards `rounded-xl` + `shadow-sm`; buttons `rounded-md`; badges `rounded-full`.
- Eyebrow panel numbering continues the `01..NN` convention; mono, 10.5px, `.12em` tracking, uppercase.
- Status colours: ok = `--primary`, warn = `--visdom-amber`, bad = `--destructive`, info = `--visdom-blue`.
- 1440px desktop-first: 240px sidebar, 60px topbar, 20px panel gaps, 20px inner padding, 24px content padding.
- `useApi` for reads, `useEventSource` for streams, `Skeleton` while loading.
- Detail = right-hand drawer opened by row click; selected row takes `--accent`.
- Stack: Next.js 15 App Router, React 19, Tailwind 4, shadcn/ui, `lucide-react`, `cva`.

One divergence worth naming: the orchestrator frontend is Vite + React 18 + a custom shell, while the
Control Plane web app is Next.js 15 + shadcn. Testing's surface should live in the Control Plane. A
third stack would guarantee the inconsistency this document exists to prevent.

---

## 5. The flow node

Artur's shaping on 20.08: one block in the flow with its own configuration panel, into which
predefined connector plugins are assembled — built-in connectors for the common cases, plus a generic
model-driven connector as the escape hatch when a client asks for a custom check. *"To jest mała
platforma, także o to w tym wszystkim chodzi."* The UI for that configuration was left open and unowned.

Concretely (**VIS-186**): a manifest under `catalog/` the way agents and flow templates are declared
today; node rendering via `CardNode.tsx` / `node-theme.tsx`; configuration in `PropertyPanel.tsx`,
with the step picker driven by the `list_steps` MCP tool rather than a hardcoded list — which is what
keeps the panel correct as the catalog grows. §11.6 (deterministic node vs model-driven agent) is
settled here.

---

## 6. First increment

Marcin's recommendation on 20.08, which Artur accepted, and which stays the right starting point:
defer the LLM steps entirely and port the deterministic checks that today sit spread step-by-step
across the flows — `run-tests.v1`, `run-quality.v1`, the QA slice of `qa.test-author.v1` — into the
single Testing block (**VIS-187**). It makes today's checks immediately reusable, gives one small API
surface, and lets the harder steps be built independently.

The sequence that follows from the dependency order:

1. **VIS-178** — settle the boundary. Everything else assumes an answer.
2. **VIS-179**, **VIS-181** — the contracts. Cheap, and both surfaces block on them.
3. **VIS-183**, **VIS-182** — faker-backed API, then the page. Demoable at this point.
4. **VIS-187**, **VIS-186** — real steps behind the surface, then the flow node.
5. **VIS-180**, **VIS-184**, **VIS-185** — history, drawer, checklist.

---

## 7. Still open

- **Where intent comes from** (§11.1). `spec-fit` needs the issue and the implementer's plan. If
  Context Fabric carries the task, the caller's narrative becomes a fallback rather than a requirement.
- **Sandbox image composition** (§11.3). Playwright plus browsers makes this sharper for Testing than
  for Review: one image carrying every toolchain grows without bound.
- **Matcher non-determinism across rounds** (§11.7). For Testing this is worse than for Review — a
  trend line built on signals whose producing step was matched non-deterministically is not a trend.
- **The step classification taxonomy**, flagged by Marcin himself on 20.08 as needing work, and still
  unowned.
- **Client-facing capability documentation.** efinti's CTO asked for it explicitly on 18.08 and there
  is nothing to send. Not in this epic; worth a ticket of its own.
