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
package internal.app.packed.application;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import app.packed.application.ApplicationConfiguration;
import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.bean.Bean;
import app.packed.build.BuildGoal;
import app.packed.container.Wirelet;
import app.packed.lifecycle.LifecycleKind;
import org.jspecify.annotations.Nullable;
import internal.app.packed.invoke.MethodHandleInvoker.ApplicationBaseLauncher;
import sandbox.app.packed.application.registry.ApplicationTemplate;

/** Implementation of {@link ApplicationTemplate}. */
public record PackedApplicationTemplate<H extends ApplicationHandle<?, ?>>(LifecycleKind lifecycleKind, Bean<?> bean, Class<? super H> handleClass,
        Function<? super ApplicationInstaller<H>, ? extends ApplicationHandle<?, ?>> handleFactory) implements ApplicationTemplate<H> {

    public static <I> PackedApplicationTemplate<ApplicationHandle<I, ApplicationConfiguration>> of(LifecycleKind kind, Bean<I> bean) {
        requireNonNull(kind, "lifecycleKind is null");
        requireNonNull(bean, "bean is null");
        return new PackedApplicationTemplate<>(kind, bean, ApplicationHandle.class, ApplicationHandle::new);
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
