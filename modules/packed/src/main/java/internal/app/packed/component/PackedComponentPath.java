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
package internal.app.packed.component;

import java.util.List;

import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;

/**
 * The path of a component.
 */
public final /* primitive */ class PackedComponentPath implements ComponentPath {
    final Object[] fragments; // We need to have a canonical representation of (of what?)
    final PackedComponentKind schema;

    PackedComponentPath(PackedApplicationPathBuilder builder) {
        this.schema = builder.schema;
        this.fragments = builder.fragments;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentKind componentKind() {
        return schema;
    }

    public static void main(String[] args) {
        ComponentKind.APPLICATION.pathNew("sfoo");
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
    record SchemaFragment(String name, ComponentPath.FragmentKind fragmentKind) {}

}
