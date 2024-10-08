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
package app.packed.context;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.scanning.BeanTrigger;
import app.packed.extension.Extension;
import app.packed.service.advanced.ServiceResolver.NoContext;

/**
 * A version of the {@link ContextualServiceProvider} annotation that is {@link Inherited}. All other functionality is
 * identical.
 * <p>
 * NOTE: Remember, inherited annotations are not inherited on interfaces. So you an abstract class if you need to design
 * a inheritance hierarchy.
 *
 * @see ContextualServiceProvider
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BeanTrigger
@Inherited
public @interface InheritableContextualServiceProvider {

    /**
     * The extension that will provide services of the annotated type.
     * <p>
     * The extension must be located in the same module as the type that is annotated with this annotation.
     */
    Class<? extends Extension<?>> extension();

    /**
     * Contexts that are required in order to use the binding class.
     * <p>
     * If this binding is attempted to be used without the context being available an {@link UnavilableContextException} will
     * be thrown.
     * <p>
     * If this method returns multiple contexts they will <strong>all</strong> be required.
     *
     * @return required contexts
     */
    Class<? extends Context<?>> context() default NoContext.class;
}