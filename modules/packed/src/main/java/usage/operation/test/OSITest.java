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
package usage.operation.test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDateTime;

import app.packed.application.App;
import app.packed.assembly.BaseAssembly;
import app.packed.extension.BeanTrigger.AnnotatedVariableBeanTrigger;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.BindableVariable;
import app.packed.extension.Extension;
import app.packed.lifetime.OnInitialize;
import app.packed.operation.Op0;
import app.packed.operation.Op1;

/**
 *
 */
public class OSITest extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(BB.class);
    }

    public static void main(String[] args) {
        App.run(new OSITest());
    }

    public static class BB {

        public BB(@Now LocalDateTime i2) {
            System.out.println(i2);
        }

        @OnInitialize
        public void sd(@Now LocalDateTime i2, @BuildTime LocalDateTime i24) {
            System.out.println(i2);
        }

        @OnInitialize
        public static void fsd(@Now LocalDateTime i2, @BuildTime LocalDateTime i24) {
            System.out.println(i2 + " " + i24);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @AnnotatedVariableBeanTrigger(extension = MyExt.class)
    @interface BuildTime {}

    static class MyExt extends Extension<MyExt> {
        MyExt() {}

        @Override
        protected BeanIntrospector newBeanIntrospector() {
            return new BeanIntrospector() {
                @Override
                public void activatedByAnnotatedVariable(Annotation hook, BindableVariable d) {
                    if (hook instanceof BuildTime) {
                        d.checkAssignableTo(LocalDateTime.class);
                        // d.bindConstant(LocalDateTime.now());
                        d.bindComputedConstant(() -> LocalDateTime.now());
                    } else if (hook instanceof InitializationTime) {
                        applicationRoot().base().installIfAbsent(AppInitializeTime.class);
                        d.checkAssignableTo(LocalDateTime.class);
                        d.bindOp(new Op1<AppInitializeTime, LocalDateTime>(b -> b.initialized) {});
                    } else if (hook instanceof Now) { // now
                        d.checkAssignableTo(LocalDateTime.class);
                        d.bindOp(new Op0<>(LocalDateTime::now) {});
                    } else {
                        super.activatedByAnnotatedVariable(hook, d);
                    }
                }
            };
        }

        private static class AppInitializeTime {
            final LocalDateTime initialized = LocalDateTime.now();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @AnnotatedVariableBeanTrigger(extension = MyExt.class)
    @interface Now {}

    @Retention(RetentionPolicy.RUNTIME)
    @AnnotatedVariableBeanTrigger(extension = MyExt.class)
    @interface InitializationTime {}
}
