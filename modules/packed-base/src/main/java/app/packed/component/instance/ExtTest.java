package app.packed.component.instance;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionContext;
import app.packed.extension.old.ContainerExtensionBean;
import app.packed.extension.old.ExtensionBeanConnection;

public class ExtTest extends Extension implements ExtTestCommon {

    public ExtTest(ExtensionContext e) {
        
        ExtensionBeanConnection<ExtTest> r = e.findAncestor(ExtTest.class);

        ExtensionBeanConnection<ExtTestCommon> rr = e.findAncestor(ExtTestCommon.class);
        
        System.out.println(r);
        System.out.println(rr);
    }

    static class ExtExtensionRuntime extends ContainerExtensionBean<Extension> implements ExtTestCommon {

    }
}
