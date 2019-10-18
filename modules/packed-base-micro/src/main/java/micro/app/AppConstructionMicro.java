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
package micro.app;

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

import app.packed.artifact.App;

/**
 *
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class AppConstructionMicro {

    @Benchmark
    public App emptyApp() {
        return App.of(VariousBundles.empty());
    }

    @Benchmark
    public App oneComponent() {
        return App.of(VariousBundles.oneComponent());
    }

    @Benchmark
    public App fiveComponents() {
        return App.of(VariousBundles.fiveComponents());
    }

    @Benchmark
    public App oneContainer() {
        return App.of(VariousBundles.oneContainer());
    }

    @Benchmark
    public App emptyAppFromImage() {
        return App.of(VariousBundles.EMPTY_IMAGE);
    }

    @Benchmark
    public App oneComponentFromImage() {
        return App.of(VariousBundles.ONE_COMPONENT_IMAGE);
    }

    @Benchmark
    public App fiveComponentsFromImage() {
        return App.of(VariousBundles.FIVE_CONTAINER_IMAGE);
    }

}
