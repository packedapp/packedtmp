BeanLifetimeMirror

Do we want this? Or do we just have a EntityBeanLifetime

We can ikke et have et Store eftersom det er forskelligt per bean

------



Har vi flere begreber her

LaunchKind Init, Start/Stop, InOperation
Managed - Yes/No
Foreign - Yes/No
LazyMode - None, Non-Proxy, Proxied
Static/vs/Instance


LifetimeProperties[Managed, Foreign]

Lazinesss does not effect lifetime...
Managed -> Does not effect lifetime, arghh, yes/no. But we don't what Managed Foreign, UnmanagedForeign (Maybe we need)

LifetimeKind <-

We can definere 23 forskellige pods in application, der kan have instances.
// Maaske har [Kind, ContainerRoot]

// singleton er et daarligt navn for en statisk bean:)
 
 
 

 
 // DefaultLifetime, for en container
 
 Bootstrap <- Always unmanaged I would say. Single Threaded
 Foreign <- Can be both (Unfortunantely)
 Plugin <- Always Managed
 Transient <-> Always Unmananged (Kunne det ogsaa bruges som Bootstrap bean, kan jo bruges til at initialisere en application lifetime)
 
 
 ------------- Hvad skal det bruges til
 Finde alle entity beans? Er nok lettere at soege paa typen
 Finde alle entity beans knyttet til en database? Er nok lettere at kigge paa namespaces
 
 Finde alle SessionBeans? Jaa, altsaa Transient?
   Maaske har vi simpelthen en SessionBeanMirror????, ForeignBeanMirror?
   
   
Som mirror typer er de maaske ikke super interessant.
  Altsaa Taenker mest det er maadeen de inteagere med andre paa der er interessant.
  
Vil gerne have


[Der er noget med Type, Usage, Instance]
Igen 23 Pods -> Som kan initiseres forskelligt, og hver har instanser
Hvis vi har 23 Pods, har 23 lifetime mirrors, Men de har samme "Type"

   
 
 
 
 