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
package app.packed.inject;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;

@Documented
@Retention(RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
/**
 *
 */
public @interface Default {

    double[] doubleValue() default {};

    float[] floatValue() default {};

    long[] longValue() default {};

    int[] intValue() default {};

    String[] value() default {};

    // Whether or not the default value is computed at initialization time...
    // See https://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/ParamConverter.html
    boolean lazy() default true;
}
// Converter, deterministic by default I think... Yes, 

//If we want we could get DefaultInt(), DefaultLong(), ...
//Maybe in converter package???

//Default -> Optional....
class Doo {

    Doo(@Default Set<String> s) {
        // List, Set should just work with empty
    }

    Doo(@Default("asdsd") String s) {

    }

    Doo(@Default(intValue = { 1, 2, 3, 4 }) List<Integer> s) {

    }
}

//Do we want String[] value()???? Saa kan vi let
//supportere f.eks. List<String>
