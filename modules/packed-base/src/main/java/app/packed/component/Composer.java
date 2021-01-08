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

import app.packed.container.Extension;

/**
 *
 */
// Can take a CCC context. And cast it and provide lookup??
// Maaske er det altid en container????
// This class should be inlign with Assembly so Either ComponentComposer or just Composer
public abstract class Composer extends Realm {

    // Must take something special...
    protected Composer() {}

    // Vi vil have den her final...
    public void lookup(MethodHandles.Lookup lookup) {}

    // De her bliver kaldt fra en statisks initializer
    @SafeVarargs
    protected static void bootstrapRejectExtensions(Class<? extends Extension>... extensions) {
        throw new UnsupportedOperationException();
    }

    @SafeVarargs
    protected static void bootstrapAllowExtensions(Class<? extends Extension>... extensions) {
        throw new UnsupportedOperationException();
    }
}
