package internal.app.packed.application.deployment;

import java.util.Optional;
import java.util.function.Function;

import app.packed.application.ApplicationMirror;
import app.packed.application.BaseImage;
import app.packed.assembly.Assembly;
import app.packed.container.Wirelet;
import app.packed.runtime.RunState;

interface BootstrapLauncher<A> {

    // Hvorfor ikke bruge BootstrapApp'en som en launcher???
    // Det betyder selv at vi altid skal chaine..
    // Men det er vel ok
    // map()->

    default Launcher<A> launcher() {
        throw new UnsupportedOperationException();
    }

    /**
     * A launcher is used before an application is launched.
     */
    public interface Launcher<A> {

        default boolean isUseable() {
            // An image returns true always

            // Optional<A> tryLaunch(Wirelet... wirelets)???
            return true;
        }

        A launch(Assembly assembly, Wirelet... wirelets);

        // /**
        // * Launches an instance of the application that this image represents.
        // *
        // * @throws ApplicationLaunchException
        // * if the application failed to launch
        // * @throws IllegalStateException
        // * if the image has already been used to launch an application and the image is not a reusable image
        // * @return the application interface if available
        // */
        // default A checkedLaunch() throws ApplicationLaunchException {
//         return checkedLaunch(new Wirelet[] {});
        // }
        //
        // default A checkedLaunch(Wirelet... wirelets) throws ApplicationLaunchException {
//         throw new UnsupportedOperationException();
        // }

        /**
         * Returns the launch mode of application(s) created by this image.
         *
         * @return the launch mode of the application
         *
         */
        RunState launchMode(); // usageMode??

        /**
         * Returns a new launcher that maps the result of the launch.
         *
         * @param <E>
         *            the type to map the launch result to
         * @param mapper
         *            the mapper
         * @return a new application image that maps the result of the launch
         */
        <E> Launcher<E> map(Function<? super A, ? extends E> mapper);

        Optional<ApplicationMirror> mirror();

        // Hmmmmmmm IDK
        // Could do sneaky throws instead
        A throwingUse(Wirelet... wirelets) throws Throwable;

        default BaseImage<A> with(Wirelet... wirelets) {
            // Egentlig er den kun her pga Launcher
            throw new UnsupportedOperationException();
        }

        /**
         * Returns a mirror for the application if available.
         *
         * @param image
         *            the image to extract the application mirror from
         * @return a mirror for the application
         * @throws UnsupportedOperationException
         *             if the specified image was not build with BuildWirelets.retainApplicationMirror()
         */
        // Eller bare Optional<Mirror>
        static ApplicationMirror extractMirror(BaseImage<?> image) {
            throw new UnsupportedOperationException();
        }

        // ALWAYS HAS A CAUSE
        // Problemet jeg ser er, hvad skal launch smide? UndeclaredThrowableException

        // App.execute
        // App.checkedExecute <---

        // Maaske er det LifetimeLaunchException
//        public static class ApplicationLaunchException extends Exception {
        //
//            private static final long serialVersionUID = 1L;
        //
//            RunState state() {
//                return RunState.INITIALIZED;
//            }
//        }
    }
}