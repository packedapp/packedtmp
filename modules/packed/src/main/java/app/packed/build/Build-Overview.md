Compose
Codegen

=================== Concepts (Well Defined
BuildLocal <-- Any kind of locals that are primarily available at build time
BuildSource <- A piece of Java code that can builds the program [Assembly, Extension, BuildHook] (Maybe composer at some point)
  Assembly   (can call out with AssemblyConfiguration to foreign code)
  Extension
  BuildHook <- Build-time interceptors (AOP) and listeners 

  

=================== Concepts (In Progresss) 
BuildAction <-- a specific instruction that does something


== Maybe add
Build Process (supportere kun en til at start med
Build Task (Thread)

// We always use VirtualThreads if spanning out 
Either that or



// Build
//// Compose, Compile
// JIT

BuildStep -> Would be compile. But Buildstep
------------
Build

Build Action (.setName?)


is build and codegen the same?

// What about extensions. And wtf they are doing? I think we need a special track extensions...

-> BuildSource | Has BuildInstructions which may be a composite

// We start with one build source... The root assembly
// Unfortunantely I think build sources have multiple phases. Assemblies only one though (build), Where I would generally say the name


// DEPRECATED, we use BuildHook for observers now
BuildObserver <- Can observe state when building the application. Used for permissions, logging, validating
