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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import app.packed.util.Nullable;

/**
 *
 */
// En klasse der laver alt med lookup... F.eks. konvertering til private lookups
// Som ogsaa forstaar typen af exceptions der skal smides.....
// Og ikke andet funktionalitet.....
class LYhingy {

    static final Lookup THIS_MODULE = MethodHandles.lookup();

    final Class<?> type;

    LYhingy(Class<?> type, @Nullable ComponentLookup lookup) {
        this.type = requireNonNull(type);
    }

    // Tager en klasse aabenlyst.... Hvad med subklasses!>!>!> No...
    // @OnHook i 2 forskellige bundles...

    // Dvs. man er optimeret for et modul, men vi kan supportere flere.....

    // En top type
}
// To typer klasser
// * Klasser der skal vaere aabne til Packed
// * Klasser der skal vaere aabne eller som tager en ComponentLookup.....
