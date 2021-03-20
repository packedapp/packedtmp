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
import static org.assertj.core.api.Assertions.assertThat;

import app.packed.component.Program;
import app.packed.component.Assembly;
import app.packed.component.Component;
import app.packed.component.ComponentStream;
import app.packed.component.ApplicationImage;
import app.packed.component.Wirelet;

/**
 *
 */
public class AppTester {

    private final Program app;

    public AppTester(Program app) {
        this.app = requireNonNull(app);
    }

    public AppTester(Assembly<?> source, Wirelet... wirelets) {
        this(Program.start(source, wirelets));
    }

    public AppTester(ApplicationImage<Program> img, Wirelet... wirelets) {
        this(img.use(wirelets));
    }

    public void assertPathExist(CharSequence s) {
        Component cc = app.resolve(s);
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

    public static AppTester of(Assembly<?> source, Wirelet... wirelets) {
        return new AppTester(Program.start(source, wirelets));
    }
}
