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
package internal.app.packed.bean;

import java.util.ArrayList;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstaller;
import app.packed.bean.sidebean.SideBeanConfiguration;

/**
 *
 */
public class SideBeanHandle extends BeanHandle<SideBeanConfiguration<?>> {

    public ArrayList<PackedSideBeanUsage> usage = new ArrayList<>();

    /**
     * @param installer
     */
    public SideBeanHandle(BeanInstaller installer) {
        super(installer);
    }

}
