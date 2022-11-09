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
package app.packed.micro.application;

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

/**
 *
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class AppConstructionMicro {

    @Benchmark
    public void appRun000Beans() {
        App.run(VariousImages.of(0));
    }

    @Benchmark
    public void appRun001Beans() {
        App.run(VariousImages.of(1));
    }

    @Benchmark
    public void appRun005Beans() {
        App.run(VariousImages.of(5));
    }

    @Benchmark
    public void appRun050Beans() {
        App.run(VariousImages.of(50));
    }

    @Benchmark
    public void appRun500Beans() {
        App.run(VariousImages.of(500));
    }

    @Benchmark
    public void appImage000Beans() {
        VariousImages.EMPTY_IMAGE.run();
    }

    @Benchmark
    public void appImage001Beans() {
        VariousImages.ONE_BEAN_IMAGE.run();
    }

    @Benchmark
    public void appImage005Beans() {
        VariousImages.FIVE_BEAN_IMAGE.run();
    }

    @Benchmark
    public void appImage050Beans() {
        VariousImages.FIFTY_BEAN_IMAGE.run();
    }

    @Benchmark
    public void appImage500Beans() {
        VariousImages.FIVEHUNDRED_BEAN_IMAGE.run();
    }
}

//9 Nov 2022 - M1
//AppConstructionMicro.appRun000Beans    avgt    5     243,449 ±    3,099  ns/op
//AppConstructionMicro.appRun001Beans    avgt    5    1584,016 ±    9,651  ns/op
//AppConstructionMicro.appRun005Beans    avgt    5    6934,713 ±  139,177  ns/op
//AppConstructionMicro.appRun050Beans    avgt    5   69777,371 ± 1354,975  ns/op
//AppConstructionMicro.appRun500Beans    avgt    5  701972,811 ± 3994,279  ns/op

//10 Okt 2022 - M1
//AppConstructionMicro.appRun000Beans    avgt    5       61,833 ±     0,314  ns/op
//AppConstructionMicro.appRun001Beans    avgt    5     1424,898 ±    30,154  ns/op
//AppConstructionMicro.appRun005Beans    avgt    5     6592,142 ±    62,136  ns/op
//AppConstructionMicro.appRun050Beans    avgt    5    84191,839 ±   563,876  ns/op
//AppConstructionMicro.appRun500Beans    avgt    5  3757036,330 ± 47382,205  ns/op

// 25 May 2022 - M1
//AppConstructionMicro.emptyApp                 avgt    5    93,113 ±  3,010  ns/op
//AppConstructionMicro.emptyAppFromImage        avgt    5    20,863 ±  1,034  ns/op
//AppConstructionMicro.fiveComponents           avgt    5  6484,989 ± 66,484  ns/op
//AppConstructionMicro.fiveComponentsFromImage  avgt    5    37,671 ±  0,813  ns/op
//AppConstructionMicro.oneComponent             avgt    5  1373,740 ±  2,870  ns/op
//AppConstructionMicro.oneComponentFromImage    avgt    5    30,983 ±  4,578  ns/op
//AppConstructionMicro.oneContainer             avgt    5   161,865 ±  4,447  ns/op

// 11 Dec 2021 - M1
//AppConstructionMicro.emptyApp                 avgt    5   78.152 ± 0.325  ns/op
//AppConstructionMicro.emptyAppFromImage        avgt    5    9.958 ± 0.117  ns/op
//AppConstructionMicro.fiveComponents           avgt    5  373.286 ± 1.811  ns/op
//AppConstructionMicro.fiveComponentsFromImage  avgt    5   29.120 ± 0.045  ns/op
//AppConstructionMicro.oneComponent             avgt    5  159.516 ± 0.851  ns/op
//AppConstructionMicro.oneComponentFromImage    avgt    5   15.450 ± 0.085  ns/op
//AppConstructionMicro.oneContainer             avgt    5  133.578 ± 0.890  ns/op

// 18 may 2020
//AppConstructionMicro.emptyApp                 avgt    5   398.362 ±  7.269  ns/op
//AppConstructionMicro.emptyAppFromImage        avgt    5   175.729 ±  2.586  ns/op
//AppConstructionMicro.fiveComponents           avgt    5  1607.786 ± 68.266  ns/op
//AppConstructionMicro.fiveComponentsFromImage  avgt    5   844.006 ± 25.447  ns/op
//AppConstructionMicro.oneComponent             avgt    5   707.705 ±  3.619  ns/op
//AppConstructionMicro.oneComponentFromImage    avgt    5   369.801 ± 10.975  ns/op
//AppConstructionMicro.oneContainer             avgt    5   921.648 ± 48.019  ns/op