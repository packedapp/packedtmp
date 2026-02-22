----
  Namespace
  NamespaceNode


------
Namespace
  Set<ResourceKey> keys();
  Set<ResourcePath> resourcePaths();

NamespaceResource
  ResourceKey
  ComponentPath

We need namespace because stuff with ids is shared across containers.
Or for example multiple databases.

Service.Namespace: ServiceNamespace
Service: ServiceNamespace/Key

// BeanNamespace -> all beans within a container have a unique name

/// Extra namespaces

// Tror ikke man som udgangspunkt kan lave namespaces lazily.
// Altsaa man kan jo sige main er lazy. fx for eventbus

A namespace has an id, a namespacetype, a name type, and an extend.

// Namespace:Kind:Container:id

// Namespace:Service:/:main

Usecases
- Cli : Application|No extension participants - Args all application, commands app lifetime or immediate 
- Service : Container|Alle har deres egen + exports -
- Metrics : Application(Family)|Alle
- Eventbus : Application|All, IDK?
- WebServer : Application|?, KDL
- Config : Application|All
- JDBC : Some Container|Down | All
// renames are very very common. (NamespaceTranslation?)
/// EventBus -> Translate one event to another.
/// WebServer -> Prefix one namespace with a name and attach to other (
/// config - rewrite root
/// JDBC -> ?

//// App-To-App
// Yes we want to use the same WebServer, JDBC, EventBus, ConfigFileFragment, MetricsServer

