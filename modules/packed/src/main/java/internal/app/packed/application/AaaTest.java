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
package internal.app.packed.application;

import app.packed.application.App;
import app.packed.assembly.BaseAssembly;
import app.packed.bean.lifecycle.Initialize;
import app.packed.bean.lifecycle.Start;
import app.packed.bean.lifecycle.Stop;
import app.packed.runtime.RunState;

/**
 *
 */
public class AaaTest extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(TB.class);
    }

    public static void main(String[] args) throws InterruptedException {
        App a = PackedApp.BOOTSTRAP_APP.launch(RunState.STARTING, new AaaTest());

        System.out.println("STate " + a.state());
        Thread.sleep(4000);
    }

    public static class TB {

        @Initialize
        public void init() {
            System.out.println("INIT");
            System.out.println(Thread.currentThread());
        }

        @Start
        public void onStart() throws InterruptedException {
            Thread.sleep(1000);
            System.out.println("STARTING");
            System.out.println(Thread.currentThread());
        }

        @Stop
        public void onstop() throws InterruptedException {
            Thread.sleep(1000);
            System.out.println("soppping");
        }
    }
}
