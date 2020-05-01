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
package app.packed.api;

import java.lang.invoke.MethodHandles;

import app.packed.artifact.App;
import app.packed.container.BaseBundle;
import app.packed.container.Extension;
import app.packed.inject.InjectionContext;
import app.packed.sidecar.ExtensionSidecar;
import app.packed.sidecar.PostSidecar;

/**
 *
 */
public class Dddd extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void compose() {
        lookup(MethodHandles.lookup());
        provide(Foo.class);
        use(MyExt.class);
    }

    public static void main(String[] args) {
        App.of(new Dddd());
        System.out.println("BY");
    }

    static class Foo {

    }

    static class MyExt extends Extension {

        MyExt(InjectionContext conb) {
            System.out.println(conb);
        }

        @PostSidecar(ExtensionSidecar.INSTANTIATION)
        static void foo(InjectionContext ic) {
            System.out.println("HEJ " + ic);
        }

        @PostSidecar(ExtensionSidecar.INSTANTIATION)
        void foo() {
            System.out.println("HEJdd");
            throw new RuntimeException();
        }

        @PostSidecar(ExtensionSidecar.INSTANTIATION)
        void foox() {
            System.out.println("HEJ");
        }

        @PostSidecar(ExtensionSidecar.INSTANTIATION)
        void fodo() {
            System.out.println("HEJ123");
        }
    }
}
