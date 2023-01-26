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
package tests.inject.injectorbind.injector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import app.packed.bindings.Key;
import app.packed.bindings.Qualifier;
import app.packed.container.BaseAssembly;
import app.packed.operation.Op1;
import app.packed.service.ServiceLocator;
import app.packed.service.ServiceWirelets;

/**
 *
 */
public class ImportTest {

    // The import at (Xxxx) and (Yyyy) both defines are service with Key<ZoneId>
    public static void main(String[] args) {
        ServiceLocator i = ServiceLocator.of(c -> {
            c.link(new London(), ServiceWirelets.transformIn(t -> t.rekey(Key.of(ZonedDateTime.class), new Key<@ZoneAnno("London") ZonedDateTime>() {})));
            c.link(new London(), ServiceWirelets.transformIn(t -> t.rekey(Key.of(ZonedDateTime.class), new Key<@ZoneAnno("Berlin") ZonedDateTime>() {})));
        });
        System.out.println(i);
    }

    public static final class I extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            provideInstance(ZoneId.systemDefault()).provideAs(ZoneId.class).export();
            providePrototype(new Op1<ZoneId, ZonedDateTime>(ZonedDateTime::now) {}).export();

        }
    }

    public static final class London extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            provideInstance(ZoneId.of("Europe/London")).provideAs(ZoneId.class);
            providePrototype(new Op1<ZoneId, ZonedDateTime>(ZonedDateTime::now) {}).export();
        }
    }

    public static final class Berlin extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            provideInstance(ZoneId.of("Europe/Berlin")).provideAs(ZoneId.class);
            providePrototype(new Op1<ZoneId, ZonedDateTime>(ZonedDateTime::now) {}).export();
        }
    }

    @Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    public @interface ZoneAnno {
        String value();
    }
}
