Binding/Service

Fungere bare ikke 100%
Er også tæt forbundet med Lifecycle Application<Session



-------- Props
boundBy : Hvem har bundet den / Dig selv | Extension

======================= STUFF

  Manual -> The user manually bounded the variable in some way to an instance

----- Binding Activators (Are always Extension based)

  AnnotatedBeanVariable  (Because a bean parameter or field is annotated with)

  AnnotatedBeanField     (Because a   bean field is annotated with)
  AnnotatedBeanMethod    (Because a bean method is annotated with

  AnnotatedBeanClass     (Because the bean class is annotated with)
  
  BindingClass             (Because the class of a referenced Variable is annotated with @)
  InheritableBindingClass  (Because the class (or super classe) of a referenced Variable is annotated with)

------ (Service) Keys
    Operation?
    Bean
    Namespace/Container  
    
    [Session <- Application]
--------
Ma


Hook
Namespace

Manual       -> A variable was manually bound

Bean.Service -> A Key/Service was bound to the
Operation.Service -> Key/Service
Namespace.Service -> Container/


Hooks
Operation.Hook
Bean.Hook


Session

// Where does it come from
  MANUAL -> Bound to a Variable
               Either by the user or extension that has a hook?
  