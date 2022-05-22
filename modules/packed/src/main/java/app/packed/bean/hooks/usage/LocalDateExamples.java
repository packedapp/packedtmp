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

import java.lang.invoke.MethodHandle;
import java.time.LocalDate;

import app.packed.application.Realm;
import app.packed.container.Extension;
import app.packed.inject.Factory;
import app.packed.inject.Factory0;
import app.packed.inject.Factory1;
import app.packed.lifecycle.OnStart;
import app.packed.operation.dependency.DependencyProvider;

/**
 *
 */
@SuppressWarnings("unused")
public class LocalDateExamples {

    void now() {
        class Now extends AutoService {

            /** {@inheritDoc} */
            @Override
            protected void build() {
                provide(new Factory0<>(LocalDate::now) {});

                // MethodHandles.publicLookup().findStatic(LocalDate.class, "now", MethodType.methodType(LocalDate.class));
            }
        }
    }

    void applicationStarted() {

        // installeret i rod containeren
        class AppStarted extends AutoService {

            
            class AppStartBean {
                volatile LocalDate started;

                @OnStart
                public void set() {
                    started = LocalDate.now();
                }
            }

            /** {@inheritDoc} */
            @Override
            protected void build() {
                // Det er jo det samme for hele applikationen... Men access patterned
                // for AppStartBean kan jo variere
                provide(new Factory1<AppStartBean, LocalDate>(b -> b.started) {});
            }
        }
    }
    

    @interface OnNow {
        
    }
    void applicationStartedAlt() {
        
        class MyExt extends Extension<MyExt> {

            @Override
            protected void hookOnBeanDependencyProvider(DependencyProvider injector) {
                injector.provide(new Factory1<AppStartBean, LocalDate>(b -> b.started) {});
            }
            
            protected final void onNew() {
                bean().install(AppStartBean.class);
            }
            
            class AppStartBean {
                volatile LocalDate started;

                @OnStart
                public void set() {
                    started = LocalDate.now();
                }
            }
        }
    }

    void beanInstanceStarted() {

        // installeret som en instans sidecar i beanen.
        class BeanStarted extends AutoService {

            class StartBean {
                volatile LocalDate started;

                LocalDate started() {
                    return started;
                }

                @OnStart
                public void set() {
                    started = LocalDate.now();
                }
            }

            /** {@inheritDoc} */
            @Override
            protected void build() {
                provide(new Factory1<>(StartBean::started) {});
            }
        }
    }

    static abstract class AutoService {
        protected abstract void build();

        public final void fail(String message) {

        }

        public final Realm realm() {
            throw new UnsupportedOperationException();
        }
        // Vi vil gerne enable convertering inde provide

        public final void enableAutoConvertering() {} // Converter
        // Hvis man ikke kalder provide taenker jeg det er missing?
        // Dvs vi failer variable ikke er Optional/@Nullable

        public final void provide(Class<?> fac) {}

        public final void provide(Factory<?> fac) {}

        public final void provide(MethodHandle handle) {}

        public final void provideInstance(Object instance) {}
    }

    static class ANow {
        final LocalDate date;

        ANow(LocalDate date) {
            this.date = date;
        }
    }
}
