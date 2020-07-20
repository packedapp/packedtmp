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
package app.packed.lifecycle2.fn;

import java.util.List;

import app.packed.base.Nullable;
import app.packed.introspection.VariableDescriptor;

/**
 *
 */
//FN (Function)
//FT (FunctionType)

// FN er _altid_ immutable....
public interface FN {

    // Ved ikke om den passer med f.eks.
    FN bind(Object o);

    // 0 or more Variables (with index from 0->n)
    // Both itself and its variables can be annotated
    // No unresolved type parameteres...

    // Nominal... Either explicit or by the user...
    // Various methods for transformations.

    // isResolved... Because of method handles... /// resolve(MethodsHandles.Lookup lookup);

    // The runtime will resolve it...

    // FNx er altid resolved....

    // bind(variable)

    // Saa hvis resolved binder vi med det samme...
    // Ellers bliver vi noedt til at delaye det...
    //// Men vi checker typer i det sidste tilfaelde

    default int numberOfVariables() {
        return variables().size();
    }

    List<VariableDescriptor> variables();

    /**
     * Binds the specified argument to a variable with the specified index as returned by {@link #variables()}. This method
     * is typically used to bind arguments to parameters on a method or constructors when key-based binding is not
     * sufficient. A typical example is a constructor with two parameters of the same type.
     * 
     * @param index
     *            the index of the variable to bind
     * @param argument
     *            the (nullable) argument to bind
     * @return a new factory
     * @throws IndexOutOfBoundsException
     *             if the specified index does not represent a valid variable
     * @throws ClassCastException
     *             if the specified argument is not compatible with the actual type of the variable
     * @throws NullPointerException
     *             if the specified argument is null and the variable does not represent a reference type
     */
    // The returned function will have new.numberOfVariables = old.numberOfVariables - 1
    // Syntes maaske bare bind
    FN bindConstant(int index, @Nullable Object argument);

    // Injection only deals with FN....
    // has variables annotated with...

    // bind() <- all of the specified type. Use bindErasedType
    // bindErasedType() <- bind all of the specified type...
}
/// Eksistere fordi vi ikke kan capture generisk information og annotation
/// Og fordi vi gerne vil mapninger bedre...