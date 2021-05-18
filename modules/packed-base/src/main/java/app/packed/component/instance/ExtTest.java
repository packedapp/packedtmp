package app.packed.component.instance;

import app.packed.container.Extension;
import app.packed.container.ExtensionAncestor;
import app.packed.container.ExtensionConfiguration;
import app.packed.container.ExtensionRuntime;

public class ExtTest extends Extension implements ExtTestCommon {

    public ExtTest(ExtensionConfiguration e, ExtensionAncestor<ExtTest> a) {
        
        ExtensionAncestor<ExtTest> r = e.findAncestor(ExtTest.class);
        if (r.isPresent()) {

        }

        ExtensionAncestor<ExtTestCommon> rr = e.findAncestor(ExtTestCommon.class);
        if (rr.isPresent()) {
            
        }
    }

    static class ExtExtensionRuntime extends ExtensionRuntime<ExtTest> implements ExtTestCommon {

    }
}
