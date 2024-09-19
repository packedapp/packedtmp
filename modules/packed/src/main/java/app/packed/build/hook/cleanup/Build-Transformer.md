
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

// Two ways to a

// Link
// -- MyAssTran.preBuild
// ---- Link(Ass)
// ....
// - Assembly.Build


// Can be applied as "ClientProxy" <-- Maybe this is Augmentation

// Can be applied as @BeanHook
// Can be applied as @AssemblyHook

//Can be applied as AssemblyTransformer

// Questions
//// 1. Stateless  (+ Locals) vs Statefull Must
//// 2. Pre/post? More control.
//// 3. Context or protected methods?

// Hook -> Match + Transformer

// BuildTransformer???

// Would be nice to have a way to fx apply @Debug everywhere.
// A readonly transformer

// Ville måske være godt at kunne få info ned...
// Ellers må vi jo have oplysningerne på ContainerConfiguration og så tage den med.

// Det ville være rigtig fedt at kunne se hvem der havde transformeret hvad

// Det ville også være fint at have hvem og hvad seperaret

// We don't actually transforming anything just prepare it
// Would be nice to able
// I think we are returning an delegating assembly

// I think delegating assembly may allow hooks annotations. But must be open!!
// No maybe this is simply the way we support it for now..

// We need some cool examples.
// Like print everytime a bean is instantiated

// Maybe it is instrument\
// Maaske optrader det en synthetics delegated assembly

// Recursively er specielt.. ComponentTransformer??? Hmmm