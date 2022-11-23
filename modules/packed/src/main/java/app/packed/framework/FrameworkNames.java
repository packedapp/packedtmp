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
package app.packed.framework;

import app.packed.bean.BeanExtension;
import app.packed.service.ServiceExtension;

/**
 * This class contains various names of extensions and modules that can be used to avoid loading the.
 * 
 */
// Fx kan det vaere svaert at lave Application layers...
// Hvis man ikke gider importere alle pakker

public final class FrameworkNames {

    /** The name of the module that defines the framework. */
    public static final String BASE_MODULE = "app.packed";

    /** The class name of {@link BeanExtension}. */
    public static final String BASE_BEAN_EXTENSION = BASE_MODULE + ".bean.BeanExtension";

    /** The class name of {@link ServiceExtension}. */
    public static final String BASE_SERVICE_EXTENSION = BASE_MODULE + ".service.ServiceExtension";

    static final String WEB_MODULE = "app.packed.web";

    static final String WEB_WEB_EXTENSION = WEB_MODULE + ".WebExtension";

    private FrameworkNames() {}
}