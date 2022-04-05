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
package zpp.packed.config;

/**
 *
 */

// Hirakisk configuration...

// Optional Schema...

// Det daarlige ved Config.ifPresent() <--- is that the schema is optional...
public class Config {

}
// void ifPresent(PATH, consumer)
// T orElse(PATH, T alternative)
// Optional find(PATH)
// @Nullable T get(PATH)
// T use(PATH)  <-- throws NoSuchElementException
