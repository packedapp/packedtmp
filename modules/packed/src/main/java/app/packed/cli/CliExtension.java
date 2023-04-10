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

import java.lang.annotation.Annotation;

import app.packed.bean.InstanceBeanConfiguration;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
import app.packed.extension.BeanElement.BeanMethod;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.ContainerLocal;
import app.packed.extension.FrameworkExtension;
import sandbox.extension.bean.BeanHandle;
import sandbox.extension.bean.BeanTemplate;
import sandbox.extension.container.ContainerBuilder;
import sandbox.extension.container.ContainerHandle;
import sandbox.extension.container.ContainerTemplate;
import sandbox.extension.domain.NamespaceTemplate;

/**
 * An extension that
 */
public class CliExtension extends FrameworkExtension<CliExtension> {

    // Vi har 1 per application.. Vi kan fx stadig injecte globalle parameters i enhver lifetime.
    // Det er bare commands der ikke fungere
    static final NamespaceTemplate<CliExtensionNamespace> DOMAIN = NamespaceTemplate.of(CliExtensionNamespace::new);

    static final ContainerLocal<Boolean> LAUNCHED = ContainerLocal.ofContainer();

    /** No. */
    CliExtension() {}

    public CliCommand.Builder addCommand(String... names) {
        return domain(DOMAIN).addCommand(names);
    }

    public <T> InstanceBeanConfiguration<T> newBean(Class<T> beanClass) {
        // Hvad goer den praecis??? Laver bean'en og exiter???

        BeanHandle<T> h = base().beanBuilder(BeanTemplate.EXTERNAL).install(beanClass);
        return new InstanceBeanConfiguration<>(h);
    }

    /** {@inheritDoc} */
    @Override
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {

            /** {@inheritDoc} */
            @Override
            public void hookOnAnnotatedMethod(Annotation hook, BeanMethod method) {
                if (hook instanceof CliCommand c) {
                    domain(DOMAIN).process(CliExtension.this, c, method);
                } else {
                    super.hookOnAnnotatedMethod(hook, method);
                }
            }
        };
    }

    private ContainerBuilder newContainer() {
        if (isInApplicationLifetime()) {
            throw new UnsupportedOperationException("This method must be called from an extension in the application lifetime");
        }
        ContainerBuilder cb = base().containerBuilder(ContainerTemplate.GATEWAY);
        // CT.addEntryPointErrorMessage("Lifetime must container at least one entry point with CliCommand")

        return cb;
    }

    // Lifetime must have at least 1 CliCommand
    public void newContainer(Assembly assembly, Wirelet... wirelets) {
        newContainer().build(assembly, wirelets);
    }

    public ContainerConfiguration newContainer(Wirelet... wirelets) {
        ContainerHandle handle = newContainer().build(wirelets);
        return new ContainerConfiguration(handle);
    }

    @Override
    protected void onApplicationClose() {
        System.out.println("Have commands for " + domain(DOMAIN).commands.keySet());

        super.onApplicationClose();
    }
}
