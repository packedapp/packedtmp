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
package xackedinject;

import static java.util.Objects.requireNonNull;

import app.packed.inject.Injector;
import app.packed.inject.InjectorBundle;
import xackedinject.DifferentEnvironments.BaseEnvironment;
import xackedinject.DifferentEnvironments.ProdEnvironment;
import xackedinject.DifferentEnvironments.TestEnvironment;

/**
 *
 */
public class DifferentEnvironments {

    public static void main(String[] args) {
        // App.run(ParseIt.def().getBundleType());

        //
        System.out.println(ParseIt.environment());

        Injector.of(ParseIt.environment().getBundleType());
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

enum ParseIt {
    TEST(TestEnvironment.class), PROD(ProdEnvironment.class);
    final Class<? extends BaseEnvironment> c;

    private static final ParseIt DEFAULT = TEST; // System.property.get("sdsdsd"); maybe with fallback

    private ParseIt(Class<? extends BaseEnvironment> c) {
        this.c = requireNonNull(c);
    }

    public Class<? extends BaseEnvironment> getBundleType() {
        return c;
    }

    public static ParseIt environment() {
        return DEFAULT;
    }
}