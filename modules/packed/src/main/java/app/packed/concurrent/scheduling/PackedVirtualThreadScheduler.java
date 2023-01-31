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
package app.packed.concurrent.scheduling;

import java.lang.invoke.MethodHandle;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.packed.extension.ExtensionContext;

/**
 *
 */
@SuppressWarnings("preview")
public class PackedVirtualThreadScheduler {

    final ExtensionContext pec;

    public PackedVirtualThreadScheduler(ExtensionContext pec) {
        this.pec = pec;
    }

    final ExecutorService es = Executors.newVirtualThreadPerTaskExecutor();

    final Set<PackedRunner> runners = ConcurrentHashMap.newKeySet();

    public void schedule(MethodHandle mh, Duration d) {
        PackedRunner pr = new PackedRunner(this, mh, d);
        es.submit(pr);
    }

}
