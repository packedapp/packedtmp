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
package testutil.util;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import app.packed.bundle.BaseAssembly;
import app.packed.bundle.Bundle;
import app.packed.bundle.Wirelet;

/** An abstract test for testing artifacts. */
public abstract class AbstractApplicationTest {

    /** An assembly with no operations. */
    public static BaseAssembly emptyAssembly() {
        return new BaseAssembly() {
            @Override
            protected void build() {}
        };
    }

    public static AppTester appOf(Bundle<?> source, Wirelet... wirelets) {
        return new AppTester(source, wirelets);
    }

    public static AppTester appOf(Consumer<? super ContainerConfigurationTester> source, Wirelet... wirelets) {
        return new AppTester(new AbstractConsumableAssembly(source) {}, wirelets);
    }

    public static ImageTester imageOf(BaseAssembly source, Wirelet... wirelets) {
        return new ImageTester(source, wirelets);
    }

    protected static abstract class AbstractConsumableAssembly extends BaseAssembly {
        final Consumer<? super ContainerConfigurationTester> ca;

        protected AbstractConsumableAssembly(Consumer<? super ContainerConfigurationTester> ca) {
            this.ca = requireNonNull(ca);
        }

        @Override
        public void build() {
            ca.accept(new ContainerConfigurationTester(configuration()));
        }
    }
}
