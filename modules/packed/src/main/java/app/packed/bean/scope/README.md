
+++++++++++ Alle scopes har en lifecycle model..... +++++++++++
ApplicationScope
TransientScope (No Close) <- Per injectionPoint(Binding)
StaticScope (Functional + Static beans)  (No instances of the bean exists)

PluginScope (Is a plugin just a container where instances can be dynamically added and removed?)
             Kind of like a session... But maybe also provide services to the outside

JobScope  (covers both scheduled and ordinary scope)
SessionScope
RequestScope
EntityScope

Thread based scopes -> WebRequestScop, JobScope  (Kindof TransientScope)
Laziable Scope = ApplicationScope, SessionScope, PluginScope, JobScope, RequestScope
Providable Scope = (not static, Not entity)
Container Scope = ApplicationScope, SessionScope, PluginScope, JobScope
TopExportable =Application, Transient 

[Thread based -> Only specific threads accesses the scope, typically 1]
[Laziable Scope -> If it makes sense to create the beans lazily]
[Proviable -> If the bean can be used to provide services]
[Container Scope -> Whether or not the scope can be used for a container]

----------------
Hvilke scopes kan bruges af hvilke andre scopes.
SessionScope i et PluginScope...
Kan kun bruges af SessionScope i det samme PluginScope
Du kan have scopes i et scope????
ComposableScope??? (PluginScope, SessionScope)

For example, Lifetime of aSessionScope for a plugin (with PluginScope). Must be whatever is destroyed first.
The plugin or the session...

----------------
@ConsumeConfig  (what scope does this annotation require) <- Related to the containers scope????
-- Her er JobScope maaske lidt interessant.. Vi kan fx merge job configs with application configs. Eller have dem separat..


----------------
Foreign??? Beans managed outside of Packed
  Tror External er bedre...
  Supporter vi stop????
  Det er fx objekter der skal valideres???
  Vi supportere faktisk slet ikke nogen lifecycle metoder.
  Men skal vi fx have et ValidationScope
  
  