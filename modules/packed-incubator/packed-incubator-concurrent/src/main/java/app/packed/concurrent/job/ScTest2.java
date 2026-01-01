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

import app.packed.application.App;
import app.packed.assembly.BaseAssembly;
import app.packed.concurrent.DaemonJob;
import app.packed.concurrent.DaemonJobContext;

/**
 *
 */
public class ScTest2 extends BaseAssembly {

    public static void main(String[] args) throws Exception {
//        ApplicationMirror m = App.mirrorOf(new ScTest2());
//        Optional<ThreadNamespaceMirror> o = m.namespace(ThreadNamespaceMirror.class);

//        o.get().daemons().forEach(c -> {
//            IO.println(c.name() + ":" + c.isInteruptAtStop());
//        });

        App.run(new ScTest2());

        Thread.sleep(10000);
    }

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provideInstance("asdasd");
        install(MuB.class);

//        p.operations(DaemonJobConfiguration.class).findAny().get().threadFactory(e -> new Thread(e));
    }

    public static class MuB {

        @DaemonJob
        public void dae(DaemonJobContext sc) throws Exception {
            IO.println("Daemon");
            throw new RuntimeException();
        }
    }
}
