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
package app.packed.concurrent.job;

import java.util.Optional;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.BaseAssembly;
import app.packed.concurrent.ThreadNamespaceMirror;

/**
 *
 */
public class ScTest2 extends BaseAssembly {

    @SuppressWarnings("unused")
    public static void main(String[] args) throws Exception {
        ApplicationMirror m = App.mirrorOf(new ScTest2());
        Optional<ThreadNamespaceMirror> o = m.namespace(ThreadNamespaceMirror.class);

//        o.get().daemons().forEach(c -> {
//            IO.println(c.name() + ":" + c.isInteruptAtStop());
//        });

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
    }
}
