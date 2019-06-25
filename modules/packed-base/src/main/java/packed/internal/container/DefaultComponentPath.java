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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.StringJoiner;

import app.packed.component.ComponentPath;
import app.packed.util.Nullable;

/**
 *
 */
public class DefaultComponentPath implements ComponentPath {

    private final String[] elements;

    DefaultComponentPath(String... elements) {
        this.elements = requireNonNull(elements);
    }

    /** {@inheritDoc} */
    @Override
    public int depth() {
        return elements.length;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRoot() {
        return elements.length == 0;
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable ComponentPath parent() {
        if (isRoot()) {
            return null;
        }
        return new DefaultComponentPath(Arrays.copyOf(elements, elements.length - 1));
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ComponentPath && toString().equals(obj.toString());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("/", "/", "");
        for (String s : elements) {
            sj.add(s);
        }
        return sj.toString();
    }

}
