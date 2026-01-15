BuildHook, can be used for
  BuildTransformation -> Changing properties of the build
  BuildObservation -> Be invoked when important things happen, Could fx post a JFR message -> App build done, took 0.123 seconds
  BuildVerification -> Invoked when completed
    
  Can both be used for transformations, observations, and verifications
  In practices there are no difference between observe and verification


IMPORTANT
  PROPAGATION (On Assembly level only)
  FILTERING (Should we use this method?)
  APPLIING (Actually applies to it)


Vs Interceptors
  I don't think this has anything to do with OperationInterceptors/MethodInterceptors, WebInterceptors
  Specifically, I don't think you can use it to introduce interceptors, Or maybe you can, idk?
////// Specifically I don't 


ORDERING
  We need determinisic ordering
  Ordering also cannot fx depend on Component Tags, as they may change
  So we also cannot specify it doing a normal install
  
  Ideelt set er det den yderste der bestemmer. Ligesom vi har system-settings for opens ect. 
  Det betyder ogsaa at Assembly, bestemmer over Bean is sidste ende

  It only makes sense to do ordering on BuildHooks of the same type  
