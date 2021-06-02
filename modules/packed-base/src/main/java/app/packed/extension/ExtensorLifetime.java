package app.packed.extension;

// Maaske have en ApplicationExtensor
// Ved ikke med Platform...

// Hoved problemet er jo. Hvordan lukker man dem ned???
// Skal vi tracke referencer?????
enum ExtensorLifetime {
    PLATFORM, 
    
    APP_TREE,

    APPLICATION,
    
    CONTAINER;
}
