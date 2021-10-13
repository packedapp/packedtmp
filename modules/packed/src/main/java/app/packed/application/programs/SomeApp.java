/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.packed.application.programs;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationLaunchMode;
import app.packed.base.Completion;
import app.packed.bundle.BaseAssembly;
import app.packed.bundle.BundleAssembly;
import app.packed.bundle.Wirelet;
import app.packed.state.sandbox.InstanceState;
import app.packed.state.sandbox.StateWirelets;
import packed.internal.application.ApplicationLaunchContext;

/**
 * An entry point for... This class contains a number of methods that can be to execute or analyze programs that are
 * written use Packed.
 * 
 * @see SomeAppImage
 */
// Tror maaske bare den doer med PanicException som standard hvis der gaar noget galt...
// noHookCaching...
// Maaske rename til App

// String res = App.job().restartable().execute(new Assembly);

//AppServer
public final class SomeApp {

    /** The artifact driver used by this class. */
    // Maybe use single-use-image as well...
    // Man kan sige det er en slags Profile+SystemInterface i et.
    // Men saa betyder det jo ogsaa at det er 2 forskellige drivere...
    // Eller ogsaa skal ImageWirelets lazy bruges

    // Install as System Namespace
    // Virker underlige at den returnere Complietion... Det betyder jo ogsaa at vi ikke skal smide
    // PanicException hvad jeg syntes vi skal

    /** A daemon driver. */
    private static final ApplicationDriver<Completion> DRIVER = ApplicationDriver.builder().executable(ApplicationLaunchMode.RUNNING)
            .buildOld(MethodHandles.empty(MethodType.methodType(Void.class, ApplicationLaunchContext.class)));

    /** Not today Satan, not today. */
    private SomeApp() {}

    /**
     * Returns the artifact driver used by this class.
     * 
     * @return the artifact driver used by this class
     */
    public static ApplicationDriver<Completion> driver() {
        return DRIVER;
    }

//    /**
//     * Runs the application.
//     * 
//     * @param assembly
//     *            the assembly to use for running
//     * @param args
//     *            program arguments
//     * @param wirelets
//     *            optional wirelets
//     * @throws RuntimeException
//     *             if the application failed to run properly
//     */
//    public static void run(Assembly<?> assembly, String[] args, Wirelet... wirelets) {
//        driver().launch(assembly, CliWirelets.args(args).andThen(wirelets));
//    }

    /**
     * This method will create and start an {@link Program application} from the specified source. Blocking until the run
     * state of the application is {@link InstanceState#TERMINATED}.
     * <p>
     * Entry point or run to termination
     * <p>
     * This method will automatically install a shutdown hook wirelet using
     * {@link StateWirelets#shutdownHook(app.packed.state.Host.StopOption...)}.
     * 
     * @param assembly
     *            the assembly to execute
     * @param wirelets
     *            wirelets
     * @throws RuntimeException
     *             if the application failed to run properly
     */
    public static void run(BundleAssembly  assembly, Wirelet... wirelets) {
        driver().launch(assembly, wirelets);
    }
}

class MainTester {

    // assertValid();
    // ServiceTester
    // <T extends ExtensionTester> use(Class<T> testerType);

    // contract(ServiceContract).assertContains();
    //// Hmm det er jo lidt et "Politisk" spoergsmaal...
    //// Fx validation skal koden fejle og kompilere???
    //// Ellers skal vi have en notifaction om det...

    // use(ServiceTester.class).assertContractContains();

    // Altsaa tit vil man jo ogsaa gerne starte noget...

    public static void main(String[] args) {
        SomeApp.run(new BaseAssembly() {
            @Override
            protected void build() {
                link(null);
            }
        });
    }

    static MainTester of(BundleAssembly  assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}