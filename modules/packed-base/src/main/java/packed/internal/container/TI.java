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
import app.packed.container.BaseBundle;
import app.packed.container.Wirelet;
import app.packed.service.ServiceWirelets;
import app.packed.sidecar.WireletSidecar;

/**
 *
 */
public class TI extends BaseBundle {

    public static void main(String[] args) {
        App.of(new TI(), new MyTestWirelet("fofoof XXXXXXXXXX"));
    }

    /** {@inheritDoc} */
    @Override
    protected void compose() {
        System.out.println("Parent = " + service());

        provideConstant(123L);
        link(new FFF(), ServiceWirelets.provide("const"));
    }

    static class FFF extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void compose() {
            provideConstant("HejHej");
        }
    }

    @WireletSidecar(inherited = true)
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
}
