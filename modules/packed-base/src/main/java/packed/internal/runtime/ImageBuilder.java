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
package packed.internal.runtime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.bundle.Bundle;
import app.packed.util.Nullable;
import packed.internal.classscan.DescriptorFactory;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.util.AbstractConfiguration;

/**
 *
 */
public abstract class ImageBuilder extends AbstractConfiguration {

    /** The lookup object. We default to public access */
    public DescriptorFactory accessor = DescriptorFactory.PUBLIC;

    /**
     * The (optional) bundle we are creating a runtime instance for, if null then we using a {@code XConfiguration} to
     * create the runtime.
     */
    @Nullable
    public final Bundle bundle;

    /**
     * @param configurationSite
     */
    protected ImageBuilder(InternalConfigurationSite configurationSite) {
        super(configurationSite);
        this.bundle = null;
    }

    /**
     * @param configurationSite
     */
    protected ImageBuilder(InternalConfigurationSite configurationSite, Bundle bundle) {
        super(configurationSite);
        this.bundle = requireNonNull(bundle);
    }

    /** {@inheritDoc} */
    public final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to use public access (default)");
        checkConfigurable();
        this.accessor = DescriptorFactory.get(lookup);
    }
}
