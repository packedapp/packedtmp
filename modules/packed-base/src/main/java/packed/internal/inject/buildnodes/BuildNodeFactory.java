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
package packed.internal.inject.buildnodes;

import static java.util.Objects.requireNonNull;

import app.packed.inject.Key;
import packed.internal.inject.InternalInjectorConfiguration;
import packed.internal.inject.factory.InternalFactory;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/** A abstract node that builds thing from a factory. */
public abstract class BuildNodeFactory<T> extends BuildNode<T> {

    /** An empty object array. */
    private final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /** The factory. */
    final InternalFactory<T> factory;

    /**
     * @param injectorConfiguration
     * @param dependencies
     * @param stackframe
     */
    BuildNodeFactory(InternalInjectorConfiguration injectorConfiguration, InternalConfigurationSite configurationSite, InternalFactory<T> factory) {
        super(injectorConfiguration, configurationSite, factory.getDependencies());
        this.factory = requireNonNull(factory, "factory is null");
    }

    /** {@inheritDoc} */
    @Override
    public final boolean needsResolving() {
        return !dependencies.isEmpty();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    T newInstance() {
        Object[] params = EMPTY_OBJECT_ARRAY;
        int size = dependencies.size();
        if (size > 0) {
            params = new Object[size];
            for (int i = 0; i < resolvedDependencies.length; i++) {
                requireNonNull(resolvedDependencies[i]);
                params[i] = resolvedDependencies[i].getInstance(injectorConfiguration.builder.getInjector(), null, (Key) dependencies.get(i).getKey());
            }
        }
        return factory.instantiate(params);
    }

    @Override
    public String toString() {
        return factory.toString();
    }
}
