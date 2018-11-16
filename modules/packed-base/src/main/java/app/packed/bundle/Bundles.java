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
package app.packed.bundle;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.reflect.Constructor;

import app.packed.util.InvalidDeclarationException;

/**
 * Various bundle utility methods.
 */
public class Bundles {

    /**
     * Instantiates a new bundle of the specified type.
     * 
     * @param bundleType
     *            the type of bundle to instantiate
     * @return the new bundle
     * @throws InvalidDeclarationException
     *             if the specified bundle type does not have a single public constructor taking no arguments
     */
    public static <T extends Bundle> T instantiate(Class<T> bundleType) {
        requireNonNull(bundleType, "bundleType is null");
        try {
            Constructor<T> c = bundleType.getDeclaredConstructor();
            return c.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new InvalidDeclarationException(
                    "A bundle must have a single public no argument constructor, and exported if in a module, bundle type = " + format(bundleType), e);
        }
    }
}
