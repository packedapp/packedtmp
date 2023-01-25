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
import java.util.ArrayList;
import java.util.List;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanIntrospector;
import app.packed.extension.Extension;
import app.packed.extension.Extension.DependsOn;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.ExtensionPoint;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;

/**
 *
 */
@DependsOn(extensions = ThreadExtension.class)
public class SchedulingExtension extends Extension<SchedulingExtension> {

    List<ConfigurableSchedule> ops = new ArrayList<>();

    /** Create a new scheduling extension. */
    SchedulingExtension() {}

    @Override
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {

            @Override
            public void hookOnAnnotatedMethod(Annotation hook, OperationalMethod on) {
                if (hook instanceof ScheduleRecurrent sr) {
                    OperationHandle oh = on.newOperation(OperationTemplate.defaults());
                    oh.specializeMirror(() -> new ScheduledOperationMirror());
                    ops.add(new ConfigurableSchedule(new Schedule(sr.millies()), oh));
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
        if (isLifetimeRoot()) {
            BeanConfiguration b = base().install(SchedulingBean.class);
            base().addCodeGenerated(b, FinalSchedule[].class, () -> {
                return ops.stream().map(ConfigurableSchedule::schedule).toArray(e -> new FinalSchedule[e]);
            });
        }
    }

    static final class ConfigurableSchedule {

        final OperationHandle callMe;

        private Schedule s;

        ConfigurableSchedule(Schedule s, OperationHandle callMe) {
            this.s = s;
            this.callMe = callMe;
        }

        FinalSchedule schedule() {
            return new FinalSchedule(s, callMe.generateMethodHandle());
        }

        void updateS(Schedule s) {
            System.out.println("Updating " + s);
            this.s = s;
        }
    }

}
