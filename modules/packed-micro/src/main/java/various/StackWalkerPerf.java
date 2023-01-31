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
package various;

import java.lang.StackWalker.Option;
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

/**
 *
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class StackWalkerPerf {

    static final StackWalker sw2 = StackWalker.getInstance();

    static final StackWalker sw = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

//    @Benchmark
//    public StackWalker stackWalkerSetup() {
//        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
//    }

    @Benchmark
    public Class<?> stackWalkerCallerClass() {
        return sw.getCallerClass();
    }

    @Benchmark
    public Class<?> reflectionCallerClass() {
        return MethodHandles.lookup().lookupClass();
    }

    @Benchmark
    public int lineNumberWithoutClassRef() {
        return sw2.walk(e->e.limit(1).toList().get(0)).getLineNumber();
    }

    @Benchmark
    public int lineNumberWithClassRef() {
        return sw.walk(e->e.limit(1).toList().get(0)).getLineNumber();
    }
}
