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
package app.packed;

/**
 *
 */
// Ideen er at separere extension constants og framework constants
// Maaske smide den i .extension???
// Altsaa maaske smide FrameworkX + BaseModuleConstants in app.packed?
// I mean in other packages it would be the root
public class BaseModuleNames {

    /** The class name of {@link BaseExtension}. */
    public static final String BASE_EXTENSION_CLASS = FrameworkNames.MODULE_BASE + ".extension.BaseExtension";

    // I think a goal is to be able to create an application without initializing this class
    /** The class name of {@link BaseExtension}. */
    public static final String CONFIG_EXTENSION_CLASS = FrameworkNames.MODULE_BASE + ".config.ConfigExtension";

    /** The class name of {@link BaseExtension}. */
    public static final String CONFIG_EXTENSION_PROPERTY_DEFAULT_NAME = CONFIG_EXTENSION_CLASS + "#DEFAULT_NAME";
}
