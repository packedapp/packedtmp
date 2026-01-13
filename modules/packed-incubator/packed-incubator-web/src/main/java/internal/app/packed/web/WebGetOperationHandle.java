/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.web;

import app.packed.bean.BeanIntrospector;
import app.packed.component.SidehandleBeanConfiguration;
import app.packed.component.SidehandleTargetKind;
import app.packed.extension.BaseExtension;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationInstaller;
import app.packed.web.HttpContext;
import app.packed.web.WebGet;
import app.packed.web.WebOperationMirror;
import internal.app.packed.extension.base.BaseExtensionOperationHandle;

/**
 * Operation handle for @WebGet annotated methods.
 */
public final class WebGetOperationHandle extends BaseExtensionOperationHandle<OperationConfiguration> {

    /** The URL pattern this handler responds to. */
    public String urlPattern;

    private WebGetOperationHandle(OperationInstaller installer) {
        super(installer);
    }

    @Override
    protected OperationConfiguration newOperationConfiguration() {
        return new OperationConfiguration(this);
    }

    @Override
    public WebOperationMirror newOperationMirror() {
        return new WebOperationMirror(this);
    }

    /**
     * Called when a @WebGet annotation is found on a method.
     */
    public static void onWebGetAnnotation(BeanIntrospector<BaseExtension> introspector, BeanIntrospector.OnMethod method, WebGet annotation) {

        // Install server manager as singleton (only once per application)
        introspector.applicationBase().installIfAbsent(WebServerManager.class, c -> c.provide());

        // Install sidehandle per operation
        SidehandleBeanConfiguration<WebServerSidehandle> sideBean = introspector.applicationBase().installSidebeanIfAbsent(WebServerSidehandle.class,
                SidehandleTargetKind.OPERATION, _ -> {});

        // Install the operation with HttpContext available
        WebGetOperationHandle handle = method.newOperation().addContext(HttpContext.class).attachToSidebean(sideBean).install(WebGetOperationHandle::new);

        handle.urlPattern = annotation.url();
    }

    @Override
    protected void onConfigured() {
        sidehandle().bindConstant(String.class, urlPattern);
    }
}
