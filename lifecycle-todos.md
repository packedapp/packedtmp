# Lifecycle Runtime API TODOs

## ManagedLifecycle

**Core issue: it mixes two concerns.** It's both a state query interface (`currentState()`, `await()`, `isFailed()`) and a command interface (`start()`, `stop()`). This is fine if the only consumer is an external controller, but the comments suggest it could also be injected into guest beans — where `stop()` on yourself is odd and `start()` is meaningless.

Consider splitting into a read-only `LifecycleState` (await, query) and a `LifecycleController` that extends it and adds start/stop. Beans get the read-only view; external hosts get the controller.

**`state()` returns `ManagedLifetimeState`** but is suppressing an exports warning and throws UOE. If this isn't ready, leave it out of the interface entirely — stub methods on public interfaces become API debt.

**`stopInfo()`** returns `Optional.empty()` as default. Same concern — it signals the feature exists but doesn't work.

---

## ManagedLifetimeState

**The desired/actual model is premature.** Without restart support, `desiredState()` is always equal to `currentState()` except during transitions — which `isTransitioning()` already captures. And during a normal transition (e.g. STARTING), the "desired" state is ambiguous (RUNNING? or just "past STARTING"?).

Defer this to when restart exists. For now, `currentState()` + `isFailed()` + `failure()` is sufficient.

**Naming**: Consider `LifecycleSnapshot` or `RunStateSnapshot`. "ManagedLifetimeState" is long and overlaps with both `ManagedLifecycle` and `RunState`.

The `zaLS` stub interface in the same file should be removed or moved.

---

## StopInfo

**Overlap with RunStateTransition**: Both have `from()` state, `failure()`, and represent "what happened when we stopped." They serve similar purposes at different abstraction levels. Pick one or make one a subset of the other.

**`Trigger` as a class with string constants** is an unusual pattern. It's essentially an open enum. Problems:
- Users can't `switch` on it
- Equality is identity-based (not string-based)
- `ENTRY_POINT_COMPLETED` and `NORMAL` are different objects but both have the string `"Normal"` — this is a bug or at least very confusing

If the set is meant to be extensible, consider making `Trigger` a sealed interface with record implementations, or at minimum fix the duplicate `"Normal"` string. If extensibility isn't actually needed, make it a proper enum.

**`hasResult()` / `isCancelled()` / `isCompletedExceptionally()` / `isCompletedNormally()`** — this mirrors `CompletableFuture`'s completion model. But a lifetime stop isn't really a future completion. A stop is either normal, forced, or failed. Simplify to: `isNormal()`, `isFailed()`, `failure()`, `trigger()`.

---

## StopOption

**Marker interface with only static factories** — this works but makes the type unimplementable by users (no methods to implement). If user extension isn't intended, `sealed interface` would make that explicit.

**Too many overlapping concepts**: `forced()`, `now()`, `now(Throwable)`, `forcedGraceTime()` — three or four ways to express "stop hard." Pick one model: `forced()` means interrupt threads immediately, `forced(Duration graceTime)` means try orderly then interrupt. That's two methods, not four.

**`parentStopping()` as a StopOption** feels wrong. That's an internal lifecycle event, not a user-facing option. It should be an internal signal, not part of the public API.

**`restart()` / `tryRestart()`** — the distinction isn't clear from the API. If `restart()` throws when restart isn't supported and `tryRestart()` is best-effort, name them `restart()` and `restartIfSupported()` or similar.

---

## RunStateTransition

**Clean and focused** — this is the best-designed type in the package. `from()`, `to()`, `failure()` is exactly the right surface for an `@OnStop` callback parameter.

---

## Package-level suggestions

1. **Reduce the type count.** `StopInfo`, `RunStateTransition`, and `ManagedLifetimeState` all describe "what state are we in and why." Consider whether `RunStateTransition` alone (with an added `trigger()`) could replace `StopInfo`.

2. **Package name**: `lifecycle.runtime` reads as "runtime stuff for lifecycle." If this is specifically about managed/running lifetimes, `lifecycle.managed` or just collapsing into `lifecycle` would be clearer.

3. **Defer restart.** Multiple types have restart concepts baked in (`StopOption.restart()`, `Trigger.RESTARTING`, `zaLS.isRestarting()`). Since none of it works yet, removing it from the API surface makes the current API smaller and avoids locking in a restart design prematurely.
