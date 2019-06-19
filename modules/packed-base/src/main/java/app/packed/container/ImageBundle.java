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
package app.packed.container;

import app.packed.app.App;
import app.packed.app.MainTest;
import app.packed.inject.Injector;
import app.packed.inject.ServiceWirelets;

/**
 *
 */
class ImageBundle extends AnyBundle {

    /**
     * Creates a new image bundle with default settings.
     * 
     * @param source
     *            the source
     * @param wirelets
     */
    static ImageBundle of(AnyBundle source, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    ImageBundle.Builder builder(AnyBundle source, Wirelet... wirelets) {
        return new Builder();
    }

    // Instead of builder, we could just have ImageBundleWirelets
    static class Builder {

    }

    static class ImageBundleWirelets {

        // retainStackTracesForEachInstantiation...
        /// Her ligger vi jo lige 1000 ns oveni hvis vi vil se hvor den er instantieret.

    }

    static class Usage {
        private final ImageBundle someBundle = ImageBundle.of(new MainTest());

        public App getIt(String s, int l) {
            return App.of(someBundle, ServiceWirelets.provide(s), ServiceWirelets.provide(l));
        }
    }

    // Output Builder

    interface UserDefinedSpawner {
        App spawn(Host h, String httpRequest, String httpResponse);
    }

    interface InjectorFactory {
        Injector spawn(String httpRequest, String httpResponse);

        // Tager disse to objekter, laver en injector fra bundlen.
        // Og outputter String
        String spawn(long str1, int str2);
    }
}
// Skal vi have forskellige injectorBuiler og AppBuilder???
// Syntes jeg egentlig ikke. Det kunne vaere fedt hvis man kunne bruge det overalt.
// F.eks. BundleContract.of(ImageBundle)
// Saa kan vi jo evt. registrerer om man kan lave en Injector eller App ud af den...

// Questions
// ? Wirelets: Yes, No
// ? Different Environments???