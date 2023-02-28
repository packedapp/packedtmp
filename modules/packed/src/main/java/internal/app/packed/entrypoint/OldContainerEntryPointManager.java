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
package internal.app.packed.entrypoint;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

import app.packed.bean.BeanConfiguration;
import app.packed.extension.Extension;
import app.packed.util.Nullable;

/** An instance of this class is shared between all entry point extensions for a single application. */
public class OldContainerEntryPointManager {

    @Nullable
    public Class<? extends Extension<?>> dispatcher;

    public BeanConfiguration ebc;

    /** All entry points. */
    public final List<EntryPointConf> entrypoints = new ArrayList<>();

    MethodHandle[] entryPoints;

    Class<? extends Extension<?>> controlledBy;

    /** Any entry point of the lifetime, null if there are none. */
    @Nullable
    public OldEntryPointSetup entryPoint;

    Class<?> resultType;

    public int takeOver(Extension<?> epe, Class<? extends Extension<?>> takeOver) {
        if (this.dispatcher != null) {
            if (takeOver == this.dispatcher) {
                return 0;
            }
            throw new IllegalStateException();
        }
        this.dispatcher = takeOver;
        // ebc = epe.provide(EntryPointDispatcher.class);
        return 0;
    }


    public static class EntryPointConf {

    }

    public static class EntryPointDispatcher {
        EntryPointDispatcher() {}
    }
}

