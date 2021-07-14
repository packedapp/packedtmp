package app.packed.bean;

// maaske er for (JpaEntity e : useExtension(JpaEntityExtensionMirror).entities())
interface JpaEntityBeanMirror extends BeanMirror, JpaEntityMirror {

}

interface JpaEntityMirror {
    String tableName();
}

// Skal vaere en konkret klasse... Men ved ikke om det skal vaere en bean...
// Ellers bare JpaExtensionMirror.repositories()
interface JpaRepositoryMirror extends BeanMirror { 
    
    // Share denne faetter paa tvaers af alt
    /// HibernateRepo
}
