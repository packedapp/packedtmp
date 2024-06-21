ComponentGuest <- The guest instance component. Either an application/container/bean
ComponentGuestAdaptor, An optional bean that gets the instance injected plus helper classes. One adaptor exists per bean instance

ComponentHost <- Manages one or more guests

ApplicationImageRepository <- Allows registration and deregistration of applications

[[ComponentGuestAdaptor is lazy registered]]


------------------ Support
@OnComponentGuestRunstateChange -> On a Host or Adaptor to be notified of state changes

ComponentHost -
  
  Injections
    ComponentInstaller install(X)
    Map<String, ComponentGuest>
    ApplicationImageRepository


// Altsaa har man behov for at specielt configure den Host?


-------- Alternative Architeuctore

@ComponentHost <--- Class annotation

And then you can specify the host bean in Container.Installer...







----------------- Guest Instance Collections [DEPRECATED]
Classes that helps maintain instances IDK, if we are notified of changes via OnComponentGuestRunstateChange just use a CHM
