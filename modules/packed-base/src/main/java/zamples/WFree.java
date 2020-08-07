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
import app.packed.component.Wirelet;
import app.packed.container.DefaultBundle;

/**
 *
 */
public class WFree extends DefaultBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        lookup(MethodHandles.lookup());
        provide(MyComp.class);
    }

    public static void main(String[] args) {
        App.of(new WFree(), new SomeWirelet("Saturday"), new SomeWirelet("Sundday"));
        App.of(new WFree());
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
}
