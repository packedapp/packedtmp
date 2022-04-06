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
package packed.internal.bean.hooks.variable;

import app.packed.application.ApplicationMirror;
import app.packed.application.entrypoint.Main;
import app.packed.bean.operation.OperationMirror;
import app.packed.container.BaseAssembly;

/**
 *
 */
public class HelloWorldAssembly extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provide(HelloWorld.class);
        exportAll();
    }

    public static void main(String[] args) {
//        App.run(new HelloWorldAssembly());
//
//        System.out.println();
//        App.driver().print(new HelloWorldAssembly());
        
        ApplicationMirror am = ApplicationMirror.of(new HelloWorldAssembly());
        
        System.out.println(am.container().operations());
        
        for (OperationMirror m : am.container().operations()) {
            System.out.println(m.bean().path());
        }
    }

    public static class HelloWorld {

        @Main
        public static void execute() {
            System.out.println("Executing HelloWorld");
        }
        
    }
}
