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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger.OnAnnotatedMethod;
import internal.app.packed.concurrent.daemon.DaemonJobOperationHandle;
import internal.app.packed.extension.base.BaseExtensionBeanIntrospector;

/**
 * Will have a dedicated thread.
 *
 * @see DaemonConfiguration
 * @see DaemonContext
 * @see DaemonMirror
 */
// A daemon is not a job, It kind of should be to be honest
// Aaahaha moving back into job...

// The only reason I don't like the deaemon name. Is that it might not use a Daemon Thread.
// An app, that has a single daemon running should only exit if the daemon uses a daemon thread
// A daemon using a non-daemon thread

// Maybe

// BackgroundJob
// SystemJob
// WorkerJob
// SupportJob
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@OnAnnotatedMethod(allowInvoke = true, introspector = DaemonJobBeanIntrospector.class)
public @interface DaemonJob {

    String startup() default "When-exactly-are we starting";

    String shutdown() default "When exactly-are we stopping";

    // resume->Never, Always, backoff policy

    /**
     * {@return whether or not the thread executing the annotated method, should be interrupted when the bean is shutdown}
     */
    boolean interruptOnStop() default true; // Maybe enum, false, true, asPerANamespaceDefaults

    /**
     * <p>
     * If more precise control of what kind of thread is used for the daemon, a {@link java.util.concurrent.ThreadFactory}
     * can be configured using {@link DaemonConfiguration#threadFactory(java.util.concurrent.ThreadFactory)}.
     *
     * @return the type of thread using for running the daemon
     *
     * @see DaemonConfiguration#threadFactory(java.util.concurrent.ThreadFactory)
     * @see DaemonMirror#threadKind()
     */
    ThreadKind threadKind() default ThreadKind.DAEMON_THREAD;
}

final class DaemonJobBeanIntrospector extends BaseExtensionBeanIntrospector {

    /** {@inheritDoc} */
    @Override
    public void onAnnotatedMethod(Annotation annotation, BeanIntrospector.OnMethod method) {
        DaemonJobOperationHandle.onDaemonJobAnnotation(this, method, (DaemonJob) annotation);
    }
}

//Could return, RESTART, EXIT, SLEEP, CONTINUE

//Tror det er noget den annoterede metode kan returnere

interface DaemonJobAction {

    // The one problem here is cleanup...
    // Do we want a stop method? Probably not
    static DaemonJobAction sleep(long duration, TimeUnit timeUnit) {
        throw new UnsupportedOperationException();
    }
}