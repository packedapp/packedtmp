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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.extension.BaseExtension;
import app.packed.extension.BeanClassActivator.AnnotatedBeanVariableActivator;

/**
 * This annotation is used to indicate that the value of a annotated variable (field or parameter) of a bean is
 * constructed doing the code generation phase of the application.
 * <p>
 * Values for a specific bean must be provided either via {@link BaseExtensionPoint}
 * <p>
 * This annotation is only usable by extensions.
 *
 * @see BindableVariable#bindGeneratedConstant(java.util.function.Supplier)
 * @see BaseExtensionPoint#addCodeGenerated(app.packed.bean.BeanConfiguration, Class, java.util.function.Supplier)
 * @see BaseExtensionPoint#addCodeGenerated(app.packed.bean.BeanConfiguration, app.packed.bindings.Key,
 *      java.util.function.Supplier)
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@AnnotatedBeanVariableActivator(extension = BaseExtension.class)
// Do we want to allow lazy creation???? Like Leyden...
public @interface CodeGenerated {}