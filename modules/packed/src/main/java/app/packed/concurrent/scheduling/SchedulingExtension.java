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
package app.packed.concurrent.scheduling;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanElement.BeanMethod;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanWrappedVariable;
import app.packed.concurrent.ThreadExtension;
import app.packed.concurrent.scheduling.ScheduledOperationConfiguration.Schedule;
import app.packed.context.ContextTemplate;
import app.packed.extension.BaseExtensionPoint.CodeGenerated;
import app.packed.extension.Extension;
import app.packed.extension.Extension.DependsOn;
import app.packed.extension.ExtensionContext;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.ExtensionPoint;
import app.packed.operation.BeanOperationTemplate;
import app.packed.operation.OperationHandle;

/**
 *
 */
@DependsOn(extensions = ThreadExtension.class)
public class SchedulingExtension extends Extension<SchedulingExtension> {

    List<ScheduledOperationConfiguration> ops = new ArrayList<>();

    /** Create a new scheduling extension. */
    SchedulingExtension() {}

    @Override
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {
            static final ContextTemplate TEMP = ContextTemplate.of(MethodHandles.lookup(), SchedulingContext.class, SchedulingContext.class);
            static final BeanOperationTemplate BOT = BeanOperationTemplate.defaults().withContext(TEMP).withReturnIgnore();

            @Override
            public void hookOnProvidedVariableType(Class<?> hook, BeanWrappedVariable v) {
                if (hook == SchedulingContext.class) {
                    v.bindInvocationArgument(1);
                } else {
                    super.hookOnProvidedVariableType(hook, v);
                }
            }

            @Override
            public void hookOnAnnotatedMethod(Annotation hook, BeanMethod on) {
                if (hook instanceof ScheduleRecurrent sr) {
                    OperationHandle oh = on.newOperation(BOT);
                    oh.specializeMirror(() -> new ScheduledOperationMirror());
                    ops.add(new ScheduledOperationConfiguration(new Schedule(Duration.ofMillis(sr.millies())), oh));
                } else {
                    super.hookOnAnnotatedMethod(hook, on);
                }
            }
        };
    }

    @Override
    protected ExtensionMirror<SchedulingExtension> newExtensionMirror() {
        return new SchedulingExtensionMirror();
    }

    @Override
    protected ExtensionPoint<SchedulingExtension> newExtensionPoint() {
        return new SchedulingExtensionPoint();
    }

    @Override
    protected void onAssemblyClose() {
        super.onAssemblyClose();
        if (isLifetimeRoot() && !ops.isEmpty()) {
            // TODO run through all ops and make sure they are scheduled

            // Install a scheduling bean that will handle the actual scheduling
            BeanConfiguration b = provide(SchedulingBean.class);

            // Gene
            base().addCodeGenerated(b, FinalSchedule[].class, () -> {
                return ops.stream().map(ScheduledOperationConfiguration::schedule).toArray(e -> new FinalSchedule[e]);
            });
        }
    }

    /**
    *
    */
    static final class FinalSchedule {

        final Schedule s;
        final MethodHandle callMe;

        FinalSchedule(Schedule s, MethodHandle callMe) {
            this.s = s;
            this.callMe = callMe;
        }
    }

    static class SchedulingBean {

        final PackedVirtualThreadScheduler vts;

        SchedulingBean(@CodeGenerated FinalSchedule[] mhs, ExtensionContext pec) {
            this.vts = new PackedVirtualThreadScheduler(pec);

            for (FinalSchedule p : mhs) {
                vts.schedule(p.callMe, p.s.d());
            }
        }
    }

}
