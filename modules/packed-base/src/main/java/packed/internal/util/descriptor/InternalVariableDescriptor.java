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
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import app.packed.util.FieldDescriptor;
import app.packed.util.ParameterDescriptor;
import app.packed.util.VariableDescriptor;
import packed.internal.inject.util.PackedServiceDependency;

/** The default abstract implementation of {@link VariableDescriptor}. */
public abstract class InternalVariableDescriptor extends InternalAnnotatedElement implements VariableDescriptor {

    /**
     * Creates a new descriptor.
     *
     * @param fieldOrParameter
     *            the field or parameter object
     */
    InternalVariableDescriptor(AnnotatedElement fieldOrParameter) {
        super(fieldOrParameter);
    }

    /**
     * The index of the variable, used when creating {@link PackedServiceDependency} instances.
     * <p>
     * If this variable is a field, this method returns {@code 0}.
     *
     * @return index of the variable.
     */
    public abstract int index();

    /**
     * Returns the parameterizedType of the variable
     *
     * @return the parameterizedType of the variable
     * @see Field#getGenericType()
     * @see Parameter#getParameterizedType()
     */
    public abstract Type getParameterizedType();

    /**
     * Tries to convert the specified value descriptor to an internal value descriptor.
     *
     * @param variable
     *            the variable to unwrap
     * @return the unwrapped variable
     * @throws IllegalArgumentException
     *             if the variable type is not a known variable type
     */
    public static InternalVariableDescriptor unwrap(VariableDescriptor variable) {
        requireNonNull(variable, "variable is null");
        if (variable instanceof InternalVariableDescriptor) {
            return (InternalVariableDescriptor) variable;
        } else if (variable instanceof FieldDescriptor) {
            return InternalFieldDescriptor.of((FieldDescriptor) variable);
        } else if (variable instanceof ParameterDescriptor) {
            return InternalParameterDescriptor.of(((ParameterDescriptor) variable).newParameter());
        } else {
            throw new IllegalArgumentException("Unknown descriptor type " + format(variable.getClass()));
        }
    }
}
