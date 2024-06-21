+++++++++++++++++++++++++++++++
[App] <---- Hvis den bestemmer noget som helst... Så skal den også med!!!
[BuildSource  - ByteCode]
[Wirelet      - Info]
[ConfigSource - Info] (Er vel ikke officielt...)
++++++++++++++++++++++++++++++


BuildSource 
   [Assembly -> SpecificationSite] (SpecificationSite: link, AppInf.x, 
   [Extension - Not tracked (only traced)]
   [BuildHook - SpecificationSite tracked]  <--- Hmm, might be another build hook??
Wirelet - Meaning - SpecificationSite
ConfigSource - SpecificationSite(May be wirelet (Wirelet is ConfigSource I guess))

Vil vi have open ended ConfigSource forstået på den måde at en extension kan definere sit eget hulumhej.
Jeg tvivler nu at nogensinde kommer til at definere en anden api 






BuildSource <- A piece of Java code that can builds the program [Assembly, Extension, BuildHook] (Maybe composer at some point)
  Assembly   (can call out with AssemblyConfiguration to foreign code)
  Extension
  BuildHook <- Build-time interceptors (AOP) and listeners 

Config [Build]
  Environment
  Network
  Config Files

Wirelet

