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
import java.util.List;

import app.packed.inject.Factory;
import app.packed.inject.IllegalAccessRuntimeException;
import app.packed.inject.InjectionException;
import app.packed.inject.TypeLiteral;
import app.packed.util.ExecutableDescriptor;
import app.packed.util.MethodDescriptor;
import app.packed.util.Nullable;
import packed.internal.inject.InternalDependency;
import packed.internal.util.ThrowableUtil;

/** The backing class of {@link Factory}. */
public class InternalFactoryExecutable<T> extends InternalFactoryMember<T> {

    /**
     * Whether or not we need to check the lower bound of the instances we return. This is only needed if we allow, for
     * example to register CharSequence fooo() as String.class. And I'm not sure we allow that..... Maybe have a special
     * Factory.overrideMethodReturnWith(), and then not allow it as default..
     */
    final boolean checkLowerBound;

    /** A factory with an executable as a target. */
    private final ExecutableDescriptor executable;

    /** A special method handle that should for this factory. */
    final MethodHandle methodHandle;

    private final int numberOfMissingDependencies;

    public InternalFactoryExecutable(TypeLiteral<T> key, ExecutableDescriptor executable, List<InternalDependency> dependencies,
            int numberOfMissingDependencies, MethodHandle methodHandle, @Nullable Object instance) {
        super(key, dependencies, instance);
        this.executable = executable;
        this.numberOfMissingDependencies = numberOfMissingDependencies;
        this.methodHandle = methodHandle;
        this.checkLowerBound = false;
    }

    /** {@inheritDoc} */
    @Override
    public Class<T> getLowerBound() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public T instantiate(Object[] params) {
        requireNonNull(methodHandle, "internal error");
        try {
            MethodHandle mh = methodHandle;
            if (instance != null) {
                mh = methodHandle.bindTo(instance);
            }
            return (T) mh.invokeWithArguments(params);
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new InjectionException("Failed to inject " + executable.descriptorTypeName(), e);
        }
    }

    @Override
    public boolean isMissingInstance() {
        return executable instanceof MethodDescriptor && !((MethodDescriptor) executable).isStatic() && instance == null;
    }

    @Override
    public String toString() {
        return executable.toString();
    }

    public boolean hasMethodHandle() {
        return methodHandle != null;
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
        MethodHandle handle;
        try {
            handle = executable.unreflect(lookup);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException(
                    "No access to the " + executable.descriptorTypeName() + " " + executable + ", use lookup(MethodHandles.Lookup) to give access", e);
        }
        return new InternalFactoryExecutable<>(getType(), executable, getDependencies(), numberOfMissingDependencies, handle, instance);
    }

    /** {@inheritDoc} */
    @Override
    public InternalFactoryMember<T> withInstance(Object instance) {
        return new InternalFactoryExecutable<>(getType(), executable, getDependencies(), numberOfMissingDependencies, methodHandle, instance);
    }
}
