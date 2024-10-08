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
package app.packed.concurrent.usage;

import java.util.Optional;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.BaseAssembly;
import app.packed.concurrent.Daemon;
import app.packed.concurrent.DaemonContext;
import app.packed.concurrent.ScheduleRecurrent;
import app.packed.concurrent.ScheduledOperationMirror;
import app.packed.concurrent.SchedulingContext;
import app.packed.concurrent.ThreadNamespaceMirror;
import app.packed.concurrent.ThreadOperationMirror;

/**
 *
 */
public class ScTest2 extends BaseAssembly {

    public static void main(String[] args) throws Exception {
        ApplicationMirror m = App.mirrorOf(new ScTest2());
        m.operations(ScheduledOperationMirror.class).forEach(c -> {
            System.out.println(c.name() + ":" + c.schedule());
        });
        m.operations(ThreadOperationMirror.class).forEach(c -> {
            System.out.println(c.name() + ":" + c);
        });
        Optional<ThreadNamespaceMirror> o = m.namespace(ThreadNamespaceMirror.class);

        o.get().scheduledOperations().forEach(c -> {
            System.out.println(c.name() + ":" + c.schedule());
        });
        o.get().daemonOperations().forEach(c -> {
            System.out.println(c.name() + ":" + c.isInteruptAtStop());
        });


        System.out.println(o.get());
        System.out.println();

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

        @Daemon
        public static void dae(DaemonContext sc) throws InterruptedException {
            System.out.println("Daemon");
            Thread.sleep(100);
        }

        @ScheduleRecurrent(millies = 88)
        public static void sch(SchedulingContext sc, ScheduledOperationMirror op) {
//            System.out.println("SCHED " + sc.invocationCount());
//            System.out.println(op.target());
            if (sc.invocationCount() == 10) {
                sc.cancel();
                System.out.println("Bye");
            }
        }
    }
}
