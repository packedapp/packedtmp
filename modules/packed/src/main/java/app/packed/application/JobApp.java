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
package app.packed.application;

import java.util.concurrent.Future;

import app.packed.assembly.Assembly;
import app.packed.assembly.BaseAssembly;
import app.packed.container.Wirelet;
import app.packed.runtime.RunState;

/**
 * Must have a main in a bean with application lifetime.
 *
 * @see app.packed.lifetime.Main
 */
public interface JobApp<T> extends App {

    Future<T> asFuture();

    static <T> T checkedRun(Class<T> resultType, Assembly assembly, Wirelet... wirelets) throws ApplicationException {
        PackedJobApp l = PackedJobApp.BOOTSTRAP_APP.expectsResult(resultType).checkedLaunch(RunState.TERMINATED, assembly, wirelets);
        return resultType.cast(l.result());
    }

    @SuppressWarnings("unchecked")
    static <T> JobApp.Image<T> imageOf(Class<T> resultType, Assembly assembly, Wirelet... wirelets) {
        return new PackedJobApp.AppImage(PackedJobApp.BOOTSTRAP_APP.expectsResult(resultType).imageOf(assembly, wirelets));
    }

    static <T> T run(Class<T> resultType, Assembly assembly, Wirelet... wirelets) {
        PackedJobApp l = PackedJobApp.BOOTSTRAP_APP.expectsResult(resultType).launch(RunState.TERMINATED, assembly, wirelets);
        return resultType.cast(l.result());
    }

    @SuppressWarnings("unchecked")
    static <T> JobApp<T> start(Class<T> resultType, Assembly assembly, Wirelet... wirelets) {
        return PackedJobApp.BOOTSTRAP_APP.expectsResult(resultType).launch(RunState.RUNNING, assembly, wirelets);
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
        PackedApp.BOOTSTRAP_APP.verify(assembly, wirelets);
    }

    interface Image<T> {

        T checkedRun(Wirelet... wirelets) throws ApplicationException;

        JobApp<T> checkedStart(Wirelet... wirelets) throws ApplicationException;

        T run(Wirelet... wirelets);

        JobApp<T> start(Wirelet... wirelets);
    }
}

class TestIt extends BaseAssembly {

    static final JobApp.Image<String> I = JobApp.imageOf(String.class, new TestIt());

    /** {@inheritDoc} */
    @Override
    protected void build() {}

    public static void main(String[] args) {
        String run = JobApp.run(String.class, new TestIt());
        run = I.run();
        System.out.println(run);
    }
}
