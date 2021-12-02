package app.packed.mirror;


// Tror maaske jeg er frisk paa et common mirror interface...
// Om ikke andet for at goere det klart for alle at man refererer et mirror....

/**
 * Doing program analysis.
 * 
 * Models are typically not retained doing runtime. However
 * use wirelets. and then you can have them injected into components...
 * as any other service
 */

// http://bracha.org/mirrors.pdf

// It's fucking mirrors...
public interface Mirror {

    // Giver det ikke mening at have module paa alt...
    // Application.module = Assembly (or composer)
    // Container.module = the same
    // Component.module ????... Hvad hvis det er en 
    // Module module(); 
}

// Packed contains a mirror API for program analysis

//Immutable

//-Mjava.base#truststore=1230912378
//-Mjava.base/truststore=1230912378
//-Mtruststore@java.base=1230912378
//Map<String, String> Module.secrets();