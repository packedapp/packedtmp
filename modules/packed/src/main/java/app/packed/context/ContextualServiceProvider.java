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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanTrigger;
import app.packed.extension.Extension;

/**
 *
 * <p>
 * If the type being provider is a generic type. It will match it independent on any actual types. There is no support
 * for refining this. It must be handled in the extension.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BeanTrigger
// Bliver ikke brugt hvis man er i namespace med en, bean overriddes, eller
public @interface ContextualServiceProvider {

    /**
     * The extension that will provide services of the annotated type.
     * <p>
     * The extension must be located in the same module as the type that is annotated with this annotation.
     */
    Class<? extends Extension<?>> extension();

    /**
     * Contexts that are required in order to use the binding class.
     * <p>
     * If no contexts are specified, the type can be used anywhere.
     * <p>
     * If this binding is attempted to be used without the context being available a
     * {@link app.packed.context.NotInContextException} will be thrown.
     * <p>
     *
     * If this method returns multiple contexts they will <strong>all</strong> be required.
     *
     * @return required contexts
     */
    Class<? extends Context<?>>[] requiresContext() default {};
}