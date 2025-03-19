LifetimeStore
  Stores beans and other important information for a single lifetime
  Immutable after Initialization of the lifetime.
  Support Mutability via Lazy/StableValue/AtomicReference stored in the lifetime
    ?? IDK about this. Let us say we have 100 constructed session beans... Well then we construct a map where we store them, I assume 
    
    
  Implementation
    Object[], but may be specialized in the future
  
  Each Entry in the store has a (Class) type
  ?Each Entry in the store has a kind. Maybe it is just fkcing beans always...
    For simplicity, we don't have attachment all other kinds of shit. Simpler model  

Lifetime Instance
  1 Application Lifetime for each Lifetime
  * Session Lifetime Instances for a single Application
  
Lifetimes a are hierachical.
  A child lifetime cannot be initialized before its parent have been initialized? IDK, bootstrap, transient beans
  A child lifetime cannot be completely shutdown before any of its children.
  However, it may start shutdown before each childnre
  
LaunchKind
  Initialization, Start/Stop, Operation (LifetimeStore can theoretically be stored on the stack)
  

LifetimeOperations vs LifecycleOperations
  Kind of meta operations, that are targeted a Lifetime, and not a bean..
  For single beans they are attached to the bean, for composite lifetimes
  They are attached to the launching bean, possible, in a parent application? Maybe just a synthetic bean
  
LifetimeBean
  A bean that extensions install in a lifetime to manage state for beans in the lifetime. For example, ScheduledRunner[].
  The lifetime bean can then have @OnStop that removes the state from a parent controller bean of some kind