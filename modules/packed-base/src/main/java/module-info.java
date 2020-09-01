module app.packed.base {
    exports app.packed.base; // then have @Preview @DeprecatedWithReason
    exports app.packed.cli;
    exports app.packed.introspection;
    exports app.packed.component;
    exports app.packed.config;
    exports app.packed.container;
    exports app.packed.guest;
    exports app.packed.hook;
    exports app.packed.inject;
    exports app.packed.lifecycle;
    exports app.packed.service;
    exports app.packed.sidecar;
    exports app.packed.statemachine;

    // Temporary...
    exports packed.internal.invoke to app.packed.banana, app.packed.function, app.packed.conta;
    exports packed.internal.invoke.typevariable to app.packed.banana, app.packed.function;
    exports packed.internal.util to app.packed.configuration, app.packed.cli, app.packed.conta;

    exports packed.internal.component to app.packed.errorhandling;
    exports packed.internal.component.wirelet to app.packed.errorhandling;
    exports packed.internal.container to app.packed.errorhandling, app.packed.conta;
    exports packed.internal.hook to app.packed.errorhandling;
    exports packed.internal.lifecycle.old to app.packed.errorhandling;
    exports packed.internal.errorhandling to app.packed.errorhandling;
    exports packed.internal.base.attribute to app.packed.attribute;
    exports packed.internal.sidecar.old to app.packed.conta;
    exports packed.internal.hook.applicator to app.packed.errorhandling, app.packed.cli;

    opens app.packed.service to app.packed.service;

    requires java.management;
}

// requires static org.graalvm.sdk;

// uses app.packed.util.ModuleEnv;
// provides app.packed.util.ModuleEnv with packed.internal.bundle.DefaultBS;