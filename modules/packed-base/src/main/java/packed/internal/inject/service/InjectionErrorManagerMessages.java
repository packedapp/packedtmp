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
package packed.internal.inject.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

import app.packed.base.Key;
import app.packed.config.ConfigSite;
import app.packed.config.ConfigSiteVisitor;
import packed.internal.config.ConfigSiteJoiner;
import packed.internal.inject.service.build.BuildtimeService;
import packed.internal.inject.service.build.ExportedBuildtimeService;
import packed.internal.util.StringFormatter;

/**
 *
 */
// Build -> Exception
// Compose -> ErrorMessage
// Instantiation/Injection -> Exception

public final class InjectionErrorManagerMessages {

    public static void addDuplicateNodes(HashMap<Key<?>, LinkedHashSet<BuildtimeService>> dublicateNodes) {
        ConfigSiteJoiner csj = new ConfigSiteJoiner();

        csj.prefix("    ", "  & ", "  & ");
        for (var e : dublicateNodes.entrySet()) {
            // e.getValue().stream().map(BSE::configSite).collect(csj.collector();

            csj.addAll(e.getValue().stream().map(BuildtimeService::configSite).collect(Collectors.toList()));
        }
        StringBuilder sb = new StringBuilder();

        // create an instance sounds like something that should not be used in the build phase...
        sb.append("ServiceExtension failed");
        int nn = 1;
        for (Map.Entry<Key<?>, LinkedHashSet<BuildtimeService>> e : dublicateNodes.entrySet()) {
            sb.append("\n\n");
            Key<?> key = e.getKey();
            String n = "";
            if (key.hasQualifiers()) {
                // TODO fix
                // String n = key.qualifier().map(ee -> "@" + ee.annotationType().getSimpleName() + " ").orElse("") +
                // key.typeLiteral().toStringSimple();
            }
            n += key.typeToken().toStringSimple();
            String ss = e.getValue().stream().map(ee -> format(ee)).collect(Collectors.joining("\n  & "));
            // A service with the key <@Foo java.lang.Integer> is configured multiple places:/
            sb.append(nn + ") Multiple services registered with the same Key<" + n + ">:\n    ");
            sb.append(ss);
            sb.append("\n");
            nn += 1;

        }

        // throw new IllegalStateException("\nMultiple services registered with the same key {" + n + "}:\n " + ss);

        throw new IllegalStateException(sb.toString());
    }

    public static void addUnresolvedExports(ServiceFabric node, HashMap<Key<?>, LinkedHashSet<ExportedBuildtimeService>> dublicateNodes) {
        // ArtifactBuildContext abc = node.context().buildContext();
    }

    static String format(BuildtimeService e) {
        // TODO FIX
        // Need to look in injectable and see if first dependency is SourceAssembly
        BuildtimeService declaringEntry = e;

        if (declaringEntry == null) {
            return e.configSite().toString();
        }
        StringBuilder sb = new StringBuilder(declaringEntry.configSite().toString());
        e.configSite().visit(new ConfigSiteVisitor() {

            /** {@inheritDoc} */
            @Override
            public void visitAnnotatedMethod(ConfigSite configSite, Method method, Annotation annotation) {
                sb.append(" via annotated method @");
                sb.append(annotation.annotationType().getSimpleName());
                sb.append(" ");
                sb.append(StringFormatter.formatShortWithParameters(method));
            }

        });
        return sb.toString();
    }
}
