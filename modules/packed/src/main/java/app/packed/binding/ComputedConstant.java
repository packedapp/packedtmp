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
package app.packed.binding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanTrigger.AnnotatedVariableBeanTrigger;
import app.packed.extension.BaseExtension;

/**
 * This annotation is used to indicate that the value of a annotated variable (field, parameter or type parameter) of a
 * bean is injected with a constant that is generated exactly once at build time (or just in time before the bean is
 * first instantiated).
 * <p>
 * Values for a specific bean must be provided either via {@link BaseExtensionPoint}
 * <p>
 * This annotation is only usable by extensions.
 *
 * @see BeanConfiguration#addComputedConstant(Class, java.util.function.Supplier)
 * @see BeanConfiguration#addComputedConstant(app.packed.util.Key, java.util.function.Supplier)
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@AnnotatedVariableBeanTrigger(extension = BaseExtension.class)
// Was @CodeGenerated, @GeneratedConstant
// BuildConstant???

// Maybe it is simply. BeanConfiguration.bindCodegeneratedConstant(Key, Supplier)???
// And then we do not have this annotation???
// It is nice documentation though. Ohh we only generate this once
// But then again we may have other constants where you could just start doubting
// For example some of the new host/guest which is also computed

// Remove this and used ordinary bean service injection
public @interface ComputedConstant {}