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
package app.packed.inject;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import app.packed.util.ConstructorDescriptor;
import app.packed.util.FieldDescriptor;
import app.packed.util.Key;
import app.packed.util.MethodDescriptor;
import app.packed.util.ParameterDescriptor;
import app.packed.util.VariableDescriptor;
import packed.internal.inject.InternalDependencyDescriptor;

/**
 * A descriptor of a dependency. An instance of this class is typically created from a parameter on a constructor or
 * method. In which case the parameter (represented by a {@link ParameterDescriptor}) can be obtained by calling
 * {@link #variable()}. A descriptor can also be created from a field, in which case {@link #variable()} returns an
 * instance of {@link FieldDescriptor}. Dependencies can be optional in which case {@link #isOptional()} returns true.
 */
// Flyt member, parameterIndex og Variable???? til ServiceRequest..
// Vi goer det kun for at faa en paenere arkitk
public interface DependencyDescriptor {

    // Vi tager alle annotations med...@SystemProperty(fff) @Foo String xxx
    // Includes any qualifier...
    // AnnotatedElement annotations();

    /**
     * Returns whether or not this dependency is optional.
     *
     * @return whether or not this dependency is optional
     */
    boolean isOptional();

    /**
     * Returns the key of this dependency.
     *
     * @return the key of this dependency
     */
    Key<?> key();

    /**
     * The member (field, method or constructor) for which this dependency was created. Or an empty {@link Optional} if this
     * dependency was not created from a member.
     * <p>
     * If this dependency was created from a member this method will an optional containing either a {@link FieldDescriptor}
     * in case of field injection, A {@link MethodDescriptor} in case of method injection or a {@link ConstructorDescriptor}
     * in case of constructor injection.
     * 
     * @return the member that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         member.
     * @see #variable()
     */
    Optional<Member> member();

    /**
     * If this dependency represents a parameter in a constructor or method. This method will return an optional holding the
     * parameter index. Otherwise this method returns an empty optional.
     *
     * @return the optional parameter index of the dependency
     */
    OptionalInt parameterIndex();

    /**
     * The variable (field or parameter) for which this dependency was created. Or an empty {@link Optional} if this
     * dependency was not created from a variable.
     * <p>
     * If this dependency was created from a field this method will return a {@link FieldDescriptor}. If this dependency was
     * created from a parameter this method will return a {@link ParameterDescriptor}.
     * 
     * @return the variable that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         variable.
     * @see #member()
     */
    Optional<VariableDescriptor> variable();

    /**
     * @param actualClass
     * @param baseClass
     * @param baseClassTypeVariableIndex
     * @return
     */
    public static <T> DependencyDescriptor fromTypeVariable(Class<? extends T> actualClass, Class<T> baseClass, int baseClassTypeVariableIndex) {
        return InternalDependencyDescriptor.fromTypeVariable(actualClass, baseClass, baseClassTypeVariableIndex);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> List<DependencyDescriptor> fromTypeVariables(Class<? extends T> actualClass, Class<T> baseClass, int... baseClassTypeVariableIndexes) {
        return (List) InternalDependencyDescriptor.fromTypeVariables(actualClass, baseClass, baseClassTypeVariableIndexes);
    }
}