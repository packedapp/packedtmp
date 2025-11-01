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
package app.packed.container.sandbox;

import app.packed.application.App;
import app.packed.assembly.Assembly;
import app.packed.assembly.BaseAssembly;
import app.packed.assembly.DelegatingAssembly;
import app.packed.bean.BeanLocal;
import app.packed.build.hook.BuildHook;
import app.packed.container.ContainerBuildHook;
import app.packed.container.ContainerConfiguration;
import app.packed.service.ProvidableBeanConfiguration;

/**
 *
 */
public class ContainerHookUsage extends BaseAssembly {

    static final BeanLocal<String> L = BeanLocal.of();

    /** {@inheritDoc} */
    @Override
    protected void build() {
        assembly().containers().forEach(c -> c.beans().forEach(b -> b.operations().forEach(o -> o.componentPath())));
        ProvidableBeanConfiguration<String> i = install(String.class);
        i.operations().count();
    }

    public static void main(String[] args) {
        Assembly a = new ContainerHookUsage();

        ContainerBuildHook hook = new ContainerBuildHook() {

            @Override
            public void onNew(ContainerConfiguration configuration) {
                configuration.named(configuration.componentPath().nameFragment(3).toString() + "1");
            }
        };

        a = BuildHook.apply(a, b -> b.observe(hook));

//        a = DelegatingAssembly.applyBuildHook(a, b -> b.observe(hook));
        //a = Assemblies.transform(MethodHandles.lookup(), a, hook);
        // a = hook.transformRecursively(MethodHandles.lookup(), a);

        App.run(a);
    }

    static class MyAss extends DelegatingAssembly {

        /** {@inheritDoc} */
        @Override
        protected Assembly delegateTo() {
            ContainerBuildHook ct = new ContainerBuildHook() {

                @Override
                public void onNew(ContainerConfiguration configuration) {
                    configuration.named(configuration.componentPath().nameFragment(3).toString() + "1");
                }
            };
            // Fungere jo ikke rigtige... Nu har vi to delegating assembly
            return BuildHook.apply(new ContainerHookUsage(), c -> c.transform(ct));
            // WAS
            // return Assemblies.transform(MethodHandles.lookup(), new ContainerTransformerUsage(), ct);

            // Could be a procted method without the lookup object... just using the getClass() instead as the caller
        }
    }
}
