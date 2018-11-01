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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

import app.packed.inject.Dependency;

/**
 * An executable descriptor.
 * <p>
 * Unlike the {@link Executable} class, this interface contains no mutable operations, so it can be freely shared.
 */
public interface ExecutableDescriptor extends Member, AnnotatedElement {

    /**
     * Returns the number of formal parameters (whether explicitly declared or implicitly declared or neither) for the
     * underlying executable.
     *
     * @return The number of formal parameters for the method this object represents
     * @return
     *
     * @see Executable#getParameterCount()
     * @see Method#getParameterCount()
     * @see Constructor#getParameterCount()
     */
    int getParameterCount();

    /**
     * Returns a list of dependencies matching the parameters of this executable.
     *
     * @return a dependency list
     * @throws RuntimeException
     *             if a dependency list could not be created. For example, if there are two qualifiers on a parameter.
     */
    List<Dependency> toDependencyList();
}
