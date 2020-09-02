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

import app.packed.component.App;

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
        return App.start(VariousBundles.empty());
    }

    @Benchmark
    public App oneComponent() {
        return App.start(VariousBundles.oneComponent());
    }

    @Benchmark
    public App fiveComponents() {
        return App.start(VariousBundles.fiveComponents());
    }

    @Benchmark
    public App oneContainer() {
        return App.start(VariousBundles.oneContainer());
    }

    @Benchmark
    public App emptyAppFromImage() {
        return VariousBundles.EMPTY_IMAGE.start();
    }

    @Benchmark
    public App oneComponentFromImage() {
        return VariousBundles.ONE_COMPONENT_IMAGE.start();
    }

    @Benchmark
    public App fiveComponentsFromImage() {
        return VariousBundles.FIVE_CONTAINER_IMAGE.start();
    }
}
// 18 may 2020
//AppConstructionMicro.emptyApp                 avgt    5   398.362 ±  7.269  ns/op
//AppConstructionMicro.emptyAppFromImage        avgt    5   175.729 ±  2.586  ns/op
//AppConstructionMicro.fiveComponents           avgt    5  1607.786 ± 68.266  ns/op
//AppConstructionMicro.fiveComponentsFromImage  avgt    5   844.006 ± 25.447  ns/op
//AppConstructionMicro.oneComponent             avgt    5   707.705 ±  3.619  ns/op
//AppConstructionMicro.oneComponentFromImage    avgt    5   369.801 ± 10.975  ns/op
//AppConstructionMicro.oneContainer             avgt    5   921.648 ± 48.019  ns/op