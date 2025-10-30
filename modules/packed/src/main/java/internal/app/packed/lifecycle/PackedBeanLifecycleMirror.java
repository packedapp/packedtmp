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
package internal.app.packed.lifecycle;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.bean.BeanMirror.Lifecycle;
import app.packed.bean.lifecycle.FactoryOperationMirror;
import app.packed.bean.lifecycle.InitializeOperationMirror;
import app.packed.bean.lifecycle.InjectOperationMirror;
import app.packed.bean.lifecycle.LifecycleModel;
import app.packed.bean.lifecycle.StartOperationMirror;
import app.packed.bean.lifecycle.StopOperationMirror;
import internal.app.packed.ValueBased;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.lifecycle.LifecycleOperationHandle.FactoryOperationHandle;
import internal.app.packed.lifecycle.LifecycleOperationHandle.InitializeOperationHandle;
import internal.app.packed.lifecycle.LifecycleOperationHandle.InjectOperationHandle;
import internal.app.packed.lifecycle.LifecycleOperationHandle.StartOperationHandle;
import internal.app.packed.lifecycle.LifecycleOperationHandle.StopOperationHandle;

/** Implementation of {@link BeanLifecycleMirror}. */
@ValueBased
public record PackedBeanLifecycleMirror(BeanSetup bean) implements Lifecycle {

    /** {@inheritDoc} */
    @Override
    public Optional<FactoryOperationMirror> factory() {
        Stream<FactoryOperationMirror> stream = stream(FactoryOperationHandle.class);
        return stream.findAny();
    }

    /** {@inheritDoc} */
    @Override
    public Stream<InitializeOperationMirror> initializers() {
        return stream(InitializeOperationHandle.class);
    }

    /** {@inheritDoc} */
    @Override
    public LifecycleModel kind() {
        return bean.beanLifecycleKind;
    }

    /** {@inheritDoc} */
    @Override
    public Stream<StartOperationMirror> starters() {
        return stream(StartOperationHandle.class);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<StopOperationMirror> stoppers() {
        return stream(StopOperationHandle.class);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<InjectOperationMirror> injects() {
        return stream(InjectOperationHandle.class);
    }

    @SuppressWarnings("unchecked")
    private <M, H extends LifecycleOperationHandle> Stream<M> stream(Class<H> type) {
        return (Stream<M>) bean.operations.lifecycleHandles.values().stream().flatMap(List::stream).filter(h -> type.isInstance(h)).map(h -> h.mirror());
    }
}
