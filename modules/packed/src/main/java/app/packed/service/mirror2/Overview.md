BindingProviders
   ManualProvider <--- IDK, maybe we need it for completeness
   HookProvider
   ServiceProvider
     OperationServiceProvider
     BeanServiceProvider
     ContextServiceProvider
     NamespaceServiceProvider

Binding
  BindingProvider provider 

Binding 
  
  ServiceBinding
     Optional<ServiceProvider> provider()
  HookBinding
    HookProvider
  ManualBinding