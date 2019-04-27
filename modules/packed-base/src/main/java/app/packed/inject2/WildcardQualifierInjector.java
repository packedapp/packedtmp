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
package app.packed.inject2;

import java.lang.annotation.Annotation;

import app.packed.util.TypeLiteral;

/**
 *
 */

// Wildcard Qualifiers har ikke en type....
public abstract class WildcardQualifierInjector<T extends Annotation> {

    protected abstract Object provide(TypeLiteral<?> type, T value);

    // protected int provideInt(T value);

}

@interface WildcardQaualifier {
    Class<? extends WildcardQualifierInjector<?>> value();
}

@WildcardQaualifier(SystemPropertyReader.class) // or Qualifier(wildcard=SystemPropertyReader.class)
@interface SystemProperty {
    String value();
}

class SystemPropertyReader extends WildcardQualifierInjector<SystemProperty> {

    /** {@inheritDoc} */
    @Override
    protected Object provide(TypeLiteral<?> type, SystemProperty value) {
        return System.getProperties().get(value.value());
    }
}