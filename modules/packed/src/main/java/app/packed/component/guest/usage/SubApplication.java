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
package app.packed.component.guest.usage;

import app.packed.assembly.BaseAssembly;
import app.packed.bean.lifecycle.Initialize;
import app.packed.bean.lifecycle.Start;
import app.packed.bean.lifecycle.Stop;

/**
 *
 */
public class SubApplication extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        System.out.println("Building sub app");
        install(SomeBean.class);
    }

    public static class SomeBean {

        @Initialize
        public void doo() {
            System.out.println("Sub initialied!!! YOU ARE FUCKING AWESOME");
        }

        @Start
        public void start() {
            System.out.println("Sub Started!!! YOU ARE FUCKING AWESOME");
        }

        @Stop
        public void stop() {
            System.out.println("Sub Stopped!!! YOU ARE FUCKING AWESOME");
        }
    }

}
