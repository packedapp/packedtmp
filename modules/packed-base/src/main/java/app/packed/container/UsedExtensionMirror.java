package app.packed.container;

import java.util.Set;

import app.packed.component.ComponentMirror;
import app.packed.mirror.Mirror;

// used by, uses...

// Giver den mening??? 
// Altsaa det vi vil spoerg om udenover typen..
// Hvorfor blev du aktiveret, hvem bruger dig
// IDK det er jo ikke noget vi registrere...

// UsedExtensionMirror
//// Kan vi sige noget om hvad vi har installeret...
//// Hvilke hooks der er knyttet op i mod den...


// Altsaa den er lidt mere beregnene...

public interface UsedExtensionMirror extends Mirror {

    /** {@return a descriptor for the extension that is being modeled.} */
    default ExtensionDescriptor descriptor() {
        return ExtensionDescriptor.of(type());
    }

    /** {@return the type of extension being modeled.} */
    Class<? extends Extension> type();
    
    // Components from own realm (I think)
    Set<ComponentMirror> installed();
}
