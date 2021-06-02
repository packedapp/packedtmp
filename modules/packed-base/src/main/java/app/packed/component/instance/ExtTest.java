package app.packed.component.instance;

import app.packed.extension.ContainerExtensor;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionConnection;
import app.packed.extension.ExtensionContext;

public class ExtTest extends Extension implements ExtTestCommon {

    public ExtTest(ExtensionContext e) {
        
        ExtensionConnection<ExtTest> r = e.findFirstAncestor(ExtTest.class);

        ExtensionConnection<ExtTestCommon> rr = e.findFirstAncestor(ExtTestCommon.class);
        
        System.out.println(r);
        System.out.println(rr);
    }

    static class ExtExtensionRuntime extends ContainerExtensor<Extension> implements ExtTestCommon {

    }
}
