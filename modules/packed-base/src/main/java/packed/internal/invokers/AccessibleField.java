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
package packed.internal.invokers;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Modifier;

import app.packed.inject.IllegalAccessRuntimeException;
import app.packed.util.FieldDescriptor;
import packed.internal.util.descriptor.InternalFieldDescriptor;

/** An accessible field. */
public final class AccessibleField<T> extends AccessibleMember<T> {

    /** The descriptor of the field. */
    private final InternalFieldDescriptor descriptor;

    /** Whether or not the field is volatile. */
    private final boolean isVolatile;

    /** The var handle of the field. */
    private final VarHandle varHandle;

    public AccessibleField(InternalFieldDescriptor descriptor, Lookup lookup, T t) {
        super(t);
        this.descriptor = descriptor;
        try {
            this.varHandle = descriptor.unreflect(lookup);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("Field " + descriptor + " is not accessible for lookup object " + lookup, e);
        }
        this.isVolatile = Modifier.isVolatile(descriptor.getModifiers());
    }

    /**
     * Creates a new field invoker.
     * 
     * @param descriptor
     *            the field descriptor
     * @param lookup
     *            the lookup object to use for access
     */
    public AccessibleField(InternalFieldDescriptor descriptor, VarHandle varHandle) {
        this.descriptor = descriptor;
        this.varHandle = requireNonNull(varHandle);
        this.isVolatile = Modifier.isVolatile(descriptor.getModifiers());
    }

    /**
     * Creates a new field invoker.
     * 
     * @param descriptor
     *            the field descriptor
     * @param lookup
     *            the lookup object to use for access
     */
    public AccessibleField(InternalFieldDescriptor descriptor, VarHandle varHandle, T t) {
        super(t);
        this.descriptor = descriptor;
        this.varHandle = requireNonNull(varHandle);
        this.isVolatile = Modifier.isVolatile(descriptor.getModifiers());
    }

    /**
     * Returns the descriptor of the field.
     * 
     * @return the descriptor of the field
     */
    public FieldDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Returns the value of this field for the given instance.
     * 
     * @param instance
     *            the instance for which to return the value
     * @return the value of this field for the specified instance
     * @see VarHandle#get(Object...)
     */
    public Object getField(Object instance) {
        if (isVolatile) {
            return varHandle.getVolatile(instance);
        } else {
            return varHandle.get(instance);
        }
    }

    /**
     * Returns whether or not the field is volatile.
     * 
     * @return whether or not the field is volatile
     */
    public boolean isVolatile() {
        return isVolatile;
    }

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

    public VarHandle varHandle() {
        return varHandle;
    }
}
