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
import app.packed.component.ComponentPath.FragmentKind;
import app.packed.extension.Extension;
import app.packed.util.Nullable;
import internal.app.packed.component.PackedComponentPath.SchemaFragment;

/**
 *
 */
public class PackedComponentKind implements ComponentKind {

    final int fragmentCount = 4;

    final SchemaFragment[] fragments;
    @Nullable
    final String fullExtensionName;

    final String name;

    final String prefix;

    PackedComponentKind(PackedComponentKindBuilder builder) {
        this.name = builder.name;
        this.fragments = builder.fragments.toArray(new SchemaFragment[builder.fragments.size()]);
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
    public Optional<String> extension() {
        return Optional.ofNullable(fullExtensionName);
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public List<Entry<String, FragmentKind>> pathFragments() {
       throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath pathNew(ComponentPath parent, Object... fragments) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath pathNew(Object... fragments) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String pathPrefix() {
        return prefix;
    }

    /** Component path schema builder. */
    public static final class PackedComponentKindBuilder implements ComponentKind.Builder {

        /** The extension responsible for the schema. */
        @Nullable
        final Class<? extends Extension<?>> extension;

        final ArrayList<SchemaFragment> fragments = new ArrayList<>();

        /** The base name of the schema. */
        final String name;

        public PackedComponentKindBuilder(String name, Class<? extends Extension<?>> extension) {
            this.name = name;
            this.extension = extension;
        }

        /** {@inheritDoc} */
        @Override
        public ComponentKind build() {
            return new PackedComponentKind(this);
        }

        private PackedComponentKindBuilder require(String name, FragmentKind kind) {
            fragments.add(new SchemaFragment(name, kind));
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PackedComponentKindBuilder requireFragmentClass(String name) {
            return require(name, FragmentKind.CLASS);
        }

        /** {@inheritDoc} */
        @Override
        public PackedComponentKindBuilder requireFragmentKey(String name) {
            return require(name, FragmentKind.KEY);
        }

        /** {@inheritDoc} */
        @Override
        public PackedComponentKindBuilder requireFragmentPath(String name) {
            return require(name, FragmentKind.PATH);
        }

        /** {@inheritDoc} */
        @Override
        public PackedComponentKindBuilder requireFragmentString(String name) {
            return require(name, FragmentKind.STRING);
        }
    }
}