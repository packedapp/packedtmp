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
package aexp.app;

import java.util.List;

import app.packed.component.ComponentConfiguration;
import app.packed.container.Extension;
import app.packed.container.InstantiationContext;
import app.packed.util.MethodDescriptor;

/**
 *
 */
public class SomeExtension extends Extension<SomeExtension> {

    void methods(ComponentConfiguration c, List<MethodDescriptor> l) {
        System.out.println(l);
    }

    /** {@inheritDoc} */
    @Override
    public void onContainerConfigured() {
        // TODO Auto-generated method stub
        super.onContainerConfigured();
    }

    /** {@inheritDoc} */
    @Override
    public void onPrepareContainerInstantiate(InstantiationContext context) {
        System.out.println("Container Instantiated");
        context.put(configuration(), "FooBar");
    }

    /** {@inheritDoc} */
    @Override
    protected void onExtensionAdded() {
        System.out.println("Extension added");
    }
}
