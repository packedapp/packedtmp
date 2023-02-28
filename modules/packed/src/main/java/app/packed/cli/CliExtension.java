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

import app.packed.bean.BeanInstallationException;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
import app.packed.extension.BeanElement.BeanMethod;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.ContainerLocal;
import app.packed.extension.FrameworkExtension;
import app.packed.extension.bean.BeanHandle;
import app.packed.extension.bean.BeanTemplate;
import app.packed.extension.container.ContainerBuilder;
import app.packed.extension.container.ContainerHandle;
import app.packed.extension.container.ContainerTemplate;
import app.packed.extension.domain.DomainTemplate;
import app.packed.extension.operation.OperationHandle;
import app.packed.extension.operation.OperationTemplate;

/**
 *
 */

// Must be in main lifetime
public class CliExtension extends FrameworkExtension<CliExtension> {

    static final DomainTemplate<CliExtensionDomain> DOMAIN_TEMPLATE = DomainTemplate.of(CliExtensionDomain::new);

    // Hmm, vi har 2 thingies here

    // Hvis den er lavet i application lifetime er det hele fint
    static final ContainerLocal<CliExtensionDomain> DOMAIN = ContainerLocal.ofApplication(CliExtensionDomain::new);

    static final ContainerLocal<Boolean> LAUNCHED = ContainerLocal.ofContainer();

    /** No */
    CliExtension() {}

    public CliCommand.Builder addCommand(String... names) {
        throw new UnsupportedOperationException();
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
                    CliExtensionDomain domain = DOMAIN.get(this);
                    OperationHandle h = null;
                    if (isInApplicationLifetime()) {
                        h = method.newOperation(OperationTemplate.defaults());

                        // check Launched
                    } else {

                        // EntryPoint.LaunchLifetime
                    }

                    var cd = new CliExtensionDomain.CliC(c, h);
                    if (domain.commands.putIfAbsent(c.name()[0], cd) != null) {
                        throw new BeanInstallationException("Multiple cli commands with the same name, name = " + c.name());
                    }

                    // OT.DEFAULTS.entryPoint();
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
        System.out.println("Have commands for " + DOMAIN.get(this).commands.keySet());

        super.onApplicationClose();
    }
}
