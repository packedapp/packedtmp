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
package tck.wirelets;

import java.util.concurrent.atomic.AtomicReference;

import app.packed.container.BaseWirelets;
import app.packed.container.Wirelet;
import tck.AppAppTest;

/**
 *
 */
public class ApplicationShutdownWireletTest extends AppAppTest {

    public void test() {
        AtomicReference<Thread> ar = new AtomicReference<>();
        Wirelet w = BaseWirelets.shutdownHook(r -> {
            Thread t = new Thread(r);
            ar.set(t);
            return t;
        });

        prep().wirelets(w);
        // prep(W).

        // It is registered when we start the application
        // test img, launch

                System.out.println(w);
        // Not ready to be implemented yet, as we still need
//        App.imageOf(null, null)
    }
}
