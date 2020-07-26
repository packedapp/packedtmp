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
package sandbox.artifact.hosttest;

import app.packed.artifact.App;
import app.packed.artifact.ArtifactImage;
import app.packed.base.Key;

/**
 *
 */
//optional??? Nej det taenker jeg ikke

//ImageSet, MutableImageSet

// Det der er med image... er jo at vi gerne vil supportere ikke-containere....
// F.eks. En Actor der kan have 3 mulige guests....

// Fungere ikke rigtigt med aktive 

public interface ImageSet {

    default ArtifactImage get() {
        return get(Key.of(ArtifactImage.class));
    }

    default ArtifactImage get(String name) {
        return get(Key.of(ArtifactImage.class).withName(name));
    }

    ArtifactImage get(Key<ArtifactImage> key);

    default boolean isImmutable() {
        return true;
    }
}

class Doo {

    void foo(ImageSet is) {
        App.start(is.get());
    }
}
