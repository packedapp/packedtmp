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
package packed.internal.container.packlet;

import java.lang.invoke.MethodHandles;
import java.util.function.Function;

import app.packed.container.SpecializeContainer;

/**
 *
 */
//Maybe its not a model... Default cannot contain a class...
// And maybe we want to programmaticaly change this in the future
// We do not support caching... Models must do it...

// Okay det fede er jo at vi faktisk kan bygge og teste den uafhaendig af alt andet...

//Doesn't know anything about Lookup objects...

// @AddOn <- but we can 

// Two modes... either default or specialized
public class PackletMotherShip {

    /** The default non-customized instance. */
    public static final PackletMotherShip DEFAULT = new PackletMotherShip();

    PackletSupportModel psm;

    // clazz itself can contain annotations...
    public PackletMethod scanFunction(Function<?, ?> f) {
        // Hvis vi registrere en function i en container...
        // Skal vi ogsaa understoette f.eks. Logger injection
        // Hvis containeren har specializeret sig i det...
        // Ja...
        throw new UnsupportedOperationException();
    }

    public PackletClass scan(Class<?> clazz) {
        return PackletClass.of(psm, clazz, null);
    }

    // Bliver kun kaldt en gang per modelXclass. Og saa cached andet steds
    // We could allow null, or test it against PSM.class.getModule() to see if user
    public PackletClass scan(Class<?> clazz, MethodHandles.Lookup lookup) {
        return PackletClass.of(psm, clazz, lookup);
    }

    public static PackletMotherShip of(Class<?> source) {
        SpecializeContainer sc = source.getAnnotation(SpecializeContainer.class);
        if (sc == null) {
            return DEFAULT;
        }
        throw new UnsupportedOperationException("Specialized containers not supported yet");
    }

}

// Denne kunne vaere interesant hvis vi supportere arbitreaere niveaur...
// Men det goer vi bare ikke. Vi har container og vi har component.

//public PackletMotherShip customize(Class<?> source) {
//    // Altsaa vi supportere ikke mere end 2 niveau'er goer vi????
//
//    // Maybe we want it static... so people don't go customize(containerClass).customize(componentClass).scan()
//
//    // In
//    SpecializeContainer sc = source.getAnnotation(SpecializeContainer.class);
//    if (sc == null) {
//        return this;
//    }
//    throw new UnsupportedOperationException("Specialized containers not supported");
//}

// Tager en klasse. og kommer ud med et eller andet???

//Specialization can be applied to both containers and components...
//Customization /Modify  (https://www.thesaurus.com/browse/customize)

//Custom

// A system that uses specialization on every bundle, class and whatever is not something you should strive for. And may be imply underlying problems....