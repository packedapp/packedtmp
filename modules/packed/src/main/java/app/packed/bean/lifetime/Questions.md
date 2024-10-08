/Vi mangler lidt "plugins" paa en eller anden maade.
Vi vil gerne enten starte den her all eller den her app.


Region

Properties
  Has a (single) thread
  Where it can be applied
  Has a context injectable
  Strict ContainerTree (fx plugin vs session)
  Annotations
    
  
Job             -> Region, Bean, Method
Session         -> Region
Tenent          -> Region
Plugin          -> Region
OnStart         -> Method
Entity          -> Bean
Request         -> Bean, Method
Event           -> Bean, Method
Prototype       -> Bean (No Context)
ServiceProvsion -> Method, Prototype Bean(hmm)

Er det 1 koncept eller flere koncepter???


Job kan vel have shared klasser mellem hvert job...


--------------
Arghhh, Efter vi har flyttet flyttet session udfra container strukturen... Ser contextjo anderledes ud.

Bean, Operation, Scope????



Scope vs Context -> Contexxt

Static vs Dynamic -> Ideen er at dynamic

------
StaticBean -> Standalone bean, does not have a runtime state
EntityBean -> Standalone bean, runtime state is not controlled by the application



EntityBeanContext


----------

Tenent->Session->Job