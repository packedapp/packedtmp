Der er 3 levels af inject
SidehandleBean
  .bindConstant()  <----- 

Sidehandle   <----- binder i virkeligheden en vaerdi til en Handle
  .bindConstant();


SidebeanInstance
  Services provided by Packed
  Services provided on a per attachment basis
  
  
  
  
  Attachment -> Per ComponentHandle
    Operation -> One Per OperationHandle
    Application -> One per ApplicationHandle
    
--  Launching with services
Vi supportere det ikke rigtigt. For operations er der besvaergelig.
Vi kan jo bestemme hvordan vi laver dens bean
Tilgaengaeld kan vi jo godt launche med parameter i forhold til applicationer
    