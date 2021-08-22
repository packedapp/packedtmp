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

import java.time.Clock;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;

import app.packed.container.Wirelet;

/**
 * A number of wirelets that can be used together with the {@link TimeExtension}.
 */
public final class TimeWirelets extends Wirelet {

    private TimeWirelets() {}

    /**
     * Returns a wirelet that can be used to set the default {@link ZoneId} of a container.
     * 
     * @param zoneId
     *            the zoneId to use as the default time zone
     * @return the wirelet
     * @see #zoneId(ZoneId)
     * @see ZoneId#of(String)
     * @throws DateTimeException
     *             if the zone ID has an invalid format
     * @throws ZoneRulesException
     *             if the zone ID is a region ID that cannot be found
     */
    public static TimeWirelets zone(String zoneId) {
        return zone(ZoneId.of(zoneId));
    }

    /**
     * Returns a wirelet that can be used to set the default {@link ZoneId} of a container.
     * 
     * @param zoneId
     *            the zoneId to use as the default time zone
     * @return the wirelet
     * @see #zoneId(ZoneId)
     */
    public static TimeWirelets zone(ZoneId zoneId) {
        throw new UnsupportedOperationException();
    }

    public static TimeWirelets clock(Clock clock) {
        throw new UnsupportedOperationException();
    }
}
// Umiddelbart ville jeg jo sige at yderste (Config) har ret over wirelets...
// Men omvendt naar man bruger en wirelet. Assumer jeg at man ikke har control
// over containeren

//For sjov har vi proevde at rename den til TimeWirelet... istedet for TimeWirelet[s]
//Fordi vi maaske gerne vil have BuildWirelets er inheritable...
