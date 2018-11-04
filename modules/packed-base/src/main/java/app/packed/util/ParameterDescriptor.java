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
package app.packed.util;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

import pckd.internal.util.descriptor.InternalParameterDescriptor;

/**
 * A parameter descriptor.
 * <p>
 * Unlike the {@link Parameter} class, this interface contains no mutable operations, so it can be freely shared.
 */
public interface ParameterDescriptor extends VariableDescriptor {

    /**
     * Return a descriptor of the executable declaring this parameter.
     *
     * @return a descriptor of the executable declaring this parameter
     * @see Parameter#getDeclaringExecutable()
     */
    ExecutableDescriptor getDeclaringExecutable();

    /**
     * Returns the index of the parameter.
     *
     * @return the index of the parameter
     */
    int getIndex();

    /**
     * Returns true if this parameter represents a variable argument list, otherwise returns false.
     *
     * @return true if an only if this parameter represents a variable argument list.
     * @see Parameter#isVarArgs()
     */
    boolean isVarArgs();

    /**
     * Creates a new {@link Parameter} corresponding to this descriptor.
     * <p>
     * This method always creates a new parameter to avoid giving access to the underlying mutable {@link Executable
     * Parameter#getDeclaringExecutable()}.
     *
     * @return a new parameter
     */
    Parameter newParameter();

    /**
     * Returns a descriptor from the specified parameter.
     *
     * @param parameter
     *            the parameter to return a descriptor for 
     * @return a descriptor from the specified parameter
     */
    static ParameterDescriptor of(Parameter parameter) {
        return InternalParameterDescriptor.of(parameter);
    }
}
