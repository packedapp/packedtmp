package app.packed.component.instance;

import app.packed.container.Extension;
import app.packed.container.ExtensionAncestorRelation;
import app.packed.container.ExtensionContext;
import app.packed.container.Extensor;

public class ExtTest extends Extension implements ExtTestCommon {

    public ExtTest(ExtensionContext e) {
        
        ExtensionAncestorRelation<ExtTest> r = e.findFirstAncestor(ExtTest.class);

        ExtensionAncestorRelation<ExtTestCommon> rr = e.findFirstAncestor(ExtTestCommon.class);
        
        System.out.println(r);
        System.out.println(rr);
    }

    static class ExtExtensionRuntime extends Extensor<ExtTest> implements ExtTestCommon {

    }
}
