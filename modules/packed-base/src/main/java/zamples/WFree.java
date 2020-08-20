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
package zamples;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import app.packed.artifact.App;
import app.packed.component.Component;
import app.packed.component.ComponentProperty;
import app.packed.component.Wirelet;
import app.packed.container.BaseBundle;

/**
 *
 */
public class WFree extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        lookup(MethodHandles.lookup());
        provide(MyComp.class);
        link(new DDD());
    }

    public void cc(Component c) {
        c.attribute(Component.PROPERTIES).contains(ComponentProperty.CONTAINER);

        // Would be so nice with a default value... I think
        c.attributes().orStuff(Component.PROPERTIES, f -> f.contains(ComponentProperty.CONTAINER), false);
    }

    public static void main(String[] args) {
        App.create(new WFree(), new SomeWirelet("Saturday"), new SomeWirelet("Sundday"));
        App.create(new WFree());
        System.out.println("Nye");
    }

    public static class MyComp {

        MyComp(Optional<SomeWirelet> o) {
            System.out.println("Hello " + o.map(s -> s.x).orElse("World"));
        }
    }

    static class SomeWirelet implements Wirelet {
        final String x;

        SomeWirelet(String x) {
            this.x = x;
        }
    }

    public class DDD extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            new Exception().printStackTrace();
        }

    }
}
