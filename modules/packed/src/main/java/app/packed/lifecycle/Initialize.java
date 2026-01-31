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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger;
import internal.app.packed.extension.base.BaseExtensionBeanIntrospector;
import internal.app.packed.lifecycle.LifecycleOperationHandle.InitializeOperationHandle;

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
 * If a bean has multiple methods annotated with {@code @Initialize}, the order in which they are invoked is undefined.
 * If a specific invocation order is required, use a single {@code @Initialize} method that calls the other methods
 * in the desired order.
 *
 * @see Inject
 * @see OnStart
 * @see OnStop
 * @see InitializeOperationConfiguration
 * @see InitializeOperationMirror
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@BeanTrigger.OnAnnotatedMethod(introspector = InitializeBeanIntrospector.class, allowInvoke = true)
public @interface Initialize {

    /**
     * Controls the execution order of this initialization method relative to beans that depend on this bean.
     * <p>
     * The concept of "natural order" is borrowed from {@link java.util.Comparator#naturalOrder()}. Just as
     * {@code Comparator.naturalOrder()} represents the default, expected ordering for comparable elements,
     * {@code naturalOrder=true} represents the default, expected ordering for lifecycle operations.
     * Setting {@code naturalOrder=false} reverses this order, similar to using {@code Comparator.reverseOrder()}.
     * <p>
     * <b>For {@code @Initialize}, natural order means:</b> This method runs <em>before</em> any beans that
     * depend on this bean are initialized. This ensures that when a dependent bean initializes, all of its
     * dependencies are already fully initialized and ready to use.
     * <p>
     * <b>Example with {@code naturalOrder=true} (default):</b> If Bean A depends on Bean B:
     * <ol>
     *   <li>Bean B's {@code @Initialize} method runs first</li>
     *   <li>Bean A's {@code @Initialize} method runs second</li>
     * </ol>
     * <p>
     * <b>Using {@code naturalOrder=false} (coordinator pattern):</b> Setting {@code naturalOrder=false}
     * causes the method to run <em>after</em> all dependent beans have been initialized. This is useful
     * for coordinator or aggregator beans that need to act once all their dependants are ready.
     * <p>
     * <b>Example:</b> A plugin manager that validates all plugins after they initialize:
     * <pre>{@code
     * class PluginManager {
     *     private List<Plugin> plugins = new ArrayList<>();
     *
     *     public void register(Plugin p) { plugins.add(p); }
     *
     *     @Initialize(naturalOrder = false)
     *     void afterAllPluginsReady() {
     *         // All plugins that registered themselves are now fully initialized
     *         plugins.forEach(Plugin::validate);
     *     }
     * }
     *
     * class ImagePlugin implements Plugin {  // depends on PluginManager
     *     @Inject
     *     void register(PluginManager manager) {
     *         manager.register(this);
     *     }
     *
     *     @Initialize
     *     void init() { ... }
     * }
     * }</pre>
     * <p>
     * With this setup:
     * <ol>
     *   <li>ImagePlugin (and other plugins) are constructed, injected, and initialized</li>
     *   <li>PluginManager.afterAllPluginsReady() runs after all plugins are ready</li>
     * </ol>
     * <p>
     * This attribute has no effect if no other beans depend on the targeted bean.
     *
     * @return {@code true} (default) to run before dependants initialize,
     *         {@code false} to run after dependants initialize (coordinator pattern)
     *
     * @see Start#naturalOrder()
     * @see Stop#naturalOrder()
     */
    boolean naturalOrder() default true;

}

final class InitializeBeanIntrospector extends BaseExtensionBeanIntrospector {
    @Override
    public void onAnnotatedMethod(Annotation annotation, BeanIntrospector.OnMethod method) {
        InitializeOperationHandle.install((Initialize) annotation, method);
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