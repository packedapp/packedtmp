package app.packed.application;

import app.packed.build.BuildException;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.inject.service.ServiceLocator;

/**
 * An entry point for all the various types of applications that are available in Packed.
 * <p>
 * This class does not prov For creating application -instances -mirrors and -images.
 */
public final class App {

    /** Appless */
    private App() {}

    public static ApplicationImage<Void> build(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static ApplicationImage<Void> buildImage(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    static Launcher launcher() {
        return new Launcher();
    }

    public static ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    /**
     * Builds
     * 
     * @param assembly
     *            the assembly to build and run an application from
     * @param args
     *            strubg
     * @param wirelets
     *            optional wirelets
     */
    public static void run(Assembly assembly, String[] args, Wirelet... wirelets) {
        ServiceLocator.of(assembly, wirelets);
    }

    /**
     * Builds an application from the specified assembly. After which a single instance is created that runs until
     * termination.
     * 
     * Waiting until the application has termianted before returning.
     * 
     * @param assembly
     *            the assembly representing the application
     * @param wirelets
     *            optional wirelets
     * @throws BuildException
     *             if the specified application could not be build
     * @throws UnhandledApplicationException
     *             if the fails during runtime
     */
    public static void run(Assembly assembly, Wirelet... wirelets) {
        ServiceLocator.of(assembly, wirelets);
    }

    static class Launcher {
        // Kunne have noget med noget Throwable

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