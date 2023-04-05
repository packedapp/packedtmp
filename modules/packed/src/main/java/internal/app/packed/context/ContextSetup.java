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
package internal.app.packed.context;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import app.packed.context.Context;
import app.packed.context.ContextMirror;
import app.packed.context.ContextScopeMirror;
import app.packed.extension.Extension;
import app.packed.operation.OperationMirror;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerTreeSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.collect.MappedMap;
import internal.app.packed.util.collect.ValueMapper;

/** Represents a context. */
public final class ContextSetup {

    /** The template used when creating the context. */
    public final PackedContextTemplate template;

    /** The root of the context. */
    public final ContextualizedElementSetup root;

    // Maaske er den final alligevel
    @Nullable
    ContainerTreeSetup containerTree;

    public ContextSetup(PackedContextTemplate template, ContextualizedElementSetup root) {
        this.template = template;
        this.root = root;
    }

    public static <K> Map<K, ContextMirror> map(Map<K, ContextSetup> map) {
        return new MappedMap<>(map, ContextSetupToContextMirrorValueMapper.INSTANCE);
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
                return context.containerTree.mirror();
            }
        }
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
}
