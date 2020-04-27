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
package testutil.assertj;

import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;

/**
 *
 */
public class Ffff {

    public static void main(String[] args) {
        for (ModuleReference rr : ModuleFinder.ofSystem().findAll()) {
            System.out.println(rr.descriptor());
//                ModuleReader rrr = rr.open();
//                try (ModuleReader mr = r.open()) {
//                    mr.list().forEach(e -> System.out.println(e.));
//                }
        }
    }
}
