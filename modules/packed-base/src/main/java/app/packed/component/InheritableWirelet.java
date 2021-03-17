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

import app.packed.container.Extension;


// Hmm tror maaske ikke den skal bruges alligevel...
// Fx time extension. Vi kunne lave den inheritable...
// Saaledes at en rod assembly ikke behoevede at have
// TimeExtensionen installeret...
// Men nej.. Syntes det andet er klarere...
// Problemet med inheritable er ogsaa at det ikke umiddelbart er klart
// hvordan vi haandtere consumation... De skal ikke consumes jo
// Men saa alligevel...
// Hvis vi faar en 
// IDK de er ret brugbare med

/**
 * By default a wirelet is only processed by the single component it is applied to. However,
 */
// Ideen er at man som default ikke bliver inherited...
// Men at man kan
// Jaaa eller omvendt... F.eks. debug... vil du vel som regel gerne alle sammen

// I selektionen minder den meget om component stream...

// Folk faar den oprindelige wirelet...
// Saa inheritable maa 100% styres af Packed

// Skal lige finde ud af omkring build-time/run-time
abstract class InheritableWirelet extends Wirelet {

    public final InheritableWirelet assembledBy(Class<? extends Assembly<?>> assemblyType) {
        throw new UnsupportedOperationException();
    }

    public final InheritableWirelet forExtension(Class<? extends Extension> extensionClass) {
        throw new UnsupportedOperationException();
    }

    // Maybe this is the last so return Wirelet
    public final InheritableWirelet ignoreThis() {
        throw new UnsupportedOperationException();
    }

    //relativ til indsaetnings stedet...
    public final InheritableWirelet matchPath(String regexp) {
        throw new UnsupportedOperationException();
    }

    public final InheritableWirelet maxDepth(int maxDeptch) {
        return this;
    }

    public final InheritableWirelet sameContainer() {
        throw new UnsupportedOperationException();
    }

    // disabled()
    public final Wirelet noInheritance() {
        return Wirelet.extractable(this);
    }

    /**
     * Returns a wirelet whose inheritance strategy can no longer be changed. This method is mainly useful if you want to
     * expose an inheritable wirelet to end-users. But you do not want the end-users be able to change the inheritance
     * strategy
     * 
     * @return this wirelet as an immutable wirelet
     */
    // fixed()? immutable()?
    public final Wirelet wirelet() {
        return Wirelet.extractable(this);
    }
}
