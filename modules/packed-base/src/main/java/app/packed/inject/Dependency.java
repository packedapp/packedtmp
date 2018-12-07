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

import app.packed.util.ConstructorDescriptor;
import app.packed.util.FieldDescriptor;
import app.packed.util.Key;
import app.packed.util.MethodDescriptor;
import app.packed.util.ParameterDescriptor;
import app.packed.util.VariableDescriptor;
import packed.internal.inject.InternalDependency;

/**
 * A dependency object. This is typically created from a parameter on a constructor or method. In which case the
 * parameter (represented by a {@link ParameterDescriptor}) can be obtained by calling {@link #getVariable()}. It can
 * also be a field, in which case {@link #getVariable()} returns an instance of {@link ParameterDescriptor}.
 * Dependencies can be optional in which case {@link #isOptional()} returns true.
 */
public interface Dependency {

    /**
     * Returns the index of the dependency. If the dependency is created from a method or constructor, the index refers to
     * index of the parameter. If the dependency is created from anything else, this method return 0.
     *
     * @return the index of the dependency
     */
    int getIndex();

    /**
     * Returns the key of this dependency.
     *
     * @return the key of this dependency
     */
    Key<?> getKey();

    /**
     * The member for which this dependency was created. Or an empty {@link Optional} if this dependency was not created
     * from a member.
     * <p>
     * If this dependency was created from a member this method will an optional containing either a {@link FieldDescriptor}
     * in case of field injection, A {@link MethodDescriptor} in case of method injection or a {@link ConstructorDescriptor}
     * in case of constructor injection.
     * 
     * @return the member that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         member.
     * @see #getVariable()
     */
    Optional<Member> getMember();

    /**
     * The variable for which this dependency was created. Or an empty {@link Optional} if this dependency was not created
     * from a variable.
     * <p>
     * If this dependency was created from a field this method will return a {@link FieldDescriptor}. If this dependency was
     * created from a parameter this method will return a {@link ParameterDescriptor}.
     * 
     * @return the variable that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         variable.
     * @see #getMember()
     */
    Optional<VariableDescriptor> getVariable();

    /**
     * Returns whether or not this dependency is optional.
     *
     * @return whether or not this dependency is optional
     */
    boolean isOptional();

    public static <T> Dependency fromTypeVariable(Class<? extends T> actualClass, Class<T> baseClass, int baseClassTypeVariableIndex) {
        return InternalDependency.fromTypeVariable(actualClass, baseClass, baseClassTypeVariableIndex);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> List<Dependency> fromTypeVariables(Class<? extends T> actualClass, Class<T> baseClass, int... baseClassTypeVariableIndexes) {
        return (List) InternalDependency.fromTypeVariables(actualClass, baseClass, baseClassTypeVariableIndexes);
    }
}