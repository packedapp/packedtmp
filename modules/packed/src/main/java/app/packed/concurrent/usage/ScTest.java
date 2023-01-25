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

import java.util.concurrent.atomic.AtomicLong;

import app.packed.application.App;
import app.packed.concurrent.ScheduleRecurrent;
import app.packed.concurrent.SchedulingContext;
import app.packed.container.BaseAssembly;

/**
 *
 */
public class ScTest extends BaseAssembly {

    public static void main(String[] args) throws InterruptedException {
        App.run(new ScTest());
        Thread.sleep(10000);
    }

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provideInstance("asdasd");
        install(MuB.class);
    }

    public static class MuB {
        static AtomicLong l = new AtomicLong();

        @ScheduleRecurrent(millies = 100)
        public static void sch(SchedulingContext sc) {
            System.out.println("SCHED " + sc.invocationCount());
            if (l.incrementAndGet() == 10) {
                sc.cancel();
            }
        }
    }
}
