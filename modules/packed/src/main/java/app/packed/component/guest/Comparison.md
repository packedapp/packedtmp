Skal selvfoelgelig fungere ligesom sidebeans.
Hvis jeg har 100 entities, skal jeg ikke 100 pool beans.

Saa install lazy, og saa brug i installer

// Kan en sidebean vaere shared???
   Fx en Pool kunne jo godt vaere en sidebean saa

Application ->   
     WrapperBeanen er altid placeret udenfor applikationer. Mht til Repository er den vel shared
  Install/Uninstall
  Launch
  Managed Map of X
  
Lifetime
  Launch
  Managed Map of X (For example, Session)

Bean
  Managed List of X
  Managed Map

=========
HttpInvoker(RequestContext context);

====================
At tilfoeje den til en host bean, kan jo let goeres fra inject...
Okay, saa vi har pool, og saa en simple sidebean der tilfoejer den paa @Inject
Hvordan faar vi koert stop.

=============== Lifetime vs Sidebean
Vi h
1 instant per lifetime
  
=============== Implementations ============
// Pool (No key)   (Needs both constructor/destructor)
// Map
// Start/Stop  (Future like)
// Launch