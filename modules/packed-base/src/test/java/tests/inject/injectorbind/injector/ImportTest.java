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

import app.packed.container.BaseBundle;
import app.packed.inject.Factory1;
import app.packed.inject.Injector;
import app.packed.util.Qualifier;

/**
 *
 */
public class ImportTest {

    // The import at (Xxxx) and (Yyyy) both defines are service with Key<ZoneId>
    public static void main(String[] args) {

        Injector i = Injector.configure(c -> {
            // c.link(new London(), OldServiceWirelets.rebindImport(Key.of(ZonedDateTime.class), new Key<@ZoneAnno("London")
            // ZonedDateTime>() {}));
            // c.link(new London(), OldServiceWirelets.rebindImport(Key.of(ZonedDateTime.class), new Key<@ZoneAnno("Berlin")
            // ZonedDateTime>() {}));
        });
        System.out.println(i);
    }

    public static final class I extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            export(provide(ZoneId.systemDefault()).as(ZoneId.class));
            export(provide(new Factory1<ZoneId, ZonedDateTime>(ZonedDateTime::now) {}).prototype());
        }
    }

    public static final class London extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            provide(ZoneId.of("Europe/London")).as(ZoneId.class);
            export(provide(new Factory1<ZoneId, ZonedDateTime>(ZonedDateTime::now) {}).prototype());
        }
    }

    public static final class Berlin extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            provide(ZoneId.of("Europe/Berlin")).as(ZoneId.class);
            export(provide(new Factory1<ZoneId, ZonedDateTime>(ZonedDateTime::now) {}).prototype());

        }
    }

    @Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    public @interface ZoneAnno {
        String value();
    }
}
