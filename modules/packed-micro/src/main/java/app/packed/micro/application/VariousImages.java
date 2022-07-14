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
package app.packed.micro.application;

import app.packed.application.App;
import app.packed.application.ApplicationLauncher;
import app.packed.container.BaseAssembly;

/**
 *
 */
public class VariousImages {

    public static final ApplicationLauncher<Void> EMPTY_IMAGE = App.buildReusable(of(0));
    public static final ApplicationLauncher<Void> ONE_BEAN_IMAGE = App.buildReusable(of(1));
    public static final ApplicationLauncher<Void> FIVE_BEAN_IMAGE = App.buildReusable(of(5));
    public static final ApplicationLauncher<Void> FIFTY_BEAN_IMAGE = App.buildReusable(of(50));
    public static final ApplicationLauncher<Void> FIVEHUNDRED_BEAN_IMAGE = App.buildReusable(of(500));

    public static BaseAssembly of(int beanCount) {
        return new BaseAssembly() {

            @Override
            public void build() {
                for (int i = 0; i < beanCount; i++) {
                    installInstance("foo").named(Integer.toString(i));
                }
            }
        };
    }
    
    public static void main(String[] args) {
        VariousImages.FIVEHUNDRED_BEAN_IMAGE.launch();
    }
}
