package app.packed.container;

// Maaske have en ApplicationExtensor
// Ved ikke med Platform...

// Hoved problemet er jo. Hvordan lukker man dem ned???
// Skal vi tracke referencer?????
public enum ExtensorLifetime {
    PLATFORM, 
    
    APP_TREE,

    APPLICATION,
    
    CONTAINER;
}
