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
package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Modifier;
import java.util.List;

import app.packed.inject.IllegalAccessRuntimeException;
import app.packed.inject.Provides;
import app.packed.inject.TypeLiteral;
import app.packed.util.FieldDescriptor;
import app.packed.util.Nullable;

/** An internal factory that reads a field. Is mainly used in connection with {@link Provides}. */
public final class InternalFactoryField<T> extends InternalFactory<T> {

    private static final int INSTANCE_GET = 0;
    private static final int INSTANCE_GET_VOLATILE = 1;
    private static final int STATIC_GET = 2;

    // private final int STATIC_GET_VOLATILE = 3;

    /** The field to read. */
    private final FieldDescriptor field;

    @Nullable
    private final Object instance;

    /** Whether or not the field is volatile. */
    private final int type;

    /** The var handle used for reading the field. */
    private final VarHandle varHandle;

    public InternalFactoryField(TypeLiteral<T> typeLiteralOrKey, FieldDescriptor field, VarHandle varHandle, Object instance) {
        super(typeLiteralOrKey, List.of());
        this.field = requireNonNull(field);
        this.varHandle = varHandle;
        this.instance = instance;
        this.type = Modifier.isVolatile(field.getModifiers()) ? 1 : 0 + (field.isStatic() ? 2 : 0);
    }

    public InternalFactoryField(InternalFactoryField<T> other, Object instance) {
        super(other.getType(), List.of());
        this.field = other.field;
        this.varHandle = other.varHandle;
        this.instance = requireNonNull(instance);
        this.type = Modifier.isVolatile(field.getModifiers()) ? 1 : 0;
    }

    public InternalFactoryField<T> withInstance(Object instance) {
        requireNonNull(instance, "instance is null");
        return new InternalFactoryField<>(this, instance);
    }

    public boolean isStatic() {
        return type >= 2;
    }

    /**
     * Compiles the code to a single method handle.
     * 
     * @return the compiled method handle
     */
    public MethodHandle compile() {
        MethodHandle mh = varHandle.toMethodHandle(Modifier.isVolatile(field.getModifiers()) ? AccessMode.GET_VOLATILE : AccessMode.GET);
        if (instance == null) {
            mh = mh.bindTo(instance);
        }
        return mh;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getLowerBound() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable T instantiate(Object[] params) {
        switch (type) {
        case INSTANCE_GET:
            requireNonNull(instance);
            return (T) varHandle.get(instance);
        case INSTANCE_GET_VOLATILE:
            return (T) varHandle.getVolatile(instance);
        case STATIC_GET:
            return (T) varHandle.get();
        default:
            return (T) varHandle.getVolatile();
        }
    }

    /**
     * Returns a new internal factory that uses the specified lookup object to instantiate new objects.
     * 
     * @param lookup
     *            the lookup object to use
     * @return a new internal factory that uses the specified lookup object
     */
    @Override
    public InternalFactory<T> withLookup(Lookup lookup) {
        VarHandle handle;
        try {
            handle = field.unreflect(lookup);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("No access to the field " + field + ", use lookup(MethodHandles.Lookup) to give access", e);
        }
        return new InternalFactoryField<>(getType(), field, handle, instance);
    }
}
