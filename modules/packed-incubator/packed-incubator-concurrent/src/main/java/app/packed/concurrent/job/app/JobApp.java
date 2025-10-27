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
package app.packed.concurrent.job.app;

import java.util.concurrent.Future;

import app.packed.application.App;
import app.packed.assembly.Assembly;
import app.packed.assembly.BaseAssembly;
import app.packed.container.Wirelet;
import app.packed.runtime.RunState;
import internal.app.packed.util.types.GenericType;

/**
 * Must have a main in a bean with application lifetime.
 *
 * @see app.packed.lifetime.Main
 */

// JobInterface tag et Functional Interface
// Og konverter hele appen til..
public interface JobApp<T> extends App {

    Future<T> asFuture();

    @SuppressWarnings("unchecked")
    static <T> JobApp.Image<T> imageOf(Class<T> resultType, Assembly assembly, Wirelet... wirelets) {
        return new PackedJobApp.AppImage(PackedJobApp.BOOTSTRAP_APP.withExpectsResult(resultType).imageOf(assembly, wirelets));
    }

    static <T> T run(Class<T> resultType, Assembly assembly, Wirelet... wirelets) {
        PackedJobApp l = PackedJobApp.BOOTSTRAP_APP.withExpectsResult(resultType).launch(RunState.TERMINATED, assembly, wirelets);
        return resultType.cast(l.result());
    }

    static <T> T run(GenericType<T> resultType, Assembly assembly, Wirelet... wirelets) {
        PackedJobApp l = PackedJobApp.BOOTSTRAP_APP.withExpectsResult(null).launch(RunState.TERMINATED, assembly, wirelets);
        return resultType.cast(l.result());
    }

    @SuppressWarnings("unchecked")
    static <T> JobApp<T> start(Class<T> resultType, Assembly assembly, Wirelet... wirelets) {
        return PackedJobApp.BOOTSTRAP_APP.withExpectsResult(resultType).launch(RunState.RUNNING, assembly, wirelets);
    }

    /**
     * Builds and verifies a job app.
     *
     * @param assembly
     *            the job app's assembly
     * @param wirelets
     *            optional wirelets
     * @throws RuntimeException
     *             if the job app could not be build
     */
    static void verify(Class<?> resultType, Assembly assembly, Wirelet... wirelets) {
        PackedJobApp.BOOTSTRAP_APP.verify(assembly, wirelets);
    }

    // Kunne vi supportere en Callable????
    // Saa kan folk submitte den til en executor...
    interface Image<T> extends Launcher<T> {

        // Mindfuck
        // Do we unwrap UnhandledApplicationExtension? Hmm
        // Maybe this is why we won't do it
        // We basically just call run as I see it
//        default Callable<T> asCallable(Wirelet... wirelets) {
//            return new Callable<>() {
//
//                @Override
//                public T call() throws Exception {
//                    return run(wirelets);
//                }
//            };
//        }

//        T checkedRun(Wirelet... wirelets) throws UnhandledApplicationException;
//
//        JobApp<T> checkedStart(Wirelet... wirelets) throws UnhandledApplicationException;

        T run();

        JobApp<T> start();
    }

    interface Launcher<T> {

    }
}

class TestIt extends BaseAssembly {

    static final JobApp.Image<String> I = JobApp.imageOf(String.class, new TestIt());

    /** {@inheritDoc} */
    @Override
    protected void build() {}

    public static void main(String[] args) {
        String run = JobApp.run(String.class, new TestIt());
        String run2 = I.run();
        IO.println(run);
        IO.println(run2);
    }
}
//
//static <T> T checkedRun(Class<T> resultType, Assembly assembly, Wirelet... wirelets) throws UnhandledApplicationException {
//  PackedJobApp l = PackedJobApp.BOOTSTRAP_APP.withExpectsResult(resultType).checkedLaunch(RunState.TERMINATED, assembly, wirelets);
//  return resultType.cast(l.result());
//}
