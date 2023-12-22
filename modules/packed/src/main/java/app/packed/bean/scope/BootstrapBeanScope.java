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
package app.packed.bean.scope;

/**
 * Ideen er lidt at have nogle beans der kan koere foer applikationen starter, og evt stoppe foerend den starter.
 * <p>
 * Det er specielt hjaelp. Version, ...
 */
// Early exit beans. Kan maaske faa et eller andet injected. Hvor de kan sige hasta lavista...
// Altsaa det er jo egentligt kun et problem fordi vi ikke er lazy...
//
// Hvis vi har hjaelp i samme klasse som Main saa er alt maskineret jo allerede implementeret der.

// Static metoder paa en bean.. Initialisere de en bean??? @Get String get()  + @Get static String getStatic();

public class BootstrapBeanScope extends BeanScope {

}
