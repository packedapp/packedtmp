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
package app.packed.concurrent.usage;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.application.App;
import app.packed.assembly.BaseAssembly;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger.OnAnnotatedMethod;
import app.packed.concurrent.JobExtension;
import app.packed.concurrent.oldscheduling.SchedulingContext;
import app.packed.extension.Extension;
import app.packed.extension.Extension.DependsOn;
import app.packed.extension.ExtensionHandle;

/**
 *
 */
public class ScTestOtherE extends BaseAssembly {

    public static void main(String[] args) throws InterruptedException {
        App.run(new ScTestOtherE());
        Thread.sleep(1000);
    }

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(MuB.class);
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @OnAnnotatedMethod(allowInvoke = true, introspector = MyE.MyI.class)
    public @interface ScheduleOther {
        String value();
    }

    @DependsOn(extensions = JobExtension.class)
    public static class MyE extends Extension<MyE> {

        /**
         * @param handle
         */
        protected MyE(ExtensionHandle<MyE> handle) {
            super(handle);
        }

        static class MyI extends BeanIntrospector<MyE> {

            @Override
            public void onAnnotatedMethod(Annotation hook, BeanIntrospector.OnMethod on) {
//                ScheduleOther so = (ScheduleOther) hook;
//                ScheduledOperationConfiguration soc = extensionHandle().use(JobExtensionPoint.class).schedule(null /* on.newDelegatingOperation() */);
//                Duration p = Duration.parse(so.value());
//                soc.setMillies((int) p.toMillis());
            }
        }
    }

    public static class MuB {

        @ScheduleOther("PT0.01S")
        public static void sch(SchedulingContext sc) {
            IO.println("SCHED " + sc.invocationCount());
        }
    }
}
