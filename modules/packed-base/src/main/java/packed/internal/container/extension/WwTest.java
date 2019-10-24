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
package packed.internal.container.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.artifact.App;
import app.packed.container.BaseBundle;
import app.packed.container.Extension;
import app.packed.container.UseExtensionLazily;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import app.packed.lang.Qualifier;

/**
 *
 */
@UseExtensionLazily(WwTest.MyExtension.class)
public class WwTest extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        install(Comp1.class);

    }

    public static void main(String[] args) {
        App.of(new WwTest());
    }

    static class Comp1 {
        @Left
        public String s = "ffiii";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    // @UseExtension(MyExtension.class)
    @Target({ ElementType.TYPE_USE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
    public @interface Left {

    }

    public static class MyExtension extends Extension {

        MyExtension() {
            System.out.println("******* Activated MyExtension ****** ");
        }

        @OnHook
        public void on(My my) {
            System.out.println("XXXAC");
        }
    }

    static class My implements Hook {

        static final class Builder implements Hook.Builder<My> {

            @OnHook
            public void on(AnnotatedFieldHook<Left> h) {
                System.out.println(h.field());
                System.out.println("AC");
            }

            /** {@inheritDoc} */
            @Override
            public My build() {
                return new My();
            }

        }
    }

}
