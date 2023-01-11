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
package app.packed.application.host;

import app.packed.application.App;
import app.packed.application.ApplicationHostExtension;
import app.packed.container.BaseAssembly;
import app.packed.entrypoint.Main;
import app.packed.extension.bridge.FromContainerGuest;

/**
 *
 */
public class FF extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(S.class);
        use(ApplicationHostExtension.class).newApplication(Dddd.class);
    }

    public static void main(String[] args) {
        App.run(new FF());
    }
    
    public static class S {
        
        @Main
        public void m() {
            System.out.println("sd");
        }
    }

    public static class Dddd {

        public Dddd(@FromContainerGuest String s) {
            System.out.println("SD");
        }
    }
}
