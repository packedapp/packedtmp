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
package packed.internal.inject;

/**
 *
 */
@FunctionalInterface
public interface Provider<T> {

    /**
     * Provides an instance of type {@code T}.
     *
     * @return the provided value
     * @throws RuntimeException
     *             if an exception is encountered while providing an instance
     */
    T get();
}

// default Stream<T> toStream() {
// // getProvide(UUID.class).limit(50).collect(Collectors.toList());
// return Stream.generate(() -> get());
// }
//
// // Super advanced
// static Provider<Integer> of(IntSupplier i) {
// return new ProviderToIntWrapper(i);
// }
// class ProviderToIntWrapper implements Provider<Integer> {
// final IntSupplier s;
//
// ProviderToIntWrapper(IntSupplier s) {
// this.s = requireNonNull(s);
// }
//
// /** {@inheritDoc} */
// @Override
// public Integer get() {
// return s.getAsInt();
// }
// }

// Ideen er at vi kan returnere en special provider type som frameworket kender.
// Og som vi kan unwrappe supplieren i, og undgaa boxing. Se f.eks.

// public void foox(@Metric("RunTimeNano") int time, @Metric("CalculationCount") int calculations) {}
//
// @interface Metric {
// String value();
// }
//
// HashMap<String, LongAdder> metrics = new HashMap<>();
//
// @Provides(wildcardQualifier = Metric.class)
// public Provider<?> fooReal(ServiceRequest r) {
// Metric m = r.getQualifier(Metric.class);
// LongAdder adder = metrics.get(m.value());
// return Provider.of(() -> adder.intValue());
// }

// I think we want it. It is very easy to implement.
// Just going to create a Injection corresponding to where it is injected.
// The free standing Injector of course creating an injection with and parameter empty
