package app.packed.bean.hooks.sandbox;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanMirror;
import app.packed.build.BuildHook;


// Hmmmmm hvad er usecases
public non-sealed interface BeanBuildHook extends BuildHook {
    
    void afterBuild(BeanConfiguration<?> configuration);

    void beforeBuild(BeanConfiguration<?> configuration);

    void verify(BeanMirror container);
}
