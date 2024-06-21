XTemplate
XConfiguration
XHandle
XHandle.Installer (Or maybe on the template???)
XMirror
XSetup



ContainerFoo newComponent(ComponentKind

BaseExtensionPoint (Or Extension


Template -> Installer -> CreateSetup->CreateHandle->CreateConfiguration  |||| Configuration[User]+Handle[Mediator]+Setup[Internal]


Configuration<->Handle<->

Configuration->Handle->Setup->Configuration

Handle is always a small value wrapper


Bliver vel noedt til at have en installer. Kan ikke lave Configuration med hverken Handle. Jo hvis der ikke er "configure fase" er handle jo fin

