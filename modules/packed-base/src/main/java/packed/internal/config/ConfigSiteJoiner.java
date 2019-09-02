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
package packed.internal.config;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collector;
import java.util.stream.Stream;

import app.packed.config.ConfigSite;
import app.packed.util.Nullable;

/**
 * This class can be used to output formatted strings from one or more {@link ConfigSite config sites}. Config site
 * joiners does not store any config sites itself, but is instead an immutable xxx. It is typically created on the fly,
 * but can also easily be stored in static fields to avoid creating new instances every time.
 * <p>
 * Unlike {@link StringJoiner} this class does not store any elements itself. Instead, after the config site joiner has
 * been properly constructed, the user must call one of the join methods: {@link #join(ConfigSite...)},
 * {@link #join(List)} or {@link #join(Stream)} to construct the final string.
 */

// Kunne ogsaa lave den kun en collector

// ConfigSiteCollectorBuilder
// sawtack indention
public final class ConfigSiteJoiner {

    private final ArrayList<ConfigSite> configSites = new ArrayList<>();

    @Nullable
    String first;
    @Nullable
    private String indent;
    @Nullable
    String intermediate;
    @Nullable
    String last;

    /**
     * @param configSite
     *            the config site to add
     * @return this {@code ConfigSiteJoiner}
     */
    public ConfigSiteJoiner add(ConfigSite configSite) {
        requireNonNull(configSite, "configSite is null");
        configSites.add(configSite);
        return this;
    }

    public ConfigSiteJoiner addAll(Iterable<ConfigSite> configSites) {
        requireNonNull(configSites, "configSites is null");
        for (ConfigSite configSite : configSites) {
            add(configSite);
        }
        return this;
    }

    // Any config sites that have already been added are ignored...
    Collector<ConfigSite, ?, String> collector() {

        // Hmm, maaske skal den vaere immutable...
        // og saa med with() <- hvor man storer den i en statisk field

        // og saa med join(ConfigSite... configSites), join(List configSites), join(Stream configSites), Collector<String, ?,
        // ConfigSite> collector();

        //// Maaske
        throw new UnsupportedOperationException();
    }

    /**
     * Indent
     * 
     * @param indent
     *            the string to indent before each line
     * @return this {@code ConfigSiteJoiner}
     */
    public ConfigSiteJoiner indent(String indent) {
        this.indent = requireNonNull(indent, "indent is null");
        return this;
    }

    /**
     * Joins all the specified config sites to a string.
     * 
     * @param configSites
     *            the config sites to join
     * @return the joined string
     */
    public String join(ConfigSite... configSites) {
        requireNonNull(configSites, "configSites is null");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < configSites.length; i++) {
            process(sb, configSites[i], i, configSites.length);
        }
        return sb.toString();
    }

    public String join(List<ConfigSite> configSites) {
        requireNonNull(configSites, "configSites is null");
        return join(configSites.toArray(n -> new ConfigSite[n]));
    }

    public String join(Stream<ConfigSite> configSites) {
        requireNonNull(configSites, "configSites is null");
        return join(configSites.toArray(n -> new ConfigSite[n]));
    }

    public ConfigSiteJoiner postfix(@Nullable String first, @Nullable String intermediate, @Nullable String last) {

        return this;
    }

    public ConfigSiteJoiner prefix(@Nullable String first, @Nullable String intermediate, @Nullable String last) {
        this.first = first;
        this.intermediate = intermediate;
        this.last = last;
        return this;
    }
    // A visititor that can format Config Sites

    private void process(StringBuilder sb, ConfigSite cs, int n, int total) {
        if (cs == null) {
            throw new NullPointerException("The config site at index " + n + " is null");
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < configSites.size(); i++) {
            if (indent != null) {
                sb.append(indent);
            }
            if (i == 0) {
                if (first != null) {
                    sb.append(first);
                }
            } else if (i == configSites.size() - 1) {
                if (last != null) {
                    sb.append(last);
                }
            } else if (intermediate != null) {
                sb.append(intermediate);
            }

            ConfigSite site = configSites.get(i);
            sb.append(site);
            sb.append("\n");
        }
        return sb.toString();
    }

    static ConfigSiteJoiner newLine() {
        throw new UnsupportedOperationException();
    }

    static ConfigSiteJoiner sameLine(CharSequence delimiter) {
        throw new UnsupportedOperationException();
    }
}
