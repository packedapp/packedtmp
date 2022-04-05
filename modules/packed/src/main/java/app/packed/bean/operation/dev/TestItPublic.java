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
package app.packed.bean.operation.dev;

import app.packed.bean.operation.OperationMirror;
import app.packed.bean.operation.dev.TestIt.RouterImpl;

/**
 *
 */
class TestItPublic {

    interface Router {
        void setMethod(String method);
    }

    interface HttpRequest {}

    interface HttpResponse {}

    static class HttpMethodOperationMirror extends OperationMirror {

        final RouterImpl impl;

        HttpMethodOperationMirror(RouterImpl impl) {
            this.impl = impl;
        }

        public String getMethod() {
            return impl.method;
        }
    }
}

// Ideen er at give mig alle operationer der container en SecurityInterceptor
// appMirror.operations(HttpReq.class).filter(o->o.interceptors.contains(HttpSequrityInterceptorMirror.class)));