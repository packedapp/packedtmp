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
package packed.internal.util.configurationsite;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;

import app.packed.util.ConfigurationSite;
import app.packed.util.FieldDescriptor;

/** A configuration point originating from an annotated method. */
public final class AnnotatedFieldConfigurationSite extends AbstractConfigurationSite {

    /** The annotation. */
    final Annotation annotation;

    /** The field. */
    final FieldDescriptor field;

    AnnotatedFieldConfigurationSite(ConfigurationSite parent, String operation, FieldDescriptor field, Annotation annotation) {
        super(parent, operation);
        this.field = requireNonNull(field);
        this.annotation = requireNonNull(annotation);
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public FieldDescriptor getField() {
        return field;
    }

    // toString
    // x.op because of @Completes on field fox.sss.dd#sd
    // install.component[/sd/qwzzz] at sdkdkdk.sdsd.Dsksks:2323 .....Ahhh, saa skal man beholde references til registrerings
    // pointet????
    // Bundle.configure at xxxx:2323
    // embed
    // dkdkd
    // sdsd
    // App.of

    // CompletionSite.. for lifecycle as ConfigurationSite for configuration???

}
