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

import static assertj.app.packed.inject.DependencyAssert.assertThat;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import app.packed.inject.Dependency;
import app.packed.inject.Key;
import app.packed.inject.Qualifier;
import app.packed.inject.TypeLiteral;

/**
 *
 */
public class DependencyTest {
    static final Key<?> KEY_QA_INT = new Key<@Q("A") Integer>() {};

    @Nested
    public class OptionalsOf {

        @Test
        public void fromTypeAttribute() {
            Dependency opString = Dependency.fromTypeVariable(TypeLiteral.class, new TypeLiteral<Optional<String>>() {}.getClass(), 0);
            assertThat(opString).keyIs(String.class);
        }
    }

    @Nested
    public class OptionalsOfInts {

        @Test
        public void fromTypeParameter() {
            assertThat(Dependency.of(OptionalInt.class)).isOptionalInt();
            // assertThat(new Dependency<OptionalInt>() {}).isOptionalInt();
            // assertThat(new Dependency<OptionalInt>() {}).keyIs(new Dependency<Optional<Integer>>() {}.getKey());

            // fromTypeParameter
            Dependency opInt = Dependency.fromTypeVariable(TypeLiteral.class, new TypeLiteral<OptionalInt>() {}.getClass(), 0);
            assertThat(opInt).isOptionalInt();

            // Annotated
            // assertThat(new Dependency<@Q("A") OptionalInt>() {}).keyIs(KEY_QA_INT);
        }
    }

    @Test
    public void ofClass() {
        assertThatNullPointerException().isThrownBy(() -> Dependency.of((Class<?>) null));

        assertThatIllegalArgumentException().isThrownBy(() -> Dependency.of(Optional.class))
                .withMessage("Cannot determine type variable <T> for type Optional<T>");

        Dependency opLong = Dependency.of(OptionalLong.class);
        assertThat(opLong).isOptional(OptionalLong.class);
        assertThat(opLong).keyIs(Long.class);

        Dependency opDouble = Dependency.of(OptionalDouble.class);
        assertThat(opDouble).isOptional(OptionalDouble.class);
        assertThat(opDouble).keyIs(Double.class);
    }

    @Retention(RUNTIME)
    @Qualifier
    @Target({ ANNOTATION_TYPE, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE })
    public @interface Q {
        String value() default "";
    }
}
