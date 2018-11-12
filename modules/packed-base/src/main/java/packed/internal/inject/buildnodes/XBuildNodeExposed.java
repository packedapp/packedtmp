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
package packed.internal.inject.buildnodes;

import java.util.List;

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionSite;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.InternalInjectorConfiguration;
import packed.internal.inject.runtimenodes.RuntimeNode;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 *
 */
public class XBuildNodeExposed<T> extends BuildNode<T> {

    /**
     * @param bundle
     * @param dependencies
     * @param stackframe
     */
    public XBuildNodeExposed(InternalInjectorConfiguration bundle,  InternalConfigurationSite configurationSite, List<InternalDependency> dependencies) {
        super(bundle, configurationSite, dependencies);
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(InjectionSite site) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public BindingMode getBindingMode() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsResolving() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    RuntimeNode<T> newRuntimeNode() {
        return null;
    }
}
