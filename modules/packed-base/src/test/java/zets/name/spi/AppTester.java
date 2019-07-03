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
import app.packed.component.Component;
import app.packed.component.ComponentStream;
import app.packed.container.ArtifactSource;
import app.packed.container.Wirelet;

/**
 *
 */
public class AppTester {

    private final App app;

    public AppTester(App app) {
        this.app = requireNonNull(app);
    }

    public AppTester(ArtifactSource source, Wirelet... wirelets) {
        this(App.of(source, wirelets));
    }

    public void assertPathExist(CharSequence s) {
        Component cc = app.useComponent(s);
        assertThat(cc).isNotNull();
        if (s.toString().startsWith("/")) {
            assertThat(cc.path().toString()).isEqualTo(s.toString());
        }
    }

    public AppTester nameIs(String expected) {
        assertThat(app.name()).isEqualTo(expected);
        return this;
    }

    public ComponentStream stream() {
        return app.stream();
    }

    public static AppTester of(ArtifactSource source, Wirelet... wirelets) {
        return new AppTester(App.of(source, wirelets));
    }
}
