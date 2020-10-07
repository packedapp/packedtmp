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

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static testutil.assertj.inject.DependencyAssert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import app.packed.base.Key;
import app.packed.base.TypeLiteral;
import packed.internal.inject.DependencyDescriptor;

/**
 *
 */
public class DependencyTest {

    static final Key<?> KEY_QA_INT = new Key< /* @Q("A") */ Integer>() {};

    @Nested
    public class OptionalsOf {

        @Test
        public void fromTypeAttribute() {
            DependencyDescriptor opString = DependencyDescriptor.fromTypeVariable(new TypeLiteral<Optional<String>>() {}.getClass(), TypeLiteral.class, 0);
            assertThat(opString).keyIs(String.class);
        }
    }

    @Nested
    public class OptionalsOfInts {

        @Test
        public void fromTypeParameter() {
            assertThat(DependencyDescriptor.of(OptionalInt.class)).isOptionalInt();
            // assertThat(new Dependency<OptionalInt>() {}).isOptionalInt();
            // assertThat(new Dependency<OptionalInt>() {}).keyIs(new Dependency<Optional<Integer>>() {}.getKey());

            // fromTypeParameter
            DependencyDescriptor opInt = DependencyDescriptor.fromTypeVariable(new TypeLiteral<OptionalInt>() {}.getClass(), TypeLiteral.class, 0);
            assertThat(opInt).isOptionalInt();

            // Annotated
            // assertThat(new Dependency<@Q("A") OptionalInt>() {}).keyIs(KEY_QA_INT);
        }
    }

    @Test
    public void ofClass() {
        assertThatNullPointerException().isThrownBy(() -> DependencyDescriptor.of((Class<?>) null));

        assertThatIllegalArgumentException().isThrownBy(() -> DependencyDescriptor.of(Optional.class))
                .withMessage("Cannot determine type variable <T> for type Optional<T>");

        DependencyDescriptor opLong = DependencyDescriptor.of(OptionalLong.class);
        assertThat(opLong).isOptional(OptionalLong.class);
        assertThat(opLong).keyIs(Long.class);

        DependencyDescriptor opDouble = DependencyDescriptor.of(OptionalDouble.class);
        assertThat(opDouble).isOptional(OptionalDouble.class);
        assertThat(opDouble).keyIs(Double.class);
    }

    @Retention(RUNTIME)
    @Key.Qualifier
    @Target({ ANNOTATION_TYPE, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE })
    public @interface Q {
        String value() default "";
    }
}
