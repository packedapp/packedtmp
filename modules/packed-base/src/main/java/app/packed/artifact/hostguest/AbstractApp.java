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
package app.packed.artifact.hostguest;

import app.packed.artifact.App;
import app.packed.artifact.ArtifactContext;

/**
 * An abstract implementation of App, that can be easy overridden.
 */
// Ideen er at folk let kan implementere app..
// Og smide nogle annoteringer paa..
// PackedApp behoever ikke extende den...

// Droppede vi lidt det med annoteringerne???

// Altsaa ArtifactDriver bliver noedt til at kende til den...
// Og analysere klassen...

// ArtifactDriver.create(MethodHandles.Lookup lookup, Class<?> impl);

// Det er sgu v2... Kan ikke se nogen grund til vi provider det i foerste omgang...
abstract class AbstractApp implements App {

//    protected final ArtifactContext context;
//
//    protected AbstractApp(ArtifactContext context) {
//        this.context = requireNonNull(context);
//    }

    // Tror vi har den her istedet for, fordi saa kan man f.eks.
    // Lave en inline klasse...
    protected abstract ArtifactContext context();

    /** {@inheritDoc} */
    @Override
    public String name() {
        return context().name();
    }
}
