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
package app.packed.application.entrypoint;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.packed.build.BuildException;
import app.packed.extension.ExtensionMember;
import app.packed.hooks.BeanMethod;
import app.packed.hooks.accessors.RealMethodSidecarBootstrap;
import app.packed.inject.service.ServiceExtension;
import packed.internal.application.ApplicationSetup;
import packed.internal.application.EntryPointSetup;
import packed.internal.application.EntryPointSetup.MainThreadOfControl;
import packed.internal.bean.BeanSetup;
import packed.internal.hooks.usesite.UseSiteMethodHookModel;

/**
 * Trying to build an application with more than a single method annotated with this annotation will fail with
 * {@link BuildException}.
 * <p>
 * Methods annotated with {@code @Main} must have a void return type.
 * <p>
 * If the application fails either at initialization time or startup time the annotated will not be invoked.
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
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtensionMember(EntryPointExtension.class)
@BeanMethod.Hook(bootstrap = MainBootstrap.class, extension = EntryPointExtension.class)
public @interface Main {}

class MainBootstrap extends RealMethodSidecarBootstrap {

    /** {@inheritDoc} */
    @Override
    protected void bootstrap() {
        MethodHandle mh = methodHandle();
        Method m = method();
        UseSiteMethodHookModel.Builder.registerProcessor(this, c -> {
            // Okay we do not automatically
            // Der er noget constant pool thingeling der ikke bliver kaldt
            // ordentligt hvis der ikke er registered en ServiceManagerSetup

            EntryPointExtension e = c.parent.useExtension(EntryPointExtension.class);
            e.hasMain = true;
            c.parent.useExtension(ServiceExtension.class);
            c.application.entryPoints = new EntryPointSetup();
            MainThreadOfControl mc = c.application.entryPoints.mainThread();
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
