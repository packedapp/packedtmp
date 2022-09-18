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
package app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import app.packed.base.Key;

/**
 * An operation type represents the arguments and return type for an operation.
 * 
 * @apiNote This class is modelled after {@link MethodType}. But includes information about annotations and unerased
 *          type information.
 */

// Mit problem med den her er lidt method return type...
// Altsaa annoteringer er jo ikke noedvendigvis knyttede til retur typen, fx @Get
public final /* primitive */ class OperationType {

    private static final Variable[] NO_VARS = {};

    /** The parameter variables. */
    private final Variable[] parameterArray;

    /** The return variable. */
    private final Variable returnVar;

    private OperationType(Variable returnVar, Variable... variables) {
        this.returnVar = returnVar;
        this.parameterArray = variables;
    }

    /**
     * Return a factory type that is identical to this one, except that the return variable has been changed to the
     * specified variable.
     *
     * @param newReturn
     *            the variable a field descriptor for the new return type
     * @return the new factory type
     */
    public OperationType changeReturnVar(Variable newReturn) {
        requireNonNull(newReturn, "newReturn is null");
        return new OperationType(newReturn, parameterArray);
    }

    /**
     * Compares the specified object with this function type for equality. That is, it returns {@code true} if and only if
     * the specified object is also a function type with exactly the same parameters and return variable.
     * 
     * @param obj
     *            object to compare
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * Returns the hash code value for this function type. It is defined to be the same as the hash code of a List whose
     * elements are the return variable followed by the parameter variables.
     * 
     * @return the hash code value for this function type
     * @see Object#hashCode()
     * @see #equals(Object)
     * @see List#hashCode()
     */
    @Override
    public int hashCode() {
        int hashCode = 31 + returnVar.hashCode();
        for (Variable ptype : parameterArray) {
            hashCode = 31 * hashCode + ptype.hashCode();
        }
        return hashCode;
    }

    /**
     * Return an array of field descriptors for the parameter types of the method type described by this descriptor
     * 
     * @return field descriptors for the parameter types
     * @apiNote freezeable arrays might be supported in the future. In which case we will not return a copy
     */
    public Variable[] parameterArray() {
        return Arrays.copyOf(parameterArray, parameterArray.length);
    }

    /** {@return the number of parameter variables in this operation type.} */
    public int parameterCount() {
        return parameterArray.length;
    }

    /**
     * Return an immutable list of field descriptors for the parameter types of the method type described by this descriptor
     * 
     * @return field descriptors for the parameter types
     */
    public List<Variable> parameterList() {
        return List.of(parameterArray);
    }

    private Class<?>[] rawParameterTypeArray() {
        Class<?>[] params = new Class<?>[parameterArray.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = parameterArray[i].getType();
        }
        return params;
    }

    /** {@return the return variable.} */
    public Variable returnVar() {
        return returnVar;
    }

    /** {@return the return variable.} */
    public Key<?> returnVarAsKey() {

        throw new UnsupportedOperationException();
    }

    /** { @return extracts the raw types for each variable and returns them as a MethodType.} */
    public MethodType toMethodType() {
        return switch (parameterArray.length) {
        case 0 -> MethodType.methodType(returnVar.getType());
        case 1 -> MethodType.methodType(returnVar.getType(), parameterArray[0].getType());
        default -> MethodType.methodType(returnVar.getType(), rawParameterTypeArray());
        };
    }

    /**
     * Returns a string representation of the function type, of the form {@code "(PT0,PT1...)RT"}. The string representation
     * of a function type is a parenthesis enclosed, comma separated list of type names, followed immediately by the return
     * variable.
     * <p>
     * Each type is represented by its {@link java.lang.Class#getSimpleName simple name}.
     */
    @Override
    public String toString() {
        // TODO We should probably call something else that toString();
        StringJoiner sj = new StringJoiner(",", "(", ")" + returnVar.toString());
        for (int i = 0; i < parameterArray.length; i++) {
            sj.add(parameterArray[i].toString());
        }
        return sj.toString();

    }

    /**
     * Returns a function type with the given return variable. The resulting function type has no parameter variables.
     * 
     * @param rtype
     *            the return variable
     * @return a function type with the given return variable
     */
    public static OperationType of(Variable returnVar) {
        requireNonNull(returnVar, "returnVar is null");
        return new OperationType(returnVar, NO_VARS);
    }

    public static OperationType of(Variable returnVar, Variable var) {
        requireNonNull(returnVar, "returnVar is null");
        requireNonNull(returnVar, "var is null");
        return new OperationType(returnVar, var);
    }

    public static OperationType of(Variable returnVar, Variable... vars) {
        throw new UnsupportedOperationException();
    }

    public static OperationType of(Class<?> returnVar, Class<?>... vars) {
        return ofMethodType(MethodType.methodType(returnVar, vars));
    }

    /**
     * {@return an op type representing the signature of the specified executable.}
     * 
     * @param executable
     *            the executable to return a op type for.
     * @throws Exception if there are type variables
     */
    public static OperationType ofExecutable(Executable executable) {
        requireNonNull(executable, "executable is null");
        Variable returnVar = executable instanceof Method m ? Variable.ofMethodReturnType(m) : Variable.ofConstructor((Constructor<?>) executable);
        Parameter[] parameters = executable.getParameters();
        if (parameters.length == 0) {
            return of(returnVar);
        }
        Variable[] vars = new Variable[parameters.length];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = Variable.ofParameter(parameters[i]);
        }
        return of(returnVar, vars);
    }

    public static OperationType ofMethodType(MethodType methodType) {
        throw new UnsupportedOperationException();
    }
}
