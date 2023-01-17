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
package ts.hooks.method;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.bean.BeanHook.AnnotatedMethodHook;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanIntrospector.OperationalMethod;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceLocator;

/**
 *
 */
public class AnnotatedMethodHookTester {

    static <T> T process(Consumer<OperationalMethod> c, Class<T> type, Class<?>... additionalTypes) {
        BaseAssembly ba = new BaseAssembly() {

            @Override
            protected void build() {
                use(MyExt.class).addOM((a, b) -> c.accept(b));
                use(ServiceExtension.class);
                provide(type).export();
                for (int i = 0; i < additionalTypes.length; i++) {
                    provide(additionalTypes[i]);
                }
            }
        };
        return ServiceLocator.of(ba).use(type);
    }

    static class MyExt extends Extension<MyExt> {
        BiConsumer<Set<Class<? extends Annotation>>, OperationalMethod> onm;

        MyExt() {}

        /**
         * @param c
         */
        void addOM(BiConsumer<Set<Class<? extends Annotation>>, OperationalMethod> c) {
            onm = onm == null ? c : onm.andThen(c);
        }

        @Override
        protected BeanIntrospector newBeanIntrospector() {
            return new BeanIntrospector() {

                @Override
                public void hookOnAnnotatedMethod(Set<Class<? extends Annotation>> hooks, OperationalMethod on) {
                    onm.accept(hooks, on);
                }
            };
        }
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @AnnotatedMethodHook(extension = MyExt.class, allowInvoke = true)
    public @interface OnM {}
}