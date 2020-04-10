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
package packed.internal.container.extension;

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

import app.packed.artifact.ArtifactImage;
import app.packed.component.Packlet;
import app.packed.component.SingletonConfiguration;
import app.packed.container.Bundle;
import app.packed.container.Extension;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;

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
    public ArtifactImage empty() {
        Bundle b = new Bundle() {
            @Override
            protected void compose() {}
        };
        return ArtifactImage.of(b);
    }

    @Benchmark
    public ArtifactImage useExtension() {
        Bundle b = new Bundle() {
            @Override
            public void compose() {
                use(MyExtension.class);
            }
        };
        return ArtifactImage.of(b);
    }

    @Benchmark
    public ArtifactImage install() {
        Bundle b = new Bundle() {
            @Override
            public void compose() {
                installInstance("foo");
            }
        };
        return ArtifactImage.of(b);
    }

    @Benchmark
    public ArtifactImage newExtensionUseInstall() {
        Bundle b = new Bundle() {
            @Override
            public void compose() {
                use(MyExtension.class);
                installInstance("foo");
            }
        };
        return ArtifactImage.of(b);
    }

    @Benchmark
    public ArtifactImage newExtensionAutoActivate() {
        Bundle b = new Bundle() {
            @Override
            public void compose() {
                installInstance(new MyStuff());
            }
        };
        return ArtifactImage.of(b);
    }

    static class MyStuff {

        @ActivateMyExtension("X")
        public void foo() {}
    }

    public static class MyExtension extends Extension {

        public void foo(SingletonConfiguration<?> cc, Foo s) {}

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Packlet(extension = MyExtension.class)
    public @interface ActivateMyExtension {
        String value();
    }

    static class Foo implements Hook {
        Foo(String s) {

        }

        static class Builder implements Hook.Builder<Foo> {
            ActivateMyExtension e;

            @OnHook
            public void anno(AnnotatedMethodHook<ActivateMyExtension> h) {
                e = h.annotation();
            }

            /** {@inheritDoc} */
            @Override
            public Foo build() {
                return new Foo(e.toString());
            }
        }
    }

}
