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
package packed.internal.container;

import app.packed.artifact.App;
import app.packed.artifact.ArtifactImage;
import app.packed.container.BaseBundle;
import app.packed.container.Extension;
import app.packed.container.ExtensionContext;
import app.packed.container.ExtensionLinked;
import app.packed.container.ExtensionSidecar;
import app.packed.container.Wirelet;
import app.packed.container.WireletFind;
import app.packed.inject.InjectionContext;
import app.packed.lifecycle.Leaving;
import app.packed.lifecycle.LifecycleContext;
import app.packed.service.ServiceWirelets;

/**
 *
 */
public class TI extends BaseBundle {

    public static void mains(String[] args) {
        App.of(new TI(), new MyTestWirelet("fofoof XXXXXXXXXX"));
//        long currentTime = System.currentTimeMillis();
//        long vmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
//        System.out.println("STARTED Application started: " + (currentTime - vmStartTime));
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.nanoTime();
        App.of(new TI(), new MyTestWirelet("fofoof XXXXXXXXXX"));
        long stop = System.nanoTime();
        System.out.println(((stop - start) / 1000000) + " ms");
        App.of(new TI(), new MyTestWirelet("fofoof XXXXXXXXXX"));
        long stop2 = System.nanoTime();
        System.out.println(((stop2 - stop) / 1000) + " us");

        App.of(new TI(), new MyTestWirelet("fofoof XXXXXXXXXX"));
        long stop3 = System.nanoTime();
        System.out.println(((stop3 - stop2) / 1000) + " us");

        ArtifactImage si = ArtifactImage.of(new TI(), new MyTestWirelet("fofoof XXXXXXXXXX"));
        for (int i = 0; i < 1000; i++) {
            App.of(si, new MyTestWirelet("fofoof XXXXXXXXXX"));
        }
        Thread.sleep(50);
        stop3 = System.nanoTime();
        App.of(si, new MyTestWirelet("fofoof XXXXXXXXXX"));
        long stop4 = System.nanoTime();
        System.out.println(((stop4 - stop3) / 1000) + " us");

    }

    /** {@inheritDoc} */
    @Override
    protected void compose() {

        provideConstant(123L);
        use(MyExte.class).foo = "Gondor";

        link(new FFF(), ServiceWirelets.provide("const"));
    }

    static class FFF extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void compose() {
            provideConstant("HejHej");
            link(new FFFFFF(), ServiceWirelets.provide("const"));
        }

    }

    static class FFFFFF extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void compose() {
            provideConstant("HejHej");
            // System.out.println(use(MyExte.class).foo);
        }
    }

    // @WireletSidecar(inherited = true)
    public static class MyTestWirelet implements Wirelet {
        String hejhej;

        MyTestWirelet(String hejhej) {
            this.hejhej = hejhej;
        }

        @Override
        public String toString() {
            return hejhej;
        }
    }

    public static class MyExte extends Extension {

        String foo;
        final LifecycleContext lc;

        MyExte(LifecycleContext lc, InjectionContext ic, @WireletFind MyTestWirelet wc) {
            // System.out.println("State " + lc.current());
            this.lc = lc;

        }

        @ExtensionLinked(onlyDirectLink = false)
        public void ff(InjectionContext ic, MyExte child) {
            child.foo = " Child of " + foo;
        }

        @Leaving(state = ExtensionSidecar.NORMAL_USAGE)
        protected void foo(ExtensionContext ec) {
            // System.out.println(ec.containerPath());
            // System.out.println("State now " + lc.current());
            // System.out.println(lc);
        }

        @Leaving(state = ExtensionSidecar.NORMAL_USAGE)
        private static void foodd(InjectionContext ic) {
            // System.out.println("Available Services " + ic.keys());
        }

    }
}
