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
package app.packed.inject.variable;

import static java.util.Objects.requireNonNull;

import app.packed.inject.Variable;

/**
 *
 */
final class PackedFunctionType {
    static final Variable[] NO_VARS = {};
    // The rtype and ptypes fields define the structural identity of the method type:
    private final Variable returnType;

    private final Variable[] variables;

    PackedFunctionType(Variable returnType, Variable[] variables) {
        this.returnType = returnType;
        this.variables = variables;
    }

    /**
     * Return a method descriptor that is identical to this one, except that the return type has been changed to the
     * specified type
     *
     * @param newReturn
     *            a field descriptor for the new return type
     * @throws NullPointerException
     *             if any argument is {@code null}
     * @return the new method descriptor
     */
    public PackedFunctionType changeReturnType(Variable newReturn) {
        requireNonNull(newReturn, "newReturn is null");
        return new PackedFunctionType(newReturn, variables);
    }

    public PackedFunctionType of(Variable returnVar) {
        requireNonNull(returnVar, "returnVar is null");
        return new PackedFunctionType(returnVar, NO_VARS);
    }

    /**
     * Returns the return type of this method type.
     * 
     * @return the return type
     */
    public Variable returnVar() {
        return returnType;
    }
}
