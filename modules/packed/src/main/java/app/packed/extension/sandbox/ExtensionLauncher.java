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
package app.packed.extension.sandbox;

import app.packed.application.ApplicationDriver;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;

/**
 *
 */
// Hmm
// Altsaa hvis vi fx har en BeanInstancePool.
// Skal vi saa tage ExtensionContext for hver en operation????
// I don't think so
public interface ExtensionLauncher<A> {
    
    /**
     * Launches an instance of the application that this image represents.
     * 
     * @throws RuntimeException
     *             if the application failed to launch
     * @throws IllegalStateException
     *             if the image has already been used to launch an application and the image is not a reusable image
     * @return the application interface if available
     */
    default A use(ExtensionBeanContext context) {
        return use(context, new Wirelet[] {});
    }

    /**
     * Launches an instance of the application that this image represents.
     * <p>
     * Launches an instance of the application. What happens here is dependent on application driver that created the image.
     * The behavior of this method is identical to {@link ApplicationDriver#launch(Assembly, Wirelet...)}.
     * 
     * @param wirelets
     *            optional wirelets
     * @return an application instance
     * @see {@link ApplicationDriver#launch(Assembly, Wirelet...)}
     */
    A use(ExtensionBeanContext context, Wirelet... wirelets);
}
