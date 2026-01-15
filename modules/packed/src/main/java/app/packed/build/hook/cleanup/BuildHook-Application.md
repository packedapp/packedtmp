DeclarationSite:

Where is the hook declared

ASSEMBLY
  The hook is declared on an Assembly

BEAN
  The hook is declared on a bean class
  
DELEGATING ASSEMBLY [Manually]
  We create a new (delegating) Assembly from another assembly

TEST
  We support it in someway with testingx
  
----- Not Supported
Wirelet
  The hook is declared on a Wirelet? Don't think we will be supporting this. 
  At least we should have a @SupportWireletHooks on the assembly.
  Or maybe take a lookup BeanHook.toWirelet(Lookup lookup)
  
      
Properties
[Observing | Transforming]
[ApplyToExtension | OnlyLocal]  (Does not make sense for Beans)