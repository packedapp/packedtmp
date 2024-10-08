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
package internal.app.packed.application.repository;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.application.ApplicationRepository;
import app.packed.application.ApplicationTemplate;
import app.packed.application.repository.ApplicationLauncher;
import app.packed.application.repository.ManagedInstance;
import app.packed.build.BuildGoal;
import internal.app.packed.ValueBased;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.application.PackedApplicationTemplate.ApplicationInstallingSource;

/** Implementation of {@link ApplicationRepository}. */
@ValueBased
public sealed abstract class AbstractApplicationRepository<I, H extends ApplicationHandle<I, ?>>
        implements ApplicationRepository<I, H>, ApplicationInstallingSource permits ManagedApplicationRepository, UnmanagedApplicationRepository {

    /** All applications that are installed in the container. */
    private final ConcurrentHashMap<String, LauncherOrFuture<I, H>> handles;

    /** MethodHandle to create the guest bean. */
    private final MethodHandle methodHandle;

    /** The template that is used to install new applications at runtime. */
    private final PackedApplicationTemplate<H> template;

    @SuppressWarnings("unchecked")
    public AbstractApplicationRepository(BuildApplicationRepository bar) {
        this.handles = new ConcurrentHashMap<>();
        bar.handles.forEach((n, h) -> handles.put(n, new PackedApplicationLauncher<>(this instanceof ManagedApplicationRepository, (H) h)));
        this.template = (PackedApplicationTemplate<H>) bar.template;
        this.methodHandle = requireNonNull(bar.mh);
    }
//
//    /** {@inheritDoc} */
//    @Override
//    public Optional<H> handle(String name) {
//        LauncherOrFuture<I, H> f = handles.get(name);
//        if (f instanceof PackedApplicationLauncher<I, H> l) {
//            return Optional.of(l.handle());
//        }
//        return Optional.empty();
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public Stream<H> handles() {
//        return launchers0().map(l -> l.handle());
//    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public final ApplicationLauncher<I> install(Consumer<? super ApplicationInstaller<H>> installer) {
        PackedApplicationInstaller<H> pai = template.newInstaller(this, BuildGoal.IMAGE, methodHandle);
        installer.accept(pai);
        ApplicationSetup setup = pai.toHandle();
        PackedApplicationLauncher<I, H> pal = new PackedApplicationLauncher<>(this instanceof ManagedApplicationRepository, (H) setup.handle());
        handles.put(UUID.randomUUID().toString(), pal);
        return pal;
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ManagedInstance<I>> instances() {
        return launchers().flatMap(l -> l.instances());
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ApplicationLauncher<I>> launcher(String name) {
        LauncherOrFuture<I, H> f = handles.get(name);
        if (f instanceof PackedApplicationLauncher<I, H> l) {
            return Optional.of(l);
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Stream<ApplicationLauncher<I>> launchers() {
        return (Stream) launchers0();
    }

    public Stream<PackedApplicationLauncher<I, H>> launchers0() {
        return handles.values().stream().filter(l -> l instanceof PackedApplicationLauncher).map(l -> (PackedApplicationLauncher<I, H>) l);
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationTemplate<H> template() {
        return template;
    }

    public static Class<?> repositoryClassFor(PackedApplicationTemplate<?> template) {
        return template.containerTemplate().isManaged() ? ManagedApplicationRepository.class : UnmanagedApplicationRepository.class;
    }
}
