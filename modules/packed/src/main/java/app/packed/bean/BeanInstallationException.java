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
package app.packed.bean;

import app.packed.application.BuildException;

/**
 * An exception that is thrown if a bean could not be installed.
 */
public class BeanInstallationException extends BuildException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public BeanInstallationException(String message) {
        super(message);
    }

    public BeanInstallationException(String message, Throwable cause) {
        super(message, cause);
    }

}