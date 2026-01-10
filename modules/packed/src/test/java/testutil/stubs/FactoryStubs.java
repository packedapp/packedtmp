/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package testutil.stubs;

/**
 *
 */
public class FactoryStubs {

    public static class Instances {

        public static final class InstanceThis {
            public static final InstanceThis INSTANCE = new InstanceThis();
        }

        public static final class InstanceIsNull {
            public static final InstanceIsNull INSTANCE = null;
        }

        public static final class InstanceIsNumberButIntValue {
            public static final Number INSTANCE = 1234;
        }

        public static final class InstanceIsI {
            public static final int INSTANCE = 123;
        }

        public static final class InstanceIsInteger {
            public static final Integer INSTANCE = 124;
        }

        public static final class InstanceIsD {
            public static final double INSTANCE = 125;
        }

        public static final class InstanceIsDouble {
            public static final Double INSTANCE = 126d;
        }

        public static final class InstanceIsL {
            public static final long INSTANCE = 127;
        }

        public static final class InstanceIsLong {
            public static final Long INSTANCE = 128L;
        }
    }

    public static class FactoryCreationType {
        public static final class CreateThis {
            public static CreateThis create() {
                return new CreateThis();
            }
        }

        public static final class CreateInteger {
            public static Integer create() {
                return 1;
            }
        }

        public static final class CreateL {
            public static long create() {
                return 2L;
            }
        }
    }

}
