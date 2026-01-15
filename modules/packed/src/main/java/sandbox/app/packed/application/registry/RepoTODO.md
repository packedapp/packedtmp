SingleLauncher
Repository - No Dynamic Applications
Repository - Applications at runt


Fix naming build/runtime
Clarify runstates

Guest/Launchers/Managed Instances

    // Shared with ContainerRepository
    interface Entry {
        boolean isBuild();

        boolean isLazy(); // Built on first usage

        // An application can be, NA, INSTALLING, AVAILABLE
        // Don't know if we at runtime
        // Hvad hvis man ikke vil installere noget paa runtime...
    }
    
    
Dependants
  Can always only install 1 of a kind, and one runtime, 