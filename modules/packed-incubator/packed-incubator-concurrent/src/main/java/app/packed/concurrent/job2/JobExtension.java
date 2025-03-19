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
package app.packed.concurrent.job2;

import java.lang.annotation.Annotation;
import java.time.Duration;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.scanning.BeanIntrospector;
import app.packed.binding.Key;
import app.packed.concurrent.annotations.ScheduleJob;
import app.packed.concurrent.job2.impl.ScheduledOperationHandle;
import app.packed.concurrent.other.SchedulingContext;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.ExtensionPoint.ExtensionUseSite;
import app.packed.extension.FrameworkExtension;
import internal.app.packed.concurrent.ScheduleImpl;
import internal.app.packed.concurrent.SchedulingTaskManager;
import internal.app.packed.concurrent.ThreadNamespaceHandle;

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
public class JobExtension extends FrameworkExtension<JobExtension> {

    /**
     * @param handle
     */
    JobExtension(ExtensionHandle<JobExtension> handle) {
        super(handle);
    }

    /** Creates a new thread extension. */

    BeanConfiguration newSchedulingBean() {
        BeanConfiguration b = provide(SchedulingTaskManager.class);
//        b.bindServiceInstance(ExecutorConfiguration.class, main().scheduler);
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

    public static class MyI extends BeanIntrospector<JobExtension> {

        @Override
        public void onExtensionService(Key<?> key, OnContextService service) {
            OnVariableUnwrapped binding = service.binder();

            Class<?> hook = key.rawType();
            if (hook == SchedulingContext.class) {
                binding.bindInvocationArgument(1);
            } else {
                super.onExtensionService(key, service);
            }
        }

        @Override
        public void onAnnotatedMethod(Annotation hook, BeanIntrospector.OnMethod on) {
            if (hook instanceof ScheduleJob schedule) {
                // Parse the schedule
                ScheduleImpl s = new ScheduleImpl(Duration.ofMillis(schedule.withFixedDelay()));

                // Find the namespace we are installing the operation into
                ThreadNamespaceHandle namespace = null;// main();

                // Install the operation
                ScheduledOperationHandle h = on.newOperation(ScheduledOperationHandle.SCHEDULING_OPERATION_TEMPLATE).install(namespace,
                        ScheduledOperationHandle::new);

                // Configure the handle
                h.s = s;
            } else {
                super.onAnnotatedMethod(hook, on);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected ExtensionPoint<JobExtension> newExtensionPoint(ExtensionUseSite usesite) {
        return new JobExtensionPoint(usesite);
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
