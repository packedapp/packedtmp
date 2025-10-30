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

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanLifetime;
import app.packed.bean.scanning.BeanIntrospector;
import app.packed.bean.scanning.BeanTrigger.OnAnnotatedField;
import app.packed.bean.scanning.BeanTrigger.OnAnnotatedMethod;
import app.packed.build.BuildException;
import app.packed.namespace.sandbox.NamespaceMetaAnnotation;
import internal.app.packed.extension.BaseExtensionBeanIntrospector;
import internal.app.packed.service.ServiceProvideOperationHandle;

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
@NamespaceMetaAnnotation
@OnAnnotatedMethod(introspector = ProvideBeanIntrospector.class, allowInvoke = true)
@OnAnnotatedField(introspector = ProvideBeanIntrospector.class, allowGet = true)
// Hvis vi laver meta annoteringen, skal vi jo naesten lave den om til en repeatable..
// Syntes godt man maa smide flere pa
public @interface Provide {
    // What about extensions? There name is FooExtension#main
    // Maybe just have it empty? and then ->Main | FooExtension#Main
    String namespace() default NamespaceMetaAnnotation.DEFAULT_NAMESPACE;
}

final class ProvideBeanIntrospector extends BaseExtensionBeanIntrospector {

    /** {@inheritDoc} */
    @Override
    public void onAnnotatedField(Annotation annotation, OnField onField) {
        if (!Modifier.isStatic(onField.modifiers())) {
            if (beanKind() != BeanLifetime.SINGLETON) {
                throw new BuildException("Not okay)");
            }
        }
        ServiceProvideOperationHandle.install((Provide) annotation, onField);
    }

    /** {@inheritDoc} */
    @Override
    public void onAnnotatedMethod(Annotation annotation, BeanIntrospector.OnMethod method) {
        if (!Modifier.isStatic(method.modifiers())) {
            if (beanKind() != BeanLifetime.SINGLETON) {
                throw new BeanInstallationException("Not okay)");
            }
        }
        ServiceProvideOperationHandle.install((Provide) annotation, method);
    }
}