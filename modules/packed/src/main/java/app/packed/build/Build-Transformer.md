Build Transformers are the AOP of building an application.

If you are using the module-path for your application. The build transformers needs open access to be applied
 
 
 
 
============================== Applying Transformers ==================================

   Assembly Annotation, 
   Bean Class Annotation,
   
   DelegatingAssembly 
   
   
   // Support programmatic access??
      // Per Bean, Per Container // What is the use case We have a ContainerConfiguration already... 
      // new ContainerConfigurationTransformer().onNew();


   // Add Transformer (Assembly Propagation). All, Specific, ect. // Apply, Propagate to Child
   // Remove Transformer (By transformer Class?) I want to
   // Reorder Transformers      
      
============================== Transformers Precedens ==================================
Who has the final word??? The one at the last end of the usage chain... This means anything in an assembly can be overridden

This also means that the assembly should take precedens over the bean??? Yes this it what it means

Should we allow to override      

Target application wins


----------- I think we will be creating the list first. And then checking the Propagators
/////// BuildableAssembly
  .getAnnotations(); Repeatableable Annotations will be added, Non-Repeatable annotations will not override for any parent delegating assembly
  Will add to List<BuildTransformers, AssemblyPropator>

DelegatingAssembly
  Will Build a List<Pair<BuildTransformer, Propagation>> 
    .addList(from DelegatingAssembly || BuildableAssembly)      
      
/////// Assembly.Link
   List<Propagated Transformers> initialList
   . Like ordinary Assembly   (call propagate)      

      
///// Bean

Assembly.List<BeanTransformers) (Have already been checked for propagation
  bean.add

Ideen er lidt hvornår checker vi AssemblyProgator??
  Naar vi har aggregerede all BuildTransformers
  Med det samme naar vi har en BuildTransformer
    Problemet er her at vi intet ved om den assembly vi evt delegater til fx Hvilke annoteringer er der. Saa det maa være efter??




============================== Transformer Filtering ===================================
Should we support filtering on a high level?
Or should the transformers support their own filtering.

This really comes down to whether or not we should be able to create reusable Where we reuse the class

      
1.
Promulgation across assemblies is one big question
-- We need assembly open
-- I think we might need a promulgationStrategy=[Local, ChildAssemblies, AllDown]

2.
The other one is ordering between the transformers. 
I see this problem as identical  to method/operation interceptors
---------- OVerride
// I sidste ende, maa vi overerst paa applikationen kunne bestemmet.
// Saa vi skal nok have en slags reflection api
//// [Target, BuildProcessor*] -> BuildProcessor*

But also annotations first, wirelet first, what about promulgated

BuildProcessorDescriptor
   Wirelet
   TargetAssembly
   Class<?> Type



3.
Do we support custom build transformers?
And if so. How do we handle them if need lookup entities?


Minor Questions

1.
Can we have just one mirror