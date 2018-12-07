module app.packed.base {
    exports app.packed.bundle;
    exports app.packed.container;
    exports app.packed.inject;
    exports app.packed.lifecycle;
    exports app.packed.util;

    exports packed.internal.bundle to app.packed.base.devtools;
    exports packed.internal.inject to app.packed.base.devtools;
    exports packed.internal.inject.builder to app.packed.base.devtools;
    exports packed.internal.invokers to app.packed.base.devtools;
    exports packed.internal.inject.runtime to app.packed.base.devtools;
    exports packed.internal.inject.support to app.packed.base.devtools;
    exports packed.internal.classscan to app.packed.base.devtools;
    exports packed.internal.util.descriptor to app.packed.base.devtools;
    exports packed.internal.util.configurationsite to app.packed.base.devtools;
}