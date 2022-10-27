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
package app.packed.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanExtensionPoint.FieldHook;
import app.packed.bean.BeanExtensionPoint.MethodHook;

/**
 * An annotation indicating that an annotated method or field on a bean provides a service to the container in which the
 * bean is installed. The key under which the service is registered is return type of the method or the field type
 * respectively.
 * 
 * <p>
 * Both fields and methods can make used of qualifiers to specify the exact key they are made available under. For
 * example, given to two qualifier annotations: {@code @Left} and {@code @Right}<pre>
 *  &#64;Left
 *  &#64;ProvideService
 *  String name = "left";
 *   
 *  &#64;Right
 *  &#64;ProvideService
 *  String provide() {
 *      return "right";
 *  }
 *  </pre>
 * 
 * The field will is made available with the key {@code Key<@Left String>} while the method will be made available with
 * the key {@code Key<@Right String>}.
 * <p>
 * Proving a null value, for example, via a null field or by returning null from a method is illegal unless the
 * dependency is optional.
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@MethodHook(allowInvoke = true, extension = ServiceExtension.class)
@FieldHook(allowGet = true, extension = ServiceExtension.class)
public @interface ProvideService {

    /**
     * Indicates whether or not the provided value is a constant.
     * <p>
     * Constant values are always eagerly constructed. For example, as part of the initialization of a container.
     * 
     * Constants may be cached by the runtime... I think we need to make some strong guarantees.
     * 
     * The provided does not have to be an constant in the way t. But from the perspectiv of the runtime. The instance
     * 
     * Constants may be cached
     * 
     * <p>
     * The default value is <code>false</code>
     * 
     * @return whether or not the provided value is a constant
     */
    /// Maaske har vi slet ikke constant....
    //// Maaske har vi en @ConstantProvide istedet for??? Eller Scope(Constant);
    boolean constant() default false;
}
