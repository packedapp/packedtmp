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
package app.packed.component.guest.usage;

import app.packed.application.ApplicationConfiguration;
import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationTemplate;
import app.packed.application.ApplicationTemplate.Installer;
import app.packed.bean.Inject;
import internal.app.packed.container.PackedContainerKind;
import internal.app.packed.container.PackedContainerTemplate;

/**
 *
 */
public record GuestBean(long nanos) {

    @Inject
    public GuestBean() {
        this(System.nanoTime());
    }

    static final ApplicationTemplate<GuestBean> T = ApplicationTemplate.of(GuestBean.class, c -> {
        c.rootContainer(new PackedContainerTemplate(PackedContainerKind.BOOTSTRAP_APPLICATION));
    });

    static class GuestApplicationHandle extends ApplicationHandle<ApplicationConfiguration, GuestBean> {

        /**
         * @param installer
         */
        GuestApplicationHandle(Installer<GuestBean> installer) {
            super(installer);
        }
    }
}
