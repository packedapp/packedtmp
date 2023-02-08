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
package app.packed.lifetime;

import app.packed.bean.BeanKind;
import app.packed.context.ContextTemplate;
import app.packed.operation.OperationTemplate;
import internal.app.packed.lifetime.PackedBeanLifetimeTemplate;

/**
 *
 */
// Contexts, Args
public sealed interface BeanLifetimeTemplate extends LifetimeTemplate permits PackedBeanLifetimeTemplate {

    /**
     * The lifetime of the bean is identical to that of its container.
     * <p>
     * A single instance of the bean will be created (if the instance was not already provided when installing the bean)
     * when the container is instantiated. Where after its lifecycle will follow that of its parent.
     * <p>
     * Beans that are part of the container's lifecycle
     */
    BeanLifetimeTemplate CONTAINER = new PackedBeanLifetimeTemplate(BeanKind.CONTAINER);

    /**
     * A single instance of the bean is created lazily when needed.
     * <p>
     *
     * @see BeanInstaller#install(Class)
     * @see BeanInstaller#installIfAbsent(Class, Consumer)
     * @see BeanInstaller#install(Op)
     * @see Map#isEmpty()
     */
    BeanLifetimeTemplate LAZY = new PackedBeanLifetimeTemplate(BeanKind.LAZY);

    /**
     * A bean that no instances. And hence no lifecycle then bean instance can go through.
     * <p>
     * The lifetime of the bean is identical to its container. A bean can never
     * <p>
     * When installing the bean either {@link BeanInstaller}
     *
     * @see BeanInstaller#install(Class)
     * @see BeanInstaller#installIfAbsent(Class, java.util.function.Consumer)
     * @see BeanInstaller#installWithoutSource()
     */
    BeanLifetimeTemplate STATIC = new PackedBeanLifetimeTemplate(BeanKind.STATIC);

    // For example, verifying. The lifetime of the bean is fully managed by someone else.
    // All operations on the bean must the bean instance!
    // Der er lidt 2 versioner her. Ind uden lifecycle, og en med
    // Med situationerne hvor jeg kommer med bean instancen. Men ellers
    // fungere alt som foer. Det maa vaere Builder
    // What if we do not want to fail lifecycle?
    BeanLifetimeTemplate EXTERNAL = new PackedBeanLifetimeTemplate(BeanKind.MANYTON);

    BeanLifetimeTemplate MANYTON = new PackedBeanLifetimeTemplate(BeanKind.MANYTON);

    // Create a new builder in order to create a custom lifetime for the bean.

    static Builder builderManyton() {
        return new PackedBeanLifetimeTemplate.PackedBuilder();
    }

    sealed interface Builder permits PackedBeanLifetimeTemplate.PackedBuilder {

        /**
         * Creates and returns a new template.
         *
         * @return the new template
         */
        BeanLifetimeTemplate build();

        default Builder inContext(ContextTemplate template) {
            // This means Context args are added to all operations.

            // builderManyton().inContext(WebContext.template).builder();
            return this;
        }

        Builder withLifetime(OperationTemplate bot);

        // No seperet MH for starting, part of init
        Builder autoStart();

        // Ideen er lidt at vi som default returnere Object vil jeg mene
        // Men man kunne sige fx AbstractEntityBean at bean.init returnere
        Builder createdAs(Class<?> clazz);
    }
}

// !(Static, Container, Lazy) -> BeanInstance must be provided for start/stop
