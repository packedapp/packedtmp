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
package app.packed.cli;

import app.packed.component.BaseComponentConfiguration;
import app.packed.component.ClassComponentBinder;
import app.packed.container.Extension;

/**
 * A extension that can make it easier to work with command line parameters.
 * <p>
 * The goal of this extension is to provide basic support for command line arguments. If you need advanced features take
 * a look at the excellent <a href="https://picocli.info">https://picocli.info</a> framework.
 */
public final class CliExtension extends Extension {

    /** It's not you it's me. */
    private CliExtension() {}

    static final ClassComponentBinder<Object, BaseComponentConfiguration> b = newClassComponentBinderBuilder().build();
    
    // Taenker man kan styre hvordan den kommer ned til

    void setDefault(String... args) {
        // If MainArgs is not provided as a wirelet to the container
        // Use these sensible values
        // Maybe we will use [] as the default value
        
        
        System.out.println(b);
    }
}