package app.packed.component;

// maaske er for (JpaEntity e : useExtension(JpaEntityExtensionMirror).entities())
interface JpaEntityBeanMirror extends BeanMirror, JpaEntityMirror {

}

interface JpaEntityMirror {
    String tableName();
}