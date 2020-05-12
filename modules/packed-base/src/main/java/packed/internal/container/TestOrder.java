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
package packed.internal.container;

import app.packed.artifact.App;
import app.packed.container.BaseBundle;
import app.packed.container.Bundle;
import app.packed.container.ExtensionSidecar;
import app.packed.lifecycle.Leaving;

/**
 *
 */
public class TestOrder extends Bundle {

    public static class SomeExtension extends app.packed.container.Extension {

        @Leaving(state = ExtensionSidecar.CHILD_LINKING)
        public void foo() {
            System.out.println("Main " + getClass().getSimpleName());
        }
    }

    public static class E1 extends SomeExtension {

        public void doStuff() {
            checkConfigurable();
        }
    }

    @ExtensionSidecar(dependencies = E1.class)
    public static class E2 extends SomeExtension {

    }

    @ExtensionSidecar(dependencies = E2.class)
    public static class E3 extends SomeExtension {

        public void doStuff() {
            checkConfigurable();
        }

        @Override
        @Leaving(state = ExtensionSidecar.NORMAL_USAGE)
        public void foo() {
            System.out.println("Main " + getClass().getSimpleName());
            // use(E1.class).doStuff();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void compose() {
        use(E3.class);
        use(E1.class);
        use(E2.class);
        link(new SomeBundle());
    }

    public static void main(String[] args) {
        App.of(new TestOrder());
    }

    public static class SomeBundle extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void compose() {
            System.out.println("Composed");
        }
    }
}
