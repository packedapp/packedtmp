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
package internal.app.packed.lifecycle.lifetime.entrypoint;

import java.lang.invoke.MethodHandle;

import app.packed.util.Nullable;
import internal.app.packed.lifecycle.lifetime.runtime.ContainerRunner;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
public class OldEntryPointSetup {

    // sync entrypoint

    // Det er jo i virkeligheden en operation...
    @Nullable
    private MainThreadOfControl mainThread;

    public boolean hasMain() {
        return mainThread != null;
    }

    public MainThreadOfControl mainThread() {
        MainThreadOfControl m = mainThread;
        if (m == null) {
            m = mainThread = new MainThreadOfControl();
        }
        return m;
    }

    public void enter(ContainerRunner runner) {
        // EntryPoint
        if (hasMain()) {
            MainThreadOfControl l = mainThread();
            if (!l.hasExecutionBlock()) {
                return; // runnint as deamon
            }

            try {
                mainThread.generatedMethodHandle.invokeExact(runner.pool());
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
        // todo run execution block

        // shutdown

    }

    public static class MainThreadOfControl {
        public MethodHandle generatedMethodHandle;

        public boolean hasExecutionBlock() {
            return generatedMethodHandle != null;
        }
    }
}
