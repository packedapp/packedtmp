# Lifecycle Overview

This document describes how the lifecycle system works in Packed at both the application level and bean level.

## RunState

The `RunState` enum defines the possible states for managed entities (applications, containers, beans):

```
UNINITIALIZED ──► INITIALIZING ──► INITIALIZED ──► STARTING ──► RUNNING ──► STOPPING ──► TERMINATED
```

**Steady States** (require external trigger to transition):
- `UNINITIALIZED` - Initial state before any lifecycle operations
- `INITIALIZED` - All initialization complete, ready to start
- `RUNNING` - Application is running and serving requests
- `TERMINATED` - Final state, no further transitions possible

**Transitional States** (auto-transition when operations complete):
- `INITIALIZING` - Executing initialization operations
- `STARTING` - Executing start operations
- `STOPPING` - Executing stop operations

## Application Lifecycle

### Launch Sequence

1. **UNINITIALIZED → INITIALIZING**
   - Application starts in UNINITIALIZED
   - `launch(RunState desiredState)` triggers transition
   - Creates runtime pool for bean instances

2. **INITIALIZING → INITIALIZED**
   - Executes all bean lifecycle operations in order:
     - FACTORY: Bean instantiation
     - INJECT: Dependency injection (@Inject fields/methods)
     - INITIALIZE: Custom initialization (@Initialize methods)
   - On failure: Transitions directly to TERMINATED (no stop methods run)
   - On success: Transitions to INITIALIZED

3. **INITIALIZED → STARTING**
   - User calls `start()` or launches to RUNNING state
   - Executes all @Start methods
   - Supports forking via StructuredTaskScope

4. **STARTING → RUNNING**
   - All start methods complete successfully
   - Application is ready for use

5. **RUNNING → STOPPING**
   - User calls `stop()` or shutdown hook triggers
   - Executes all @Stop methods

6. **STOPPING → TERMINATED**
   - All stop methods complete
   - Application reaches final state

### State Management

The `RegionalManagedLifetime` class manages state transitions:

- Uses `ReentrantLock` for thread-safe state changes
- `await(RunState)` blocks until desired state is reached
- Multiple threads can call `start()` or `stop()` safely
- Only one thread performs actual state transition

## Bean Lifecycle

Beans go through up to 5 lifecycle phases, controlled by annotations:

### 1. Factory (Construction)

- Bean instance is created
- Runs during INITIALIZING state
- Always runs before dependants are constructed

### 2. Inject (@Inject)

- Dependencies injected into fields and methods
- Runs during INITIALIZING state
- Should only store dependencies, no business logic

### 3. Initialize (@Initialize)

- Custom initialization logic
- Runs during INITIALIZING state
- Controlled by `naturalOrder`:
  - `true` (default): Runs BEFORE dependant beans initialize
  - `false`: Runs AFTER all dependant beans initialize (coordinator pattern)

### 4. Start (@Start)

- Prepare bean for use (e.g., load data, open connections)
- Runs during STARTING state
- Supports:
  - `fork=true`: Execute in separate virtual thread
  - `naturalOrder=true` (default): Run before dependants start
  - `naturalOrder=false`: Run after dependants start

### 5. Stop (@Stop)

- Cleanup and shutdown
- Runs during STOPPING state
- Supports:
  - `fork=true`: Execute in separate virtual thread
  - `naturalOrder=true` (default): Run AFTER dependants stop
  - `naturalOrder=false`: Run BEFORE dependants stop (pre-notification)

## Dependency Ordering

The framework maintains a dependency graph between beans. If Bean A depends on Bean B:

| Phase | naturalOrder=true | naturalOrder=false |
|-------|-------------------|-------------------|
| Initialize | B first, then A | A first, then B |
| Start | B first, then A | A first, then B |
| Stop | A first, then B | B first, then A |

This ensures:
- Dependencies are ready before dependants during startup
- Dependants release dependencies before cleanup during shutdown

## Natural Order

The `naturalOrder` attribute controls execution order relative to the dependency graph:

```java
// Default: This bean initializes BEFORE beans that depend on it
@Initialize
void init() { ... }

// Coordinator pattern: This bean initializes AFTER beans that depend on it
@Initialize(naturalOrder = false)
void afterDependantsReady() { ... }

// Default: This bean stops AFTER beans that depend on it have stopped
@Stop
void cleanup() { ... }

// Pre-notification: This bean runs BEFORE dependants stop
@Stop(naturalOrder = false)
void notifyShutdown() { ... }
```

The concept is borrowed from `Comparator.naturalOrder()` - the default, expected ordering for the operation type.

## Forking with StructuredTaskScope

The @Start and @Stop annotations support forking operations to run concurrently:

```java
@Start(fork = true)
void loadDataAsync() {
    // Runs in a virtual thread
    // Application waits for completion before entering RUNNING
}

// Or manually fork multiple tasks:
@Start
void start(StartContext ctx) {
    ctx.fork(() -> loadUsers());
    ctx.fork(() -> loadProducts());
    // Both complete before RUNNING state
}
```

Implementation details:
- Uses Java 21+ `StructuredTaskScope` with virtual threads
- Joiner: `awaitAllSuccessfulOrThrow()` - all tasks must succeed
- All forked tasks must complete before state transition
- `interruptOnStopping=true` (default): Interrupt if stop requested during startup

## LifecycleKind

Beans have one of three lifecycle kinds:

| Kind | Construction | Destruction | Start/Stop |
|------|--------------|-------------|------------|
| NONE | External (static beans) | N/A | Not supported |
| UNMANAGED | By framework | Garbage collector | Not supported |
| MANAGED | By framework | Explicit stop() | Fully supported |

- **NONE**: Static beans with no instance lifecycle
- **UNMANAGED**: Prototype-scoped beans, created on demand, not tracked after creation
- **MANAGED**: Full lifecycle management, observable state, explicit cleanup

## StopInfo

When a bean or application stops, `StopInfo` provides the reason:

```java
@Stop
void cleanup(StopContext ctx) {
    StopInfo info = ctx.stopInfo();

    if (info.isFailed()) {
        Throwable cause = info.failure().get();
        // Handle failure cleanup
    }

    if (info.isCancelled()) {
        // Shutdown was cancelled/interrupted
    }

    RunState stoppedFrom = info.stoppedFromState();
    // RUNNING, STARTING, INITIALIZING, etc.
}
```

Predefined triggers:
- `SHUTDOWN_HOOK` - JVM shutdown
- `TIMEOUT` - Time limit exceeded
- `NORMAL` - Normal completion
- `FAILED_INTERNALLY` - Internal failure

## Lifetime Concepts

### ContainerLifetimeSetup

Manages the lifetime of a container and its beans:
- Holds list of beans in installation order
- Maintains ordered operation lists for each phase
- Creates runtime pool (`LifetimeStore`) for bean instances

### BeanLifetimeSetup

For beans with independent lifetimes (separate from container):
- Single bean with its own lifecycle
- Parent reference to container lifetime

### LifetimeStore

Runtime storage for bean instances:
- Maps beans to indices for fast lookup
- Created once per container lifetime
- Passed to runners during lifecycle execution

## Key Classes

| Class | Purpose |
|-------|---------|
| `RunState` | State enum and utilities |
| `RegionalManagedLifetime` | State machine and orchestration |
| `ContainerRunner` | Coordinates start/stop execution |
| `StartRunner` | Executes @Start methods |
| `StopRunner` | Executes @Stop methods |
| `ContainerLifetimeSetup` | Build-time lifetime configuration |
| `LifetimeStore` | Runtime bean instance storage |
| `LifecycleOperationHandle` | Operation handle hierarchy |
