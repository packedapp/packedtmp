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
package app.packed.container.env;

/**
 *
 */
// Har Environment automatisk en default vaerdi??? Hvis det bliver naevnt en gang?
enum AcmeFooEnvironment implements RuntimeEnvironment {
    TEST, PRODUCTION {
        @Override
        public boolean saveStackTraces() {
            return false;
        }
    };
    {
        // RuntimeEnvironment.registerSystemPropertyAlias(MethodHandles.lookup(), "foo");
    }

    // du kan ikke kalde
    // AnyBundle.environment() <- Because if we do not have explicitly set, how can we now we are going to use it...

    public static void main(String[] args) {
        System.out.println(TEST.saveStackTraces());
        System.out.println(PRODUCTION.saveStackTraces());
    }

    // Her er RuntimeEnvironment a little bad
    // Session
    // --GoldCustomer
    // --SilverCustomer
}
// Problemet er nu at vi gerne vil kunne koere f.eks.
// -Denv=TEST
/// Vi kan ikke rely on class initializers (registerSystemPropertyAlias)... Fordi vi ikke ved hvornaar den er koert..
// Med mindre vi installere den som en ServiceLoader