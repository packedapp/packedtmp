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
package packed.internal.inject.util.nextapi;

import java.lang.annotation.Annotation;

import app.packed.inject.Factory;
import app.packed.inject.Factory1;
import app.packed.util.TypeLiteral;

/**
 *
 */
// Vi vil gerne
//// fullfilled alle depedendies qualified med en specific annotering
//// Kan returnere forskellige typer alt efter parameterens type

// Wildcard Qualifiers har ikke en type....

// ProvisionException -> Problemer med at finde et element der skal injectes, eller lave det.
// InjectionException -> Problemer med at injecte et korrect elements. Typisk fordi en user metoder smiden en exception.

abstract class WildcardQualifierInjector<T extends Annotation> {

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

//// Et haabloest forsoeg
// Ikke saerlig paen...

@interface WildcardQaualifier2 {
    Class<? extends Factory<?>> value();
}

class SystemPropertyReader2 extends Factory1<SystemProperty, Object> {

    protected SystemPropertyReader2() {
        super(value -> System.getProperties().get(value.value()));
    }
}