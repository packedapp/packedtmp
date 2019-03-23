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
package packed.internal.inject.buildnodes.maker;

import java.lang.invoke.MethodHandles;

import app.packed.inject.InjectionSite;
import app.packed.inject.Injector;
import app.packed.inject.Provides;

/**
 *
 */
public class InstanceMaker {

    public Object make(InjectionSite site) {
        return null;
    }

    public static void main(String[] args) {
        Injector i = Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.provide(new X());
            c.provide(NeedIt.class);
        });

        i.with(String.class);

        System.out.println("Bye");
    }

    // TODO men kan godt constructor dependende paa en statisk streng for et felt paa samme klasse
    // Vi skal have owner med inde paa BuildNodeDefault....
    // Eller maaske en owned node... Nej lad os sgu bare noejes med en node.....

    public static class X {

        @Provides
        static final String foo = "dfdfdf";

        @Provides
        public String fff() {
            return "pdfpdpd";
        }
    }

    public static class NeedIt {
        NeedIt(String s) {
            System.out.println("Du eer for cool Kasper " + s);
        }
    }
}
