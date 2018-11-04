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
package packed.internal.util.descriptor;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import app.packed.inject.Dependency;
import app.packed.util.FieldDescriptor;
import app.packed.util.ParameterDescriptor;
import app.packed.util.VariableDescriptor;

/** The default abstract implementation of {@link VariableDescriptor}. */
public abstract class AbstractVariableDescriptor extends AbstractAnnotatedElement implements VariableDescriptor {

    /** The variable as a dependency, lazy calculated. */
    private volatile Dependency dependency;

    /**
     * Creates a new AbstractVariableDescriptor.
     *
     * @param fieldOrParameter
     *            the field or parameter object
     */
    AbstractVariableDescriptor(AnnotatedElement fieldOrParameter) {
        super(fieldOrParameter);
    }

    /**
     * The index of the variable, used when creating {@link Dependency} instances.
     *
     * @return index of the variable.
     */
    public abstract int getIndex();

    /**
     * Returns the parameterizedType of the variable
     *
     * @return the parameterizedType of the variable
     */
    public abstract Type getParameterizedType();

    /**
     * Returns this variable as a dependency.
     *
     * @return this variable as a dependency
     */
    public Dependency toDependency() {
        Dependency dependency = this.dependency;
        return dependency == null ? this.dependency = toDependency0() : dependency;
    }

    protected abstract Dependency toDependency0();

    /**
     * Tries to convert the specified value descriptor to an abstract value descriptor.
     *
     * @param variable
     *            the variable to unwrap
     * @return the unwrapped variable
     * @throws IllegalArgumentException
     *             if the specified variable could not be unwrapped
     */
    public static AbstractVariableDescriptor unwrap(VariableDescriptor variable) {
        requireNonNull(variable, "variable is null");
        if (variable instanceof AbstractVariableDescriptor) {
            return (AbstractVariableDescriptor) variable;
        } else if (variable instanceof FieldDescriptor) {
            return InternalFieldDescriptor.of((FieldDescriptor) variable);
        } else if (variable instanceof ParameterDescriptor) {
            return InternalParameterDescriptor.of(((ParameterDescriptor) variable).newParameter());
        } else {
            throw new IllegalArgumentException("Unknown descriptor type " + format(variable.getClass()));
        }
    }
}
