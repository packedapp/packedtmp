When can a managed lifetime exist

/// A pod, does it need daemon threads?


App.run(@Main "HelloWorld") <- Must exit

App.run("HelloWorld") <- No EntryPoint|No start threads? <- Just exists (Don't fail, it is useful for demos)

Session(Managed) <- Must not exit, but also does not have Entry point
 
Pod.run(@Main "HelloWorld") <- Must exit??? I don't know


||||| What is really the usecase for shutdown except for threads? If there are no threads within a I don't any use for shutdown
||||| LifetimeBean shutdown makes sense, for it is really only controlling access from outside of the lifetime

/// Altsaa Kan jo have en webserver hvor alle traaedene er i parent app'en, saa der er ikke noget eksekverede derinde
/// Men virtuelle virtuelle traade er maaske tilknyttet lifetimen for sub-applicationen

Session(isManaged) <-- does not need threads


When can an Application Exit???