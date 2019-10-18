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

import app.packed.artifact.App;
import app.packed.artifact.ArtifactImage;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentExtension;
import app.packed.container.Bundle;
import app.packed.container.Extension;
import app.packed.container.ExtensionComposer;
import app.packed.container.UseExtension;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.Hook;
import app.packed.hook.HookGroupBuilder;
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
public class FromImage {

    static final ArtifactImage EMPTY = ArtifactImage.of(new Bundle() {
        @Override
        protected void configure() {}
    });

    static final ArtifactImage USE_EXTENSION = ArtifactImage.of(new Bundle() {
        @Override
        public void configure() {
            use(MyExtension.class);
        }
    });
    static final ArtifactImage INSTALL = ArtifactImage.of(new Bundle() {
        @Override
        public void configure() {
            use(ComponentExtension.class).installInstance("foo");
        }
    });
    static final ArtifactImage INSTALL_AUTO_ACTIVATE = ArtifactImage.of(new Bundle() {
        @Override
        public void configure() {
            use(ComponentExtension.class).installInstance(new MyStuff());
        }
    });

    @Benchmark
    public App empty() {
        return App.of(EMPTY);
    }

    @Benchmark
    public App useExtension() {
        return App.of(USE_EXTENSION);
    }

    @Benchmark
    public App install() {
        return App.of(INSTALL);
    }

    @Benchmark
    public App newExtensionAutoActivate() {
        return App.of(INSTALL_AUTO_ACTIVATE);
    }

    static class MyStuff {

        @ActivateMyExtension("X")
        public void foo() {}
    }

    public static class MyExtension extends Extension {

        public void foo(ComponentConfiguration<?> cc, Foo s) {}

        static class Composer extends ExtensionComposer<MyExtension> {

            /** {@inheritDoc} */
            @Override
            protected void configure() {}
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @UseExtension(MyExtension.class)
    public @interface ActivateMyExtension {
        String value();
    }

    static class Foo implements Hook {
        final String s;

        Foo(String s) {
            this.s = s;
        }

        static class Builder implements HookGroupBuilder<Foo> {
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
