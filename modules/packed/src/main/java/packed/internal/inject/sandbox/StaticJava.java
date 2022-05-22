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
package packed.internal.inject.sandbox;

/**
 *
 */
public class StaticJava {

//    // Can only be created at build-time for static java
//    /* sealed */ interface RuntimeLocal<T> {
//        T get();
//
//        static <T> RuntimeLocal<T> of(Supplier<T> supplier) {}
//        static <T> RuntimeLocal<T> ofLazy(Supplier<T> supplier) {}
//    }
//
//    /* sealed */ interface ReinitLocal<T> {
//        T get();
//
//        static <T> ReinitLocal<T> of(Supplier<T> supplier) {}
//        static <T> ReinitLocal<T> of(Supplier<T> supplier, Consumer<T> cleaner) {}
//    }
//    
//    static class MixAndMatch {
//        private static final MethodHandle MH = null; // Initialized as normal
//        private static final RuntimeLocal<LocalDateTime> RUNTIME_TIME = RuntimeLocal.of(LocalDateTime::now);
//        private static final ReinitLocal<LocalDateTime> PROCESS_TIME = ReinitLocal.of(LocalDateTime::now);
//        
//        public static void printApplicationStart() {
//            System.out.println("Application started running at " + RUNTIME_TIME.get());
//        }
//        
//        public static void printApplicationProcessStart() {
//            System.out.println("Application started running in this process at " + PROCESS_TIME.get());
//        }
//    }
}
