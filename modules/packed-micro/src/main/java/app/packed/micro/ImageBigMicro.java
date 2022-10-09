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

import app.packed.application.App;
import app.packed.container.BaseAssembly;
import app.packed.micro.Letters.A;
import app.packed.micro.Letters.NeedsA;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ImageBigMicro {

    static final App.Launcher INSTALL31 = App.newImage(new BaseAssembly() {
        @Override
        public void build() {
            lookup(MethodHandles.lookup());
            // install(A.class);
            // install(NeedsA.class);
            for (int i = 0; i < 10; i++) {
                link(new BaseAssembly() {
                    @Override
                    public void build() {
                        lookup(MethodHandles.lookup());
                        install(A.class);
                        install(NeedsA.class);
                    }
                });
            }
        }
    });

    static final App.Launcher INSTALL253 = App.newImage(new BaseAssembly() {
        @Override
        public void build() {
            lookup(MethodHandles.lookup());
            for (int i = 0; i < 4; i++) {
                link(new BaseAssembly() {
                    @Override
                    public void build() {
                        lookup(MethodHandles.lookup());
                        install(A.class);
                        install(NeedsA.class);
                        for (int i = 0; i < 4; i++) {
                            link(new BaseAssembly() {
                                @Override
                                public void build() {
                                    lookup(MethodHandles.lookup());
                                    install(A.class);
                                    install(NeedsA.class);
                                    for (int i = 0; i < 4; i++) {
                                        link(new BaseAssembly() {
                                            @Override
                                            public void build() {
                                                lookup(MethodHandles.lookup());
                                                install(A.class);
                                                install(NeedsA.class);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
    });
    static final App.Launcher INSTALL253_NOS = App.newImage(new BaseAssembly() {
        @Override
        public void build() {
            lookup(MethodHandles.lookup());
            for (int i = 0; i < 4; i++) {
                link(new BaseAssembly() {
                    @Override
                    public void build() {
                        lookup(MethodHandles.lookup());
                        installInstance(new A());
                        installInstance(new NeedsA(new A()));
                        for (int i = 0; i < 4; i++) {
                            link(new BaseAssembly() {
                                @Override
                                public void build() {
                                    lookup(MethodHandles.lookup());
                                    installInstance(new A());
                                    installInstance(new NeedsA(new A()));
                                    for (int i = 0; i < 4; i++) {
                                        link(new BaseAssembly() {
                                            @Override
                                            public void build() {
                                                lookup(MethodHandles.lookup());
                                                installInstance(new A());
                                                installInstance(new NeedsA(new A()));
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
    });

    @Benchmark
    public Void install31() {
        INSTALL31.run();
        return null;
    }

    @Benchmark
    public Void install253() {
        INSTALL253.run();
        return null;
    }

    @Benchmark
    public Void install253NOS() {
        INSTALL253_NOS.run();
        return null;
    }
}
// 30 August 2020
// ImageBigMicro.install253  avgt    5  26602.802 ± 2293.217  ns/op
// ImageBigMicro.install31   avgt    5   3110.621 ±   22.553  ns/op