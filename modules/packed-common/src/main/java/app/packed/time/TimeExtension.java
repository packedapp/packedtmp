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
package app.packed.time;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.Tag;
import app.packed.extension.Extension;
import spp.packed.extension.SearchForAncestor;

/**
 * An extension that makes working with the various concepts in {@code java.time} package easier. More specifically this
 * extension provides a structured way to work with
 * 
 * Clock
 * 
 * ZoneId
 *
 * <p>
 * Ved ikke om det skal ligge her eller i noget i18n
 * Data time formatters... Maybe we have some default once... Logging could use it...
 * 
 * <p>
 * On time zone. Will we auto convert to TimeZone??? Maybe not actually
 * <p>
 * This extension provides no special support for working with legacy APIs such as {@link Date}, {@link Calendar} or
 * {@link TimeZone}.
 */

// Vi ligger beslag paa Clock, TimeZone.. Sorry...

// Virker some scoped... Men med et component trae...
// Er brugbart for ting. Der 99/100 er ens
//ComponentTreeScope<ZoneId>

// CTS.get(ZoneID.class)..

// Det jeg godt kan lide ved denne extension, er at den er saa pokker simple
// En extension. Og saa nogle auto services...
// wire(FooCompo.class, ServiceWirelets.provide(SomeZoneId))..
// I think this would override any extension...

// InstanceX -> ZoneID (Runtime)  <--- Usercode via @Provide
// ApiPoint->ZoneID (Buildtime)

// No support for runtime configuration of zone ids or the clock
// Som udgangspunkt heller ingen support for ikke programatiske timezones. dvs. via ConfigExtension
public class TimeExtension extends Extension {

    // Taenker man kigger i parent'en first...
    // Taenker default er system time zone for root

    /** The container clock. Will use {@link Clock#systemDefaultZone()} as the default if no clock is set. */
    @Nullable
    Clock clock;

    /** The container clock. Will use {@link Clock#systemDefaultZone()} as the default if no */
    @Nullable
    ZoneId zoneId;

    @Nullable
    @SearchForAncestor
    TimeExtension ancestor;

    @Nullable
    @SearchForAncestor
    TimeBean bean;

    /** No time for you. */
    TimeExtension() {}

    // Er det her noget mere "globalt???" policy
    public TimeExtension defaultClock(Clock clock) {
        this.clock = requireNonNull(clock, "clock is null");
        return this;
    }

    void foo() {
        qualifiedZone(new Key<@Tag("qwqwe") ZoneId>() {}, zoneId);
    }

    public void qualifiedZone(Key<ZoneId> key, String zoneId) {}

    public void qualifiedZone(Key<ZoneId> key, ZoneId zoneId) {}

    /**
     * Sets the
     * 
     * @param zoneId
     * 
     * @see #setZone(ZoneId)
     */
    public TimeExtension zone(String zoneId) {
        return zone(ZoneId.of(zoneId));
    }

    /**
     * <p>
     * If you j
     * 
     * @param zoneId
     * 
     * @see TimeWirelets#zoneId(ZoneId)
     */
    // containerZone??? zone()
    // Saa omvendt gider vi vel ikke ogsaa til at bruge
    // ServiceExtension.containerProvide...
    public TimeExtension zone(ZoneId zoneId) {
        this.zoneId = requireNonNull(zoneId, "zoneId is null");
        return this;
    }
}
