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
package app.packed.concurrent.job;

import java.io.IOException;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.BaseAssembly;
import app.packed.concurrent.DaemonJob;
import app.packed.concurrent.DaemonJobContext;
import app.packed.lifecycle.Stop;
import app.packed.web.HttpContext;
import app.packed.web.WebGet;

/**
 *
 */
public class ScTest2 extends BaseAssembly {

    public static void main(String[] args) throws Exception {
        // ApplicationMirror m = App.mirrorOf(new ScTest2());
//        Optional<ThreadNamespaceMirror> o = m.namespace(ThreadNamespaceMirror.class);

//        o.get().daemons().forEach(c -> {
//            IO.println(c.name() + ":" + c.isInteruptAtStop());
//        });

        App app = App.start(new ScTest2());
        Thread.sleep(30000);
        app.stop();
        Thread.sleep(1000);
        System.out.println(app.state());
    }

    /** {@inheritDoc} */
    @Override
    protected void build() {

        link(assembly().finder().findOne("foo.bar"));
        assembly().finder().findAll(ServiceLoader.load(BaseAssembly.class)).forEach(e -> link(e));

        provideInstance("asdasd");
        install(MuB.class);

//        p.operations(DaemonJobConfiguration.class).findAny().get().threadFactory(e -> new Thread(e));
    }

    public static class MuB {

        private final AtomicLong al = new AtomicLong();

        @WebGet(url = "/json")
        public void json(HttpContext ctx) throws IOException {
            ctx.response().write("{\"status\":\"" + al.get() + "\"}", "application/json");
        }

        @DaemonJob
        public void dae(ApplicationMirror am, DaemonJobContext sc) throws Exception {
            while (sc.awaitShutdown(1, TimeUnit.SECONDS)) {
                IO.println("Daemon " + am.allOperations().count());
                al.incrementAndGet();
            }
        }

        @Stop
        public void g() {
            System.out.println("Goodnight");
        }
    }
}
