                Outside       Controller        Inside                    Scope                                     Stoppable     Restartable
Application      XApp      ManagedLifecycle     ApplicationContext        Application                                 Yes          Maybe
Session            ?               ?            SessionContext            Namespace?                                   ?            No
Request            None            ?            RequestContext            Operation, Bean, Namespace, Application     Exception     No
DaemonJob       DaemonJobFuture    ?            DaemonJobContext          Operation, Bean, Namespace, Application      Yes         Maybe



ManagedLifecycle
    Control  start, stop
    Info     state, await (Altid external, eller internal hedder det vel sleep????)
    
    
Invokation thingies

_ invoke(x)  -> DaemonJob Start/Stop i et kald

Stopable invoke(x) -> Start() and then call Stop to stop it 

Bean/Record -> Faar injected Stopable 