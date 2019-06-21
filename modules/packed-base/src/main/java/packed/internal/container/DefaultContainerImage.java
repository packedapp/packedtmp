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
package packed.internal.container;

import java.util.function.Supplier;

import app.packed.app.App;
import app.packed.app.MainTest;
import app.packed.container.AnyBundle;
import app.packed.container.ContainerImage;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;
import app.packed.inject.Injector;
import app.packed.inject.ServiceWirelets;

/**
 *
 */
// ContainerImage taenker maaske det er et fint Navn....

public class DefaultContainerImage implements ContainerImage {

    DefaultContainerImage.Builder builder(ContainerSource source, Wirelet... wirelets) {
        return new Builder();
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends AnyBundle> source() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DefaultContainerImage with(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DefaultContainerImage withName(String name) {
        return with(Wirelet.name(name));
    }

    /**
     * Creates a new image bundle with default settings.
     * 
     * @param source
     *            the source
     * @param wirelets
     */
    static DefaultContainerImage of(ContainerSource source, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    // Ideen er lidt at vi kan lade vaere at lave bundled foerend det skal bruges...
    //// Men ved ikke lige hvor meget vi sparer
    static DefaultContainerImage of(Supplier<ContainerSource> source, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    // Instead of builder, we could just have ImageBundleWirelets
    static class Builder {

    }

    static class ImageBundleWirelets {

        // retainStackTracesForEachInstantiation...
        /// Her ligger vi jo lige 1000 ns oveni hvis vi vil se hvor den er instantieret.

    }

    // Output Builder

    interface InjectorFactory {
        // Tager disse to objekter, laver en injector fra bundlen.
        // Og outputter String
        String spawn(long str1, int str2);

        Injector spawn(String httpRequest, String httpResponse);
    }

    static class Usage {
        private final DefaultContainerImage someBundle = DefaultContainerImage.of(new MainTest());

        public App getIt(String s, int l) {
            return App.of(someBundle, ServiceWirelets.provide(s), ServiceWirelets.provide(l));
        }
    }

    interface UserDefinedSpawner {
        // App spawn(Host h, String httpRequest, String httpResponse);
    }
}
// Skal vi have forskellige injectorBuiler og AppBuilder???
// Syntes jeg egentlig ikke. Det kunne vaere fedt hvis man kunne bruge det overalt.
// F.eks. BundleContract.of(ImageBundle)
// Saa kan vi jo evt. registrerer om man kan lave en Injector eller App ud af den...

// Questions
// ? Wirelets: Yes, No
// ? Different Environments???
// Ideen er du kan bygge et image... Og saa blive ved med at instantiere det
// Alt er ligesom paa plads... Det er kun dine input parameter der er anderledes.....
// I love it.....

// Minder ufattelig meget om et bundle. Men har ingen mutable operations....
// Bundle -> Container Image -> Container|BundleDescriptor|NativeImage