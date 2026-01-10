/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.lifecycle;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger;
import app.packed.namespace.sandbox.NamespaceOperation;
import internal.app.packed.extension.base.BaseExtensionBeanIntrospector;
import internal.app.packed.lifecycle.LifecycleOperationHandle.InjectOperationHandle;

/**
 * Unlike many other popular dependency injection frameworks. There are usually no requirements in Packed to use
 * <code>@Inject</code> annotations on the constructor or method that must have dependencies injected. However, in some
 * situations an annotation can be used for providing greater control over how dependencies are being injected.
 * <p>
 * One such example is if a dependency should only be injected if it is available. Injecting {@code null} instead of
 * throwing an exception
 * <p>
 * While we support direct injection onto fields and into methods. We recommend using constructor injection where ever
 * possible. List reasons...
 * <p>
 * A field or method annotated with {@code @Inject} is represented with {@link InitializeOperationMirror} in the mirror
 * API.
 * <p>
 * When used on methods this annotation is closely related to the {@link Initialize} annotation. If you are only
 * injecting services into a bean, use {@link Inject}. If you are performing any kind of computation, use
 * {@link Initialize}. Methods annotated with {@link Inject} will always be executed before methods annotated with
 * {@link Initialize}.
 *
 * <p>
 * The annotation can also be applied to
 *
 * <p>
 * Injection of services into static fields or method are not supported. There is no general support for injecting into
 * static fields or methods. If you absolutely need it, it is fairly easy to support yourself
 */
@Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NamespaceOperation
@BeanTrigger.OnAnnotatedField(introspector = InjectBeanIntrospector.class, allowSet = true)
@BeanTrigger.OnAnnotatedMethod(introspector = InjectBeanIntrospector.class, allowInvoke = true)
public @interface Inject {}

final class InjectBeanIntrospector extends BaseExtensionBeanIntrospector {
    @Override
    public void onAnnotatedField(Annotation annotation, OnField onField) {
        InjectOperationHandle.install((Inject) annotation, onField);
    }

    @Override
    public void onAnnotatedMethod(Annotation annotation, BeanIntrospector.OnMethod onMethod) {
        InjectOperationHandle.install((Inject) annotation, onMethod);
    }
}