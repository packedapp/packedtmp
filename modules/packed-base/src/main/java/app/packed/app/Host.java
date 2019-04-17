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
package app.packed.app;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;

import app.packed.bundle.Bundle;
import app.packed.container.ComponentInstaller;
import app.packed.contract.Contract;

// En Session App, kan f.eks. have en request Component... Som loebende har reqeuests som boern....
// Men som ikke injecter nye ting som ikke er i sessionen...

/**
 *
 */
// Must be able to close/shutdown a host...
///// Rejects new apps, but do we shutdown existing?
///// And do we remove
// A host does not have a lifecycle

// All exported components??? Nah... do not think you necessarily want to export these components
// Maybe, when you embed it, you must provide a contract

// AbstractHost <- Must be registered as a component... Technically you could use a runtime component as a host....

// Host er bygget ovenpå en prototype component... Som viser flexibiliten....
// Maaske er hosten en selvstaendig component, og har et enkelt prototype barn kaldet apps...
// Ja det lyder som en god ide....
// Skal man kunne fjerne dem??? Ja det taenker jeg...

// ComponentProfile -> Kan have en listener... Der er lokal paa componenten???
// Ja hvis vi vil have flere hosts... Saa bliver den noedt til at vaere lokal...

///// Hvorfor kan du kun deploye apps der????
/// Hvad med et actor system???:

// AppHost extends AnyHost<App>
// SessionHost!?!?@?!

// Kan en host redifinere et environment...????
//// Man burde kunne styre nogle ting ihvertfald
// Altsaa det ville jo vaere super fedt at kunne teste ting...
/// Ved at starte forskellige hosts op...
public interface Host {

    /**
     * Returns a stream of all applications currently registered with this host.
     * 
     * @return a stream of all applications currently registered with this host
     */
    Stream<App> apps();

    /**
     * Closes the host for further deployments. Attempting to deploy applications after this method has been invoked will
     * result in {@link IllegalStateException} being thrown.
     * <p>
     * This method can be used together with {@link #undeployAll()}
     * <p>
     * Ongoing deployments will not be effected.
     * 
     * @return this host
     */
    Host close();

    /**
     * Returns the contract that the host commits to.
     * 
     * @return the contract that the host commits to
     */
    Contract contract();

    // deploy and do not start
    // deploy start asynchronously (when do you not want to start, I think this is default
    // deploy start synchronously
    /**
     * Deploys the specified bundle as a application and starts it asynchronously.
     * 
     * @param bundle
     *            the bundle to deploy
     * @return the application
     */

    // Den skal hedde noejagtig det samme som de statiske metoder paa App.
    AppRunner with(Bundle bundle);

    /**
     * Deploys the specified bundle as a application using the specified options.
     * 
     * @param bundle
     *            the bundle to deploy
     * @param args
     *            the deployment options
     * @throws IllegalStateException
     *             if this host has been closed, for example, if {@link #close()} has been invoked
     */
    void run(Bundle bundle, String... args);

    /**
     * Undeploys the specified application. If the application has not already been shutdown. Invoking this method will
     * shutdown the application. Once the application has been fully terminated it will removed from this host.
     * 
     * @param application
     *            the application to undeploy
     * @return the application
     * @throws IllegalArgumentException
     *             if the specified application has never been deployed in this host
     */
    App undeploy(App application);

    /**
     * Undeploys all applications that are registered with this host.
     * 
     * @return this host
     */
    Host undeployAll();

    // More a configuration then a builder...
    // Configurator -> via consumer -> When you have many lines, and is not standalone
    // Configuration -> When you have more than 1 expected line
    // Builder -> Standalone, no lifecycle,

    static HostBuilder install(ComponentInstaller installer) {
        // We don't know exactly what service we can use yet...
        // Her kommer
        throw new UnsupportedOperationException();
    }
}

//// Okay saa
// Hosts skal explicit registeres naar man configure en component.
// ---- Det gav heller ingen mening at man bare kan hooke paa hvor man vil...
// I foerste omgang supportere vi kun hosts....
// Hvis vi skal supportere enkeltstaaende komponenter... Maa vi tage det en anden gang.
// Jeg kan ikke lige komme i tanke om en use case...

// A component can only be host one time
// component.newHost(HostBuilder...)
// I think we want to explicitly
// I think it is hidden....

//// Woooow, specificer at man vil være host, eller component holder....
// 95 af tiden vil du det bare ikke.....
// Leaf-static...

// Man kan ikke baade være host...
// Og saa have statiske komponenter under sig...

// DET ER i hvert fald noget der bliver konfigureret paa ContainerConfiguration
// configuration.asHost()...

// InternalHost can injectes i Componenten....... Og saa kan man udstille de ting man vil...

class MyComponent {
    final Host host;

    MyComponent(Host host) {
        this.host = requireNonNull(host);
    }

}