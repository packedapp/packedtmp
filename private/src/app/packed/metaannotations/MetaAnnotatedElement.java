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
package app.packed.metaannotations;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.function.Function;

/**
 * A meta annotated element extends . That is annotations on other annotations
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
    default boolean isMetaAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getMetaAnnotation(annotationClass) != null;
    }

    <T extends Annotation> T getMetaAnnotation(Class<T> annotationClass);

    Annotation[] getMetaAnnotations();

    Annotation[] getDeclaredMetaAnnotations();

    default <T extends Annotation> T getDeclaredMetaAnnotation(Class<T> annotationClass) {
        requireNonNull(annotationClass, "annotationClass is null");
        for (Annotation annotation : getDeclaredMetaAnnotations()) {
            if (annotationClass == annotation.annotationType()) {
                return annotationClass.cast(annotation);
            }
        }
        return null;
    }

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
/**
 *
 * @apiNote In the future, if the Java language permits, {@link ClassDescriptor} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
//http://cr.openjdk.java.net/~mcimadamore/reflection-manifesto.html
//http://cr.openjdk.java.net/~mcimadamore/x-reflection/index.html?valhalla/reflect/runtime/RuntimeMirror.Kind.html

//Vi bliver noedt til at have en ClassDescriptor hvis vi vil have meta annotationer...
interface ClassDescriptor<T> extends MetaAnnotatedElement {
    Class<?> reflect();

    // Den gamle hook..
    // Naah lav den som statisk function taenker jeg...
    T analyze(Lookup caller, Class<?> target);
}

// Vi kan have en @MetaAnnotationCollisionResolver(Class List<Annotation> annotering man kan proppe paa et element...
@interface MethodAnnotationCollusionResolver {
    Class<? extends Annotation>[] onAnnotation();

    Class<? extends Function<List<Annotation>, Annotation>> resolver();
}