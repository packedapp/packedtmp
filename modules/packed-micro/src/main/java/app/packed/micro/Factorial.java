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
package app.packed.micro;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 *
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class Factorial {

    static final long[] factorials;

    static final List<Long> facts;

    static {
        factorials = new long[] { 1L, 1L, 1L * 2, 1L * 2 * 3, 1L * 2 * 3 * 4, 1L * 2 * 3 * 4 * 5, 1L * 2 * 3 * 4 * 5 * 6, 1L * 2 * 3 * 4 * 5 * 6 * 7,
                1L * 2 * 3 * 4 * 5 * 6 * 7 * 8, 1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9, 1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10,
                1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11, 1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12,
                1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13, 1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13 * 14,
                1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13 * 14 * 15, 1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13 * 14 * 15 * 16,
                1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13 * 14 * 15 * 16 * 17,
                1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13 * 14 * 15 * 16 * 17 * 18,
                1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13 * 14 * 15 * 16 * 17 * 18 * 19,
                1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13 * 14 * 15 * 16 * 17 * 18 * 19 * 20 };

        ArrayList<Long> ll = new ArrayList<>();
        for (long l : factorials) {
            ll.add(l);
        }
        facts = List.copyOf(ll);
    }

    public static long factorial(int n) {
        return (n < factorials.length) ? factorials[n] : Long.MAX_VALUE;
    }

    public static long factorial2(int n) {
        return (n < factorials.length) ? facts.get(n) : Long.MAX_VALUE;
    }
    @Benchmark
    public static long facLongArray(Blackhole blackHole) {
        int x = 15;
        return factorial(x);
    }

    @Benchmark
    public static long facList(Blackhole blackHole) {
        int x = 15;

        return factorial2(x);
    }

    @Benchmark
    public static long facConstant(Blackhole blackHole) {
        return 4123123123L;
    }
}
