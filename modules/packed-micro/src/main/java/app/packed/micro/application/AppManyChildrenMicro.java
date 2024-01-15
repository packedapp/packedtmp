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
package app.packed.micro.application;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import app.packed.application.App;
import app.packed.assembly.BaseAssembly;

/**
 *
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class AppManyChildrenMicro {

    @Param({ "0", "1", "10", "100", "1000", "10000", "100000", "1000000" })
    static int size;

    @Benchmark
    public void manyChildren() {
        App.run(new BaseAssembly() {
            @Override
            protected void build() {
                for (int i = 0; i < size; i++) {
                    link(new TAssembly(Integer.toString(i)));
                }
            }
        });
    }

    static class TAssembly extends BaseAssembly {

        final String name;

        TAssembly(String name) {
            this.name = name;
        }

        @Override
        protected void build() {
            named(name);
        }
    }
}

// I think we have the naming issue again, but this time with containers.

//AppManyChildrenMicro.manyChildren        0  avgt    5         65,989 ±         1,666  ns/op
//AppManyChildrenMicro.manyChildren        1  avgt    5        184,667 ±         6,334  ns/op
//AppManyChildrenMicro.manyChildren       10  avgt    5       1196,666 ±        27,391  ns/op
//AppManyChildrenMicro.manyChildren      100  avgt    5      10951,434 ±        71,562  ns/op
//AppManyChildrenMicro.manyChildren     1000  avgt    5      96571,737 ±      2972,824  ns/op
//AppManyChildrenMicro.manyChildren    10000  avgt    5     990598,824 ±     37506,454  ns/op
//AppManyChildrenMicro.manyChildren   100000  avgt    5   16812652,692 ±   1396797,559  ns/op
//AppManyChildrenMicro.manyChildren  1000000  avgt    5  431405683,233 ± 284982335,052  ns/op