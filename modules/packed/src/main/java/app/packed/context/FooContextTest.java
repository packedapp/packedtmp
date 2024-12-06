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
package app.packed.context;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.BaseAssembly;
import app.packed.bean.BeanConfiguration;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;

/**
 *
 */
public class FooContextTest extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        use(MyExt.class);
        installInstance(123);
        System.out.println(NewExt.class.getPermittedSubclasses()[0]);
    }

    public static void main(String[] args) {
        ApplicationMirror m = App.mirrorOf(new FooContextTest());
        m.container().allBeans().forEach(e -> System.out.println(e.contexts()));
        System.out.println("BYE");
    }

    public sealed interface NewExt permits MyExt {}

    public static final class MyExt extends Extension<MyExt> implements NewExt {

        protected MyExt(ExtensionHandle<MyExt> handle) {
            super(handle);
        }

        @Override
        protected void onNew() {
            BeanConfiguration b = base().installInstance("sdf");
            System.out.println(b);
        }
    }
}
