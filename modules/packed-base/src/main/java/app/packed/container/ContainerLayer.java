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

import java.util.Set;

/**
 *
 */

// Layers in layers??????? Why not...

// layer.newPrivateLayer("foo") -> final name = presentation.foo

// Naa vi skal visuelle de 4 modes.
/// Saa laver vi fire kasser paa rad og raekke. Og 1 pil fra top og en pil ud fra bund for alm link, 1 up for incoming,
// 1 ned for outgoing, og ingen fra private...

// or have mainLayer() and then
// newEmptyLayer(String name, includeMainAsSuccessor, BundleLayer... )
// newEmptyLayer("Infrastructure", false);

// Basically there are 4 types of new layer
// empty
// import from main layer
// export to main layer
// share with main layer

// Problemet med den her model, uden wirelets. Er at den er super svaer at styre hostet...
// F.eks. Hvis vi faar Database fra en Host. Hvordan sikre vi os, at den kun
// er til raadighed for persistence laget???
// Layer l = newLayer("sdsd", wirelets...).dependOn(persistance"); <-dependOn must be performed before linking...
// Maaske skal vi loese Host problemerne foerst
// renamed to ContainerLayer... to emphaize it is containers we put in different layers...

// Alternativt er at implementere layers via en enum...
//// Fordele: Genbrug paa tvaers af bundles
//// Kan du langt sigt bruge med annoteringer
// link(AcmeLayers.PRESENTATION, new XBundle());

// Vi kunne ogsaa lave det som en konfigurations klasse.... Hvor vi laver det hele
// Idielt set har vi en klasse, med public static ContainerLayer DATA = ddddd, SERVICE = dddd
public interface ContainerLayer {

    /**
     * Returns all layers that this layer is directly dependent on.
     * 
     * @return all layers that this layer is directly dependent on
     */
    Set<ContainerLayer> dependencies();

    default Set<ContainerLayer> dependents() {
        return Set.of();
    }

    <T extends Bundle> T link(T child, Wirelet... wirelets);

    // Everything is private to the layer... What about dependencies???

    // Den her tager in, men ikke ud.
    // Burde man lave en der hverken tog ind eller ud?? As in truely private?
    // Man kan stadig smide ting ind via wirelets...
    /// f.kes. linkPrivate(new HibernateBundle().loadConfFrom("/asdsad"));
    <T extends Bundle> T linkPrivate(T child, Wirelet... wirelets);

    default <T extends Bundle> T linkIncoming(T child, Wirelet... wirelets) {
        // Will link from any incoming layers....
        // But dependencies will never see it...
        return child;
    }

    default <T extends Bundle> T linkOutgoing(T child, Wirelet... wirelets) {
        return child;
    }

    /**
     * Returns the name of the layer.
     * 
     * @return the name of the layer
     */
    String name();
}

// Grunden til at vi taenker enum er godt. Er at hvis vi har 7 domain modules.
// Saa gider vi ikke definere lag resolutions 7 gange...

// Evt en abstract klasse, hvor vi kan kalde en constructor med alle parametrene
// Alle lag skal have samme declaring klasse???

enum XXX implements ContainerLayer {
    Ddd {

        /** {@inheritDoc} */
        @Override
        public Set<ContainerLayer> dependencies() {
            return Set.of(Ddd, Pppp);
        }
    },
    Pppp {

        /** {@inheritDoc} */
        @Override
        public Set<ContainerLayer> dependencies() {
            return Set.of(Ddd, Pppp);
        }
    },
    HHH {

        /** {@inheritDoc} */
        @Override
        public Set<ContainerLayer> dependencies() {
            return Set.of(Ddd, Pppp);
        }
    };

    /** {@inheritDoc} */
    @Override
    public <T extends Bundle> T link(T child, Wirelet... wirelets) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Bundle> T linkPrivate(T child, Wirelet... wirelets) {
        // TODO Auto-generated method stub
        return null;
    }
}