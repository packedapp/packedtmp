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
package app.packed.introspection;

import java.lang.reflect.Modifier;

/**
 *
 * @apiNote In the future, if the Java language permits, {@link MemberDescriptor} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
public interface MemberDescriptor {

    /**
     * Returns the Class object representing the class or interface that declares the member or constructor represented by
     * this Member.
     *
     * @return an object representing the declaring class of the underlying member
     */
    Class<?> getDeclaringClass();

    /**
     * Returns the simple name of the underlying member or constructor represented by this Member.
     *
     * @return the simple name of the underlying member
     */
    String getName();

    /**
     * Returns the Java language modifiers for the member or constructor represented by this Member, as an integer. The
     * Modifier class should be used to decode the modifiers in the integer.
     *
     * @return the Java language modifiers for the underlying member
     * @see Modifier
     */
    int getModifiers();

    /**
     * Returns {@code true} if this member was introduced by the compiler; returns {@code false} otherwise.
     *
     * @return true if and only if this member was introduced by the compiler.
     * @jls 13.1 The Form of a Binary
     * @since 1.5
     */
    boolean isSynthetic();
}
