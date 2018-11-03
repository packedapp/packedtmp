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
package pckd.internals.util.descriptor;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.packed.inject.Dependency;
import app.packed.util.ExecutableDescriptor;

/** The default abstract implementation of {@link ExecutableDescriptor}. */
public abstract class AbstractExecutableDescriptor extends AbstractAnnotatedElement implements ExecutableDescriptor {

    /** A cached list of this dependencies matching the parameters of the executable. */
    private volatile List<Dependency> dependencies;

    /** The executable */
    final Executable executable;

    /** An array of the parameter descriptor for this executable */
    private final InternalParameterDescriptor[] parameters;

    /** The parameter types of the executable. */
    final Class<?>[] parameterTypes;

    /**
     * Creates a new ExecutableMirror from the specified executable.
     *
     * @param executable
     *            the executable to mirror
     */
    public AbstractExecutableDescriptor(Executable executable) {
        super(executable);
        this.executable = executable;
        Parameter[] realParameters = executable.getParameters();
        this.parameters = new InternalParameterDescriptor[realParameters.length];
        for (int i = 0; i < realParameters.length; i++) {
            this.parameters[i] = new InternalParameterDescriptor(this, realParameters[i], i);
        }
        this.parameterTypes = executable.getParameterTypes();
    }

    /** {@inheritDoc} */
    @Override
    public final Class<?> getDeclaringClass() {
        return executable.getDeclaringClass();
    }

    /** {@inheritDoc} */
    @Override
    public final int getModifiers() {
        return executable.getModifiers();
    }

    /** {@inheritDoc} */
    @Override
    public final int getParameterCount() {
        return parameters.length;
    }

    /**
     * Returns an array of parameter mirrors of the executable.
     *
     * @return an array of parameter mirrors of the executable
     */
    // TODO unsafe arrays
    public final InternalParameterDescriptor[] getParameters() {
        return parameters;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isSynthetic() {
        return executable.isSynthetic();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isVarArgs() {
        return executable.isVarArgs();
    }

    public final boolean matchesParameters(Class<?>[] parameterTypes) {
        return Arrays.equals(this.parameterTypes, parameterTypes);
    }

    /**
     * Creates a new Executable from this descriptor.
     *
     * @return a new Executable from this descriptor
     */
    public abstract Executable newExecutable();

    /**
     * Returns a list of dependencies matching the parameters of this executable.
     *
     * @return a dependency list
     */
    @Override
    public final List<Dependency> toDependencyList() {
        List<Dependency> d = this.dependencies;
        if (d != null) {
            return d;
        }

        switch (parameters.length) {
        case 0:
            return dependencies = List.of();
        case 1:
            return dependencies = List.of(parameters[0].toDependency());
        case 2:
            return dependencies = List.of(parameters[0].toDependency(), parameters[1].toDependency());
        default:
            ArrayList<Dependency> list = new ArrayList<>(parameters.length);
            for (int i = 0; i < parameters.length; i++) {
                list.add(parameters[i].toDependency());
            }
            return dependencies = List.copyOf(list);
        }
    }

    /**
     * Creates a new descriptor from a method or constructor.
     *
     * @param executable
     *            the executable to create a descriptor for
     * @return a new descriptor
     */
    public static AbstractExecutableDescriptor of(Executable executable) {
        requireNonNull(executable, "executable is null");
        return executable instanceof Method ? InternalMethodDescriptor.of((Method) executable) : InternalConstructorDescriptor.of((Constructor<?>) executable);
    }
}
