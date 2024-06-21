

# Mirrors
BuildInstanceMirror <- A mirror of the actual BuildHookInstance


Wirelets
Wrappers (Are just wirelets and Assemblies.wireWith
@Assembly  (Maybe have extensions. @NewApp adds the annotations pre existing)
@Bean

AssemblyModel scans Class

Wirelet -> Aggregates

---- Algo
  forEachWirelet() -> 
     Verify ApplicationSite for ApplicationHook & AssemblyHook
     PackedContainerBuilder.buildHooksFromWirelets.add(Hook, Propagation) //BuildHookWirelet???
  
  AssemblyModel.Scan (static)
  
  Wirelet+AssemblyModel merge
   (class, List<Hook, Propagation>)
   ApplicationHooks->Added directly to ApplicationSetup
   AssemblyHooks -> Added directly to AssemblyHooks



----- Unknown


- I think we need to be very clear that they are applied to all Xs in the same Assembly.
  for example newContainer(Wirelet.observer(someBeanBuildHook) <--- Will be applied to all beans in this container and subcontainers in this assembly

- We don't need lookup for interconnecting newContainer(xxxx) as this is inter assembly. Actually not for linking either. Maybe we should stress Inter/Outer Assembly


