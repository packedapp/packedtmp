package app.packed.extension.sandbox.connect;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionBean;

public interface ExtensionBeanConnector<E extends Extension, B extends ExtensionBean<E>> {
    E findExtension();

    void inherit();
    
    void deferToRuntime();

    enum ConnectionMode {
        // Eller ogsaa bare connect og connectParent();
        PARENT, SAME_APPLICATION_OR_PARENT, ANY;
    }
}

class NewExt {

    // Saa vi har sagt hvilken bean det er
    <E extends Extension, B extends ExtensionBean<E>> ExtensionBeanConnector<E, B> connect(Class<B> extensionBean) {
        throw new UnsupportedOperationException();
    }
}

class Usage extends NewExt {

    public void build() {
        ExtensionBeanConnector<MyExt, MyExtBean> b = connect(MyExtBean.class);
        System.out.println(b);
    }
}