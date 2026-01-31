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
import app.packed.bean.BeanTrigger.OnAnnotatedMethod;
import internal.app.packed.extension.base.BaseExtensionBeanIntrospector;
import internal.app.packed.lifecycle.LifecycleOperationHandle.StartOperationHandle;

/**
 * An annotation used to indicate that a method should be invoked whenever the bean reaches the
 * {@link RunState#STARTING} state.
 *
 * <p>
 * A simple usage example:
 *
 * <pre>
 * &#064;OnStart()
 * public void executeOnBeanStart() {
 *     IO.println(&quot;Bean is starting&quot;);
 * }
 * </pre>
 *
 * Methods annotated with OnStart can have any service that is also available from the component. For example, the
 * following method will print out the name and the state of the container when it starts.
 *
 * <pre>
 * &#064;OnStart()
 * public void hello(Container container) {
 *     IO.println(&quot;The current state of container &quot; + container.getName() + &quot; is &quot; + container.getState());
 * }
 * </pre>
 * <p>
 * A common usage examples is initializing services with data:
 *
 * For example, fetching it from disk.
 *
 * <pre>
 * &#064;OnStart()
 * public void hello() throws IOException {
 *     // load data from disk
 *     // prepare service for use
 * }
 * </pre>
 *
 * Notice that this method throws a checked exception. Any method that throws an Exception will result in the
 * application failing to start. After which the application will automatically move to the shutdown phase.
 * <p>
 * Normally services are not available from xx until all services have been successfully started. However, by using this
 * annotation. Services that not yet completed startup can be injected. It is up to the user to make sure that invoking
 * method on instances that injected this does not cause any problems. For example, calling a method on another service
 * that only works when the container is in the running phase.
 * <p>
 * If a bean has multiple methods annotated with {@code @Start}, the order in which they are invoked is undefined.
 * If a specific invocation order is required, use a single {@code @Start} method that calls the other methods
 * in the desired order.
 *
 * @see StartContext
 * @see StartOperationMirror
 * @see StartOperationConfiguration
 * @see Initialize
 * @see Stop
 */
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@OnAnnotatedMethod(introspector = StartBeanIntrospector.class, requiresContext = StartContext.class, allowInvoke = true)
public @interface Start {

    /**
     *
     * {@return whether or not a new thread should be forked when running the annotated method.}
     * <p>
     * {@link StartContext} provides a number of special ways to fork.
     * @see StartContext#fork(java.util.concurrent.Callable)
     */
    boolean fork() default false;

    /**
     * If the annotated method is still running when the lifetime is stopped (for example, if another bean failed to start
     * properly). This attribute can be used indicate to the framework that the thread executing the operation should be
     * interrupted by the framework.
     *
     * @return
     */
    boolean interruptOnStopping() default true;

    // forkMode
    // forkAsDaemon (keepRunning) = Mark the bean as keep running. Don't await

    /**
     * Controls the execution order of this start method relative to beans that depend on this bean.
     * <p>
     * The concept of "natural order" is borrowed from {@link java.util.Comparator#naturalOrder()}. Just as
     * {@code Comparator.naturalOrder()} represents the default, expected ordering for comparable elements,
     * {@code naturalOrder=true} represents the default, expected ordering for lifecycle operations.
     * Setting {@code naturalOrder=false} reverses this order, similar to using {@code Comparator.reverseOrder()}.
     * <p>
     * <b>For {@code @Start}, natural order means:</b> This method runs <em>before</em> any beans that
     * depend on this bean are started. This ensures that when a dependent bean starts, all of its
     * dependencies are already running and available.
     * <p>
     * <b>Example with {@code naturalOrder=true} (default):</b> If Bean A depends on Bean B:
     * <ol>
     *   <li>Bean B's {@code @Start} method runs first</li>
     *   <li>Bean A's {@code @Start} method runs second</li>
     * </ol>
     * <p>
     * <b>Using {@code naturalOrder=false} (coordinator pattern):</b> Setting {@code naturalOrder=false}
     * causes the method to run <em>after</em> all dependent beans have started. This is useful for
     * coordinators that need to act once all their dependants are up and running.
     * <p>
     * <b>Example:</b> A task coordinator that dispatches work only after all workers are running:
     * <pre>{@code
     * class TaskCoordinator {
     *     private List<Worker> workers = new ArrayList<>();
     *
     *     public void addWorker(Worker w) { workers.add(w); }
     *
     *     @Start(naturalOrder = false)
     *     void beginDispatching() {
     *         // All workers are now started and ready
     *         workers.forEach(Worker::enableTaskProcessing);
     *     }
     * }
     *
     * class DataWorker implements Worker {  // depends on TaskCoordinator
     *     @Inject
     *     void register(TaskCoordinator coordinator) {
     *         coordinator.addWorker(this);
     *     }
     *
     *     @Start
     *     void start() { ... }
     * }
     * }</pre>
     * <p>
     * With this setup:
     * <ol>
     *   <li>All DataWorker instances start and are ready to process tasks</li>
     *   <li>TaskCoordinator.beginDispatching() runs after all workers are started</li>
     * </ol>
     * <p>
     * This attribute has no effect if no other beans depend on the targeted bean.
     *
     * @return {@code true} (default) to run before dependants start,
     *         {@code false} to run after dependants start (coordinator pattern)
     *
     * @see Initialize#naturalOrder()
     * @see Stop#naturalOrder()
     */
    boolean naturalOrder() default true;

    // String phase.

    // BeforeDependants, Before_Dependants_FORKED, AFTER_DEPENDANTS // Late must be

    // Mode, EAGER, EAGER_FORK, LATE
    public enum ForkMode {
        AFTER_DEPENDANTS,

        BEFORE_DEPENDANTS,

        DISABLED,

        FORK_AFTER_DEPENDANTS_JOIN_BEFORE_START,

        FORK_BEFORE_DEPENDANTS_JOIN_AFTER_DEPENDANTS,

        FORK_BEFORE_DEPENDANTS_JOIN_BEFORE_START,

        // On the way back. Ved ikke om den giver mening
        // Tror simpelthen ikke den giver mening
        // Basalt set fungere den som @OnStop(reverse order) -> task.join
        JOIN_AFTER_DEPENDANTS, // Await on all dependants having started

        // Tror vi har en java api her???
        // Ignore dependencies, but before Application is marked as running
        JOIN_BEFORE_START,
    }
}

final class StartBeanIntrospector extends BaseExtensionBeanIntrospector {
    @Override
    public void onAnnotatedMethod(Annotation annotation, BeanIntrospector.OnMethod method) {
        StartOperationHandle.install((Start) annotation, method);
    }
}