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

import java.util.function.BiConsumer;

import app.packed.base.TypeToken;
import app.packed.bean.BeanDriver;
import app.packed.bean.BeanKind;
import app.packed.bean.operation.OC2;
import app.packed.bean.operation.OperationDriver;
import app.packed.bean.operation.dev.TestItPublic.HttpMethodOperationMirror;
import app.packed.bean.operation.dev.TestItPublic.HttpRequest;
import app.packed.bean.operation.dev.TestItPublic.HttpResponse;
import app.packed.bean.operation.dev.TestItPublic.Router;
import app.packed.extension.Extension;

/**
 *
 */
public class TestIt extends Extension<TestIt> {

    BeanDriver<?> functionalBean;

    static final OperationDriver D = OperationDriver.of(new TypeToken<BiConsumer<HttpRequest, HttpResponse>>() {}, null);

    private BeanDriver<?> fb() {
        BeanDriver<?> fb = functionalBean;
        if (fb == null) {
            functionalBean = fb = bean().newApplicationBeanDriver(BeanKind.FUNCTIONAL);
            // fb.setPrefix("fWeb")
        }
        return fb;
    }

    Router onGet(BiConsumer<HttpRequest, HttpResponse> c) {
        RouterImpl ri = new RouterImpl();
        fb().addOperation(ri);

        return ri;
    }

    Router onPost(BiConsumer<HttpRequest, HttpResponse> c) {
        RouterImpl ri = new RouterImpl();
        fb().addOperation(ri);
        return ri;
    }

    class RouterImpl extends OC2 implements Router {
        String method;

        RouterImpl() {

        }

        /** {@inheritDoc} */
        @Override
        public void setMethod(String method) {
            this.method = method;
        }

        protected HttpMethodOperationMirror mirror() {
            return new HttpMethodOperationMirror(this);
        }
    }
}
