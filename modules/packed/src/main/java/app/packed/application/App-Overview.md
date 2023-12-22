An overview of the various App Interfaces

=== BootstrapApp 
The app that everyone uses to create their own app.

====================== Managed Apps ========================
== App
Conceptually like a Future with no result

Interface Features
  close/shutdown
  await state
  query state

Error handling
  Launch/Execution thread can be interrupted
  Can happen doing launch Internally
  Can happen doing runtime Internally
  

  An App must either have threads running after it has started, or an entry point.
  Well no, not after we have added Close.
  
== JobApp  (extends App????)
Conceptually like a Future with a single result

Features
  close/shutdown
  await state
  query state
  

== CliApp
Conceptually like a Future with no result
// Has a lot of default settings
  

  
  
======= SYNCHRONIZATION MODE ===========
We support both sync (run) and async (start/close) usage in the same interface ALWAYS (other we need to double the number of interfaces)
  
  // Sync mode will stop when exits main
  // Async mode will stop when explicitly called, or for example, main exits 
  
  
No Entry points -> Runs until shutdown or no non-deamon threads running
Entry points -> Runs until entry points exists






====================== Unmanaged Apps ========================