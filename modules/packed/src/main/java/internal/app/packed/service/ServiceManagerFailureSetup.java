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
package internal.app.packed.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

import app.packed.base.Key;
import internal.app.packed.service.build.ExportedServiceSetup;
import internal.app.packed.service.build.ServiceSetup;

/**
 * An error manager is lazily created when an the configuration of an injection manager fails
 */
public class ServiceManagerFailureSetup {

    /** A map of multiple exports of the same key. */
    public final LinkedHashMap<Key<?>, LinkedHashSet<ServiceSetup>> failingDuplicateExports = new LinkedHashMap<>();

    /** A map of all keyed exports where an entry matching the key could not be found. */
    public final LinkedHashMap<Key<?>, LinkedHashSet<ExportedServiceSetup>> failingUnresolvedKeyedExports = new LinkedHashMap<>();

    /** A map of build entries that provide services with the same key. */
    public final LinkedHashMap<Key<?>, LinkedHashSet<ServiceSetup>> failingDuplicateProviders = new LinkedHashMap<>();
    
 // Build -> Exception
 // Compose -> ErrorMessage
 // Instantiation/Injection -> Exception


    public static void addDuplicateNodes(HashMap<Key<?>, LinkedHashSet<ServiceSetup>> dublicateNodes) {
//        ConfigSiteJoiner csj = new ConfigSiteJoiner();
//
//        csj.prefix("    ", "  & ", "  & ");
//        for (var e : dublicateNodes.entrySet()) {
//            // e.getValue().stream().map(BSE::configSite).collect(csj.collector();
//
//            csj.addAll(e.getValue().stream().map(BuildtimeService::configSite).collect(Collectors.toList()));
//        }
        StringBuilder sb = new StringBuilder();

        // create an instance sounds like something that should not be used in the build phase...
        sb.append("ServiceExtension failed");
        int nn = 1;
        for (Map.Entry<Key<?>, LinkedHashSet<ServiceSetup>> e : dublicateNodes.entrySet()) {
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

    public static void addUnresolvedExports(InternalServiceExtension node, HashMap<Key<?>, LinkedHashSet<ExportedServiceSetup>> dublicateNodes) {
        // ArtifactBuildContext abc = node.context().buildContext();
    }

    static String format(ServiceSetup e) {
        return "";
//        // TODO FIX
//        // Need to look in injectable and see if first dependency is SourceAssembly
//        BuildtimeService declaringEntry = e;
//
//        if (declaringEntry == null) {
//            return e.configSite().toString();
//        }
//        StringBuilder sb = new StringBuilder(declaringEntry.configSite().toString());
//        e.configSite().visit(new ConfigSiteVisitor() {
//
//            /** {@inheritDoc} */
//            @Override
//            public void visitAnnotatedMethod(ConfigSite configSite, Method method, Annotation annotation) {
//                sb.append(" via annotated method @");
//                sb.append(annotation.annotationType().getSimpleName());
//                sb.append(" ");
//                sb.append(StringFormatter.formatShortWithParameters(method));
//            }
//
//        });
//        return sb.toString();
    }
}
