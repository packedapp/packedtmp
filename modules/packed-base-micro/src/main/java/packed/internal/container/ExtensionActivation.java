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
import java.util.function.BiConsumer;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import app.packed.component.ComponentConfiguration;
import app.packed.container.ArtifactImageInterface;
import app.packed.container.Bundle;
import app.packed.container.Extension;
import app.packed.container.ExtensionActivator;
import app.packed.container.ExtensionHookProcessor;
import app.packed.hook.AnnotatedMethodHook;
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
    public ArtifactImageInterface empty() {
        Bundle b = new Bundle() {};
        return ArtifactImageInterface.of(b);
    }

    @Benchmark
    public ArtifactImageInterface useExtension() {
        Bundle b = new Bundle() {
            @Override
            public void configure() {
                use(MyExtension.class);
            }
        };
        return ArtifactImageInterface.of(b);
    }

    @Benchmark
    public ArtifactImageInterface install() {
        Bundle b = new Bundle() {
            @Override
            public void configure() {
                install("foo");
            }
        };
        return ArtifactImageInterface.of(b);
    }

    @Benchmark
    public ArtifactImageInterface newExtensionUseInstall() {
        Bundle b = new Bundle() {
            @Override
            public void configure() {
                use(MyExtension.class);
                install("foo");
            }
        };
        return ArtifactImageInterface.of(b);
    }

    @Benchmark
    public ArtifactImageInterface newExtensionAutoActivate() {
        Bundle b = new Bundle() {
            @Override
            public void configure() {
                install(new MyStuff());
            }
        };
        return ArtifactImageInterface.of(b);
    }

    static class MyStuff {

        @ActivateMyExtension
        public void foo() {

        }
    }

    public static class MyExtension extends Extension {
        protected void set(ComponentConfiguration a) {}
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @ExtensionActivator(Builder.class)
    public @interface ActivateMyExtension {

    }

    static class Builder extends ExtensionHookProcessor<MyExtension> {

        @OnHook
        public void anno(AnnotatedMethodHook<ActivateMyExtension> h) {

        }

        /** {@inheritDoc} */
        @Override
        public BiConsumer<ComponentConfiguration, MyExtension> onBuild() {
            return (a, b) -> {
                b.set(a);
            };
        }
    }
}
