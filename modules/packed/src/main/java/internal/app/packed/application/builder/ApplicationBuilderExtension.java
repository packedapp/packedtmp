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
package internal.app.packed.application.builder;

/**
 * An extension that allows to build new applications on runtime.
 */
// Don't know if need this. But I think we do

// Ideen er lidt at BaseExtension jo ikke kan staa som baade operator og owner...
// Saa taenker vi har brug for en extension der kan staa som owner
// Og maybe Application is just the user...

// Nah man kan kun laver en bootstrap app via den Composer.
// Saa ingen grund til en specific extension. Vi kan bare
// hacke indmaden direkte.

public class ApplicationBuilderExtension {}
