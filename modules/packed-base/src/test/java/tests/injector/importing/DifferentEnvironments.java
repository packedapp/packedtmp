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
package tests.injector.importing;

import static java.util.Objects.requireNonNull;

import app.packed.bundle.InjectorBundle;
import app.packed.inject.Injector;
import tests.injector.importing.DifferentEnvironments.BaseEnvironment;
import tests.injector.importing.DifferentEnvironments.ProdEnvironment;
import tests.injector.importing.DifferentEnvironments.TestEnvironment;

/**
 *
 */
public class DifferentEnvironments {

    public static void main(String[] args) {
        // App.run(ParseIt.def().getBundleType());

        //
        System.out.println(ParseIT.def());

        Injector.of(ParseIT.def().getBundleType());
    }

    public abstract class BaseEnvironment extends InjectorBundle {

        @Override
        protected void configure() {}

    }

    public final class TestEnvironment extends BaseEnvironment {
        @Override
        protected void configure() {
            super.configure();
            // set test
        }
    }

    public final class ProdEnvironment extends BaseEnvironment {
        @Override
        protected void configure() {
            super.configure();
            // set prod
        }
    }
}

enum ParseIT {
    TEST(TestEnvironment.class), PROD(ProdEnvironment.class);
    final Class<? extends BaseEnvironment> c;

    private static final ParseIT DEFAULT = TEST; // System.property.get("sdsdsd"); maybe with fallback

    private ParseIT(Class<? extends BaseEnvironment> c) {
        this.c = requireNonNull(c);
    }

    public Class<? extends BaseEnvironment> getBundleType() {
        return c;
    }

    public static ParseIT def() {
        return DEFAULT;
    }

}