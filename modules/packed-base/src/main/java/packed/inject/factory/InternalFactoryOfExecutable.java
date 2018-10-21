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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import app.packed.inject.Dependency;
import app.packed.inject.Factory;
import app.packed.inject.Key;
import packed.util.invokers.ConstructorInvoker;
import packed.util.invokers.ExecutableInvoker;

/** The backing class of {@link Factory}. */
public class InternalFactoryOfExecutable<T> extends InternalFactory<T> {

    @Override
    public InternalFactory<T> withMethodLookup(Lookup lookup) {
        final MethodHandle handle;
        try {
            handle = executable.unreflect(lookup);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("The specified lookup does not give acces to the " + executable.descriptor().descriptorName());
        }
        return new InternalFactoryOfExecutable<>(getKey(), executable, dependencies, numberOfMissingDependencies, handle);
    }

    private final List<Dependency> dependencies;

    public final ExecutableInvoker executable;

    /** A special method handle that should for this factory. */
    final MethodHandle methodHandle;

    private final int numberOfMissingDependencies;

    /**
     * Whether or not we need to check the lower bound of the instances we return. This is only needed if we allow, for
     * example to register CharSequence fooo() as String.class. And I'm not sure we allow that..... Maybe have a special
     * Factory.overrideMethodReturnWith(), and then not allow it as default..
     */
    final boolean checkLowerBound;

    InternalFactoryOfExecutable(Key<T> key, ExecutableInvoker executable) {
        this(key, executable, executable.descriptor().toDependencyList(), executable.descriptor().getParameterCount(), null);
    }

    InternalFactoryOfExecutable(Key<T> key, ExecutableInvoker executable, List<Dependency> dependencies, int numberOfMissingDependencies,
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

    /** {@inheritDoc} */
    @Override
    public Class<T> forScanning() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Dependency> getDependencies() {
        return dependencies;
    }

    @Override
    public int getNumberOfUnresolvedDependencies() {
        return numberOfMissingDependencies;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T instantiate(Object[] params) {
        return (T) executable.instantiate(params);
    }

    @Override
    public String toString() {
        return executable.toString();
    }

    public static <T> InternalFactory<T> from(Class<T> type) {
        ExecutableInvoker executable = ExecutableInvoker.getDefaultFactoryFindStaticMethod(type);
        if (executable == null) {
            executable = ConstructorInvoker.find(type);// moc.constructors().findInjectable();
        }
        return new InternalFactoryOfExecutable<>(Key.of(type), executable);
    }
}
