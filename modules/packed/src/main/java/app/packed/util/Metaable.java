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

import java.lang.annotation.Annotation;

/**
 *
 */
// Must be placed on annotations that can be used for meta annotations
// Ideen er lidt at det ikke er alle annotering man bare kan lave meta annotationer for
@interface Metaable {} // Evt kan vi negere den NotMetable IDK


@interface MetaAnnotationFor {
    Class<? extends Annotation> metaFor();
}
