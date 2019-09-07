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
package packed.internal.inject.build;

import static java.util.Objects.requireNonNull;

import app.packed.container.extension.ExtensionPipeline;

/**
 *
 */
public final class InjectionPipeline extends ExtensionPipeline<InjectionPipeline> {

    /// ARGHHH... vi skal jo resolve foerst....

    public final InjectionExtensionNode ib;

    InjectionPipeline(InjectionPipeline previous) {
        this.ib = previous.ib;
    }

    public InjectionPipeline(InjectionExtensionNode ib) {
        this.ib = requireNonNull(ib);
    }

    /** {@inheritDoc} */
    @Override
    protected InjectionPipeline split() {
        return new InjectionPipeline(this);
    }

    // Always invoked after resolving...
}
