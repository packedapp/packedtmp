package app.packed.sql;

import app.packed.bean.BeanExtensionSupport;
import app.packed.bean.ContainerBeanConfiguration;
import app.packed.extension.ExtensionSupport;
import app.packed.extension.RootedExtension;

// Har egentlig taget navn efter java.sql pakken og java.sql modulet.

// Formaal provides 1 or more DataSources for injection or usage in someway
// Support for DataSource caching/ Custom eller homemade

// Config/Wirelet/JFR/Metrics

/**
 * This extension manages 1 or more data sources.
 */
//DataSourceExtension
//DataSourceExtension
// JDBCExtension
public class SQLExtension extends RootedExtension {
    
    ContainerBeanConfiguration<InnerBean> abc;

    SQLExtension() {}

    @Override
    protected void onNew() {
        abc = use(BeanExtensionSupport.class).install(InnerBean.class);
    }

    static class InnerBean {
        SQLPoolExtensionPoint point;
    }

    public class Sub extends ExtensionSupport {

        // en ting er med start, noget andet er med slut
        public void extendPool(ContainerBeanConfiguration<? extends SQLPoolExtensionPoint> bean) {
            use(BeanExtensionSupport.class).extensionPoint(abc, (a, b) -> a.point = b, bean);
        }
    }
}
