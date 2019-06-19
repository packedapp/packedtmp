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
package app.packed.layer;

import app.packed.container.AnyBundle;
import app.packed.container.Wirelet;
import app.packed.contract.Contract;

/**
 *
 */

// http://www.methodsandtools.com/archive/onionsoftwarearchitecture.php

// Layer extension? WiringExtension?

// LayerWirelets.into()

// Ved ikke om vi skal have navngivne layers???
// Jeg taenker her paa f.eks at lave en host, med Hibernate+Web
// Her er Hiberate i data-layer, og Web i presentatio-layer

// Layers are always for a single container/Bundle...
// Maybe we can only link other modules in them...
// And not normal components..
class OldBundle {

    protected final OldLayer mainLayer(OldLayer... predecessors) {
        // Layers are an AnyBundle thingy...
        // Can only be called once???
        throw new UnsupportedOperationException();
    }

    protected final OldLayer newEmptyLayer(String name, OldLayer... predecessors) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param name
     *            the name of the layer, must be unique among all layers defined in the same bundle
     * @param predecessors
     *            preds
     * @return the new layer
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, "main" or if another layer with the same name has been
     *             registered
     */
    protected final OldLayer newOldLayer(String name, OldLayer... predecessors) {
        // maybe "" is just the main layer...

        // Okay we need to able to addLayers as predessestors to the main layer....
        // sucessotorToMainLayer, precessorToMainLayer() only available for empty layer...

        // check same bundle
        // Maybe skip name and have a setter... If you put every bundle into its own layer it is a bit annoying...

        // The

        throw new UnsupportedOperationException();
    }
}

public class OldLayer extends Wirelet {
    // To Interface?
    public OldLayer() {

    }

    public <T extends AnyBundle> T link(T bundle, Wirelet... wirelets) {
        return bundle;
    }

    public OldLayer(String name, OldLayer... dependencies) {

    }

    public Contract incoming() {
        throw new UnsupportedOperationException();
    }

    public Contract outgoing() {
        throw new UnsupportedOperationException();
    }

    // layer.link(Bundle... wirelets)

    // All direkte komponenter i en container.. Kan baade bruge og give dem dependencies...

    // If we allow transformations w
    static class X {
        static final OldLayer PERSISTENCE = new OldLayer("Persistence");

        static final OldLayer BUSINESS = new OldLayer("Business", PERSISTENCE);

        static final OldLayer PRESENTATION = new OldLayer("Presentation", BUSINESS);
    }

    static class MyBundle extends OldBundle {

        @SuppressWarnings("unused")
        protected void configure() {
            OldLayer persistence = newOldLayer("persistence");
            OldLayer business = newOldLayer("business", persistence);
            OldLayer presentation = newOldLayer("presentation", business);

            // link(new MyBundle(), business, presentation);

            // Tror maaske denne er fedest...
            /// Tror ikke vi vil have en bundle i flere layers....
            // business.link(new MyBundle());
        }

    }
}
