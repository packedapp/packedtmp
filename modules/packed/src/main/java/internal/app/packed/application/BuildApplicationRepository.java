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

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationTemplate;
import app.packed.build.BuildGoal;

/**
 *
 */
public final class BuildApplicationRepository {

    private final Map<String, Fut> buildThese = new HashMap<>();

    final Map<String, ApplicationHandle<?, ?>> handles = new HashMap<>();

    final HashMap<ApplicationTemplate<?>, MethodHandle> ms = new HashMap<>();

    public void add(PackedApplicationTemplate<?> template, String name, Consumer<?> installer) {
        buildThese.putIfAbsent(name, new Fut(template, name, installer));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void build() {
        for (Fut f : buildThese.values()) {

            PackedApplicationInstaller<?> pai = f.template.newInstaller(BuildGoal.IMAGE);
            pai.launcher = requireNonNull(ms.get(f.template));
            pai.bar = this;
            ((Consumer) f.installer).accept(pai);

            ApplicationHandle<?, ?> ah = pai.application.handle();

            handles.put(f.name, ah);
        }
    }

    Map<String, ApplicationHandle<?, ?>> forInit() {
        return handles;
    }

    public void onLauncherBuild(ApplicationTemplate<?> template, MethodHandle mh) {
        ms.put(template, mh);
    }

    private record Fut(PackedApplicationTemplate<?> template, String name, Consumer<?> installer) {}
}
