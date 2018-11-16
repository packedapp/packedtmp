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
package app.packed.util;

import java.lang.reflect.AnnotatedElement;

import packed.internal.util.ErrorMessageBuilder;

/**
 *
 */
// See also http://openjdk.java.net/jeps/277
public enum ApiStatus {

    /** The API may change at any time. */
    PREVIEW,

    /** The API is fully released and stable. Will not change at short notice. */
    STABLE,

    /**
     * The API is no longer support No longer supported (and may have been replaced), and may be removed in the future at an
     * announced date. Use not encouraged.
     */
    DEPRECATED,

    DEPRECATED_FOR_REMOVAL; // maybe ditch this...

    // If on on service interface, automatically bind it. Ignore on implementation
    // It says feature, because we would prefer one forPreview method instead of one for each feature type

    // bundle.forDeprecation(Feature feature, String description);

    // bundle.forPreview(Feature feature, String description);

    // expose(Feature)

    public static ApiStatus fromAnnotatedElement(AnnotatedElement e) {
        boolean isPreview = e.isAnnotationPresent(Preview.class);
        boolean isDeprecated = e.isAnnotationPresent(Deprecated.class);

        if (isPreview && isDeprecated) {
            throw new InvalidDeclarationException(
                    ErrorMessageBuilder.of(e).cannot("both be annotated with @" + Preview.class.getSimpleName() + " and @" + Deprecated.class.getSimpleName())
                            .toResolve("remove either of the annotations"));
        }
        if (isPreview) {
            return PREVIEW;
        } else if (isDeprecated) {
            return DEPRECATED;
        }
        return STABLE;
    }
}
