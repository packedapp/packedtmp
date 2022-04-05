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
package app.packed.bean.operation.examples;

import app.packed.application.ApplicationMirror;
import app.packed.container.BaseAssembly;

/**
 *
 */
public class Usage extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provide(Usage.class).export();
    }

    public static void main(String[] args) {
        ApplicationMirror am = ApplicationMirror.of(new Usage());
        
        for (ServiceExportMirror m : am.operations(ServiceExportMirror.class)) {
            System.out.println(m.bean());
        }
        
//        for (ServiceExportMirror m : ServiceExportMirror.selectAll(new Usage())) {
//            System.out.println(m.bean());
//        }
    }
}
