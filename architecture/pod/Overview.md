# Overview

En pod er en samling af beans. 
  ? Der har samme lifetime???

Det er ikke nødvendigvis en Container, fordi vi fx kan have session beans flere steder.


App
  - Admin
     SomeSessionBean
  - User
     SomeOtherSessionBean
     
## Implications
 * Wirelets does not really work at runtime here.

We need to specify args to the pod.
But now we have Namespaces and pods that do not necessarily overlap 

PodLauncher
   argument(Key, Object) 

Naar man instantiere en application laver man en Pod...


=============
Containers, Extensions, Namespaces, Pods

Containers cannot be instantiated...

A pod is a Bean thing??? Pods can also contain namespace instances???

Pod  (Lifetime?)
  0 or 1 Parent Pod
  1 or more Beans
  0 or more Namespaces
  0 or more Entrypoints
  
  Single thread for initialization
  Lazy beans
  
  
  @Get instance bean   (Does not have a pod)
  @Get request bean    (Has a pod)

  @Job instance bean   (Does not have a pod)
  @Job job bean    (Has a pod)

--------
Pod kraever bean instances.
Men "ingen pod" kan lave et nyt namespace???
 
  
? Can a bean or namespace belong to more than 1 pod