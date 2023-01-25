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
import java.lang.invoke.MethodHandle;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanIntrospector;
import app.packed.concurrent.ScheduledOperationConfiguration.Schedule;
import app.packed.extension.BaseExtensionPoint.CodeGenerated;
import app.packed.extension.Extension;
import app.packed.extension.Extension.DependsOn;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.ExtensionPoint;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import internal.app.packed.lifetime.runtime.PackedExtensionContext;

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

            @Override
            public void hookOnVariableType(Class<?> hook, BindableBaseVariable v) {
                if (hook == SchedulingContext.class) {
                    v.bindToInvocationArgument(1);
                } else {
                    super.hookOnVariableType(hook, v);
                }
            }

            @Override
            public void hookOnAnnotatedMethod(Annotation hook, OperationalMethod on) {
                if (hook instanceof ScheduleRecurrent sr) {
                    OperationHandle oh = on.newOperation(OperationTemplate.defaults().withArg(SchedulingContext.class));
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
            BeanConfiguration b = base().install(SchedulingBean.class);

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

        SchedulingBean(@CodeGenerated FinalSchedule[] mhs, PackedExtensionContext pec) {
            this.vts = new PackedVirtualThreadScheduler(pec);
            
            for (FinalSchedule p : mhs) {
                vts.schedule(p.callMe, p.s.d());
            }
        }
    }

}
