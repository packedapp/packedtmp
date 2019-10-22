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
package packed.internal.hook.model.testit;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import packed.internal.hook.model.OnHookContainerModelBuilder;
import packed.internal.reflect.ClassProcessor;

/**
 *
 */
public class Stuff {

    @Anno1(12233)
    private String hahad;

    @Anno1(12322)
    private String haha;

    public static void main(String[] args) {
        OnHookContainerModelBuilder ohs = new OnHookContainerModelBuilder(new ClassProcessor(MethodHandles.lookup(), MyHook.class, false));
        ohs.process();

        MyHook mh = Hook.Builder.test(MethodHandles.lookup(), MyHook.class, Stuff.class);

        System.out.println("----");
        System.out.println(mh.val);
    }

    static class MyHook implements Hook {

        final String val;

        MyHook(String val) {
            this.val = requireNonNull(val);
        }

        static class Builder implements Hook.Builder<MyHook> {

            int sum;

            @OnHook
            private void foo(AnnotatedFieldHook<Anno1> hook) {
                this.sum += hook.annotation().value();
            }

            /** {@inheritDoc} */
            @Override
            public MyHook build() {
                return new MyHook("foobar " + sum);
            }
        }
    }

}
