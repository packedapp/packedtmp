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
package app.packed.concurrent;

import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanElement.BeanMethod;
import app.packed.bean.BeanIntrospector;
import app.packed.binding.Key;
import app.packed.binding.UnwrappedBindableVariable;
import app.packed.context.Context;
import app.packed.context.ContextTemplate;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.ExtensionPoint.ExtensionUseSite;
import app.packed.extension.FrameworkExtension;
import app.packed.operation.OperationTemplate;
import internal.app.packed.concurrent.ExecutorConfiguration;
import internal.app.packed.concurrent.ScheduleImpl;
import internal.app.packed.concurrent.SchedulingTaskManager;

/**
 * This extension allows for multiple threads within an application.
 * <p>
 * There is nothing forcing anyone to use this extension for forking threads.
 *
 */

// Er det bare på BaseExtension?? Det taenker jeg

// Controls everything about threads...

// We can have a concurrency extension as well that is more.
// What kind of model do you use...

// Vi vil gerne have vores egne executors. Saa de bliver lukket ned
// naar containeren bliver shutdown...

// Must be enabled for Fork

// Hvad hvis lukker den ned asynchront indefra containeren??

// Maybe it is built-in..
// Can't fanthom many applications not threads
public class ThreadExtension extends FrameworkExtension<ThreadExtension> {

    /**
     * @param handle
     */
    ThreadExtension(ExtensionHandle<ThreadExtension> handle) {
        super(handle);
    }

    /** A context template. */
    static final ContextTemplate SCHEDULING_CONTEXT_TEMPLATE = ContextTemplate.of(SchedulingContext.class, c -> {});

    static final ContextTemplate DAEMON_CONTEXT_TEMPLATE = ContextTemplate.of(DaemonContext.class, c -> {});

    static final OperationTemplate SCHEDULING_OPERATION_TEMPLATE = OperationTemplate.defaults()
            .reconfigure(c -> c.inContext(SCHEDULING_CONTEXT_TEMPLATE).returnIgnore());

    static final OperationTemplate DAEMON_OPERATION_TEMPLATE = OperationTemplate.defaults()
            .reconfigure(c -> c.inContext(DAEMON_CONTEXT_TEMPLATE).returnIgnore());

    private BeanConfiguration schedulingBean;

    /** Creates a new thread extension. */

    BeanConfiguration initSchedulingBean() {
        BeanConfiguration b = schedulingBean;
        if (b == null) {
            b = schedulingBean = provide(SchedulingTaskManager.class);
            b.bindServiceInstance(ExecutorConfiguration.class, main().scheduler);
        }
        return b;
    }

    /**
     * Schedules an operation.
     *
     * @param op
     *            the operation that will be invoked
     * @return a configuration object representing the scheduled operation
     */
//    public ScheduledOperationConfiguration schedule(Op<?> op) {
//        throw new UnsupportedOperationException();
//    }

    @Override
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {

            @Override
            public void onContextualServiceProvision(Key<?> key, Class<?> actualHook, Set<Class<? extends Context<?>>> contexts, UnwrappedBindableVariable v) {
                Class<?> hook = key.rawType();
                if (hook == SchedulingContext.class || hook == DaemonContext.class) {
                    v.bindInvocationArgument(1);
                } else {
                    super.onContextualServiceProvision(key, actualHook, contexts, v);
                }
            }

            @Override
            public void onAnnotatedMethod(Annotation hook, BeanMethod on) {
                if (hook instanceof ScheduleRecurrent schedule) {
                    // Parse the schedule
                    ScheduleImpl s = new ScheduleImpl(Duration.ofMillis(schedule.millies()));

                    // Find the namespace we are installing the operation into
                    ThreadNamespaceHandle namespace = main();

                    // Install the operation
                    ScheduledOperationHandle h = on.newOperation(SCHEDULING_OPERATION_TEMPLATE).install(namespace, ScheduledOperationHandle::new);

                    // Configure the handle
                    h.s = s;
                } else if (hook instanceof Daemon daemon) {
                    // Find the namespace we are installing the operation into
                    ThreadNamespaceHandle namespace = main();

                    // Install the operation
                    DaemonOperationHandle h = on.newOperation(DAEMON_OPERATION_TEMPLATE).install(namespace, DaemonOperationHandle::new);

                    // Configure the handle
                    h.useVirtual = daemon.useVirtual();
                } else {
                    super.onAnnotatedMethod(hook, on);
                }
            }
        };
    }

    // Altssa Hvor brugbare er de her uden at man kan faa fat i andet en den context????
    public DaemonOperationConfiguration fDaemon(Consumer<DaemonContext> action) {
        return namespace().addDaemon(action);
    }

    public ThreadNamespaceConfiguration namespace() {
        return main().configuration(this);
    }

    private ThreadNamespaceHandle main() {
        return namespaceLazy(ThreadNamespaceHandle.TEMPLATE, "main", c -> c.install(ThreadNamespaceHandle::new));
    }

    /** {@inheritDoc} */
    @Override
    protected ExtensionPoint<ThreadExtension> newExtensionPoint(ExtensionUseSite usesite) {
        return new ThreadExtensionPoint(usesite);
    }

}

// disableThis
// disableThis+Kids
// disableThis for everyone except LifecycleExtension
// disableThis for user

// Starting Threads
// Monitoring threads
// Thread limits

// Scheduling???
// As in Thread Scheduling???

// I think yes
// IDK maybe a scheduler.

// ForkedOperation (bean, operationId)
