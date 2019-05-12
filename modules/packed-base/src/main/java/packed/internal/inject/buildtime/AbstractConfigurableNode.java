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
package packed.internal.inject.buildtime;

import static java.util.Objects.requireNonNull;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigurationSite;

/**
 * An abstract configuration object. That provides basic support for strings tags, setting a description, and freezing a
 * configuration.
 */
public abstract class AbstractConfigurableNode {

    /** The configuration site of this object. */
    protected final InternalConfigurationSite configurationSite;

    /** The description. */
    @Nullable
    private String description;

    /** Whether or not the configuration has been frozen. */
    private boolean isFrozen;

    /** A tag set (lazy initialized) */
    @Nullable
    private AbstractConfigurableTagSet<String> tags;

    protected AbstractConfigurableNode() {
        this.configurationSite = null;
    }

    /**
     * Creates a new abstract configuration
     * 
     * @param configurationSite
     *            the configuration site of the configuration
     */
    protected AbstractConfigurableNode(InternalConfigurationSite configurationSite) {
        this.configurationSite = requireNonNull(configurationSite);
    }

    protected final void checkConfigurable() {
        if (isFrozen) {
            throw new IllegalStateException("This configuration has been frozen and can no longer be modified");
        }
    }

    /**
     * Returns the configuration site of this configuration.
     * 
     * @return the configuration site of this configuration
     */
    public final InternalConfigurationSite configurationSite() {
        return configurationSite;
    }

    public void freeze() {
        isFrozen = true;
    }

    /**
     * Returns the description.
     *
     * @return the configuration
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Returns an immutable copy of all tags that have been registered.
     *
     * @return an immutable copy of all tags that have been registered
     */
    public final Set<String> immutableCopyOfTags() {
        AbstractConfigurableTagSet<String> tags = this.tags;
        return tags == null ? Set.of() : tags.toImmutableSet();
    }

    protected void onFreeze() {

    }

    /**
     * Sets the description of this configuration.
     *
     * @param description
     *            the description to set
     * @return this configuration
     * @throws IllegalStateException
     *             if this configuration can no longer be configured
     */
    public AbstractConfigurableNode setDescription(String description) {
        checkConfigurable();
        this.description = description;
        return this;
    }

    /**
     * Returns the mutable tag set of this builder.
     *
     * @return the mutable tag set of this builder
     */
    public final Set<String> tags() {
        AbstractConfigurableTagSet<String> tags = this.tags;
        return tags == null ? this.tags = new AbstractConfigurableTagSet<>() {
            @Override
            protected void checkMutable() {
                AbstractConfigurableNode.this.checkConfigurable();
            }
        } : tags;
    }

    /** An abstract implementation of a mutable set which can be made immutable at a later point. */
    // TODO maaske lave den om til ikke statisk, hvis vi ikke skal bruge den andre steder

    // Er det et problem at det her set har en reference til configurations objektet???
    // Vil bruge holde fast paa en reference til tagsettet???
    // Vi kan evt erstattet referencen af feltet med et der bliver nullet ud naar man koerer immutableCoptOfTags
    static abstract class AbstractConfigurableTagSet<T> extends AbstractSet<T> {

        /** The set to delegate all operations to. */
        private final HashSet<T> set = new HashSet<>();

        /** {@inheritDoc} */
        @Override
        public final boolean add(T tag) {
            requireNonNull(tag, "cannot add null");
            checkMutable();
            return set.add(tag);
        }

        /** Before any mutable operations, this method is called to make sure the set can still be mutated. */
        protected abstract void checkMutable();

        @Override
        public final void clear() {
            checkMutable();
            set.clear();
        }

        /** {@inheritDoc} */
        @Override
        public final boolean contains(Object o) {
            return set.contains(o);
        }

        /** {@inheritDoc} */
        @Override
        public final Iterator<T> iterator() {
            Iterator<T> iterator = set.iterator();
            return new Iterator<>() {

                /** {@inheritDoc} */
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                /** {@inheritDoc} */
                @Override
                public T next() {
                    return iterator.next();
                }

                /** {@inheritDoc} */
                @Override
                public void remove() {
                    checkMutable();
                    iterator.remove();
                }
            };
        }

        /** {@inheritDoc} */
        @Override
        public final boolean remove(Object o) {
            checkMutable();
            return set.remove(o);
        }

        /** {@inheritDoc} */
        @Override
        public final int size() {
            return set.size();
        }

        /**
         * Returns an immutable copy of this set.
         *
         * @return an immutable copy of this set
         */
        public final Set<T> toImmutableSet() {
            return set.isEmpty() ? Set.of() : Set.copyOf(set);
        }
    }
}

/** Support for prepopulating the components attributes. */
// private Context attributes;
// public final Context context() {
// Context attributes = this.attributes;
// return attributes == null ? this.attributes = new DefaultContext() : attributes;
// }
