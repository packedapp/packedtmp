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

import app.packed.bean.BeanLifetime;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.lifecycle.BeanLifecycleMirror;
import app.packed.bean.lifecycle.BeanLifecycleModel;
import app.packed.bean.lifecycle.InitializeOperationMirror;
import app.packed.bean.lifecycle.StartOperationMirror;
import app.packed.bean.lifecycle.StopOperationMirror;
import internal.app.packed.ValueBased;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOnStartHandle;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.ForInitialize;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationStopHandle;

/** Implementation of {@link BeanLifecycleMirror}. */
@ValueBased
public record PackedBeanLifecycleMirror(BeanSetup bean) implements BeanLifecycleMirror {

    /**
     * If instances of this bean is created at runtime. This method will return the operation that creates the instance.
     *
     * @return operation that creates instances of the bean. Or empty if instances are never created
     */
    // instantiatedBy

    // Syntes maaske bare skal lede efter den i operations()?
    // Saa supportere vi ogsaa flere factory metodes hvis vi har brug for det en gang
    // We don't support multi factory for default installs.
    // However custom bean templates may support it
    @Override
    public Optional<InitializeOperationMirror> factory() {
        if (bean.beanKind != BeanLifetime.STATIC && bean.bean.beanSourceKind != BeanSourceKind.INSTANCE) {
            return Optional.of((InitializeOperationMirror) bean.operations.first().mirror());
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private <M, H extends BeanLifecycleOperationHandle> Stream<M> stream(Class<H> type) {
        return (Stream<M>) bean.operations.lifecycleHandles.values().stream().flatMap(List::stream).filter(h -> type.isInstance(h)).map(h -> h.mirror());
    }

    /** {@inheritDoc} */
    @Override
    public Stream<InitializeOperationMirror> initializers() {
        return stream(ForInitialize.class);
    }

    /** {@inheritDoc} */
    @Override
    public BeanLifecycleModel kind() {
        return bean.beanLifecycleKind;
    }

    /** {@inheritDoc} */
    @Override
    public Stream<StartOperationMirror> starters() {
        return stream(LifecycleOnStartHandle.class);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<StopOperationMirror> stoppers() {
        return stream(LifecycleOperationStopHandle.class);
    }
}
