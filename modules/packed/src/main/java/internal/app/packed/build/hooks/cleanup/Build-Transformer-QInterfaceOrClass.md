Q) Are we an interface or a class

We need a common class/interface because you will need to apply it somewhere.
So some method needs to take an instance of the transformer as a parameter

Problem is if the transformer should carry Propagation/Filter state then an interface is bad

Hvor tit laver vi transformers 

Filtering is always programatically... if (annotated with)

So  Propagation is always seperate...
It does not make sense 

Goer det paa SampleBean 

1)
Maybe abstract class ->

abstract class BeanTransformer2 {
  public final Propagation propagation();  //Optional???
  public final List<BuildTransformer.Descriptor> transformers();
  
  interface Descriptor {
    Class<? extends BuildTransformer> transformerClass();
    ComponentKind componentKind();
    Propagation propagation();
    Set<String> methods(); // the overridden methods
  }
}

Q) To seal or not to Seal

Currently we support 
   ApplicationTransformer 
   AssemblyTransformer 
   ContainerTransformer 
   BeanTransformer 
   OperationTransformer
   
I think this is fine to start