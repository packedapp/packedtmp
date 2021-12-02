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

import app.packed.application.ApplicationImage;
import app.packed.application.programs.Program;
import app.packed.container.BaseAssembly;
import app.packed.container.Wirelet;

/**
 *
 */
public class ImageTester {

    private final ApplicationImage<Program> image;

    public ImageTester(BaseAssembly source, Wirelet... wirelets) {
        this(Program.imageOf(source, wirelets));
    }

    public AppTester newApp(Wirelet... wirelets) {
        return new AppTester(image, wirelets);
    }

    public ImageTester(ApplicationImage<Program> image) {
        this.image = requireNonNull(image);
    }

    public ImageTester nameIs(String expected) {
        return this;
    }
}
