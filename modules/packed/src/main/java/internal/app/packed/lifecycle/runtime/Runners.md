Vi må have 4 modes

// Hvad skal vi gemme
  OperationHandles unsorted
  OperationHandles sorted
  
  MethodHandle

// HVor
  BeanSetup
  BeanOperationSet
  BeanLifetimeSetup
  RegionalLifetimeSetup

Bean
  Init only   [Store in BeanLifetimeSetup] SingleMethodHandle
  Start/Stop  [Store in BeanLifetimeSetup]
  
Regional
  Unmanaged
  Managed
  
  
  