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
package packed.internal.componentcache.exam;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import app.packed.app.Main;
import app.packed.component.ComponentConfiguration;
import app.packed.container.ContainerExtensionActivator;
import app.packed.container.ContainerExtension;
import app.packed.util.MethodDescriptor;

/**
 *
 */
public class Dddd {

    // @Provide
    public void foo() {

    }

    @Main
    public void foodd() {}

    // @Main
    @OnX
    public void xfoodd() {}

    static class MyExtension extends ContainerExtension<MyExtension> {

        void methods(ComponentConfiguration cc, List<MethodDescriptor> list) {
            System.out.println("Adding Component with " + list + " methods");
        }

        /** {@inheritDoc} */
        @Override
        protected void onExtensionAdded() {
            System.out.println("ADDED extension");
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    @ContainerExtensionActivator(OnXConfigurator.class)
    public @interface OnX {}
}
