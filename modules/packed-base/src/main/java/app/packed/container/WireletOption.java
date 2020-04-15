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
package app.packed.container;

/**
 *
 */
// Wirelet skal laves til et interface..

@interface WireletOption {

    Class<?> provideAs() default void.class; // Will automatically provide as

    Class<? extends WireletPipeline<?, ?>> pipeline() default NoWireletPipeline.class;

    // assembleOnly
    // linkOnly
    // hostOnly
}

// Must use Optional/Nullable for wirelet
// Works for both wirelets and pipeline
@interface ProvideWirelet {}

class NoWireletPipeline extends WireletPipeline<NoWireletPipeline, NoWirelet> {}

class NoWirelet extends PipelineWirelet<NoWireletPipeline> {}

// vil automatisk bliver provided som service
@WireletOption(provideAs = Doofar.class)
class Doofar extends Wirelet {

}