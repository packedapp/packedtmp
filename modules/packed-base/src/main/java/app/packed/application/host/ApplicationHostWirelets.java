package app.packed.application.host;

import app.packed.container.Wirelet;

// Folk vil nok taenke det er er til selve extensionen???
class ApplicationHostWirelets {

    // Some default taenker jeg en host starter foerend en guest application??? IDK
    // Maaske er det noget man konfigurere. Giver jo kun mening fra en extension.
    // Dem der bliver registreret paa runtime har jo altid en started host
    static Wirelet initializeWithHost() {
        throw new UnsupportedOperationException();
    }
}
