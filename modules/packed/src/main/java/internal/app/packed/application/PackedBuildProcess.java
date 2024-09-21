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

import java.util.concurrent.atomic.AtomicLong;

import app.packed.build.BuildProcess;

/**
 *
 */
public final class PackedBuildProcess implements BuildProcess {
    final static AtomicLong PROCESS_ID_BUILDER = new AtomicLong();

    static final ScopedValue<PackedBuildProcess> VAR = ScopedValue.newInstance();

    public final PackedApplicationInstaller<?> application;

    private final long processId;

    Thread thread;

    PackedBuildProcess(PackedApplicationInstaller<?> application) {
        this.processId = PROCESS_ID_BUILDER.incrementAndGet();
        this.application = requireNonNull(application);
        this.thread = Thread.currentThread();
    }

    /** {@inheritDoc} */
    @Override
    public long processId() {
        return processId;
    }

    public static PackedBuildProcess get() {
        return VAR.get();
    }
}
