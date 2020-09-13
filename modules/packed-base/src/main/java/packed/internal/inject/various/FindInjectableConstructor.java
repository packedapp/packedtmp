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
package packed.internal.inject.various;

import static packed.internal.util.StringFormatter.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import app.packed.base.InvalidDeclarationException;
import app.packed.inject.Inject;
import packed.internal.util.StringFormatter;

/**
 * A utility class for finding an injectable constructor
 */
//Maybe allow to override to throw custom exception..
public final class FindInjectableConstructor {

    public static Constructor<?> find(Class<?> type) {
        if (type.isArray()) {
            throw new IllegalArgumentException(format(type) + " is an array and cannot be instantiated");
        } else if (type.isAnnotation()) {
            throw new IllegalArgumentException(format(type) + ") is an annotation and cannot be instantiated");
        } else if (Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalArgumentException("'" + StringFormatter.format(type) + "' is an abstract class and cannot be instantiated");
        }

        Constructor<?>[] constructors = type.getDeclaredConstructors();

        // If we only have 1 constructor, return it.
        if (constructors.length == 1) {
            return constructors[0];
        }

        // See if we have a single constructor annotated with @Inject
        Constructor<?> constructor = null;
        for (Constructor<?> c : constructors) {
            if (c.isAnnotationPresent(Inject.class)) {
                if (constructor != null) {
                    throw new InvalidDeclarationException(
                            "Multiple constructors annotated with @" + Inject.class.getSimpleName() + " on class " + format(type));
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // See if we have a single public constructor
        for (Constructor<?> c : constructors) {
            if (Modifier.isPublic(c.getModifiers())) {
                if (constructor != null) {
                    throw new IllegalArgumentException(
                            "No constructor annotated with @" + Inject.class.getSimpleName() + ". And multiple public constructors on class " + format(type));
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // See if we have a single protected constructor
        for (Constructor<?> c : constructors) {
            if (Modifier.isProtected(c.getModifiers())) {
                if (constructor != null) {
                    throw new IllegalArgumentException("No constructor annotated with @" + Inject.class.getSimpleName()
                            + ". And multiple protected constructors on class " + format(type));
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // Remaining constructors are either private or package private constructors
        for (Constructor<?> c : constructors) {
            if (!Modifier.isPrivate(c.getModifiers())) {
                if (constructor != null) {
                    throw new IllegalArgumentException("No constructor annotated with @" + Inject.class.getSimpleName()
                            + ". And multiple package-private constructors on class " + format(type));
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // Only private constructors left, and we have already checked whether or not we only have a single method
        // So we must have more than 1 private methods
        throw new IllegalArgumentException(
                "No constructor annotated with @" + Inject.class.getSimpleName() + ". And multiple private constructors on class " + format(type));
    }
}
//  // Try to find a single static method annotated with @Inject
//  Method method = null;
//  for (Method m : type.getDeclaredMethods()) {
//      if (Modifier.isStatic(m.getModifiers()) && m.isAnnotationPresent(Inject.class)) {
//          if (method != null) {
//              throw new IllegalArgumentException("There are multiple static methods annotated with @Inject on " + format(type));
//          }
//          method = m;
//      }
//  }
//
//  // If a single method has been found, use it
//  if (method != null) {
//      // Det er jo i virkeligheden en Key vi laver her, burde havde det samme checkout..
//      if (method.getReturnType() == void.class /* || returnType == Void.class */) {
//          throw new IllegalArgumentException("Static method " + method + " annotated with @Inject cannot have a void return type."
//                  + " (@Inject on static methods are used to indicate that the method is a factory for a specific type, not for injecting values");
//      } else if (TypeUtil.isOptionalType(method.getReturnType())) {
//          throw new IllegalArgumentException("Static method " + method + " annotated with @Inject cannot have an optional return type ("
//                  + method.getReturnType().getSimpleName() + "). A valid instance needs to be provided by the method");
//      }
//      // Sporgsmaalet er om den skal have this this.class som return type...
//      // Og saa brugere skal bruge Factory.findStaticInject(Class, Type); <----
//      return method;
//  }
