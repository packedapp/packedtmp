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
package app.packed.operation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.util.function.Consumer;

import org.junit.jupiter.api.Disabled;

import app.packed.binding.Qualifier;

/** Test of {@link Op}. */
@Disabled
public class OpTest {

    

    // /** Tests {@link Factory#forInstance(Object)}. */
    // @Test
    // public void forInstance() {
    // assertThrows(NullPointerException.class, () -> Factory.forInstance(null));
    //
    // Factory<Simple> simple = Factory.from(new Simple());
    // // assertTrue(simple.isInstantiated());
    // assertEquals(Key.get(Simple.class), simple.getDefaultKey());
    //
    // assertEquals(Key.get(SimpleWithQualified.class, Qualified.class), Factory.forInstance(new SimpleWithQualified()));
    //
    // // Factory<Simple> simple = Factory.forInstance(new Simple());
    // // So what do we test for.
    // // Inject, Mixin, no void onProvide
    //
    // Factory<Number> fNumber = Factory.forInstance(123); // Just tests the generic signature
    // assertNotNull(fNumber);
    // }

    public static class Stubs {

        @Retention(RUNTIME)
        @Qualifier
        public @interface Qualified {}

        @Retention(RUNTIME)
        @Qualifier
        public @interface QualifiedWithValue {
            String value() default "34";
        }

        public static class Simple {}

        @Qualified
        public static class SimpleWithQualified {}

        @Qualified
        public static class SimpleWithQualifiedValue {}

        public static class SimpleConsumer implements Consumer<Simple> {
            @Override
            public void accept(Simple t) {}
        }
    }
}
