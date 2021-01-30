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

import app.packed.component.Assembly;
import app.packed.component.Image;
import app.packed.component.Wirelet;
import app.packed.state.Host;
import app.packed.state.Host.StopOption;
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

// Tror ikke jeg gider lave den her...
// Folk maa sgu selv styre deres wirelets...
final class OldMain {

    final Wirelet wirelet;

    private OldMain(Wirelet wirelet) {
        this.wirelet = requireNonNull(wirelet);
    }

    OldMain add(Wirelet wirelet) {
        // vi laver copy of.. Saa kan Main altid blive sharet.
        return new OldMain(Wirelet.combine(this.wirelet, wirelet));
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

    OldMain printBanner() {
        // Bare en masse smaa wirelets vi konkatinere
        // Saa folk ikke behoever lede over det hele efter dem...
        return this;
    }

    // maaske mere disable shutdown hook...
    OldMain shutdownHook() {
        return this;
    }

    // To shutdown hook or not...
    // Maybe just delay it.

    OldMain timeToRun(long timeout, TimeUnit unit, StopOption... options) {
        return add(StateWirelets.timeToRun(timeout, unit, options));
    }

    static OldMain defaults() {
        // Ideen er at denne metode
        // main() = defaaults().execute()
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