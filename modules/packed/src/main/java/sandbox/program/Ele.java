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
package sandbox.program;

import app.packed.assembly.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;

/**
 *
 */
public class Ele extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provide(Foo.class).export();
        use(MyE.class);
    }

    public static void main(String[] args) {
        ProgramY.start(new Ele());
    }

    public static class MyE extends Extension<MyE> {

        /**
         * @param handle
         */
        protected MyE(ExtensionHandle<MyE> handle) {
            super(handle);
        }
    }

    record Foo() {
        Foo {
            System.out.println("Foox");
        }
    }
}
