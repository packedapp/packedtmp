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
package packed.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

/** Tests {@link AnnotationUtil}. */
public class AnnotationUtilTest {

    /** Tests {@link AnnotationUtil#validateRuntimeRetentionPolicy(Class)}. */
    public static class ValidateRuntimeRetentionPolicy {

        /** Tests {@link AnnotationUtil#validateRuntimeRetentionPolicy(Class)}. */
        @Test
        public void validateRuntimeRetentionPolicy() {
            AnnotationUtil.validateRuntimeRetentionPolicy(RetentionPolicyRuntime.class);

            assertThatThrownBy(() -> AnnotationUtil.validateRuntimeRetentionPolicy(NoRetentionPolicy.class)).hasMessage(
                    "The annotation type @NoRetentionPolicy must have a runtime retention policy (@Retention(RetentionPolicy.RUNTIME), but did not have any retention policy")
                    .hasNoCause().isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> AnnotationUtil.validateRuntimeRetentionPolicy(RetentionPolicySource.class)).hasMessage(
                    "The annotation type @RetentionPolicySource must have runtime retention policy (@Retention(RetentionPolicy.RUNTIME), but was SOURCE")
                    .hasNoCause().isInstanceOf(IllegalArgumentException.class);
            
            assertThatThrownBy(() -> AnnotationUtil.validateRuntimeRetentionPolicy(RetentionPolicyClass.class)).hasMessage(
                    "The annotation type @RetentionPolicyClass must have runtime retention policy (@Retention(RetentionPolicy.RUNTIME), but was CLASS")
                    .hasNoCause().isInstanceOf(IllegalArgumentException.class);
        }

        static @interface NoRetentionPolicy {}

        @Retention(RetentionPolicy.SOURCE)
        static @interface RetentionPolicySource {}

        @Retention(RetentionPolicy.CLASS)
        static @interface RetentionPolicyClass {}

        @Retention(RetentionPolicy.RUNTIME)
        static @interface RetentionPolicyRuntime {}
    }
}
