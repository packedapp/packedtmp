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
package app.packed.bean.lifecycle;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.lifecycle.Initialize.Introspector;
import app.packed.bean.scanning.BeanIntrospector;
import app.packed.bean.scanning.BeanTrigger;
import app.packed.extension.BaseExtension;
import internal.app.packed.extension.InternalBeanIntrospector;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.BeanInitializeOperationHandle;

/**
 * Indicates that the annotated method must be invoked as part of the targeted bean's initialization.
 * <p>
 *
 * The Initialization process of a bean is as follows:
 *
 * <ul>
 * <li><b>Bean Instantiation:</b> The bean is instantiated if needed. Beans that have been installed, for example, via
 * {@link BaseExtension#installInstance(Object)} or {@link BaseExtension#installStatic(Class)} will not ignore this
 * step. it was not previously installed as an instance or as a static bean.</li>
 *
 * <li><b>Bean Injection:</b> Dependencies of the bean can be injected using the {@code @Inject} annotation, applied to
 * fields or methods. Methods that are annotated with {@code @Inject} should not perform any logic except for storing
 * the dependencies in a fields.</li>
 *
 * <li><b>Bean Initialization:</b> {@code @Initialize} annotation is used to mark method that the initialization
 * process. The initialization takes place in <i>natural order</i> (i.e., before any other beans that depend on this
 * bean are initialized).</li>
 * </ul>
 *
 * <p>
 * Methods using this annotation will be represented by a {@link OnInitializeOperationMirror} in the mirror API.
 * <p>
 * Any values returned by the annotated method is ignored by the runtime.
 * <p>
 * If an annotated methods throws an exception during initialization, the bean will fail to be put into action.
 * Depending on the lifetime of the bean, this may result in the application being unable to be initialized.
 * <p>
 * Methods (or fields) annotated with {@link Inject} on the same bean are always executed before methods annotated with
 * {@code OnInitialize}.
 * <p>
 * Attempting to install a bean (that uses this annotation) that do not have a lifecycle, for example, a static bean.
 * Will fail with {@link UnavailableLifecycleException}.
 * <p>
 * If multiple methods uses this annotation on the same bean and have the same value for the {@link #order()} attribute.
 * The framework may choose to invoke them in any sequence.
 *
 * @see Inject
 * @see OnStart
 * @see OnStop
 * @see InitializeOperationConfiguration
 * @see InitializeOperationMirror
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@BeanTrigger.OnAnnotatedMethod(introspector = Introspector.class, allowInvoke = true)
public @interface Initialize {

    /**
     * Controls the order in which beans in the same lifetime that depend on each other are initialized.
     * <p>
     * The default order is to invoke the annotated method <strong>before</strong> any other beans that depends on the
     * targeted bean are initialized.
     * <p>
     * This attribute has no effect if no other beans depend on the targeted bean.
     *
     * @return whether or not the annotated method should be run before or after dependent beans in the same lifetime are
     *         initialized.
     */
    boolean naturalOrder() default true;

    final class Introspector extends InternalBeanIntrospector<BaseExtension> {
        @Override
        public void onAnnotatedMethod(Annotation annotation, BeanIntrospector.OnMethod method) {
            BeanInitializeOperationHandle.install((Initialize) annotation, method);
        }
    }
}

//How do we say I want to run this after the application has finished installing
//Could have both application and bean as lifetime()
//I don't we support this.. We shouldn't start loose threads here...
//Maybe we will have an extension escape hatch to implement this

//I don't think we have a Context??? What can it specifically do???
//runAfterLifetimeInitialization, runAfterApplicationInitialize();

///**
// *
// */
//// Ogsaa tilgaengelig fra factory, constructor.
//
//// Den kan ikke rigtig noget. Maaske vi bare skal droppe den
//interface BeanInitializationContext extends Context<BaseExtension> {
//
//    /**
//     * @return
//     */
//    boolean isManaged();
//
//    /**
//     * @param action
//     * @throws if
//     *             already late
//     */
//    void runAfterDependencies(Runnable action);
//
//    // still not convinced
//    void runOnInitializationFailure(Runnable action);
//
//    // Den giver ingen mening vil jeg mene.
//    // Er det kun for application's lifetimen?
//    // Skal vel ikke have den for entity bean?
//    // For 3 timer siden genstartede applicationen
//    // Tror vi maa konfiguere applicationen til at gemme det
//    Optional<StopInfo> restartedFrom();
//}