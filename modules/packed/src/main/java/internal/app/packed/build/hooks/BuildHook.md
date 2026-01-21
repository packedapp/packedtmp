Version 0,1
  BuildableAssembly Only



Shared
  ignoreDuplicates default true

Callbacks
  Operation
    onNew               Just created, before returning to user
    onConfigured        User can no longer configure it
    onLastChance        We are going to stop having it configured
    onPastLastChance    You can no longer configure it
    onBuildSuccess (OperationMirror)

   Container
     onNew
     onNewExtension
     onBuildSuccess
     
Open Questions
  - Instances Per Assembly class or per Assembly instance 
  - User beans only, or extension beans as well
Not Implemented
   Beans
   DelegateAssemblySupport
   Propagation   (Only to applied assembly)
   BuildHook.apply
   Reordering (Maybe we don't support it, but only disable)