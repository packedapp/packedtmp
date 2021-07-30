package app.packed.cli;

import app.packed.application.ApplicationMirror;
import app.packed.container.Wirelet;

/**
 *
 */
// Maaske har vi ikke et specielt mirror...
// Det eneste vi spare er useExtension(CliExtensionMirror.class)
interface CliMirror extends ApplicationMirror {

    // Altsaa det er jo lidt problem.. Hvis man skal bruge nogle mainargs
    // Til at bestemme metoden man skal bruge

    // methods -> Assemblies that will be started

    // Kan vi tage any Assembly<Application> instead???
    static CliMirror of(CliAssembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}
