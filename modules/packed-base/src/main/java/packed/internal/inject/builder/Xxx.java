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
package packed.internal.inject.builder;

import java.lang.invoke.MethodHandles;

import app.packed.bundle.BundlingImportOperation;
import app.packed.inject.Injector;
import app.packed.inject.InjectorBundle;
import app.packed.inject.Provides;

/**
 *
 */
public class Xxx {

    public static void main(String[] args) {

        Injector i = Injector.of(c -> {
            c.bindInjector(StringBundle.class, new MyImportStage());
        });

        i.services().forEach(e -> System.out.println(e.getKey()));

    }

    static class MyImportStage extends BundlingImportOperation {
        MyImportStage() {
            super(MethodHandles.lookup());
        }

        @Provides
        public static long ll(String i) {
            return i.length();
        }
    }

    public static class StringBundle extends InjectorBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            expose(bind("Foooo"));
        }
    }
}
