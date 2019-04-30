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

import java.util.List;

import app.packed.bundle.Bundle;
import app.packed.bundle.OldWiringOperation;
import app.packed.inject.Injector;
import packed.internal.bundle.BundleSupport;
import packed.internal.config.site.InternalConfigurationSite;

/**
 *
 */
public class AnyWiring {

    /** {@link Bundle} or an instance of an {@link Injector}. */
    Object child;

    /** The configuration site of this object. */
    public final InternalConfigurationSite configurationSite;

    final List<OldWiringOperation> operations;

    protected AnyWiring(InternalConfigurationSite configurationSite, Object child, OldWiringOperation[] operations) {
        this.configurationSite = requireNonNull(configurationSite);
        this.operations = BundleSupport.invoke().extractWiringOperations(operations, null);
        this.child = requireNonNull(child);
    }
}
