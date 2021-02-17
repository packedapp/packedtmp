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
package app.packed.component;

/**
 * Images are immutable ahead-of-time configured component systems. By configuring an system ahead of time, the actual
 * time to instantiation the system can be severely decreased often down to a couple of microseconds. In addition to
 * this, images can be reusable, so you can create multiple systems from a single image.
 * 
 * Creating artifacts in Packed is already really fast, and you can easily create one 10 or hundres of microseconds. But
 * by using artifact images you can into hundres or thousounds of nanoseconds.
 * <p>
 * Use cases: Extremely fast startup.. graal
 * 
 * Instantiate the same container many times
 * <p>
 * Limitations:
 * 
 * No structural changes... Only whole artifacts
 * 
 * <p>
 * An image can be used to create new instances of {@link app.packed.component.App} or other artifact images. Artifact
 * images can not be used as a part of other containers, for example, via
 * 
 * @apiNote In the future, if the Java language permits, {@link Image} may become a {@code sealed} interface, which
 *          would prohibit subclassing except by explicitly permitted types.
 * 
 * @see App#buildImage(Assembly, Wirelet...)
 */
// Maybe artifact image anyway?? ArtifactDriver er jo lidt mere flex nu...
// Image<void>
public interface Image<A> {

    /**
     * Returns the root component of the image.
     * 
     * @return the root component of the image
     */
    Component component();

    /**
     * Uses the image.
     * 
     * @param wirelets
     *            optional wirelets
     * @return the result of using the image
     */
    A use(Wirelet... wirelets);
}

interface ZImage<A> {
    // Hmmmmmmm IDK
    // Could do sneaky throws instead
    A throwingUse(Wirelet... wirelets);
}