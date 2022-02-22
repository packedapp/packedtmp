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
package zandbox.packed.hooks;

import java.util.Optional;

import app.packed.application.App;
import app.packed.bean.BeanExtension;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.extension.Extension.DependsOn;

/**
 *
 */
public class ZestMe2 extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(Bla.class);
        use(MyExt.class);
        link(new Foo());
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
//        ExtensionDescriptor ed = ExtensionDescriptor.of(rMyExt.class);
        App.run(new ZestMe2() /* , BuildWirelets.printDebug().all() */);
        // System.out.println(ed.dependencies());
        System.out.println(System.currentTimeMillis() - start);

        // ApplicationMirror m = App.mirrorOf(new ZestMe2());
        // System.out.println("-----");

        // System.out.println(ExtensionDescriptor.of(MyExt.class).dependencies());
    }

    public record Bla(Optional<String> os) {}

    public static class Foo extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            use(MyExt.class);
        }

    }

    @DependsOn(extensions = { BeanExtension.class })
    public static class MyExt extends Extension<MyExt> {

        MyExt() {}

        /** {@inheritDoc} */
        @Override
        protected void onNew() {
            if (containerPath().depth() == 0) {
                //beans().installInstance(new Hibib("123"));
                //beans().install(Habab.class);
            } else {
              //  beans().install(Habab.class);
            }
            System.out.println("ADDED");
        }

        /** {@inheritDoc} */
        @Override
        protected void onApplicationClose() {
            System.out.println("Configured");
        }
    }

    public record Hibib(String s) {}

    public record Habab(Hibib b, Optional<Habab> hab) {
        public Habab {
            System.out.println(b);
        }
    }
}
