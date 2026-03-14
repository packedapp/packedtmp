

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

