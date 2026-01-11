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

import java.util.function.Function;

import app.packed.application.ApplicationConfiguration;
import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.application.ApplicationTemplate;
import app.packed.bean.Bean;
import app.packed.build.BuildGoal;
import app.packed.container.Wirelet;
import app.packed.util.Nullable;
import internal.app.packed.invoke.MethodHandleInvoker.ApplicationBaseLauncher;

/** Implementation of {@link ApplicationTemplate}. */
public record PackedApplicationTemplate<H extends ApplicationHandle<?, ?>>(Bean<?> bean, Class<? super H> handleClass,
        Function<? super ApplicationInstaller<H>, ? extends ApplicationHandle<?, ?>> handleFactory, boolean isManaged
       ) implements ApplicationTemplate<H> {

    public Class<?> guestClass() {
        return bean.beanClass();
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

    /** {@inheritDoc} */
    @Override
    public boolean isManaged() {
        return isManaged;
    }

    /** Implementation of {@link ApplicationTemplate.Builder}. */
    public static final class Builder<I> implements ApplicationTemplate.Builder<I> {

        private final Bean<I> bean;

        private boolean managed = true;

        public Builder(Bean<I> bean) {
            this.bean = bean;
        }

        /** {@inheritDoc} */
        @Override
        public ApplicationTemplate<ApplicationHandle<I, ApplicationConfiguration>> build() {
            return build(ApplicationHandle.class, ApplicationHandle::new);
        }

        /** {@inheritDoc} */
        @Override
        public <H extends ApplicationHandle<I, ?>> ApplicationTemplate<H> build(Class<? super H> handleClass,
                Function<? super ApplicationInstaller<H>, ? extends H> handleFactory) {
            return new PackedApplicationTemplate<>(bean, handleClass,
                    handleFactory, managed);
        }

        /** {@inheritDoc} */
        @Override
        public Builder<I> unmanaged() {
            this.managed = false;
            return this;
        }
    }
}
