Primary Bean
Side Bean -> Supports the primary bean


Multi instance per Bean
  10 @CronSchedule -> 10 Sidebean instances
  10 @BeanInstantiateTime -> 1 Sidebean instance
  
Lifetime/Lifecycle
  Per Bean/ApplicationInstance
  Per Bean/ContainingInstance  (Application, Session, Request)
  Per Bean/BeanInstance
  
Lifecycle Order
  Before/After primary bean? Configurable? on the configuration, on the sidebean

Provide
  ToPrimaryBean <- Should be able to provide "Services" to the primary bean, but maybe not directly used (for example, @InitializationTime Instant for PrimaryBean)
  @ToPrimaryBean(InitializationTime.class)
  Instant get()
  This also probably means that OnVariable should support installation, as this is typically the target
  
Injection
  AsABean <- I get all the possible injections from where the sidebean was configured (Like a prototype bean)
  FromPrimaryBean <- For example BeanInfo... Starting @FromPrimaryBean BeanInfo.componentPath

Invokers
  @FromPrimaryBean MyInvoker, Hmm, maybe special @OperationInvoker      
  
Binding
  Can bind build constants to a sidecar use site
  
  
?????

SideBean on SideBean??? 
  Should probably fail??? At least only dependant extensions