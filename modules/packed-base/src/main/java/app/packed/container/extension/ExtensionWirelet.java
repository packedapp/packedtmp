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
package app.packed.container.extension;

import app.packed.container.Wirelet;

/**
 * Extensions that define their own wirelets must extend this class.
 * 
 * Extension wirelets that uses the same extension pipeline type are processed in the order they are specified in. No
 * guarantees are made for extension wirelets that define for different extension pipeline types.
 */
public abstract class ExtensionWirelet<T extends ExtensionWireletPipeline<?, ?>> extends Wirelet {

    // Ellers skal vi have stages'ene her
    // processBefore()
    // processAfter()
    // ....
    // Vel ikke hvis det er pipelinen der bestemmer....

    /**
     * Process this wirelet.
     * 
     * @param pipeline
     *            the extensions pipeline
     */
    protected abstract void process(T pipeline);

    // HVORFOR ikke bare en metode vi kan invoke fra extension'en?
    // Det virker ikke naar vi image.with(some wirelets)....
    // Fordi det kun er wirelets der bliver "koert".
    // Vi koere ikke hver extension...
}

/// Maaske kan vi godt lave tmp bundles????
/// Hvis vi bare stopper inde graf hullumhej...
/// Det betyder dog ogsaa
