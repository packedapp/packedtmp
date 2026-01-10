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
package app.packed.bean;

/** This enum represents the various type of bean sources that can be used when installing a bean. */
public enum BeanSourceKind {

    /**
     * A {@link Class} was specified when installing the bean.
     *
     * @see BaseExtension#install(Class)
     * @see BaseExtension#installStatic(Class)
     */
    CLASS,

    /**
     * A bean instance was specified when installing the bean.
     *
     * @see BaseExtension#installInstance(Object)
     */
    INSTANCE,

    /**
     * An {@link Op} was specified when installing the bean.
     *
     * @see BaseExtension#install(Op)
     */
    OP,

    /** A bean that has been synthetically created or modified from another bean    . */
    SYNTHETIC,

    /**
     * No source was specified when installing the bean.
     * <p>
     * Sourceless beans always returns {@code void.class} as its bean class.
     */
    SOURCELESS;
}
