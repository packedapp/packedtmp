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
package app.packed.cli;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.packed.build.BuildException;
import app.packed.extension.Extension;
import app.packed.hooks.MethodHook;
import app.packed.hooks.accessors.RealMethodSidecarBootstrap;
import app.packed.service.ServiceExtension;
import packed.internal.application.ApplicationSetup;
import packed.internal.application.ApplicationSetup.MainThreadOfControl;
import packed.internal.component.bean.BeanSetup;
import packed.internal.hooks.usesite.UseSiteMethodHookModel;

/**
 * Trying to build an application with more than a single method annotated with this annotation will fail with
 * {@link BuildException}.
 * <p>
 * If the container fails to start, the method will never be invoked.
 * <p>
 * When the annotated method returns the container will automatically be stopped. If the annotated method fails with an
 * unhandled exception the container will automatically be shutdown with the exception being the cause.
 * <p>
 * Annotated methods will never be invoked more than once??? Well if we have some retry mechanism
 */
// A single method. Will be executed.
// and then shutdown container down again
// Panic if it fails???? or do we not wrap exception??? I think we wrap...
// We always wrap in container panic exception
// @EntryPoint
// What happens with CLI
// We can have multiple entry points
// Some of them deamons and some of them not...
// Det er maaske mere noget med state end kun container...
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@MethodHook(bootstrap = MySidecar.class, extension = Extension.class)
// I think this creates a job...
//
public @interface Main {
    // forceSpawnThread???

    // It can be the same thread, but I think it is a different request.
    // spawnRequest = true;
}

class MySidecar extends RealMethodSidecarBootstrap {

    static {
        $requireRunnableApplication();
    }

    /** {@inheritDoc} */
    @Override
    protected void bootstrap() {
        MethodHandle mh = methodHandle();
        Method m = method();
        UseSiteMethodHookModel.Builder.registerProcessor(this, c -> {
            // Okay we do not automatically
            // Der er noget constant pool thingeling der ikke bliver kaldt
            // ordentligt hvis der ikke er registered en ServiceManagerSetup
            c.container.useExtension(ServiceExtension.class);
            MainThreadOfControl mc = c.application.mainThread();
            mc.isStatic = Modifier.isStatic(m.getModifiers());
            mc.cs = (BeanSetup) c;
            mc.methodHandle = mh;
        });
    }
    protected void onInit(ApplicationSetup application, Runnable r) {
        // application.setup...
    }

//    @OnInitialize
//    protected void onInit(Runnable r) {
//
//    }
}

// Skal det styres paa shell niveau?? Eller wirelet niveau..
// Tror det bliver styres paa runtime niveau
// boolean spawnThread() default false;

// remainRunning, nej det er sgu en anden annotering
// We will gerne kunne foresporge om en Container har en Computer
// Det har en semantics betydning. Det har et request
// boolean stopOnSucces() default true;

// ExecutionResult<T>... Maaske bare CompletableFuture

// Vi skal have en maade hvorpaa en shell driver skal kunne faa et resultat.

// CLI vs Execute

// Cli must be void...