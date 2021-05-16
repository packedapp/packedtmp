package app.packed.application;

import app.packed.container.Extension;
import app.packed.state.sandbox.InstanceState;

// Okay det ville vaere alt for vildt...
// Men ville selvfoelgelig kun installere den i root containeren...
// Alts

// Kan simpelthen ogsaa vaere at man har reject ApplicationRuntimeExtension
// som afgoer om man kan bruge det...
// Ligesom vi har reject(FileExtension, NetExtension, ApplicationRuntimeExtension)

//
public class ApplicationRuntimeExtension extends Extension {
    ApplicationRuntimeExtension() {}

    public void defaultLaunchMode(InstanceState is) {
        // Kan ogsaa konfigurere alle disse hosts/applications ting her vel????

    }
}
// Man kan ikke selv installere den selfoelgelig...
// Den fejler hvis det ikke er en root container i en applikation

//Plusser
// Det er sgu dejlig let..

// ApplicationRuntime

// Her kan vi proppe alskins goer dit og goer dat...