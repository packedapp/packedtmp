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
package packed.internal.config;

import java.lang.invoke.MethodHandles;

/**
 *
 */
// Ideen er man kan saa saaden en i en extension...
// Maaske bare tage en MethodHandle.lookup????
// Eller der er jo ingen grund til at en extension skal have et
// helt lookup object...
public class ExtensionLookup {

    static ExtensionLookup of(MethodHandles.Lookup lookup) {
        throw new UnsupportedOperationException();
    }
}
// Vi skal registere et schema...

// Saa en extension registrere et "muligt" schema

// Men er det statisk?????
// Naah vi kender jo ikke det komplete schema foerend alle extensions er registereret...

// Evt. har vi nogle metoder der skal bruges efter alle extensions er configureret...

// User Methods... Must be used after all extension has been configured...

// Altsaa ellers kan vi ihvertfald ikke ma
