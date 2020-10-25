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
package packed.internal.inject.service;

import java.util.function.Function;
import java.util.function.Predicate;

import app.packed.base.Key;
import app.packed.component.Wirelet;
import app.packed.cube.ExtensionMember;
import app.packed.inject.Service;
import app.packed.inject.ServiceContract;
import app.packed.inject.ServiceExtension;
import app.packed.inject.ServiceTransformer;

/**
 *
 */
public class WireletToContext implements ServiceTransformer {

    public ServiceContract childContract() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <T> void decorate(Key<T> key, Function<? super T, ? extends T> decoratingFunction) {}

    /** {@inheritDoc} */
    @Override
    public void rekey(Key<?> existingKey, Key<?> newKey) {}

    /** {@inheritDoc} */
    @Override
    public void remove(Key<?>... keys) {}

    /** {@inheritDoc} */
    @Override
    public void removeIf(Predicate<? super Service> filter) {}

    /** {@inheritDoc} */
    @Override
    public void retain(Key<?>... keys) {}

    /** {@inheritDoc} */
    @Override
    public <T> void provideInstance(Key<T> key, T instance) {}

    @ExtensionMember(ServiceExtension.class)
    public static abstract class ServiceWireletTo extends Wirelet {
        protected abstract void process(WireletToContext context);
    }
}
