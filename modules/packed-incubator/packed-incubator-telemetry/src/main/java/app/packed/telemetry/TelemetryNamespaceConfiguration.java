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
package app.packed.telemetry;

/**
 *
 */
// Tracer?
// Logging?
// Metrics?
//// Or just 1
public class TelemetryNamespaceConfiguration {
    //Autoset Span exceptions when logging (Probably true by default)

    // I think 0 is great because it is not valid in a distributed setting
    // TraceID -> fixed(0), random, LongSupplier, Incremental

    // SpanID -> fixed(0), random, LongSupplier, Incremental
    // Span er jo i
    //IDless Tracer, id less span

    // Maybe have

    public void enableTracer() {

    }
}
//OpenTelemetryExtension {
  // useOtherImplementation
//}