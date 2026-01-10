ApplicationWrapper <- Sidebean
   1 attachment -> ApplicationMirror
   Jeg har nogle ønsker til ting jeg vil have injected
OperationInvokerSidebean
  Invokeren indlaest
  
  
Forskellen er Sidebean er tilknyttet en bean i operationen
Application -> 100% standalone

Men en sidebean behoever vel ikke noedvendig vaere en sidebean til en faktisk bean.
  Maaske mere en slags SideComponent
  
  
  Application
    Har nogle services der kan injectes som default, og saa kan nok ogsaa expose nogen custom