package app.packed.component.sandhox;

import java.util.List;
import java.util.Map;

// Hvem provider features... Component, ComponentConfiguration, ComponentStream 

//Component

// Ok, man kan have 
// 0 features
// 1 features
// N features

// ComponentLike (baseclass for Component + ComponentConfiguration?)

public interface FeatureContainer {
    Map<Class<? extends Feature>, List<Feature>> features();

    <T extends Feature> List<T> features(Class<T> featureType);
}

interface FeatureContainer2 {
    Map<Class<? extends Feature>, FeatureSet<?>> features();

    <T extends Feature> FeatureSet<T> features(Class<T> featureType);
}

interface FeatureSet<T> extends Iterable<T> {
    T one();
}