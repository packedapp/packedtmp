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
package internal.app.packed.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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

import app.packed.application.App;
import app.packed.assembly.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;

/**
 *
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ExtensionActivation {

    @Benchmark
    public App.Image empty() {
        BaseAssembly b = new BaseAssembly() {
            @Override
            protected void build() {}
        };
        return App.imageOf(b);
    }

    @Benchmark
    public App.Image useExtension() {
        BaseAssembly b = new BaseAssembly() {
            @Override
            public void build() {
                use(MyExtension.class);
            }
        };
        return App.imageOf(b);
    }

    @Benchmark
    public App.Image install() {
        BaseAssembly b = new BaseAssembly() {
            @Override
            public void build() {
                installInstance("foo");
            }
        };
        return App.imageOf(b);
    }

    @Benchmark
    public App.Image newExtensionUseInstall() {
        BaseAssembly b = new BaseAssembly() {
            @Override
            public void build() {
                use(MyExtension.class);
                installInstance("foo");
            }
        };
        return App.imageOf(b);
    }

    @Benchmark
    public App.Image newExtensionAutoActivate() {
        BaseAssembly b = new BaseAssembly() {
            @Override
            public void build() {
                installInstance(new MyStuff());
            }
        };
        return App.imageOf(b);
    }

    static class MyStuff {

        @ActivateMyExtension("X")
        public void foo() {}
    }

    public static class MyExtension extends Extension<MyExtension> {

        /**
         * @param handle
         */
        protected MyExtension(ExtensionHandle handle) {
            super(handle);
        }
        // public void foo(SingletonConfiguration<?> cc, Foo s) {}

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    //@Packlet(extension = MyExtension.class)
    public @interface ActivateMyExtension {
        String value();
    }

}
