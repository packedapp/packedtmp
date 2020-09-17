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
package packed.internal.sidecar;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.LocalDateTime;

import app.packed.component.App;
import app.packed.container.BaseBundle;
import app.packed.sidecar.ActivateSidecar;

/**
 *
 */
public class Foo extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        provide(MyComp.class);
    }

    public static void main(String[] args) {
        App.of(new Foo());
    }

    public static class MyComp {

        public MyComp(LocalDateTime ldt) {
            System.out.println("INIT " + ldt);
        }

        @Hej
        public void foo() {
            System.out.println("HEJHEJ");
        }
    }

    @Target(ElementType.METHOD)
    @Retention(RUNTIME)
    @ActivateSidecar(TestIt.class)
    public @interface Hej {

    }
}
