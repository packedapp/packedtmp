/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.component;

import java.util.List;

import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import internal.app.packed.ValueBased;

/**
 * The path of a component.
 */
@ValueBased
public final class PackedComponentPath implements ComponentPath {

    final Object[] fragments;

    /** The kind of component the path represents. */
    private final PackedComponentKind kind;

    PackedComponentPath(PackedApplicationPathBuilder builder) {
        this.kind = builder.schema;
        this.fragments = builder.fragments;
    }

    PackedComponentPath(PackedComponentKind kind, Object[] fragments) {
        this.kind = kind;
        this.fragments = fragments.clone();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentKind componentKind() {
        return kind;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(kind.prefix).append(":");
        for (int i = 0; i < kind.fragmentCount; i++) {
            Object o = fragments[i];
            switch (kind.fragments[i].fragmentKind) {
            case CLASS -> sb.append(o);
            case KEY -> sb.append(o);
            case PATH -> sb.append(String.join("/", (List<String>) o));
            case STRING -> sb.append(o);
            }
            sb.append(":");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        ComponentPath cp = ComponentKind.APPLICATION.pathNew("sfoo");
        IO.println(cp);
    }

    /**
     * Builds a resource path.
     * <p>
     * A builder is acquired by calling {@link Schema#builder()}.
     */
    interface Builder {

        Builder addClass(Class<?> clazz);

        Builder addName(String value);

        Builder addPath(List<String> path);

        Builder addPath(String... path);

        /**
         * {@return the new path}
         *
         * @throws IllegalStateException
         *             if the path is not constructed correctly, for example, if there are fragments that have not been filled
         *             out
         */
        ComponentPath build();
    }

    static class PackedApplicationPathBuilder implements PackedComponentPath.Builder {
        private int cursor;
        private final Object[] fragments;
        private final PackedComponentKind schema;

        PackedApplicationPathBuilder(PackedComponentKind schema) {
            this.schema = schema;
            this.fragments = new Object[schema.fragmentCount];
        }

        /** {@inheritDoc} */
        @Override
        public PackedApplicationPathBuilder addClass(Class<?> clazz) {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public PackedApplicationPathBuilder addName(String name) {
            cursor++;
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public PackedApplicationPathBuilder addPath(List<String> path) {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public PackedApplicationPathBuilder addPath(String... path) {
            cursor++;
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public ComponentPath build() {
            if (cursor != schema.fragmentCount) { // Schema.fram
                throw new IllegalStateException();
            }
            return new PackedComponentPath(this);
        }

    }

    record SchemaFragment(String name, ComponentPath.FragmentKind fragmentKind) {}

}
