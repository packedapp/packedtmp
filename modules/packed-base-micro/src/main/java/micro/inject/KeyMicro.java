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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import app.packed.inject.Key;

/**
 *
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class KeyMicro {

    static final Map<String, Integer> MAP_STRING_INTEGER = Map.of();

    static final Field MAP_STRING_INTEGER$ = Arrays.stream(KeyMicro.class.getDeclaredFields()).filter(e -> e.getName().equals("MAP_STRING_INTEGER")).findFirst()
            .get();

    static final String STRING = "";

    static final Field STRING$ = Arrays.stream(KeyMicro.class.getDeclaredFields()).filter(e -> e.getName().equals("STRING")).findFirst().get();
    //
    // @Benchmark
    // public Key<?> keyFromFieldMapStringInteger() {
    // return Key.fromField(MAP_STRING_INTEGER$);
    // }
    //
    // @Benchmark
    // public Key<?> keyFromFieldString() {
    // return Key.fromField(STRING$);
    // }
    //
    // @Benchmark
    // public Key<String> newKeyString() {
    // return new Key<String>() {};
    // }

    // @Benchmark
    public Key<String> KeyOfString() {
        return Key.of(String.class);
    }
}
