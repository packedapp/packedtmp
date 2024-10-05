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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.build.BuildGoal;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.application.PackedApplicationTemplate.ApplicationInstallingSource;

/**
 *
 */
public final class BuildApplicationRepository implements ApplicationInstallingSource {

    private final ArrayList<Consumer<? super ApplicationInstaller<?>>> children = new ArrayList<>();

    final Map<String, ApplicationHandle<?, ?>> handles = new HashMap<>();

    MethodHandle mh;

    public final PackedApplicationTemplate<?> template;

    /**
     * @param t
     */
    public BuildApplicationRepository(PackedApplicationTemplate<?> template) {
        this.template = requireNonNull(template);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <H extends ApplicationHandle<?, ?>> void add(Consumer<? super ApplicationInstaller<H>> installer) {
        children.add((Consumer) installer);
    }

    public void build() {
        for (Consumer<? super ApplicationInstaller<?>> con : children) {
            PackedApplicationInstaller<?> installer = template.newInstaller(this, BuildGoal.IMAGE, mh);
            con.accept(installer);
            // TODO check that install has been invoked
            handles.put(installer.name, installer.toHandle().handle());
        }
    }

    public void onCodeGenerated(MethodHandle mh) {
        this.mh = requireNonNull(mh);
    }

}
