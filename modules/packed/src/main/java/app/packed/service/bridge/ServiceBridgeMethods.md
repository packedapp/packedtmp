shared between incoming and outgoing

remove(Key), remove(Class), removeIf(Predicate), removeAll();
retain(Key), retain(Class), retainIf(Predicate);
peek(Key, Consumer),peek(Class, Consumer),

rekey(Key from, Key to), rekeyAll(Function<Key, Key)
decorate(Key, Function),decorate(Class, Function), decorateAll(BiFunction(Key, Object, Object))

map(Op), replace(Op); // must not use Hooks 

replace(Class, Object), replace(Key, Object)



intoChild (Maaske er de paa hoved interfaced, or saa har vi kun share
ServiceContract childContract();




---- OLD

// Provide instances() (Runtime) // I think they must match a service...

// Transform Incoming, Outgoing  (not runtime)
// Contract check (Runtime) <- IDK hmm, Altsaa vel ikke hvis vi ikke har en service locator
// Anchoring <- Save services in the container that is used directly by the container

// provideInstance/provideConstant/provide (if transformers have provideConstant I think we should here as well)
// transform
// contract
// anchor

// exportTransitive
