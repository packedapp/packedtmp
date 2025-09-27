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
package app.packed.assembly;

import app.packed.application.App;
import app.packed.build.BuildProcess;

/**
 *
 */
public class Ffff extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        System.out.println("OK " + BuildProcess.current().currentAssembly());

        // Maybe we have a RootAssembly and ChildAssembly
        assembly().application().restrictUpdatesToThisAssembly();
        link(new Other(), "child");
    }


    static class Other extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            super.assembly().application().named("asd");
            System.out.println("OK " + BuildProcess.current().currentAssembly());
        }
    }
    void main() {
        App.mirrorOf(new Ffff());
    }
}
