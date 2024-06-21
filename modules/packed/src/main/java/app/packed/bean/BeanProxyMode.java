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
// On Assembly, Bean, Extension?
// Maybe have a boolean overrideableAtBeanLevel default true

// I don't think we need this. Proxies are always if_needed
public @interface BeanProxyMode {
    Mode mode() default Mode.IF_NEEDED;

    // Maybe if_needed is default???
    public enum Mode {
        ALWAYS, IF_NEEDED, NEVER;
    }
}
// What about ALWAYS + final classes?
// Maybe ALWAYS does not make sense...
// Like why would you proxy a class that didn't need it



// We also need

// @Transactional on outer classes could be done without a bean proxy...
// Something like Cached->Similar...



//////////////// Old stuff
// I think might have @BeanMethodActivator(requiresProxy=true)
//On Class Or Method, or maybe seoearate annotation...

//The idea is, for example,

//@Timed
//@Get
//public void someBeanMethod() {}

//Will not create proxy? Or will it.


//Anyway the idea is to be able to force a proxy for the bean class

//Could be a transformer??
@interface BeanProxyForced {

}

