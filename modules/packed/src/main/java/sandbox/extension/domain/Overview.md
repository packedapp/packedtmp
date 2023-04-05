Usecases
- CLI : Application|No extension participants - Args all application, commands app lifetime or immediate 
- Service : Container|Alle har deres egen + exports -
- Metrics : Application(Family)|Alle
- Eventbus : Application|All, IDK?
- WebServer : Application|?, KDL
- Config : Application|All
- JDBC : Some Container|Down | All
// renames are very very common.
/// EventBus -> Translate one event to another.
/// WebServer -> Prefix one namespace with a name and attach to other (
/// config - rewrite root
/// JDBC -> ?

//// App-To-App
// Yes we want to use the same WebServer, JDBC, EventBus, ConfigFileFragment, MetricsServer


/// I use Domain X -> 


- FileRealm :
- NetRealm?

FamilyRoot
ApplicationRoot
ContainerLifetimeRoot
Container


Scope
Tror ikke man kan have andet en Application Scope
-- Fx NamespaceOperation.navigator() kan jo ikke inkludered andre applicationer
   hvis de bliver bygget concurrently
   
   
   
   

   
------ 