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

import java.lang.invoke.MethodHandle;

import packed.internal.util.descriptor.InternalExecutableDescriptor;

/**
 *
 */
public abstract class MethodHandleProvider {

    /** The descriptor of the field. */
    private final InternalExecutableDescriptor descriptor;

    /** The var handle of the field. */
    private final MethodHandle methodHandle;

    MethodHandleProvider(InternalExecutableDescriptor descriptor, MethodHandle methodHandle) {
        this.descriptor = requireNonNull(descriptor, "descriptor is null");
        this.methodHandle = requireNonNull(methodHandle);
    }

    public InternalExecutableDescriptor getDescriptor() {
        return descriptor;
    }

    public abstract Object invoke() throws Throwable;

    public class MethodHandleFixedArray extends MethodHandleProvider {

        final Object[] arguments;

        /**
         * @param descriptor
         * @param methodHandle
         */
        MethodHandleFixedArray(InternalExecutableDescriptor descriptor, MethodHandle methodHandle, Object... args) {
            super(descriptor, methodHandle);
            this.arguments = requireNonNull(args);
        }

        /** {@inheritDoc} */
        @Override
        public Object invoke() throws Throwable {
            return methodHandle.invokeWithArguments(arguments);
        }
    }
}
