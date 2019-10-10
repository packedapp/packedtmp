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
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionComposer;
import app.packed.service.ServiceExtension;

/**
 *
 */
public class Fff2 extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        use(MyExtension.class);
        link(new OtherB());
        System.out.println(path());
    }

    public static void main(String[] args) {
        App.of(new Fff2());
        System.out.println("Bye");
    }

    public static class OtherB extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            setName("fooobar");
            use(MyExtension.class);
            System.out.println(path());
            System.out.println("------");
        }
    }

    public static class MyExtension extends Extension {

        static final class Composer extends ExtensionComposer<MyExtension> {

            /** {@inheritDoc} */
            @Override
            protected void configure() {
                onExtensionInstantiated(e -> e.use(ServiceExtension.class).provideInstance(123L));
                onLinkage((p, c) -> System.out.println("Linking " + p.context().containerPath() + " to " + c.context().containerPath()));
            }
        }
    }
}
