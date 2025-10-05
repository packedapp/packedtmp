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
package sandbox.application;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.BaseAssembly;
import app.packed.bean.BeanLifetime;
import app.packed.bean.BeanTemplate;
import app.packed.bean.lifecycle.Initialize;
import app.packed.build.BuildProcess;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;

/**
 *
 */
public class Asss extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provideInstance("fooo");

        provide(Foo.class);
        install(Boo.class);

        use(MyExt.class);
        container().beans().forEach(e -> IO.println(e.operations().toList()));

        container().beans().forEach(e -> IO.println(e.toString()));

        IO.println("Build ID " + BuildProcess.current().processId());
    }

    public static void main(String[] args) {
        ApplicationMirror am = App.mirrorOf(new Asss());
        IO.println();
        am.container().allBeans().forEach(e -> IO.println(e.name()));
        IO.println();
        am.container().beans().forEach(e -> IO.println(e.name()));

        App.run(new Asss());
    }

    public record Foo(String s) {

        @Initialize
        public void onStart() {
            IO.println("Initized!!");
        }
    }

    @SuppressWarnings("unused")
    public void foo() {
        BeanTemplate a = BeanTemplate.of(BeanLifetime.SINGLETON);
        BeanTemplate.of(BeanLifetime.SINGLETON);
        // BeanTemplateWithBuilder b = BeanTemplateWithBuilder.builder().rcreateAs(String.class).build();
        BeanTemplate t = BeanTemplate.FUNCTIONAL;

    }

    public record Boo(Foo f) {}

    public static class MyExt extends Extension<MyExt> {

        MyExt(ExtensionHandle<MyExt> handle) {
            super(handle);
        }

        @Override
        protected void onNew() {
            base().installInstance("sdfsdf").named("MainBean");
            base().installInstance(new Foox()).named("MainBean1");
        }
    }

    public static class Foox {

        @Initialize
        public void onStart() {
            IO.println("Initized");
        }
    }
}
