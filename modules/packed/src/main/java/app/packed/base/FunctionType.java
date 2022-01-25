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
package app.packed.base;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodType;
import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.List;

/**
 * A function type represents the arguments and return type accepted and returned by an invokable function. This class
 * is modelled after {@link MethodType}.
 */
//Som MethodType men med Annotations og generic types
//InvokableType, OperationType
//// Ideen er lidt at en BeanOperation har en FunctionType...
public final class FunctionType {

    static final Variable[] NO_VARS = {};

    private final Variable returnType;

    private final Variable[] variables;

    FunctionType(Variable returnType, Variable[] variables) {
        this.returnType = returnType;
        this.variables = variables;
    }

    /**
     * Return a factory type that is identical to this one, except that the return type has been changed to the specified
     * type
     *
     * @param newReturn
     *            a field descriptor for the new return type
     * @throws NullPointerException
     *             if any argument is {@code null}
     * @return the new method descriptor
     */
    public FunctionType changeReturnType(Variable newReturn) {
        requireNonNull(newReturn, "newReturn is null");
        return new FunctionType(newReturn, variables);
    }

    /** {@return the return variable.} */
    public Variable returnVar() {
        return returnType;
    }

    /**
     * Return an array of field descriptors for the parameter types of the method type described by this descriptor
     * 
     * @return field descriptors for the parameter types
     * @apiNote freezeable arrays might be supported in the future.
     */
    public Variable[] variableArray() {
        return Arrays.copyOf(variables, variables.length);
    }

    /** {@return the number of variables in this function type.} */
    public int variableCount() {
        return variables.length;
    }

    /**
     * Return an immutable list of field descriptors for the parameter types of the method type described by this descriptor
     * 
     * @return field descriptors for the parameter types
     */
    public List<Variable> variableList() {
        throw new UnsupportedOperationException();
    }

    public static FunctionType of(Variable returnVar) {
        requireNonNull(returnVar, "returnVar is null");
        return new FunctionType(returnVar, NO_VARS);
    }

    static FunctionType ofExecutable(Executable e) {
        throw new UnsupportedOperationException();
    }

    static FunctionType ofMethodType(MethodType methodType) {
        throw new UnsupportedOperationException();
    }

    static MethodType toMethodType() {
        throw new UnsupportedOperationException();
    }
}
