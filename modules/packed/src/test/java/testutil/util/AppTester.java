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

import app.packed.application.BootstrapApp.Image;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import sandbox.program.ProgramX;

/**
 *
 */
public class AppTester {

    private final ProgramX app;

    public AppTester(ProgramX app) {
        this.app = requireNonNull(app);
    }

    public AppTester(Assembly  source, Wirelet... wirelets) {
        this(ProgramX.start(source, wirelets));
    }

    public AppTester(Image<ProgramX> img, Wirelet... wirelets) {
        this(img.launch(wirelets));
    }

    public void assertPathExist(CharSequence s) {
//        Component cc = app.resolve(s);
//        assertThat(cc).isNotNull();
//        if (s.toString().startsWith("/")) {
//            assertThat(cc.path().toString()).isEqualTo(s.toString());
//        }
    }

    public AppTester nameIs(String expected) {
        assertThat(app.name()).isEqualTo(expected);
        return this;
    }
}
