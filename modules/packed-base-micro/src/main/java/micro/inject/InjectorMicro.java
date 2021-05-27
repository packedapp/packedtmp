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
package micro.inject;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import app.packed.container.BaseAssembly;
import app.packed.service.ServiceExtension;
import packed.internal.service.sandbox.Injector;

/**
 *
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class InjectorMicro {

    @Benchmark
    public Injector emptyInjector() {
        return Injector.configure(c -> {});
    }

    @Benchmark
    public Injector injectorStringInstance() {
        return Injector.configure(c -> c.provideInstance("foo"));
    }

    @Benchmark
    public Injector injectorStringExportedInstance() {
        return Injector.of(new SimpleInjectorAssembly());
    }

    @Benchmark
    public String injectorStringInstanceUse() {
        return Injector.of(new SimpleInjectorAssembly()).use(String.class);
    }

    @Benchmark
    public Injector injectorServiceNeedingString() {
        return Injector.configure(c -> {
            c.lookup(MethodHandles.lookup());
            c.provideInstance("foo");
            c.provide(NeedsString.class);
        });
    }

    public static class NeedsString {
        NeedsString(String s) {}
    }

    static class SimpleInjectorAssembly extends BaseAssembly {

        @Override
        public void build() {
            ServiceExtension e = use(ServiceExtension.class);
            provideInstance("Hey");
            e.export(String.class);
        }
    }
}
