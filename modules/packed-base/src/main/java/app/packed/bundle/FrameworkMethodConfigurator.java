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
package app.packed.bundle;

import app.packed.container.Extension;
import app.packed.inject.InjectorExtension;
import app.packed.inject.ProvidesHelper;
import app.packed.util.Key;

/**
 *
 */
// Er det maaden vi ogsaa skal fikse hooks paa...
public abstract class FrameworkMethodConfigurator {

    protected abstract void configure();

    protected final void requireExtension(Class<? extends Extension<?>> extensionType) {}

    protected final void enabledInjection() {}

    protected final void addInjectable(Key<?> key, String description) {}

    protected final void addInjectable(Class<?> key, String description) {}
}

// MethodHandles must be installed via a SupportBundle...
class ProvidesConfiguator extends FrameworkMethodConfigurator {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        requireExtension(InjectorExtension.class);
        enabledInjection();
        addInjectable(ProvidesHelper.class, "A helper object for @Provides");
    }
}
