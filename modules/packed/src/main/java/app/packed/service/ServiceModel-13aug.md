Namespace Service - A service from the namespace

Bean Service - A service that is only available for a specific bean

Operation Service - A service that is only available for a specific operation

--------------- Rethink
Lifetime
Extension and services (maybe it is services)

AutoService - Just depend on it


[A service is ca

Injection
[Operation][Bean][NamespacesX]

Either a Bean has a ServiceNamespace. Or Namespace is not just for containers

Possibilitites
  * A namespace is both for container, and-or beans, and-or-operations
    Cons: Namespace is now a lot more complicated than a set of containers
  * A service either belongs to a 
  
  



Services <- Are users
Hooks <- Are extensions  
  

ServiceScope
  NAMESPACE, BEAN, OPERATION
  
  
BindingKind
  HOOK, MANUAL, SERVICE

Force bindings...., Override, Proxy ect