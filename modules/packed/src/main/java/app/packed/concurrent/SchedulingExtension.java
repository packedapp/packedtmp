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

import app.packed.bean.BeanIntrospector;
import app.packed.extension.Extension;
import app.packed.extension.Extension.DependsOn;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;

/**
 *
 */
@DependsOn(extensions = ThreadExtension.class)
public class SchedulingExtension extends Extension<SchedulingExtension> {

    SchedulingExtension() {}

    List<OperationHandle> ops = new ArrayList<>();

    @Override
    protected BeanIntrospector newBeanIntrospector() {

        return new BeanIntrospector() {

            @Override
            public void hookOnAnnotatedMethod(Annotation hook, OperationalMethod on) {
                if (hook instanceof ScheduleRecurrent sr) {
                    OperationHandle oh = on.newOperation(OperationTemplate.defaults());
                    oh.specializeMirror(() -> new ScheduledOperationMirror());
                    ops.add(oh);
                } else {
                    super.hookOnAnnotatedMethod(hook, on);
                }
            }
        };
    }

    @Override
    protected void onAssemblyClose() {
        super.onAssemblyClose();
        if (isLifetimeRoot()) {
            base().install(SchedulingBean.class);
        }
    }
}
