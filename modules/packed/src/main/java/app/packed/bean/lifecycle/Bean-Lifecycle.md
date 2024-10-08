Vi har 5 operationer

Factory, Inject, Initialize, Start, Stop

Factory, Inject, Initialize -> Mapped into an initializer

Annotation
Mirror
Configuration
(3 x Context)



Maaske er Inject en CreateOperation
// Og saa har vi en liste...
// Og en en metode-> isInstantiating


BeanLifecycleOperationConfiguration
BeanLifecycleOperationMirror

Factory [BeanFactoryOperationMirror, BeanFactoryConfiguration] 
Inject [Inject, InjectOperationMirror, InjectConfiguration]
Initialize [OnInitialize, OnInitializeOperationMirror, OnInitializeConfiguration]
Start [OnStart, OnStartContext, OnStartOperationMirror, OnStartConfiguration]
Stop [OnStop, OnStopContext, OnStopOperationMirror, OnStopConfiguration]


// I don't know if we map factory+inject into same type...
// Or maybe it is is a Initialization method
// Eneste problem er dependency order, som ikke giver mening for inject, factory



BeanFactory, BeanStart,... Why not BeanInject than???



// Sub folder to Bean????

// New Hierarchy
// LifecycleOperation
//// BeanCreationLifecycleOperation
////// BeanFactoryLifecycleOperation  (Maaske bliver de slaaet sammen en? Nej vi kan have flere inject men kun en factory
////// BeanInjectLifecycleOperation
//// BeanInitializeLifecycleOperation
//// BeanStartLifecycleOperation
//// BeanStopLifecycleOperation