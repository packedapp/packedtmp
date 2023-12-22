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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentPath.Schema.FragmentKind;
import app.packed.extension.Extension;
import app.packed.util.Nullable;

/**
 * The path of a component.
 */
public final /* primitive */ class PackedComponentPath implements ComponentPath {
    final Object[] fragments; // We need to have a canonical representation of (of what?)
    final PackedComponentPathSchema schema;

    PackedComponentPath(PackedApplicationPathBuilder builder) {
        this.schema = builder.schema;
        this.fragments = builder.fragments;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath.Schema schema() {
        return schema;
    }

    public static void main(String[] args) {
        ComponentPath.Schema.APPLICATION.newPath("sfoo");
        ComponentKind.APPLICATION.pathOf("sfoo");
    }

    /** Component path schema builder. */
    public static final class ComponentPathSchemaBuilder implements ComponentPath.Schema.Builder {

        /** The extension responsible for the schema. */
        @Nullable
        final Class<? extends Extension<?>> extension;

        final ArrayList<SchemaFragment> fragments = new ArrayList<>();

        /** The base name of the schema. */
        final String name;

        ComponentPathSchemaBuilder(String name, Class<? extends Extension<?>> extension) {
            this.name = name;
            this.extension = extension;
        }

        /** {@inheritDoc} */
        @Override
        public Schema build() {
            return new PackedComponentPathSchema(this);
        }

        private ComponentPathSchemaBuilder require(String name, FragmentKind kind) {
            fragments.add(new SchemaFragment(name, kind));
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public ComponentPathSchemaBuilder requireClass(String name) {
            return require(name, FragmentKind.CLASS);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentPathSchemaBuilder requireKey(String name) {
            return require(name, FragmentKind.KEY);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentPathSchemaBuilder requireString(String name) {
            return require(name, FragmentKind.STRING);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentPathSchemaBuilder requirePath(String name) {
            return require(name, FragmentKind.PATH);
        }
    }

    static class PackedApplicationPathBuilder implements PackedComponentPath.Builder {
        private int cursor;
        private final Object[] fragments;
        private final PackedComponentPathSchema schema;

        PackedApplicationPathBuilder(PackedComponentPathSchema schema) {
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
    /**
     *
     */
    static class PackedComponentPathSchema implements ComponentPath.Schema {

        final int fragmentCount = 4;

        @Nullable
        final String fullExtensionName;

        final String name;

        final String prefix;

        PackedComponentPathSchema(ComponentPathSchemaBuilder builder) {
            this.name = builder.name;
            if (builder.extension == null) {
                this.fullExtensionName = null;
                this.prefix = "name";
            } else {
                this.fullExtensionName = builder.extension.getCanonicalName();
                this.prefix = builder.extension.getSimpleName() + "." + name;
            }
        }

        /** {@inheritDoc} */
        @Override
        public ComponentPath newPath(Object... fragments) {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public String prefix() {
            return prefix;
        }

        /** {@inheritDoc} */
        @Override
        public Optional<String> extension() {
            return Optional.ofNullable(fullExtensionName);
        }

        /** {@inheritDoc} */
        @Override
        public List<Entry<String, FragmentKind>> fragments() {
            return null;
        }
    }

    record SchemaFragment(String name, ComponentPath.Schema.FragmentKind fragmentKind) {}

}
