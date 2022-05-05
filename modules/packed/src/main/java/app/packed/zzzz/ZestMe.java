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
package app.packed.zzzz;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.application.entrypoint.EntryPointExtension;
import app.packed.application.entrypoint.EntryPointExtensionMirror;
import app.packed.application.entrypoint.EntryPointExtensionPoint;
import app.packed.bean.BeanExtension;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.container.Extension.DependsOn;
import app.packed.inject.service.PublicizeExtension;

/**
 *
 */
public class ZestMe extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(Foo.class);
        use(MyExt.class);
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
//        ExtensionDescriptor ed = ExtensionDescriptor.of(rMyExt.class);
        App.run(new ZestMe() /* , BuildWirelets.printDebug().all() */);
        // System.out.println(ed.dependencies());
        System.out.println(System.currentTimeMillis() - start);

        ApplicationMirror m = App.mirrorOf(new ZestMe());
        System.out.println("-----");
        System.out.println(m.container().useExtension(EntryPointExtensionMirror.class).dispatcher());

        // System.out.println(ExtensionDescriptor.of(MyExt.class).dependencies());
    }

    public static class Foo {

    }

    @DependsOn(extensions = { PublicizeExtension.class, EntryPointExtension.class, BeanExtension.class })
    public static class MyExt extends Extension<MyExt> {

        MyExt() {}

        /** {@inheritDoc} */
        @Override
        protected void onNew() {
            bean().installInstance(new Hibib("123"));
            bean().install(Habab.class);

            System.out.println("ADDED");
            System.out.println(use(EntryPointExtensionPoint.class).dispatcher());
            //System.out.println(use(EntryPointSupport.class).registerEntryPoint(null));
            System.out.println(use(EntryPointExtensionPoint.class).dispatcher());
        }

        /** {@inheritDoc} */
        @Override
        protected void onApplicationClose() {
            System.out.println("Configured");
        }
    }

    public record Hibib(String s) {}
    public record Habab(Hibib b) {}
}
