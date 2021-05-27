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
package app.packed.cli;

import app.packed.application.Program;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.service.ServiceExtension;

/**
 *
 */
public class ZestMe extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(Foo.class);
        //use(MyExt.class);
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
//        ExtensionDescriptor ed = ExtensionDescriptor.of(rMyExt.class);
        Program.start(new ZestMe() /* , BuildWirelets.printDebug().all() */);
        // System.out.println(ed.dependencies());
        System.out.println(System.currentTimeMillis() - start);

        // System.out.println(ExtensionDescriptor.of(MyExt.class).dependencies());
    }

    public static class Foo {

    }

    public static class MyExt extends Extension {

        MyExt() {}

        /** {@inheritDoc} */
        @Override
        protected void onNew() {
            System.out.println("ADDED");
        }

        /** {@inheritDoc} */
        @Override
        protected void onComplete() {
            System.out.println("Configured");
        }

        static {
            $dependsOn(ServiceExtension.class);
        }
    }
}
