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
import internal.app.packed.extension.base.BaseExtensionBeanIntrospector;
import internal.app.packed.lifecycle.LifecycleOperationHandle.StopOperationHandle;

/**
 * An annotation used to indicate that a particular method should be invoked whenever the declaring entity reaches the
 * {@link RunState#STOPPING} state.
 * <p>
 * Static methods annotated with OnStop are ignore.
 * <p>
 * If a bean has multiple methods annotated with {@code @Stop}, the order in which they are invoked is undefined.
 * If a specific invocation order is required, use a single {@code @Stop} method that calls the other methods
 * in the desired order.
 *
 * @see OnInitialize
 * @see OnStart
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
// Some examples:
// https://stackoverflow.com/questions/26547532/how-to-shutdown-a-spring-boot-application-in-a-correct-way
//https://www.smilecdr.com/our-blog/the-pros-and-cons-of-spring-smartlifecycle

// Channels -> Notification: Notifiers friends and families about the pending shutdown
// Do the actual shutdown
// Notifaction again: Shit has been shutdown
@BeanTrigger.OnAnnotatedMethod(introspector = StopBeanIntrospector.class, requiresContext = StopContext.class, allowInvoke = true)
public @interface Stop {

//    // What is the usecase?
//    boolean onlyOnApplicationStop() default false;

    boolean fork() default false;

    /**
     * Controls the execution order of this stop method relative to beans that depend on this bean.
     * <p>
     * The concept of "natural order" is borrowed from {@link java.util.Comparator#naturalOrder()}. Just as
     * {@code Comparator.naturalOrder()} represents the default, expected ordering for comparable elements,
     * {@code naturalOrder=true} represents the default, expected ordering for lifecycle operations.
     * Setting {@code naturalOrder=false} reverses this order, similar to using {@code Comparator.reverseOrder()}.
     * <p>
     * <b>For {@code @Stop}, natural order means:</b> This method runs <em>after</em> any beans that
     * depend on this bean have stopped. This is the reverse of {@link Initialize} and {@link Start},
     * reflecting the natural shutdown pattern: dependants should release their dependencies before
     * the dependencies themselves shut down.
     * <p>
     * <b>Example with {@code naturalOrder=true} (default):</b> If Bean A depends on Bean B:
     * <ol>
     *   <li>Bean A's {@code @Stop} method runs first (the dependant stops first)</li>
     *   <li>Bean B's {@code @Stop} method runs second (the dependency stops last)</li>
     * </ol>
     * <p>
     * This mirrors how resources are typically managed: if A depends on B, then A must stop using B
     * before B can safely shut down.
     * <p>
     * <b>Using {@code naturalOrder=false} (pre-notification pattern):</b> Setting {@code naturalOrder=false}
     * causes the method to run <em>before</em> dependent beans have stopped. This is useful for
     * broadcasting shutdown notifications while dependants are still active.
     * <p>
     * <b>Example:</b> A client that notifies users before its connection shuts down:
     * <pre>{@code
     * class ChatClient {  // depends on ConnectionPool
     *     @Inject ConnectionPool pool;
     *
     *     @Stop(naturalOrder = false)
     *     void notifyShutdown() {
     *         // ConnectionPool is still running, we can send messages
     *         pool.broadcast("System shutting down");
     *     }
     * }
     *
     * class ConnectionPool {
     *     @Stop
     *     void closeConnections() { ... }
     * }
     * }</pre>
     * <p>
     * With this setup:
     * <ol>
     *   <li>ChatClient.notifyShutdown() runs while ConnectionPool is still active</li>
     *   <li>ConnectionPool.closeConnections() runs after the notification is sent</li>
     * </ol>
     * <p>
     * This attribute has no effect if no other beans depend on the targeted bean.
     *
     * @return {@code true} (default) to run after dependants stop,
     *         {@code false} to run before dependants stop (pre-notification pattern)
     *
     * @see Initialize#naturalOrder()
     * @see Start#naturalOrder()
     */
    boolean naturalOrder() default true;

    // Timeout?
    public enum ForkPolicy {
        FORK, FORK_AWAIT_AFTER_DEPENDENCIES, NO_FORK;
    }
}

final class StopBeanIntrospector extends BaseExtensionBeanIntrospector {
    @Override
    public void onAnnotatedMethod(Annotation annotation, BeanIntrospector.OnMethod method) {
        StopOperationHandle.install((Stop) annotation, method);
    }
}
