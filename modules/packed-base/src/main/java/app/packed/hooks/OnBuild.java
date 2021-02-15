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
package app.packed.hooks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
// Ville helst kalde den @OnBuild... Men vi gider ikke have den dukker op hver gang.
// Folk laver code complete
// Den er god fordi vi kan injecte andet end bare build context...
// Men ogsaa fx extension

// Kan en build metode returnere et object der kan caches

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnBuild {

}

// Had this attributes.. Men syntes vi skal finde en anden maade...
//boolean runtimeBundle() default false;

//boolean buildtimeBundle() default true;

// Maybe allow for static methods on component sources

// @OnBuild
// public void building(Component c) {
//   sysout(Adding this component as part of c);
// }

// Extension???? Skal vel treates paa samme maade component... Paa naer at instance er fin.


// Hook Target>
// Buildtime-Components
// Runtime Components
// Hooks (meta)

//  