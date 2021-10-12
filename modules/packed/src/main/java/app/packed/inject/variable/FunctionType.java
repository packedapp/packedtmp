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

import java.lang.invoke.MethodType;
import java.lang.reflect.Executable;
import java.util.List;

/**
 * This class is modelled to {@link MethodType}
 *
 */
// Som MethodType men med Annotations og generic types
public interface FunctionType {

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
    FunctionType changeReturnType(Variable newReturn);

    Variable returnType();

    /**
     * Return an array of field descriptors for the parameter types of the method type described by this descriptor
     * 
     * @return field descriptors for the parameter types
     */
    Variable[] variableArray();

    /** {@return the number of variables in this function type.} */
    int variableCount();

    /**
     * Return an immutable list of field descriptors for the parameter types of the method type described by this descriptor
     * 
     * @return field descriptors for the parameter types
     */
    List<Variable> variableList();

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
