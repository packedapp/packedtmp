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
package internal.app.packed.application;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Function;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.application.ApplicationTemplate;
import app.packed.build.BuildGoal;
import app.packed.container.ContainerTemplate;
import app.packed.container.Wirelet;
import app.packed.operation.Op;
import app.packed.util.Nullable;
import internal.app.packed.component.ComponentTagHolder;
import internal.app.packed.container.PackedContainerTemplate;
import internal.app.packed.invoke.MethodHandleInvoker.ApplicationBaseLauncher;

/** Implementation of {@link ApplicationTemplate}. */
public record PackedApplicationTemplate<H extends ApplicationHandle<?, ?>>(Class<?> guestClass, @Nullable Op<?> op, Class<? super H> handleClass,
        Function<? super ApplicationInstaller<H>, ? extends ApplicationHandle<?, ?>> handleFactory, PackedContainerTemplate<?> rootContainer,
        Set<String> componentTags) implements ApplicationTemplate<H> {

    public PackedApplicationTemplate(Class<?> guestClass, @Nullable Op<?> op, Class<? super H> handleClass,
            Function<? super ApplicationInstaller<H>, ? extends ApplicationHandle<?, ?>> handleFactory) {
        this(guestClass, op, handleClass, handleFactory, null, Set.of());
    }

    /** {@inheritDoc} */
    @Override
    public PackedApplicationTemplate<H> withComponentTags(String... tags) {
        return new PackedApplicationTemplate<>(guestClass, op, handleClass, handleFactory, rootContainer, ComponentTagHolder.copyAndAdd(componentTags, tags));
    }

    /** {@inheritDoc} */
    @Override
    public PackedApplicationTemplate<H> withRootContainer(ContainerTemplate<?> template) {
        requireNonNull(template);
        return new PackedApplicationTemplate<>(guestClass, op, handleClass, handleFactory, (PackedContainerTemplate<?>) template, componentTags);
    }

    /**
     * Creates a new {@link ApplicationInstaller} from this template.
     *
     * @param goal
     *            the build goal
     * @param wirelets
     *            optional wirelets
     * @return a new application installer
     */
    public PackedApplicationInstaller<H> newInstaller(@Nullable ApplicationInstallingSource source, BuildGoal goal, ApplicationBaseLauncher launcher,
            Wirelet... wirelets) {
        PackedApplicationInstaller<H> installer = new PackedApplicationInstaller<>(this, launcher, goal);
        installer.containerInstaller.processWirelets(wirelets);
        return installer;
    }

    public interface ApplicationInstallingSource {}
}
