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
package app.packed.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanHook.AnnotatedVariableHook;
import app.packed.bean.BeanIntrospector.BindableVariable;

/**
 * This annotation is used to indicate that the variable is constructed doing the code generation phase of the
 * application.
 * <p>
 * Man kan selvfoelgelig kun bruge den paa
 * 
 * <p>
 * This annotation can only used on beans owned by an extension.
 * 
 * @see BindableVariable#bindToGenerated(java.util.function.Supplier)
 * @see Extension#addCodeGenerated(app.packed.bean.BeanConfiguration, Class, java.util.function.Supplier)
 * @see Extension#addCodeGenerated(app.packed.bean.BeanConfiguration, app.packed.binding.Key,
 *      java.util.function.Supplier)
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@AnnotatedVariableHook(extension = BaseExtension.class)
public @interface CodeGenerated {}
