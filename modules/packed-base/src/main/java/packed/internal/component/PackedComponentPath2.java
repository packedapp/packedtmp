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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import app.packed.component.ComponentPath;
import app.packed.lang.Nullable;

/**
 *
 */
// Vi laver en ny ny, der bruger mindre hukommelse.
// Pga af descriptors som har en path... alternativ skal
// vi smide componenten med istedet... Men hmm det ved jeg ikke
// om vi har lyst til...
public final class PackedComponentPath2 implements ComponentPath {

    /** The parent of this path. */
    @Nullable
    private final PackedComponentPath2 parent;

    final String name;

    int hash;

    // encoded depth + total lenght
    final int stuff = 123123;

    /** String representation, created lazily */
    volatile String fullpath;

    private PackedComponentPath2() {
        this.name = null;
        this.parent = null;
    }

    private PackedComponentPath2(PackedComponentPath2 parent, String name) {
        this.parent = requireNonNull(parent);
        this.name = requireNonNull(name);
    }

    public PackedComponentPath2 spawn(String name) {
        return new PackedComponentPath2(this, name);
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(ComponentPath o) {
        // TODO Auto-generated method stub
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int length() {
        // TODO Auto-generated method stub
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public char charAt(int index) {
        // TODO Auto-generated method stub
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public CharSequence subSequence(int start, int end) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public int depth() {
        // TODO Auto-generated method stub
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRoot() {
        return parent == null;
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable ComponentPath parent() {
        return parent;
    }
}
