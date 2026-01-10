/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.concurrent.usage;

import java.util.Optional;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.BaseAssembly;
import app.packed.concurrent.DaemonJob;
import app.packed.concurrent.DaemonJobContext;
import app.packed.concurrent.ThreadNamespaceMirror;
import app.packed.concurrent.annotations.ScheduleJob;
import app.packed.concurrent.oldscheduling.ScheduledOperationMirror;
import app.packed.concurrent.oldscheduling.SchedulingContext;

/**
 *
 */
public class ScTest2 extends BaseAssembly {

    public static void main(String[] args) throws Exception {
        ApplicationMirror m = App.mirrorOf(new ScTest2());
        m.operations().ofType(ScheduledOperationMirror.class).forEach(c -> {
            IO.println(c.name() + ":" + c.schedule());
        });
        Optional<ThreadNamespaceMirror> o = m.namespace(ThreadNamespaceMirror.class);

//        o.get().daemons().forEach(c -> {
//            IO.println(c.name() + ":" + c.isInteruptAtStop());
//        });


        IO.println(o.get());
        IO.println();

        App.run(new ScTest2());
        Thread.sleep(1000);
    }

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provideInstance("asdasd");
        install(MuB.class);
    }

    public static class MuB {

        @DaemonJob
        public static void dae(DaemonJobContext sc) throws InterruptedException {
            IO.println("Daemon");
            Thread.sleep(100);
        }

        @ScheduleJob(withFixedDelay = 88)
        public static void sch(SchedulingContext sc, ScheduledOperationMirror op) {
//            IO.println("SCHED " + sc.invocationCount());
//            IO.println(op.target());
            if (sc.invocationCount() == 10) {
                sc.cancel();
                IO.println("Bye");
            }
        }
    }
}
