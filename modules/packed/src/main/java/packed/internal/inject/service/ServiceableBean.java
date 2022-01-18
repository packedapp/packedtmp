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

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.inject.service.ServiceExtension;
import packed.internal.bean.BeanSetup;
import packed.internal.inject.service.build.BeanInstanceServiceSetup;

/**
 *
 */
public final class ServiceableBean {
    final BeanSetup bean;

    Key<?> export;

    Key<?> provide;

    public ServiceableBean(BeanSetup bean) {
        this.bean = requireNonNull(bean);
    }

    public void onWired() {
        if (provide == null && export == null) {
            return;
        }
        ServiceManagerSetup sms = bean.parent.beans.getServiceManager();
        BeanInstanceServiceSetup setup = new BeanInstanceServiceSetup(bean, provide);
        if (provide != null) {
            sms.addService(setup);
        }
        if (export != null) {
            sms.exports().export(setup);
        }
    }

    public Key<?> defaultKey() {
        return bean.defaultKey();
    }

    public void export() {
        export = InternalServiceUtil.checkKey(bean.hookModel.clazz, bean.defaultKey());
        bean.parent.useExtension(ServiceExtension.class);
    }

    public void provide() {
        provide = InternalServiceUtil.checkKey(bean.hookModel.clazz, bean.defaultKey());
        bean.parent.useExtension(ServiceExtension.class);
    }

    public void provideAs(Class<?> key) {
        provide = InternalServiceUtil.checkKey(bean.hookModel.clazz, key);
        bean.parent.useExtension(ServiceExtension.class);
    }

    public void provideAs(Key<?> key) {
        provide = InternalServiceUtil.checkKey(bean.hookModel.clazz, key);
        bean.parent.useExtension(ServiceExtension.class);
    }

// Ser dum ud naar man laver completion
    public Optional<Key<?>> providedAs() {
        return Optional.ofNullable(provide);
    }
}
