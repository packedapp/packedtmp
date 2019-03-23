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
package packed.internal.inject.providers;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Modifier;

import packed.internal.inject.Provider;
import packed.internal.util.descriptor.InternalFieldDescriptor;

/**
 *
 */
public abstract class VarHandleProvider {

    /** The descriptor of the field. */
    private final InternalFieldDescriptor descriptor;

    /** Whether or not the field is volatile. */
    private final boolean isVolatile;

    /** The var handle of the field. */
    private final VarHandle varHandle;

    VarHandleProvider(InternalFieldDescriptor descriptor, VarHandle varHandle) {
        this.descriptor = requireNonNull(descriptor, "descriptor is null");
        this.varHandle = requireNonNull(varHandle);
        this.isVolatile = Modifier.isVolatile(descriptor.getModifiers());
    }

    public InternalFieldDescriptor getFieldDescriptor() {
        return descriptor;
    }

    public abstract Object getValue();

    public abstract void setValue(Object value);

    public final class VarHandleProviderInstance extends VarHandleProvider {

        /** The instance to operate on. */
        private final Object instance;

        /**
         * @param descriptor
         * @param varHandle
         */
        VarHandleProviderInstance(InternalFieldDescriptor descriptor, VarHandle varHandle, Object instance) {
            super(descriptor, varHandle);
            this.instance = requireNonNull(instance);
        }

        /** {@inheritDoc} */
        @Override
        public Object getValue() {
            if (isVolatile) {
                return varHandle.getVolatile(instance);
            } else {
                return varHandle.get(instance);
            }
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
        @Override
        public void setValue(Object value) {
            if (isVolatile) {
                varHandle.setVolatile(instance, value);
            } else {
                varHandle.set(instance, value);
            }
        }
    }

    // How does this class get optimized away...Who has a reference???
    public final class VarHandleProviderInstanceProvided extends VarHandleProvider {

        /** The instance to operate on. */
        private final Provider<?> instanceProvider;

        /**
         * @param descriptor
         * @param varHandle
         */
        VarHandleProviderInstanceProvided(InternalFieldDescriptor descriptor, VarHandle varHandle, Provider<?> instanceProvider) {
            super(descriptor, varHandle);
            this.instanceProvider = requireNonNull(instanceProvider);
        }

        /** {@inheritDoc} */
        @Override
        public Object getValue() {
            Object instance = requireNonNull(instanceProvider.get());
            if (isVolatile) {
                return varHandle.getVolatile(instance);
            } else {
                return varHandle.get(instance);
            }
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
        @Override
        public void setValue(Object value) {
            Object instance = requireNonNull(instanceProvider.get());
            if (isVolatile) {
                varHandle.setVolatile(instance, value);
            } else {
                varHandle.set(instance, value);
            }
        }
    }

    public final class VarHandleProviderStatic extends VarHandleProvider {

        /**
         * @param descriptor
         * @param varHandle
         */
        VarHandleProviderStatic(InternalFieldDescriptor descriptor, VarHandle varHandle) {
            super(descriptor, varHandle);
        }

        /** {@inheritDoc} */
        @Override
        public Object getValue() {
            if (isVolatile) {
                return varHandle.getVolatile();
            } else {
                return varHandle.get();
            }
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
        @Override
        public final void setValue(Object value) {
            if (isVolatile) {
                varHandle.setVolatile(value);
            } else {
                varHandle.set(value);
            }
        }
    }
}
