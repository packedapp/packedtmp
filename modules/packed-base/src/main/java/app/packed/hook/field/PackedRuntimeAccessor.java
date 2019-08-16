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
package app.packed.hook.field;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.component.ComponentConfiguration;
import app.packed.hook.RuntimeAccessor;
import app.packed.util.IllegalAccessRuntimeException;
import packed.internal.container.AbstractComponentConfiguration;

/**
 *
 */
public class PackedRuntimeAccessor<T> implements RuntimeAccessor<T> {

    public final InternalFieldOperation<T> afo;

    public final MethodHandle mh;

    public final MethodHandles.Lookup lookup;

    public final Field field;

    public PackedRuntimeAccessor(MethodHandles.Lookup lookup, Field field, InternalFieldOperation<T> afo) {
        this.afo = requireNonNull(afo);
        try {
            mh = lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("foo", e);
        }
        this.lookup = lookup;
        this.field = field;
    }

    /** {@inheritDoc} */
    @Override
    public void onReady(ComponentConfiguration cc, Consumer<T> consumer) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <S> void onReady(ComponentConfiguration cc, Class<S> sidecarType, BiConsumer<S, T> consumer) {
        // TODO check instance component if instance field...
        AbstractComponentConfiguration pcc = (AbstractComponentConfiguration) cc;
        pcc.checkConfigurable();
        pcc.del.add(new DelayedAccessor.SidecarDelayerAccessor(this, sidecarType, consumer));
    }
}
