package app.packed.bean;

import app.packed.build.BuildHook;

public non-sealed interface BeanBuildHook extends BuildHook {
    void preBuild(BeanConfiguration configuration);

    void posteBuild(BeanConfiguration configuration);

    void verify(BeanMirror container);
}
