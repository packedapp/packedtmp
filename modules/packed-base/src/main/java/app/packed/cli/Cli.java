package app.packed.cli;

import java.util.Optional;

import app.packed.application.App;
import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationLauncher;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;

// Maaske er det en application der udelukkende starter andre applicationer...
// Maaske er det ikke en gang en application... Jo, fordi man skal kunne lave
// 
interface Cli {

    Optional<Throwable> failure();

    int exitCode();
    // information omkring hvordan det er gaaet.. alt efter Exception handling

    static Cli main(String... args) {
        return null;
    }

    static ApplicationLauncher<Cli> launcher(Assembly<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    static void launcher(Assembly<?> assembly, String[] args, Wirelet... wirelets) {
        App.run(assembly, wirelets);
    }
    
    static void run(Assembly<?> assembly, Wirelet... wirelets) {
        App.run(assembly, wirelets);
    }

    static void run(Assembly<?> assembly, String[] args, Wirelet... wirelets) {
        App.run(assembly, wirelets);
    }

    static ApplicationDriver<Cli> driver() {
        throw new UnsupportedOperationException();
    }
}
