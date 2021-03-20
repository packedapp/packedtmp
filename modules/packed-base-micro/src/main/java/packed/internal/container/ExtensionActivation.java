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
package packed.internal.container;

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

import app.packed.component.PreviousKnownAsApp;
import app.packed.component.ApplicationImage;
import app.packed.container.BaseAssembly;
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
public class ExtensionActivation {

    @Benchmark
    public ApplicationImage<PreviousKnownAsApp> empty() {
        BaseAssembly b = new BaseAssembly() {
            @Override
            protected void build() {}
        };
        return PreviousKnownAsApp.buildImage(b);
    }

    @Benchmark
    public ApplicationImage<PreviousKnownAsApp> useExtension() {
        BaseAssembly b = new BaseAssembly() {
            @Override
            public void build() {
                use(MyExtension.class);
            }
        };
        return PreviousKnownAsApp.buildImage(b);
    }

    @Benchmark
    public ApplicationImage<PreviousKnownAsApp> install() {
        BaseAssembly b = new BaseAssembly() {
            @Override
            public void build() {
                installInstance("foo");
            }
        };
        return PreviousKnownAsApp.buildImage(b);
    }

    @Benchmark
    public ApplicationImage<PreviousKnownAsApp> newExtensionUseInstall() {
        BaseAssembly b = new BaseAssembly() {
            @Override
            public void build() {
                use(MyExtension.class);
                installInstance("foo");
            }
        };
        return PreviousKnownAsApp.buildImage(b);
    }

    @Benchmark
    public ApplicationImage<PreviousKnownAsApp> newExtensionAutoActivate() {
        BaseAssembly b = new BaseAssembly() {
            @Override
            public void build() {
                installInstance(new MyStuff());
            }
        };
        return PreviousKnownAsApp.buildImage(b);
    }

    static class MyStuff {

        @ActivateMyExtension("X")
        public void foo() {}
    }

    public static class MyExtension extends Extension {

        // public void foo(SingletonConfiguration<?> cc, Foo s) {}

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    //@Packlet(extension = MyExtension.class)
    public @interface ActivateMyExtension {
        String value();
    }

}
