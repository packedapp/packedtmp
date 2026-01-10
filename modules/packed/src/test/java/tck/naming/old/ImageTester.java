/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package tck.naming.old;

import static java.util.Objects.requireNonNull;

import app.packed.application.BootstrapApp;
import app.packed.application.BootstrapApp.Image;
import app.packed.assembly.BaseAssembly;
import app.packed.container.Wirelet;
import sandbox.program.ProgramX;

/**
 *
 */
public class ImageTester {

    private final BootstrapApp.Image<ProgramX> image;

    public ImageTester(BaseAssembly source, Wirelet... wirelets) {
        this(ProgramX.imageOf(source, wirelets));
    }

    public AppTester newApp() {
        return new AppTester(image);
    }

    public ImageTester(Image<ProgramX> image) {
        this.image = requireNonNull(image);
    }

    public ImageTester nameIs(String expected) {
        return this;
    }
}
