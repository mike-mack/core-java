/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spine3.server.event.enrich;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;

import java.util.Properties;
import java.util.Set;

import static org.spine3.io.IoUtil.loadAllProperties;

/**
 * A map from an event enrichment Protobuf type name to the corresponding type name(s) of event(s) to enrich.
 *
 * <p>Example:
 * <p>{@code proto.type.MyEventEnrichment} - {@code proto.type.FirstEvent},{@code proto.type.SecondEvent}
 *
 * @author Alexander Litus
 */
/* package */ class EventEnrichmentsMap {

    /**
     * A path to the file which contains enrichment and event Protobuf type names.
     * Is generated by Gradle during the build process.
     */
    private static final String PROPS_FILE_PATH = "enrichments.properties";

    /** A separator between event types in the `.properties` file. */
    private static final String EVENT_TYPE_SEPARATOR = ",";

    private static final ImmutableMultimap<String, String> enrichmentsMap = buildEnrichmentsMap();

    private EventEnrichmentsMap() {}

    /** Returns the immutable map instance. */
    /* package */ static ImmutableMultimap<String, String> getInstance() {
        return enrichmentsMap;
    }

    private static ImmutableMultimap<String, String> buildEnrichmentsMap() {
        final ImmutableSet<Properties> propertiesSet = loadAllProperties(PROPS_FILE_PATH);
        final Builder builder = new Builder(propertiesSet);
        final ImmutableMultimap<String, String> result = builder.build();
        return result;
    }

    private static class Builder {

        private final Iterable<Properties> properties;
        private final ImmutableMultimap.Builder<String, String> builder;

        /* package */ Builder(Iterable<Properties> properties) {
            this.properties = properties;
            this.builder = ImmutableMultimap.builder();
        }

        /* package */ ImmutableMultimap<String, String> build() {
            for (Properties props : this.properties) {
                put(props);
            }
            return builder.build();
        }

        private void put(Properties props) {
            final Set<String> enrichmentTypes = props.stringPropertyNames();
            for (String enrichmentType : enrichmentTypes) {
                final String eventTypesStr = props.getProperty(enrichmentType);
                final Iterable<String> eventTypes = FluentIterable.from(eventTypesStr.split(EVENT_TYPE_SEPARATOR));
                put(enrichmentType, eventTypes);
            }
        }

        private void put(String enrichmentType, Iterable<String> eventTypes) {
            for (String eventType : eventTypes) {
                builder.put(enrichmentType, eventType);
            }
        }
    }
}
