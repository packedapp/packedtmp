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

import java.util.HashMap;
import java.util.Map;

import app.packed.application.ApplicationHandle;
import app.packed.build.BuildGoal;

/**
 *
 */
public class BuildApplicationRepository {

    private Map<String, ApplicationHandle<?, ?>> handles = new HashMap<>();

    public Map<String, ApplicationHandle<?, ?>> forInit() {
        return handles;
    }

    public void build(ApplicationSetup parent, Fut fut) {

        PackedApplicationTemplate pat = (PackedApplicationTemplate) fut.template;

        PackedApplicationInstaller pai = pat.newInstaller(BuildGoal.IMAGE);
        fut.installer.accept(pai);

        ApplicationHandle<?, ?> ah = pai.application.handle();

        fut.bar.handles.put(fut.name, ah);
    }
}
