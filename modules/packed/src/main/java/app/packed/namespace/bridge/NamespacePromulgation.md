Skal supportere
  Nye applikationer lavet paa runtime...
  Hmm, vi bliver jo noedt til at hente configurationen paa runtime
  
  
  
  Jeg laver en ny application/container
  Specificere, namespace, Promulgering
  
  newApplication.promulgate(LoggingNamespacePromulgator.prefixAllWith
  
    newApplication.
       promulgate(LoggingNamespacePromulgator.defaults()).
       promulgate(JDBCNamespacePromulgator.ALL)
       promulgate(EventBusNamespacePromulgator.adapt(FooEvent->BooEvent);
       
----
Maybe we need to open a gate if MainServiceNamespace needs to talk to ExtensionServiceNamespace