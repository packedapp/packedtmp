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
package app.packed.telemetry;

/**
 *
 */
public @interface NewSpan {

    String spanName() default "${operation#name}";

    // ville vaere saa fraekt at kunne sige "invocationCount=job#invocationCount"
    String[] attributes() default "";

    // I don't think we can say onFork unfortunately
//    @Span(always = true, onFork=true, attributes = "request")
}