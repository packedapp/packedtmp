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
package packed.util.descriptor;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import app.packed.inject.Dependency;
import app.packed.util.ExecutableDescriptor;
import app.packed.util.ParameterDescriptor;

/** The default abstract implementation of {@link ExecutableDescriptor}. */
public abstract class AbstractExecutableDescriptor extends AbstractAnnotatedElement implements ExecutableDescriptor {

    /** A cached list of this dependencies matching the parameters of the executable. */
    private volatile List<Dependency> dependencies;

    /** The executable */
    private final Executable executable;

    /** An array of the parameter descriptor for this executable */
    private final InternalParameterDescriptor[] parameters;

    /** The parameter types of the executable. */
    private final Class<?>[] parameterTypes;

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

    public abstract MethodHandle unreflect(MethodHandles.Lookup lookup) throws IllegalAccessException;

    /**
     * Returns the declaring class of the executable.
     *
     * @return the declaring class of the executable
     * @see Executable#getDeclaringClass()
     */
    @Override
    public final Class<?> getDeclaringClass() {
        return executable.getDeclaringClass();
    }

    /** {@inheritDoc} */
    @Override
    public final int getModifiers() {
        return executable.getModifiers();
    }

    /**
     * Returns the number of parameters for the executable
     *
     * @return the number of parameters to the executable
     */
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

    /**
     * Returns the parameter types of the executable.
     *
     * @return the parameter types of the executable
     */
    // TODO unsafe arrays
    public final Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public final Iterator<ParameterDescriptor> iterator() {
        return (Iterator) List.of(parameters).iterator();
    }

    /**
     * Creates a new Executable from this descriptor.
     *
     * @return a new Executable from this descriptor
     */
    public abstract Executable newExecutable();

    /**
     * Returns a list of dependencies matching the the parameters of this executable.
     *
     * @return a dependency list
     */
    public final List<Dependency> toDependencyList() {
        List<Dependency> dependencies = this.dependencies;
        if (dependencies != null) {
            return dependencies;
        }

        switch (parameters.length) {
        case 0:
            dependencies = List.of();
            break;
        case 1:
            dependencies = List.of(parameters[0].toDependency());
            break;
        case 2:
            dependencies = List.of(parameters[0].toDependency(), parameters[1].toDependency());
            break;
        default:
            ArrayList<Dependency> list = new ArrayList<>(parameters.length);
            for (int i = 0; i < parameters.length; i++) {
                list.add(parameters[i].toDependency());
            }
            dependencies = List.copyOf(list);
        }
        return this.dependencies = dependencies;
    }

    /**
     * Creates a new executable descriptor from a method or constructor.
     *
     * @param executable
     *            the executable to create a descriptor for
     * @return a new executable descriptor
     */
    public static AbstractExecutableDescriptor of(Executable executable) {
        requireNonNull(executable, "executable is null");
        return executable instanceof Method ? InternalMethodDescriptor.of((Method) executable) : InternalConstructorDescriptor.of((Constructor<?>) executable);
    }
}
