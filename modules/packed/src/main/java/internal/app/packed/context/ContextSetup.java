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
package internal.app.packed.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import app.packed.context.Context;
import app.packed.context.ContextMirror;
import app.packed.context.ContextScopeMirror;
import app.packed.extension.Extension;
import app.packed.operation.OperationMirror;
import org.jspecify.annotations.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerTreeSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.service.ServiceProviderSetup.ContextServiceProviderSetup;
import internal.app.packed.service.util.ServiceMap;
import internal.app.packed.util.collect.MappedMap;
import internal.app.packed.util.collect.ValueMapper;

/** Represents a context. */
public final class ContextSetup {

    @Nullable
    ContainerTreeSetup containerTree;

    // Either Argument or @ContainerContextProvide
    Object howToAccessToTheContext;

    /** The root of the context. */
    public final ContextualizedComponentSetup root;

    /** The template used when creating the context. */
    public final ContextModel template;

    public final ServiceMap<ContextServiceProviderSetup> serviceProvides = new ServiceMap<>();

    public ContextSetup(ContextModel template, ContextualizedComponentSetup root) {
        this.template = template;
        this.root = root;
    }

    public Class<? extends Context<?>> contextClass() {
        return template.contextClass();
    }

    // So copy of?
    public static Map<Class<? extends Context<?>>, ContextSetup> allContextsFor(ContextualizedComponentSetup element) {
        HashMap<Class<? extends Context<?>>, ContextSetup> map = new HashMap<>();
        element.forEachContext(c -> map.putIfAbsent(c.contextClass(), c));
        return map;
    }

    public static Map<Class<? extends Context<?>>, ContextMirror> allMirrorsFor(ContextualizedComponentSetup element) {
        return new MappedMap<>(allContextsFor(element), ContextSetupToContextMirrorValueMapper.INSTANCE);
    }

    private static final class ContextSetupToContextMirrorValueMapper implements ValueMapper<ContextSetup, ContextMirror> {

        private static final ContextSetupToContextMirrorValueMapper INSTANCE = new ContextSetupToContextMirrorValueMapper();

        /** {@inheritDoc} */
        @Override
        public Optional<Object> forValueSearch(Object object) {
            if (object instanceof PackedContextMirror m) {
                return Optional.of(m.context());
            }
            return Optional.empty();
        }

        /** {@inheritDoc} */
        @Override
        public ContextMirror mapValue(ContextSetup context) {
            return new PackedContextMirror(context);
        }
    }

    /** Implementation of {@link ContextMirror}. */
    private record PackedContextMirror(ContextSetup context) implements ContextMirror {

        /** {@inheritDoc} */
        @Override
        public Class<? extends Context<?>> contextClass() {
            return context.template.contextClass();
        }

        /** {@inheritDoc} */
        @Override
        public Class<? extends Extension<?>> extensionClass() {
            return context.template.extensionClass();
        }

        /** {@inheritDoc} */
        @Override
        public Collection<OperationMirror> initiatingOperations() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public ContextScopeMirror scope() {
            if (context.root instanceof OperationSetup os) {
                return os.mirror();
            } else if (context.root instanceof BeanSetup bs) {
                return bs.mirror();
            } else {
                throw new UnsupportedOperationException();
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "ContextMirror: " + contextClass().getSimpleName();
        }
    }
}
