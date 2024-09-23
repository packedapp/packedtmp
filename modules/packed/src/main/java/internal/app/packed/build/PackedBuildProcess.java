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
package internal.app.packed.build;

import static java.util.Objects.requireNonNull;

import java.lang.ScopedValue.Carrier;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import app.packed.assembly.Assembly;
import app.packed.build.BuildProcess;
import app.packed.util.Nullable;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.assembly.AssemblySetup;

/**
 *
 */
// Here in app.packed.application because of all the packed private fields
public final class PackedBuildProcess implements BuildProcess {

    /** Generates build process id's. */
    private final static AtomicLong PROCESS_ID_BUILDER = new AtomicLong();

    private static final ScopedValue<PackedBuildProcess> VAR = ScopedValue.newInstance();

    /** The active assembly. */
    @Nullable
    private AssemblySetup active;

    public final PackedApplicationInstaller<?> application;

    private final long processId = PROCESS_ID_BUILDER.incrementAndGet();

    private Thread thread;

    public PackedBuildProcess(PackedApplicationInstaller<?> application) {
        this.application = requireNonNull(application);
        this.thread = Thread.currentThread();
    }

    public Carrier carrier() {
        return ScopedValue.where(PackedBuildProcess.VAR, this);
    }

    public void checkBuildThread() {
        Thread t = thread;
        if (Thread.currentThread() != t) {
            if (t == null) {
                throw new IllegalStateException("Not building");
            }
            throw new IllegalStateException("Not on Build thread");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Class<? extends Assembly>> currentAssembly() {
        AssemblySetup a = active;
        if (a == null) {
            return Optional.empty();
        }
        return Optional.of(a.assembly.getClass());
    }

    public void finished() {
        thread = null;
    }

    public void pop(AssemblySetup assembly) {
        this.active = assembly;
    }

    /** {@inheritDoc} */
    @Override
    public long processId() {
        return processId;
    }

    public AssemblySetup push(AssemblySetup assembly) {
        AssemblySetup current = active;
        this.active = requireNonNull(assembly);
        return current;
    }

    public static PackedBuildProcess get() {
        return VAR.get();
    }
}
