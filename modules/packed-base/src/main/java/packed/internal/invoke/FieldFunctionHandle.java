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
package packed.internal.invoke;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Modifier;

import app.packed.util.FieldDescriptor;
import app.packed.util.IllegalAccessRuntimeException;
import app.packed.util.Nullable;
import app.packed.util.TypeLiteral;

/** An invoker that can read and write fields. */
public final class FieldFunctionHandle<T> extends InvokableMember<T> {

    /** The field we invoke. */
    public final FieldDescriptor field;

    /** Whether or not the field is static. */
    public final boolean isStatic;

    /** Whether or not the field is volatile. */
    public final boolean isVolatile;

    /** A var handle that can be used to read the field. */
    @Nullable
    private final VarHandle varHandle;

    @SuppressWarnings("unchecked")
    public FieldFunctionHandle(FieldDescriptor field) {
        super((TypeLiteral<T>) field.getTypeLiteral(), null);
        this.field = field;
        this.varHandle = null;
        this.isVolatile = Modifier.isVolatile(field.getModifiers());
        this.isStatic = Modifier.isStatic(field.getModifiers());
    }

    private FieldFunctionHandle(FieldFunctionHandle<T> other, Object instance) {
        super(other.getReturnType(), requireNonNull(instance));
        this.field = other.field;
        this.varHandle = other.varHandle;
        this.isVolatile = Modifier.isVolatile(field.getModifiers());
        this.isStatic = Modifier.isStatic(field.getModifiers());
    }

    public FieldFunctionHandle(TypeLiteral<T> typeLiteralOrKey, FieldDescriptor field, VarHandle varHandle, Object instance) {
        super(typeLiteralOrKey, instance);
        this.field = requireNonNull(field);
        this.varHandle = varHandle;
        this.isVolatile = Modifier.isVolatile(field.getModifiers());
        this.isStatic = Modifier.isStatic(field.getModifiers());
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

    @Override
    public boolean isMissingInstance() {
        return !field.isStatic() && instance == null;
    }

    /**
     * Sets the value of the field
     * 
     * @param instance
     *            the instance for which to set the value
     * @param value
     *            the value to set
     * @see VarHandle#set(Object...)
     * @throws UnsupportedOperationException
     *             if the underlying method is a static method
     */
    public void setOnInstance(Object instance, Object value) {
        if (isStatic) {
            throw new UnsupportedOperationException("Underlying field " + field + " is static");
        }
        if (isVolatile) {
            varHandle.setVolatile(instance, value);
        } else {
            varHandle.set(instance, value);
        }
    }

    @Override
    public FieldFunctionHandle<T> withInstance(Object instance) {
        requireNonNull(instance, "instance is null");
        if (this.instance != null) {
            throw new IllegalStateException("An instance has already been set");
        } else if (isStatic) {
            throw new IllegalStateException("The field is static");
        }
        return new FieldFunctionHandle<>(this, instance);
    }

    /**
     * Returns a new internal factory that uses the specified lookup object to instantiate new objects.
     * 
     * @param lookup
     *            the lookup object to use
     * @return a new internal factory that uses the specified lookup object
     */
    @Override
    public FieldFunctionHandle<T> withLookup(Lookup lookup) {
        VarHandle handle;
        try {
            if (Modifier.isPrivate(field.getModifiers())) {
                lookup = lookup.in(field.getDeclaringClass());
            }
            handle = field.unreflectVarHandle(lookup);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("No access to the field " + field + ", use lookup(MethodHandles.Lookup) to give access", e);
        }
        return new FieldFunctionHandle<>(getReturnType(), field, handle, instance);
    }
}
/**
 * Returns the value of this field for the given instance.
 * 
 * @param instance
 *            the instance for which to return the value
 * @return the value of this field for the specified instance
 * @see VarHandle#get(Object...)
 */