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

import app.packed.application.ApplicationImage;
import app.packed.application.Program;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;

/**
 *
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ImageMicro {

    static final ApplicationImage<Program> EMPTY = Program.newImage(new BaseAssembly() {
        @Override
        protected void build() {}
    });

    static final ApplicationImage<Program> USE_EXTENSION = Program.newImage(new BaseAssembly() {
        @Override
        public void build() {
            use(MyExtension.class);
        }
    });
    static final ApplicationImage<Program> INSTALL = Program.newImage(new BaseAssembly() {
        @Override
        public void build() {
            installInstance("foo");
        }
    });
    static final ApplicationImage<Program> INSTALL_AUTO_ACTIVATE = Program.newImage(new BaseAssembly() {
        @Override
        public void build() {
            installInstance(new MyStuff());
        }
    });

    @Benchmark
    public Program empty() {
        return EMPTY.launch();
    }

    @Benchmark
    public Program useExtension() {
        return USE_EXTENSION.launch();
    }

    @Benchmark
    public Program install() {
        return INSTALL.launch();
    }

    @Benchmark
    public Program newExtensionAutoActivate() {
        return INSTALL_AUTO_ACTIVATE.launch();
    }

    static class MyStuff {

        public void foo() {}
    }

    public static class MyExtension extends Extension {

    }

}
