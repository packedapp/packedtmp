How is the interaction between Assembly (both delegating and final) Annotations and Build Transformers.


  I think we should have a warning about removing build transformers! 
  They will be ignore
  
  
  // @TransformX(BooTransformer, remove FooTransformer.class transformer from AnnotationList)
  // @TransformX(FooTransformer.class
  // What can we do here
  
  //// Remove it from annotation keep as transformer silently
  //// Remove it from annotations, remove it from Transformers
  //// Fail with message saying you cannot remove BuildTransformers


  /// The other way we define the transformer first, then tries to remove it
  
  // @TransformX(FooTransformer.class
  // @TransformX(BooTransformer, remove FooTransformer.class transformer from AnnotationList)
  // What can we do here
  
  //// Remove it from annotation keep as transformer silently
  //// Remove it from annotations, remove it from Transformers
  //// Fail with message saying you cannot remove BuildTransformers
  Hmmmmmm, tror jeg lige jeg skal sove paa den her

  Et andet problem er hvis vi tilfoejer en BuildTransformer annoteringen. Bliver den saa tilfoejet som en BuildTransformer???? Og hvor bliver den tilfoejet i listen
  // Maaske har vi en AnnotationTransformer Der er separate fra AssemblyTransformer??? Som bliver koert forest. Men hvad saa med raekkefoelgen fra delegating assemblies.
  // Saa kommer  alle build transformer annoteringerne til slut, mens DelegatingAssembly.transformer bibeholder raekkefoelgen



Kan man fjerne BuildTransformer annotations???
  // Hvis vi har     default void transformAnnotations(AnnotationListTransformer transformer) {}
    Vil det vaere underligt

Ideen er lidt hvornår checker vi AssemblyProgator??
  Naar vi har aggregerede all BuildTransformers
  Med det samme naar vi har en BuildTransformer
    Problemet er her at vi intet ved om den assembly vi evt delegater til fx Hvilke annoteringer er der. Saa det maa være efter??




