package app.packed.cli;

import java.util.Optional;

import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationImage;
import app.packed.application.programs.SomeApp;
import app.packed.bundle.Assembly;
import app.packed.bundle.Wirelet;

// Maaske er det en application der udelukkende starter andre applicationer...
// Maaske er det ikke en gang en application... Jo, fordi man skal kunne lave

// En central observation er at hvis der er flere entry points skal de alle returnere det samme
// Det giver fx ikke mening at returnere Daemon fra en og Injector fra en anden
interface Cli {

    Optional<Throwable> failure();

    int exitCode();
    // information omkring hvordan det er gaaet.. alt efter Exception handling

    static Cli main(String... args) {
        return null;
    }

    static ApplicationImage<Cli> launcher(Assembly  assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    static void launcher(Assembly  assembly, String[] args, Wirelet... wirelets) {
        SomeApp.run(assembly, wirelets);
    }
    
    static void run(Assembly  assembly, Wirelet... wirelets) {
        SomeApp.run(assembly, wirelets);
    }

    static void run(Assembly  assembly, String[] args, Wirelet... wirelets) {
        SomeApp.run(assembly, wirelets);
    }

    static ApplicationDriver<Cli> driver() {
        throw new UnsupportedOperationException();
    }
}
