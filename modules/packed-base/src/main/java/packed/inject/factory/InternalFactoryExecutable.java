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
package packed.inject.factory;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import app.packed.inject.Dependency;
import app.packed.inject.Factory;
import app.packed.inject.InjectionException;
import app.packed.inject.Key;
import packed.util.ThrowableUtil;
import packed.util.descriptor.AbstractExecutableDescriptor;
import packed.util.descriptor.InternalConstructorDescriptor;
import packed.util.descriptor.InternalMethodDescriptor;

/** The backing class of {@link Factory}. */
public class InternalFactoryExecutable<T> extends InternalFactory<T> {

    /**
     * Whether or not we need to check the lower bound of the instances we return. This is only needed if we allow, for
     * example to register CharSequence fooo() as String.class. And I'm not sure we allow that..... Maybe have a special
     * Factory.overrideMethodReturnWith(), and then not allow it as default..
     */
    final boolean checkLowerBound;

    private final List<Dependency> dependencies;

    public final AbstractExecutableDescriptor executable;

    /** A special method handle that should for this factory. */
    final MethodHandle methodHandle;

    private final int numberOfMissingDependencies;

    InternalFactoryExecutable(Key<T> key, AbstractExecutableDescriptor executable) {
        this(key, executable, executable.toDependencyList(), executable.getParameterCount(), null);
    }

    InternalFactoryExecutable(Key<T> key, AbstractExecutableDescriptor executable, List<Dependency> dependencies, int numberOfMissingDependencies,
            MethodHandle methodHandle) {
        super(key);
        this.executable = executable;
        this.numberOfMissingDependencies = numberOfMissingDependencies;
        this.dependencies = dependencies;
        this.methodHandle = methodHandle;
        this.checkLowerBound = false;
    }

    /** {@inheritDoc} */
    protected T create(Object[] args) {
        return null;
    }

    @Override
    public List<Dependency> getDependencies() {
        return dependencies;
    }

    /** {@inheritDoc} */
    @Override
    public Class<T> getLowerBound() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T instantiate(Object[] params) {
        requireNonNull(methodHandle, "internal error");
        try {
            return (T) methodHandle.invokeWithArguments(params);
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new InjectionException("Failed to inject " + executable.descriptorName(), e);
        }
    }

    @Override
    public boolean isAccessibleWith(Lookup lookup) {
        return super.isAccessibleWith(lookup);
    }

    @Override
    public String toString() {
        return executable.toString();
    }

    public boolean hasMethodHandle() {
        return methodHandle != null;
    }

    @Override
    public InternalFactory<T> withMethodLookup(Lookup lookup) {
        final MethodHandle handle;
        try {
            handle = executable.unreflect(lookup);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("No access to the " + executable.descriptorName() + " " + executable+ ", use lookup(MethodHandles.Lookup) to give access");
        }
        return new InternalFactoryExecutable<>(getKey(), executable, dependencies, numberOfMissingDependencies, handle);
    }

    public static <T> InternalFactory<T> from(Class<T> type) {
        AbstractExecutableDescriptor executable  = InternalMethodDescriptor.getDefaultFactoryFindStaticMethodx(type);
        if (executable == null) {
            executable = InternalConstructorDescriptor.findDefaultForInject(type);// moc.constructors().findInjectable();
        }
        return new InternalFactoryExecutable<>(Key.of(type), executable);
    }
}
