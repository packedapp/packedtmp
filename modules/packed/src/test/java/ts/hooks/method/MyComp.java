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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.bean.BeanElement.BeanMethod;
import app.packed.bean.BeanHook.AnnotatedMethodHook;
import app.packed.bean.BeanIntrospector;
import app.packed.container.AbstractComposer;
import app.packed.container.AbstractComposer.ComposerAction;
import app.packed.extension.Extension;
import app.packed.framework.AnnotationList;

/**
 *
 */
public class MyComp {

    static MyComp mc;

    static MyComp of(ComposerAction<? super Composer> ca) {
        throw new UnsupportedOperationException();
    }
    public static class Composer extends AbstractComposer {

        @Override
        protected void preCompose() {
            use(MyExt.class);
        }

        public void onM(Consumer<BeanMethod> c) {
            onM2((a, b) -> c.accept(b));
        }

        public void onM2(BiConsumer<AnnotationList, BeanMethod> c) {
            use(MyExt.class).addOM(c);
        }
    }

    static class InBean {

    }

    static class MyExt extends Extension<MyExt> {
        BiConsumer<AnnotationList, BeanMethod> onm;

        MyExt() {}

        /**
         * @param c
         */
        void addOM(BiConsumer<AnnotationList, BeanMethod> c) {
            onm = c == null ? c : onm.andThen(c);
        }

        @Override
        protected BeanIntrospector newBeanIntrospector() {
            return new BeanIntrospector() {

                @Override
                public void hookOnAnnotatedMethod(AnnotationList hooks, BeanMethod on) {
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
