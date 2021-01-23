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
package app.packed.component;

import java.lang.invoke.MethodHandles;
import java.util.function.Function;

import app.packed.container.Extension;
import packed.internal.component.PackedArtifactDriver;

/**
 *
 */
// Can take a CCC context. And cast it and provide lookup??
// Maaske er det altid en container????
// This class should be inlign with Assembly so Either ComponentComposer or just Composer

//Composer<T>?
public abstract class Composer extends Realm {

    // Must take something special...
    protected Composer() {}

    // Vi vil have den her final...
    public void lookup(MethodHandles.Lookup lookup) {}

    // De her bliver kaldt fra en statisks initializer
    // Ikke hvis man skal bruge en ArtifactDriver...
    @SafeVarargs
    protected static void $RejectExtensions(Class<? extends Extension>... extensions) {
        throw new UnsupportedOperationException();
    }

    @SafeVarargs
    protected static void $AllowExtensions(Class<? extends Extension>... extensions) {
        throw new UnsupportedOperationException();
    }

    // Eller ogsaa har vi en anden driver??? ComposerDriver...

    // Hmm. vi kan jo godt have flere forskellige configurationer...
    // Altsaa fx tillader vi ikke andre extensions hvis vi laver en ServiceLocator i nogen tilfaelde
    // Mens vi nok goere det i andre...
    // Men

    public <D extends Composer, A> A configure(ArtifactDriver<A> adriver, ComponentDriver<D> driver, Composable<D> consumer, Wirelet... wirelets) {
        return compose(adriver, driver, e -> e, consumer, wirelets);
    }

    // Det er vel fint at lave den her metode public????
    protected static <C extends Composer, D, A> A compose(ArtifactDriver<A> adriver, ComponentDriver<D> driver, Function<D, C> factory, Composable<C> consumer,
            Wirelet... wirelets) {
        PackedArtifactDriver<A> ad = (PackedArtifactDriver<A>) adriver;
        return ad.configure(driver, factory, consumer, wirelets);
    }
}
