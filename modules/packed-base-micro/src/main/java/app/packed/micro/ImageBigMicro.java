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

import app.packed.bundle.BaseAssembly;
import app.packed.component.App;
import app.packed.component.Image;
import app.packed.micro.Letters.A;
import app.packed.micro.Letters.NeedsA;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ImageBigMicro {

    static final Image<App> INSTALL31 = App.imageOf(new BaseAssembly() {
        @Override
        public void build() {
            // install(A.class);
            // install(NeedsA.class);
            for (int i = 0; i < 10; i++) {
                link(new BaseAssembly() {
                    @Override
                    public void build() {
                        install(A.class);
                        install(NeedsA.class);
                    }
                });
            }
        }
    });

    static final Image<App> INSTALL253 = App.imageOf(new BaseAssembly() {
        @Override
        public void build() {
            for (int i = 0; i < 4; i++) {
                link(new BaseAssembly() {
                    @Override
                    public void build() {
                        install(A.class);
                        install(NeedsA.class);
                        for (int i = 0; i < 4; i++) {
                            link(new BaseAssembly() {
                                @Override
                                public void build() {
                                    install(A.class);
                                    install(NeedsA.class);
                                    for (int i = 0; i < 4; i++) {
                                        link(new BaseAssembly() {
                                            @Override
                                            public void build() {
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
    static final Image<App> INSTALL253_NOS = App.imageOf(new BaseAssembly() {
        @Override
        public void build() {
            for (int i = 0; i < 4; i++) {
                link(new BaseAssembly() {
                    @Override
                    public void build() {
                        installInstance(new A());
                        installInstance(new NeedsA(new A()));
                        for (int i = 0; i < 4; i++) {
                            link(new BaseAssembly() {
                                @Override
                                public void build() {
                                    installInstance(new A());
                                    installInstance(new NeedsA(new A()));
                                    for (int i = 0; i < 4; i++) {
                                        link(new BaseAssembly() {
                                            @Override
                                            public void build() {
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
    public App install31() {
        return INSTALL31.use();
    }

    @Benchmark
    public App install253() {
        return INSTALL253.use();
    }

    @Benchmark
    public App install253NOS() {
        return INSTALL253_NOS.use();
    }
}
// 30 August 2020
// ImageBigMicro.install253  avgt    5  26602.802 ± 2293.217  ns/op
// ImageBigMicro.install31   avgt    5   3110.621 ±   22.553  ns/op