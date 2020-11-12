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
package app.packed.conversion;

/**
 *
 */
@FunctionalInterface
public interface Converter<F, T> {

    /**
     * Converts the specified value.
     * 
     * @param from
     *            the value to convert from
     * @return a conversion
     */
    Conversion<T> conversion(F from);
}

// Conversion.addContext() dem der kalder den...

// Success -> Non_Null
// Success -> Null

// Failure

interface CSandbox<F, T> {

    // Maaske den her alligevel. Men kan saa kan man have en
    // ConversionContext context

    default Conversion<T> conversion(F from) {
        return conversion(from, /* ConversionContext.NO_CONTEXT */ null);

        // Conversion.failed(ConversionContext cc, F from, String message);

        // Kan Vi have ConversionContext<F> ????

        // Det vi gerne vil undgaa er at skulle skrive. The Field xxxxx

        // F.eks. KeyConversion
        // Er [Method|Field->TypeVariable->Key]
    }

    Conversion<T> conversion(F from, ConversionContext context);

    // Hvordan haandtere vi null?
    // Vi skal smide en exception
    T convert(F from);

    // Hvordan haandtere vi null?
    // Vi skal smide en exception
    T convert(F from, ConversionContext context);

}