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
package packed.internal.inject.function;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Modifier;

import app.packed.inject.IllegalAccessRuntimeException;
import app.packed.inject.Provides;
import app.packed.inject.TypeLiteral;
import app.packed.util.FieldDescriptor;
import app.packed.util.Nullable;

/** An internal factory that reads a field. Is mainly used in connection with {@link Provides}. */
public final class FieldInvoker<T> extends InternalFactoryMember<T> {

    /** The field we invoke. */
    public final FieldDescriptor field;

    /** A var handle that can be used to read the field. */
    @Nullable
    private final VarHandle varHandle;

    /** Whether or not the field is volatile. */
    public final boolean isVolatile;

    /** Whether or not the field is static. */
    public final boolean isStatic;

    /**
     * Sets the value of the field
     * 
     * @param instance
     *            the instance for which to set the value
     * @param value
     *            the value to set
     * @see VarHandle#set(Object...)
     */
    public void setField(Object instance, Object value) {
        if (isVolatile) {
            varHandle.setVolatile(instance, value);
        } else {
            varHandle.set(instance, value);
        }
    }

    @SuppressWarnings("unchecked")
    public FieldInvoker(FieldDescriptor field) {
        super((TypeLiteral<T>) field.getTypeLiteral(), null);
        this.field = field;
        this.varHandle = null;
        this.isVolatile = Modifier.isVolatile(field.getModifiers());
        this.isStatic = Modifier.isStatic(field.getModifiers());
    }

    public FieldInvoker(TypeLiteral<T> typeLiteralOrKey, FieldDescriptor field, VarHandle varHandle, Object instance) {
        super(typeLiteralOrKey, instance);
        this.field = requireNonNull(field);
        this.varHandle = varHandle;
        this.isVolatile = Modifier.isVolatile(field.getModifiers());
        this.isStatic = Modifier.isStatic(field.getModifiers());
    }

    private FieldInvoker(FieldInvoker<T> other, Object instance) {
        super(other.getType(), requireNonNull(instance));
        this.field = other.field;
        this.varHandle = other.varHandle;
        this.isVolatile = Modifier.isVolatile(field.getModifiers());
        this.isStatic = Modifier.isStatic(field.getModifiers());
    }

    @Override
    public FieldInvoker<T> withInstance(Object instance) {
        requireNonNull(instance, "instance is null");
        if (this.instance != null) {
            throw new IllegalStateException("An instance has already been set");
        }
        return new FieldInvoker<>(this, instance);
    }

    @Override
    public boolean isMissingInstance() {
        return !field.isStatic() && instance == null;
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
    public @Nullable T invoke(Object[] params) {
        if (isStatic) {
            if (isVolatile) {
                return (T) varHandle.getVolatile();
            } else {
                return (T) varHandle.get();
            }
        }
        requireNonNull(instance);
        if (isVolatile) {
            return (T) varHandle.getVolatile(instance);
        } else {
            return (T) varHandle.get(instance);
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
    public FieldInvoker<T> withLookup(Lookup lookup) {
        VarHandle handle;
        try {
            if (Modifier.isPrivate(field.getModifiers())) {
                lookup = lookup.in(field.getDeclaringClass());
            }
            handle = field.unreflect(lookup);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("No access to the field " + field + ", use lookup(MethodHandles.Lookup) to give access", e);
        }
        return new FieldInvoker<>(getType(), field, handle, instance);
    }
}
