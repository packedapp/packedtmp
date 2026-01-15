/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.application.repository;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.build.BuildGoal;
import app.packed.lifecycle.LifecycleKind;
import internal.app.packed.ValueBased;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.application.PackedApplicationTemplate.ApplicationInstallingSource;
import internal.app.packed.invoke.MethodHandleInvoker.ApplicationBaseLauncher;
import sandbox.app.packed.application.registry.ApplicationRegistry;
import sandbox.app.packed.application.registry.LaunchableApplication;

/** Implementation of {@link ApplicationRepository}. */
@ValueBased
public sealed abstract class AbstractApplicationRepository<I, H extends ApplicationHandle<I, ?>>
        implements ApplicationRegistry<I, H>, ApplicationInstallingSource permits ManagedApplicationRepository, UnmanagedApplicationRepository {

    /** All applications that are installed or being installed into the container. */
    private final ConcurrentHashMap<String, ApplicationLauncherOrFuture<I, H>> applications;

    /** MethodHandle to create the guest bean. */
    private final ApplicationBaseLauncher applicationLauncher;

    /** The template that is used to install new applications at runtime. */
    private final PackedApplicationTemplate<H> template;

    @SuppressWarnings("unchecked")
    public AbstractApplicationRepository(BuildApplicationRepository bar) {
        this.applications = new ConcurrentHashMap<>();
        bar.handles.forEach((n, h) -> applications.put(n, new PackedInstalledApplication<>(this instanceof ManagedApplicationRepository, (H) h)));
        this.template = (PackedApplicationTemplate<H>) bar.template;
        this.applicationLauncher = requireNonNull(bar.mh);
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<LaunchableApplication<I>> application(String name) {
        ApplicationLauncherOrFuture<I, H> f = applications.get(name);
        return f instanceof PackedInstalledApplication<I, H> l ? Optional.of(l) : Optional.empty();
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public final Stream<LaunchableApplication<I>> applications() {
        return (Stream) applications0();
    }

    /**
     * {@return a stream of all applications that been successfully installed, excludes applications that are in the process
     * of being installed}
     */
    public final Stream<PackedInstalledApplication<I, H>> applications0() {
        return applications.values().stream().filter(l -> l instanceof PackedInstalledApplication).map(l -> (PackedInstalledApplication<I, H>) l);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public final LaunchableApplication<I> install(Consumer<? super ApplicationInstaller<H>> installer) {
        PackedApplicationInstaller<H> pai = template.newInstaller(this, BuildGoal.IMAGE, applicationLauncher);
        installer.accept(pai);
        ApplicationSetup setup = pai.toSetup();

        PackedInstalledApplication<I, H> pal = new PackedInstalledApplication<>(this instanceof ManagedApplicationRepository, (H) setup.handle());
        applications.put(UUID.randomUUID().toString(), pal);
        return pal;
    }

    public static Class<?> repositoryClassFor(PackedApplicationTemplate<?> template) {
        return template.lifecycleKind() == LifecycleKind.MANAGED ? ManagedApplicationRepository.class : UnmanagedApplicationRepository.class;
    }
}
