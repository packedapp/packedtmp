package app.packed.application;

import app.packed.build.BuildWirelets;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.service.ServiceLocator;

/**
 * An entry point for all the various types of applications that are available in Packed.
 * 
 * For creating application -instances -mirrors and -images.
 */
// Ved ikke om vi vil have den i endelig version.
// Maaske er den bare god i forbindelse med udvikling.
public final class App {

    /** Appless */
    private App() {}

    // App.daemon().restartable(RestartablePolicy.IMMEDIATLY).start(new Assembly());
    // App.cli().execute()
    // App.cli().execute()

    public static void cli() {
        throw new UnsupportedOperationException();
    }

    public static void mainImage(Assembly<?> assembly, Wirelet... wirelets) {
        // Som cli. Men optimeret for lavt hukommelse
        // Vi clear'er altid vores bean cache faetre naar vi laver images
        throw new UnsupportedOperationException();
    }

    public static void cli(Assembly<?> assembly, Wirelet... wirelets) {
        ServiceLocator.of(assembly, wirelets);
    }

    public static JobBuilder job() {
        throw new UnsupportedOperationException();
    }

    public static ServiceLocator serviceLocator(Assembly<?> assembly, Wirelet... wirelets) {
        return ServiceLocator.of(assembly, wirelets);
    }

    /**
     * @param assembly
     * @param wirelets
     * @return
     * 
     * @see BuildWirelets#reusableImage()
     */
    // Det vi taenker er at det er sjaeldent at et rod image skal genbruges.
    public static ApplicationImage<ServiceLocator> serviceLocatorImage(Assembly<?> assembly, Wirelet... wirelets) {
        return ServiceLocator.imageOf(assembly, wirelets);
    }

    public static ApplicationMirror serviceLocatorMirror(Assembly<?> assembly, Wirelet... wirelets) {
        return ServiceLocator.mirrorOf(assembly, wirelets);
    }

    public interface JobBuilder {
        // mirror();
        // launch();
        // ...
    }
}

//??? App kunne ogsaa vaere en klasse hvor der shutcuts for alt?
//App.cli(new Assembly());
//App.mirror(new Assembly());
//App.serviceLocator().newLauncher(new Assembly);
//App.serviceLocator(new Assembly());
//App.daemon().restartable().execute(new Assembly);
//App.daemon().restartable().newLauncher(new Assembly);

//App.create/start/execute + mirror + launcher