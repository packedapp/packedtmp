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
package packed.internal.service.buildtime;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

import app.packed.base.Key;
import app.packed.config.ConfigSite;
import app.packed.config.ConfigSiteVisitor;
import app.packed.introspection.MethodDescriptor;
import packed.internal.config.ConfigSiteJoiner;
import packed.internal.service.buildtime.export.ExportedBuildEntry;
import packed.internal.util.StringFormatter;

/**
 *
 */
// Build -> Exception
// Compose -> ErrorMessage
// Instantiation/Injection -> Exception

public final class ErrorMessages {

    public static void addDuplicateNodes(HashMap<Key<?>, LinkedHashSet<BuildEntry<?>>> dublicateNodes) {
        ConfigSiteJoiner csj = new ConfigSiteJoiner();

        csj.prefix("    ", "  & ", "  & ");
        for (var e : dublicateNodes.entrySet()) {
            // e.getValue().stream().map(BSE::configSite).collect(csj.collector();

            csj.addAll(e.getValue().stream().map(BuildEntry::configSite).collect(Collectors.toList()));
            System.out.println(csj.toString());
        }
        System.out.println("------");
        StringBuilder sb = new StringBuilder();

        // create an instance sounds like something that should not be used in the build phase...
        sb.append("ServiceExtension failed");
        int nn = 1;
        for (Map.Entry<Key<?>, LinkedHashSet<BuildEntry<?>>> e : dublicateNodes.entrySet()) {
            sb.append("\n\n");
            Key<?> key = e.getKey();
            String n = key.qualifier().map(ee -> "@" + ee.annotationType().getSimpleName() + " ").orElse("") + key.typeLiteral().toStringSimple();
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

    public static void addUnresolvedExports(ServiceExtensionNode node, HashMap<Key<?>, LinkedHashSet<ExportedBuildEntry<?>>> dublicateNodes) {
        // ArtifactBuildContext abc = node.context().buildContext();
        // System.out.println(abc);
    }

    static String format(BuildEntry<?> e) {
        if (e.declaringEntry() == null) {
            return e.configSite().toString();
        }
        StringBuilder sb = new StringBuilder(e.declaringEntry().configSite().toString());
        e.configSite().visit(new ConfigSiteVisitor() {

            /** {@inheritDoc} */
            @Override
            public void visitAnnotatedMethod(ConfigSite configSite, MethodDescriptor method, Annotation annotation) {
                sb.append(" via annotated method @");
                sb.append(annotation.annotationType().getSimpleName());
                sb.append(" ");
                sb.append(StringFormatter.formatShortWithParameters(method));
            }

        });
        return sb.toString();
    }
}
