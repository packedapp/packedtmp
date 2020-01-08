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
package app.packed.lang.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.function.Function;

/**
 *
 */
// Ideen er at man kan forspoerge disse metoder istedet for.
// Vil automatisk udpakke meta annotateringer...

// Betyder vist ogsaa at vi skal have en ClassDescriptor

// Hvad med Caching???
// Tror ikke vi laver caching... Eller hva...
// Hvis @OnStart -> OnLifecycle()..
// Skal vi virkelig resolve det hver gang????
// Tror vi cacher...
// Foerst koere vi if (annotation.getClass().hasAnnotations() inde vi slaa op i cachen...
// ClassValue<Annotation> cv =...
// i
interface MetaAnnotatedElement extends AnnotatedElement {

    // Problemet er den kan vaere 0, 1 eller flere....
    // Taenker bare vi siger ja for > 0, og saa vi fejle naar vi proever at hente den ud
    boolean isMetaAnnotationPresent(Class<? extends Annotation> annotationClass);
    // forEachAnnotation
    // cv.get(OnStart.class).contains(ClassOnLifecycle.class)
}
// Target bliver noedt til at passe.... F.eks. hvis Get er en meta-annotering...
// Saa kan definere
// @Target(Field)
// @Get
// @interface MyGet {}

// Og smide den paa et field...

// Maaske validere i alle tilfaelde paanaaer hvis den kun AnnotationType target..

// Dvs skal vi tage en ElementType???? og saa validere mod et bitmap...
// Eller vi kan jo egentlig godt validere det selv...
// Vi kan ikke lave det som en utility function...

// Vi kan have en @MetaAnnotationCollisionResolver(Class List<Annotation> annotering man kan proppe paa et element...
@interface MethodAnnotationCollusionResolver {
    Class<? extends Annotation>[] onAnnotation();

    Class<? extends Function<List<Annotation>, Annotation>> resolver();
}