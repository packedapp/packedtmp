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
package app.packed.container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 *
 */
class ExtensionBeanConfigurationHelper {

    public static void main(String[] args) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle caseMh = lookup.findVirtual(String.class, "concat", MethodType.methodType(String.class, String.class));
        caseMh = MethodHandles.dropArguments(caseMh, 0, int.class);

        MethodHandle caseDefault = MethodHandles.insertArguments(caseMh, 1, "default: ");
        MethodHandle case0 = MethodHandles.insertArguments(caseMh, 1, "case 0: ");
        MethodHandle case1 = MethodHandles.insertArguments(caseMh, 1, "case 1: ");

        MethodHandle mhSwitch = MethodHandles.tableSwitch(caseDefault, case0, case1);
        
        System.out.println((String) mhSwitch.invokeExact(-1, "data"));
        System.out.println((String) mhSwitch.invokeExact(0, "data"));
        System.out.println((String) mhSwitch.invokeExact(1, "data"));
        System.out.println((String) mhSwitch.invokeExact(2, "data"));
//
//        MethodHandles.lo
//        
//        assertEquals("default: data", (String) mhSwitch.invokeExact(-1, "data"));
//        assertEquals("case 0: data", (String) mhSwitch.invokeExact(0, "data"));
//        assertEquals("case 1: data", (String) mhSwitch.invokeExact(1, "data"));
//        assertEquals("default: data", (String) mhSwitch.invokeExact(2, "data"));
    }
}
