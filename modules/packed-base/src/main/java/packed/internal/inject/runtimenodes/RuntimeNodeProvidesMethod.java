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
package packed.internal.inject.runtimenodes;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Function;

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionSite;
import app.packed.inject.Key;
import app.packed.inject.Provides;
import packed.internal.inject.CommonKeys;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.buildnodes.BuildNodeProvidesMethod;

/**
 * A runtime node for a method annotated with {@link Provides}.
 */
// TODO: we could optimize it, so we only
// introduce a boolean BuildNode.needsServiceProvider()
public final class RuntimeNodeProvidesMethod<T> extends RuntimeNode<T> {

    /** The object instance that contains the providing method. */
    private final Object instance;

    /** The method annotated with {@link Provides}. */
    // private final AnnotationProvidesReflectionData methodProvides;

    /** */
    private final Function<InjectionSite, ?>[] providers;

    /**
     * Creates a new RuntimeNodeProvidesMethod node from a build node
     *
     * @param node
     *            the build node to create this node from
     */
    @SuppressWarnings("unchecked")
    public RuntimeNodeProvidesMethod(BuildNodeProvidesMethod<T> node) {
        super(node);
        List<InternalDependency> dependencies = node.getDependencies();
        // this.methodProvides = requireNonNull(node.providesMethod);

        this.providers = new Function[dependencies.size()];
        for (int i = 0; i < dependencies.size(); i++) {
            Key<?> key = dependencies.get(i).getKey();
            if (key.equals(CommonKeys.INJECTION_SITE_KEY)) {
                providers[i] = site -> site;
            } else {
                RuntimeNode<?> runtimeNode = node.resolvedDependencies[i].toRuntimeNode();
                InjectionSite is = null;// InternalInjectionSite.of(node.getInjector(), key, node.getContainer(), node.getDeclaringComponent());
                providers[i] = site -> runtimeNode.getInstance(is);
            }
        }

        // The owner is either a BuildNodeFactory or BuildNodeInstance, in neither case
        // we need a valid Injection site, so we just pass along a null.
        this.instance = requireNonNull(node.declaringNode().getInstance(null));
    }

    /** {@inheritDoc} */
    @Override
    public BindingMode getBindingMode() {
        return BindingMode.PROTOTYPE;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public T getInstance(InjectionSite site) {
        Object[] params = new Object[providers.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = providers[i].apply(site);
        }

        return (T) instance;
        // return (T) methodProvides.getMethod().invoke(instance, params);
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return false;
    }
}
