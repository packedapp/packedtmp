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
package app.packed.concurrent.other;

import app.packed.assembly.BaseAssembly;

/**
 *
 */
public class SchTest extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
//        use(ScheduledJobExtension.class).schedule(new Op0<>(() -> System.out.println("Hi")) {});
//
//        use(ScheduledJobExtension.class).schedule(Op.ofRunnable(() -> System.out.println("Hi")));
    }

}
