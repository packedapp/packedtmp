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

/**
 *
 */
public non-sealed class FunctionalBeanConfiguration extends BeanConfiguration<Void /* -> to void with Valhalla */ > {

    /**
     * @param handle
     */
    protected FunctionalBeanConfiguration(BeanMaker<Void> handle) {
        super(handle);
    }

    /** {@inheritDoc} */
    @Override
    public final BeanKind kind() {
        return BeanKind.FUNCTIONAL_BEAN;
    }

    /** {@inheritDoc} */
    @Override
    public FunctionalBeanConfiguration named(String name) {
        super.named(name);
        return this;
    }
}

//Maaske man skal returnere en FunctionConfiguration istedet for
//Ideen er vi har en FunctionalBeanConfiguration

//Ved ikke om det er taenkt som enkelt delene i en FunctionalBean??
abstract /* non-sealed */ class FunctionConfiguration /* extends ComponentConfiguration */ {

}
