ApplicationHandle fungere ikke super godt

XConfiguration is used for hooks... So we need 1Handle=1Configuration and we need to always construct it

Vi har reelt set 3 ting.


ApplicationConfiguration
ApplicationHandle
ApplicationTemplate
ApplicationInstaller

ApplicationInterface
ApplicationImage
ApplicationLauncher (Kun aktuelt hvis man vil launcher 1 gang, men så skal man ikke bruge Repo)

Handler<AppInterface, Image, Launcher>
Launcher
Image
AppInterface

[ApplicationMirror] skal jo også castes


OperationTemplate
  OperationInstaller
    OperationHandle
      InvokerFactory
        <Invoker>
      OperationConfiguration  


BeanTemplate      -> Extension
 BeanInstaller     -> Extension
  BeanHandle        -> Extension
   InvokerFactory    -> [Creates an Image, Launcher is args]
     <Invoker>
   BeanConfiguration -> User
     <BeanInstance>

// Runtime
Invoker

Template->Installer->Handle -> BeanConfiguraiton



ApplicationImage  -> 



Vi vil gerne kunne lave 
  A
  XApp.Image
  XApp.Launcher
  
  
=== Fra  
BootStrap
RepoKid (Altid kun images)
