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
package packed.internal.extension;

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
import app.packed.component.ComponentConfiguration;
import app.packed.container.BaseBundle;
import app.packed.extension.ActivateExtension;
import app.packed.extension.AnnotatedMethodHook;
import app.packed.extension.Extension;
import app.packed.extension.OnHook;
import app.packed.extension.OnHookAggregateBuilder;

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
        BaseBundle b = new BaseBundle() {};
        return ArtifactImage.of(b);
    }

    @Benchmark
    public ArtifactImage useExtension() {
        BaseBundle b = new BaseBundle() {
            @Override
            public void configure() {
                use(MyExtension.class);
            }
        };
        return ArtifactImage.of(b);
    }

    @Benchmark
    public ArtifactImage install() {
        BaseBundle b = new BaseBundle() {
            @Override
            public void configure() {
                install("foo");
            }
        };
        return ArtifactImage.of(b);
    }

    @Benchmark
    public ArtifactImage newExtensionUseInstall() {
        BaseBundle b = new BaseBundle() {
            @Override
            public void configure() {
                use(MyExtension.class);
                install("foo");
            }
        };
        return ArtifactImage.of(b);
    }

    @Benchmark
    public ArtifactImage newExtensionAutoActivate() {
        BaseBundle b = new BaseBundle() {
            @Override
            public void configure() {
                install(new MyStuff());
            }
        };
        return ArtifactImage.of(b);
    }

    static class MyStuff {

        @ActivateMyExtension("X")
        public void foo() {}
    }

    public static class MyExtension extends Extension {

        @OnHook(Builder.class)
        public void foo(ComponentConfiguration cc, String s) {}
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @ActivateExtension(MyExtension.class)
    public @interface ActivateMyExtension {
        String value();
    }

    static class Builder implements OnHookAggregateBuilder<String> {
        ActivateMyExtension e;

        public void anno(AnnotatedMethodHook<ActivateMyExtension> h) {
            e = h.annotation();
        }

        /** {@inheritDoc} */
        @Override
        public String build() {
            return e.toString();
        }
    }
}
