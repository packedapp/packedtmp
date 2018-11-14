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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;

import app.packed.inject.IllegalAccessRuntimeException;
import packed.internal.util.descriptor.InternalExecutableDescriptor;

/**
 *
 */
public class AccessibleExecutable<T> extends AccessibleMember<T> {

    /** The descriptor of the field. */
    private final InternalExecutableDescriptor descriptor;

    /** The method handle of the executable. */
    private final MethodHandle methodHandle;

    public AccessibleExecutable(InternalExecutableDescriptor descriptor, Lookup lookup, T t) {
        super(t);
        this.descriptor = descriptor;
        try {
            this.methodHandle = descriptor.unreflect(lookup);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException(descriptor.descriptorTypeName() + " " + descriptor + " is not accessible for lookup object " + lookup, e);
        }
    }

    /**
     * Creates a new field invoker.
     * 
     * @param descriptor
     *            the field descriptor
     * @param lookup
     *            the lookup object to use for access
     */
    public AccessibleExecutable(InternalExecutableDescriptor descriptor, MethodHandle methodHandle) {
        this.descriptor = descriptor;
        this.methodHandle = requireNonNull(methodHandle);
    }

    /**
     * Creates a new field invoker.
     * 
     * @param descriptor
     *            the field descriptor
     * @param lookup
     *            the lookup object to use for access
     */
    public AccessibleExecutable(InternalExecutableDescriptor descriptor, MethodHandle methodHandle, T t) {
        super(t);
        this.descriptor = descriptor;
        this.methodHandle = requireNonNull(methodHandle);
    }

    public final Object invoke(Object... arguments) throws Throwable {
        return methodHandle.invokeWithArguments(arguments);
    }

    /**
     * Returns the descriptor of the field.
     * 
     * @return the descriptor of the field
     */
    public InternalExecutableDescriptor descriptor() {
        return descriptor;
    }

    public MethodHandle methodHandle() {
        return methodHandle;
    }
}
