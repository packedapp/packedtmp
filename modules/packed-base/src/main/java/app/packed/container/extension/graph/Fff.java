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
package app.packed.container.extension.graph;

import app.packed.app.App;
import app.packed.container.BaseBundle;
import app.packed.container.extension.ComposableExtension;
import app.packed.container.extension.ExtensionComposer;

/**
 *
 */
public class Fff extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        link(new OtherB());
        use(MyExtension.class);
        setName("doo");
    }

    public static void main(String[] args) {
        App.of(new Fff());
        System.out.println("Bye");
    }

    public static class OtherB extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            use(MyExtension.class);
            System.out.println("------");
        }

    }

    public static class MyExtension extends ComposableExtension<MyExtension.Composer> {

        static final class Composer extends ExtensionComposer<MyExtension> {

            /** {@inheritDoc} */
            @Override
            protected void configure() {
                onLinkage((p, c) -> System.out.println("Linked " + p.context().containerPath() + " to " + c.context().containerPath()));
            }
        }
    }
}
