package app.packed.application;

// Grunden til vi laver den er jo fordi..
// Vi kan bygge et app image i noget der bliver instantieret med det samme
public interface ApplicationBuildInfo {

    // defaultLaunchMode() -> Lazy
    
    // Ved ikke om vi skal have den her...
    // Den er maaske bare noget vi kan holde internt...
    boolean isStaticImage();
    
    boolean isClosedWorld(); // isStaticImage
}
