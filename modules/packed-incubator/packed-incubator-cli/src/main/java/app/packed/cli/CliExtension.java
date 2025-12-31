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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.assembly.Assembly;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.container.ContainerBuildLocal;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerInstaller;
import app.packed.container.ContainerTemplate;
import app.packed.container.Wirelet;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.FrameworkExtension;

/**
 * An extension that
 */

// Det er faktisk kun CliCommand der stjaeler execution
// CliOption er ikke noget problem
public class CliExtension extends FrameworkExtension<CliExtension> {

    /**
     * @param handle
     */
    CliExtension(ExtensionHandle<CliExtension> handle) {
        super(handle);
    }

    // Vi har 1 per application.. Vi kan fx stadig injecte globalle parameters i enhver lifetime.
    // Det er bare commands der ikke fungere
    static final ContainerBuildLocal<Boolean> LAUNCHED = ContainerBuildLocal.of();

    /** No. */

    public CliCommandConfiguration addCliCommand(Consumer<CliCommandContext> action) {
        return namespace().addCliCommand(action);
    }

    // Must be application scoped (or same lifetime as the namespace, or prototype, idk)
    // Probably need some BiConsumerThrow
    public <T> CliCommandConfiguration addCliCommand(InstanceBeanConfiguration<T> bean, BiConsumer<T, CliCommandContext> action) {
        throw new UnsupportedOperationException();
    }

    public <T> InstanceBeanConfiguration<T> newBean(Class<T> beanClass) {
        // Hvad goer den praecis??? Laver bean'en og exiter???

//        BeanHandle<T> h = base().beanBuilder(BeanTemplate).install(beanClass);
//        return new InstanceBeanConfiguration<>(h);
        throw new UnsupportedOperationException();
    }

    CliNamespaceHandle namespaceHandle() {
        return applicationRoot().namespaceLazy(CliNamespaceHandle.TEMPLATE, "main");
    }

    public CliNamespaceConfiguration namespace() {
        return namespaceHandle().configuration(this);
    }

    private ContainerInstaller<?> newContainer() {
        if (isInApplicationLifetime()) {
            throw new UnsupportedOperationException("This method must be called from an extension in the application lifetime");
        }
        ContainerInstaller<?> cb = base().newContainer(ContainerTemplate.GATEWAY);
        // CT.addEntryPointErrorMessage("Lifetime must container at least one entry point with CliCommand")

        return cb;
    }

    // Lifetime must have at least 1 CliCommand
    public void newContainer(Assembly assembly, Wirelet... wirelets) {
        newContainer().install(assembly, wirelets);
    }

    public ContainerConfiguration newContainer(Wirelet... wirelets) {
        ContainerHandle<?> handle = newContainer().install(wirelets);
        return handle.configuration();
    }

    @Override
    protected void onClose() {
        IO.println("Have commands for " + namespaceHandle().commands.keySet());

        super.onClose();
    }
}
