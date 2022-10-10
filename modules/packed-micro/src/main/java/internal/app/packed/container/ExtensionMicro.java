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
package internal.app.packed.container;

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

import app.packed.container.Extension;

/**
 *
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ExtensionMicro {

    @Benchmark
    public MyExtension newExtension() {
        return new MyExtension();
    }

    @Benchmark
    public MyExtension newExtensionCachedLambdaFactory() {
        return ExtensionModelWithCachedSupplier.newInstance(MyExtension.class);
    }

    @Benchmark
    public Extension<?> newExtensionCachedMethodHandle() {
        return ExtensionModel.of(MyExtension.class).newInstance(null);
    }

    @Benchmark
    public MyExtension newExtensionSupplierCachedWrapped() {
        return ExtensionClassCache3.newInstance(MyExtension.class);
    }

    public static class MyExtension extends Extension<MyExtension> {
        MyExtension(){}
    }
}
// packed.internal.container.ExtensionMicro.newExtension                          N/A  avgt    5     2.945 ±  0.043  ns/op
// packed.internal.container.ExtensionMicro.newExtensionCachedLambdaFactory       N/A  avgt    5     5.064 ±  0.044  ns/op
// packed.internal.container.ExtensionMicro.newExtensionCachedMethodHandle        N/A  avgt    5     6.961 ±  0.030  ns/op
// packed.internal.container.ExtensionMicro.newExtensionSupplierCachedWrapped     N/A  avgt    5     5.062 ±  0.035  ns/op
