package app.packed.attribute;

import app.packed.inject.sandbox.ServiceAttributes;

@interface AttributeRef {

    /** The class that declares the attribute. */
    // Owner?
    Class<?> declaredBy();

    /**
     * The name of the attribute.
     * 
     * @return the name of the attribute
     */
    String name();
}

class MyPolicy {

    // Er sgu ikke paent
    public boolean doRetry(@AttributeRef(declaredBy = ServiceAttributes.class, name = "exported-services") long aa) {
        return false;
    }
}