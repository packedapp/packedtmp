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
package app.packed.bean.hooks.usage;

import java.time.LocalDate;

import app.packed.bean.hooks.BeanVariable;
import app.packed.container.Extension;
import app.packed.inject.Factory1;
import app.packed.lifecycle.OnStart;
import app.packed.operation.dependency.DependencyProvider;

/**
 *
 */
// Obviously... People might choose to use OnAppStart before the application is properly started
public class InjectAppStart {

    @DependencyProvider.Hook(extension = MyExtAppStart.class)
    @BeanVariable.AnnotatedWithHook(extension = MyExtAppStart.class)
    @interface OnAppStart {}

    static class MyExtAppStart extends Extension<MyExtAppStart> {

        @Override
        protected void hookOnBeanDependencyProvider(DependencyProvider injector) {
            // injector checkTyoe

            injector.provide(new Factory1<AppStartBean, LocalDate>(b -> b.started) {});
        }

        protected final void onNew() {
            bean().install(AppStartBean.class);
        }

       static class AppStartBean {
            volatile LocalDate started;

            @OnStart
            public void set() {
                started = LocalDate.now();
            }
        }
    }
}
