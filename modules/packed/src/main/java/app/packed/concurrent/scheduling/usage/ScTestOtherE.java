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
package app.packed.concurrent.scheduling.usage;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;

import app.packed.application.App;
import app.packed.bean.BeanElement.BeanMethod;
import app.packed.bean.BeanHook.AnnotatedMethodHook;
import app.packed.bean.BeanIntrospector;
import app.packed.concurrent.scheduling.ScheduledOperationConfiguration;
import app.packed.concurrent.scheduling.SchedulingContext;
import app.packed.concurrent.scheduling.SchedulingExtension;
import app.packed.concurrent.scheduling.SchedulingExtensionPoint;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.extension.Extension.DependsOn;
import app.packed.operation.BeanOperationTemplate;

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
    @AnnotatedMethodHook(allowInvoke = true, extension = MyE.class)
    public @interface ScheduleOther {
        String value();
    }

    @DependsOn(extensions = SchedulingExtension.class)
    public static class MyE extends Extension<MyE> {
        MyE() {}

        @Override
        protected BeanIntrospector newBeanIntrospector() {
            return new BeanIntrospector() {

                @Override
                public void hookOnAnnotatedMethod(Annotation hook, BeanMethod on) {
                    ScheduleOther so = (ScheduleOther) hook;
                    ScheduledOperationConfiguration soc = use(SchedulingExtensionPoint.class)
                            .schedule(on.newOperation(BeanOperationTemplate.defaults().withArg(SchedulingContext.class)));
                    Duration p = Duration.parse(so.value());
                    soc.setMillies((int) p.toMillis());
                }
            };
        }
    }

    public static class MuB {

        @ScheduleOther("PT0.01S")
        public static void sch(SchedulingContext sc) {
            System.out.println("SCHED " + sc.invocationCount());
        }
    }
}
