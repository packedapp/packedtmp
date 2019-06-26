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
package zets.name.spi;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import app.packed.app.App;
import app.packed.container.AnyBundle;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;
import zets.namepath.ConfAction;

/**
 *
 */
public class AppTester {

    private final App app;

    public AppTester(ContainerSource source, Wirelet... wirelets) {
        this(App.of(source, wirelets));
    }

    public AppTester(App app) {
        this.app = requireNonNull(app);
    }

    public AppTester nameIs(String expected) {
        assertThat(app.name()).isEqualTo(expected);
        return this;
    }

    public static AppTester of(ContainerSource source, Wirelet... wirelets) {
        return new AppTester(App.of(source, wirelets));
    }

    public static AppTester of(ConfAction action, Wirelet... wirelets) {
        return of(new TestBundle(action), wirelets);
    }

    static class TestBundle extends AnyBundle {
        final ConfAction ca;

        TestBundle(ConfAction ca) {
            this.ca = requireNonNull(ca);
        }

        @Override
        public void configure() {
            ca.apply(configuration());
        }
    }
}
