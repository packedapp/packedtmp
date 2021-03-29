package app.packed.component;

import app.packed.base.Qualifier;

@Qualifier
public @interface Scoped {
    ComponentScope value() default ComponentScope.CONTAINER;
}
