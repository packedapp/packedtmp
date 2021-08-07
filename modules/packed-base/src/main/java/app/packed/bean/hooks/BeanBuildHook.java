package app.packed.bean.hooks;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanMirror;
import app.packed.build.BuildHook;


// Hmmmmm hvad er usecases
public non-sealed interface BeanBuildHook extends BuildHook {
    
    void preBuild(BeanConfiguration<?> configuration);

    void posteBuild(BeanConfiguration<?> configuration);

    void verify(BeanMirror container);
}
