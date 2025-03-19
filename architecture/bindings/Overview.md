Resolution 
  Manual  -> 
  Hook    ->
  Service ->
  
// Problemet er her AutoService...
// Okay saa baade Service og Hook har en eller anden form for BindingMatcher [Annotation | Key]

Services Resolution: Operation->Bean->Context->ServiceNamespace

Resolution = [Manual | Service [Operation, Bean, Extension Context[Class?], Namespace], Annotation[...]]


// Prep

// Early Resolution
//// Look for Annotated Variable Bean Trigger
//// Look for Extension/Context Service Bean Trigger

////  [These cannot be rebound], thats it, can either be manually bound later, will attempt to be service resolved later

The rest will be resolved as a service (Key) or m

/ Can manually override by binding index or as Service, will fail for bindings that have been resolved already

//// AssemblyResolution
// Resolution -> Keys bound to operation, Keys bound to bean, Keys bound to service namespace



-------------------- Injectors
FooInjector
  call();


MyExtensionBean
   MyExtensionBean(FooInjector[] injectors) maybe MyExtensionBean(@Injectors FooInjector[] injectors) 
  



boundBy = Realm

Context = Hook

  
Constant vs Operation

Buildtime vs Runtime

Service vs Hook

Constant Runtime -> Constant per Bean Instance? or Application Instance

----
De der abstract invokers. De er maaske en (scan free) bean i virkeligheden... Eller maaske en sidecar de foelger jo instancen og faar injected instancen???
  
  [Build time value][Runtime value]

Der er build values, saa kan der kommer run