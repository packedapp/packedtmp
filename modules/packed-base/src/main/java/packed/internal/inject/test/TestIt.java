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
package packed.internal.inject.test;

import app.packed.inject.ServiceLocator;

/**
 *
 */
public class TestIt {

    public static void main(String[] args) {
        ServiceLocator sl = ServiceLocator.of(t -> {
            t.provideInstance("goo");
            t.provideInstance(123);
        });
        System.out.println(sl.getClass());
        System.out.println(sl.keys());
        sl = sl.transform(t -> t.provideInstance(123L));
        sl.selectAll().use(Long.class);
        System.out.println("Bye");
    }

}
