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
package app.packed.service;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * A annotation indicating that a given object indicates that
 * 
 * Nice annotation
 * <p>
 * Composite is not inherited..
 */
@Documented
@Retention(RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE })
public @interface Composite {}

// If this annotation is used on a field
// Tror kun vi tillader den paa en klasse...
// Og saa tillader vi at constructeren ikke behoever vaere offentlig...
// Paa den maade kan man injecte ting som maaske er hemmelige...
// Altsaa den er jo ikke 100% needed i foerste omgang...
// Men den har jo altsaa lidt effect