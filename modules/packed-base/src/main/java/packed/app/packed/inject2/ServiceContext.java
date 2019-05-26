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
package packed.app.packed.inject2;

import java.util.Set;

import app.packed.util.Key;

/**
 *
 */
// Oehhh er det egentlig service descriptoren??
// Nej, service descriptor er extern vil jeg sige
// ServiceContext er internt, kan kun injectes ind i selve servicen.
// Eller i den metoder der provider services...
// Maaske slaa den sammen med ProvisionContext <- Saa er det ogsaa vi ligesom kan sige
// Er noget man kun bruge i forbindelse med at lave en service.
// ServiceContext
public interface ServiceContext {

    boolean exported();

    Set<Key<?>> exportedAs();

    Key<?> key();
}
