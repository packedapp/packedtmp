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
package app.packed.cli;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.TimeUnit;

import app.packed.component.App;
import app.packed.component.ArtifactDriver;
import app.packed.component.Assembly;
import app.packed.component.Image;
import app.packed.component.Wirelet;
import app.packed.state.Host;
import app.packed.state.Host.StopOption;
import app.packed.state.RunState;
import app.packed.state.StateWirelets;

/**
 *
 */

// immutable or mutable???

/// If we only call it once. immutable shouldn't be a problem...

// I don't think this works with passive systems...
// IDK
// Nej vil mene det er en fejl hvis der ikke er en livs cyclus...

// Maaske er det i virkeligheden en shell driver vi laver her...
// Og bruger..Vi vil jo i virkeligheden gerne supporte at man ogsaa kan lave images

// <Integer> <--- exit code

// Har kun noget der executor.....
// Og primaert mod at bliver kaldt fra main...

// Men meget af det taenker jeg skal styres af system wirelets...
// Maaske endda MainWirelets... (The first system???
public final class Main {

    final Wirelet wirelet;

    private Main(Wirelet wirelet) {
        this.wirelet = requireNonNull(wirelet);
    }

    Main add(Wirelet wirelet) {
        // vi laver copy of.. Saa kan Main altid blive sharet.
        return new Main(Wirelet.combine(this.wirelet, wirelet));
    }

    void execute() {

    }

    public void execute(Assembly<?> bundle, String[] args, Wirelet... wirelets) {
        // eller Main.defaults().main(args).execute(bundle)

        // Syntes maaske vi skal droppe Wirelet... wirelets)??
        // Og saa koere den lidt clean

        execute(bundle, Wirelet.combine(wirelets, MainArgs.of(args)));
    }

    public void execute(Assembly<?> bundle, Wirelet... wirelets) {
        Host.execute(bundle, Wirelet.combine(wirelet, wirelets));
    }

    Image<Void> image() {
        throw new UnsupportedOperationException();
    }

    Main printBanner() {
        // Bare en masse smaa wirelets vi konkatinere
        // Saa folk ikke behoever lede over det hele efter dem...
        return this;
    }

    // maaske mere disable shutdown hook...
    Main shutdownHook() {
        return this;
    }

    // To shutdown hook or not...
    // Maybe just delay it.

    Main timeToRun(long timeout, TimeUnit unit, StopOption... options) {
        return add(StateWirelets.timeToRun(timeout, unit, options));
    }

    static Main defaults() {
        // Ideen er at denne metode
        // main() = defaaults().execute()
        throw new UnsupportedOperationException();
    }
    
    public static ArtifactDriver<Void> driver() {
        throw new UnsupportedOperationException();
    }

    public static Image<Void> imageOf(Assembly<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static void main(Assembly<?> bundle, String[] args, Wirelet... wirelets) {
        Host.execute(bundle, Wirelet.combine(wirelets, MainArgs.of(args)));
    }

    /**
     * This method will create and start an {@link App application} from the specified source. Blocking until the run state
     * of the application is {@link RunState#TERMINATED}.
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
     *             if the application did not execute properly
     * @see StateWirelets#shutdownHook(app.packed.state.Host.StopOption...)
     */
    public static void main(Assembly<?> assembly, Wirelet... wirelets) {
        Host.execute(assembly, wirelets);
    }

    static Main of() {
        throw new UnsupportedOperationException();
    }
}

class Zandbox {

    // Laver et image, en host component og virker som main()
    // Paa en eller anden maade skal vi ogsaa kunne specificere en ErrorHandling strategy
    // ErrorHandling.asWirelet...
    // Wirelets-> til bundlen og ikke hosten...

    public static void restartable(Assembly<?> bundle, Object errorHandler, String[] args, Wirelet... wirelets) {}

    public static void restartable(Assembly<?> bundle, Object errorHandler, Wirelet... wirelets) {}

    public static void restartable(Assembly<?> bundle, String[] args, Wirelet... wirelets) {}

    // Er maaske lidt mere til noget builder agtigt.
    public static void restartable(Assembly<?> bundle, Wirelet... wirelets) {}

    public static Image<Void> restartableImageOf(Assembly<?> bundle, Object errorHandler, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static Image<Void> restartableImageOf(Assembly<?> bundle, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    // sync deamon???????
    // App.main(new Goo(), args);

    // If there is an executioner. it will sync else deamon

}

// main <-- installs CTRL
// main.execute does not