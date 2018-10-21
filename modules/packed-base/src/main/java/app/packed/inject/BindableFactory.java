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
package app.packed.inject;

import java.util.List;

import packed.inject.factory.InternalFactory;

/**
 * A {@link Factory} that allows for initial binding of some of the parameters before dependency injection is performed.
 * This is typically used
 *
 * All externally available methods that takes a Factory as a parameter in Packed will make an internal copy of the
 * argument list that have been on this class. There binding or unbinding on a factory has no effect after a method
 * taking a factory returns.
 *
 *
 */

// Api's that takes bindable factories as parameters and keeps them for longer then the method invocation time.
// Should invoke immutable(), to make sure users do not rebind values.
// To make sure t

// Kan man nogensinde lave et BindableFactory der ikke er en executable?????????????
// Ja man kunne jo lave en fra->til Factory. Men som udgangs
public final class BindableFactory<T> extends Factory<T> implements Bindable {

    /** The bindable arguments */
    final Object[] arguments;

    long bindings;

    BindableFactory(InternalFactory<T> factory) {
        super(factory);
        this.arguments = new Object[factory.getDependencies().size()];
    }

    BindableFactory(InternalFactory<T> factory, Object[] arguments) {
        super(factory);
        this.arguments = arguments;
    }

    /** {@inheritDoc} */
    @Override
    BindableFactory<T> mutableCopyOf() {
        Object[] newArguments = new Object[arguments.length];
        System.arraycopy(arguments, 0, newArguments, 0, arguments.length);
        return new BindableFactory<>(factory, newArguments);
    }

    /**
     * Returns an immutable copy of this factory.
     *
     * @return an immutable copy of this factory
     */
    public Factory<T> immutableCopyOf() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new factory from the specified class using the following rules:
     *
     * //A single static method annotated with @Inject return the same type as the specified class //Look for a single
     * constructor on the class, return it //If multiple constructor, look for one annotated with Inject (if more than 1
     * annotated with Inject->fail) //if one constructor has more parameters than any other constructor return that. // Else
     * fail with Illegal Argument Exception
     *
     * @param implementation
     *            the implementation type
     * @return a factory for the specified implementation type
     * @throws NullPointerException
     *             if the specified implementation is null
     */
    public static <T> BindableFactory<T> find(Class<T> implementation) {
        return Factory.find(implementation).mutableCopyOf();
    }
    // The same methods as Factory, except for instance

    /** {@inheritDoc} */
    @Override
    public Bindable bind(Dependency dependency, Object instance) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Bindable bindLazy(Dependency dependency, Factory<?> factory) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Bindable bindPrototype(Dependency dependency, Factory<?> factory) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<Dependency> getBindableDependencies() {
        return null;
    }
}
