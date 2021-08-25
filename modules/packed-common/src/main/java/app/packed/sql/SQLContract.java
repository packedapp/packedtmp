package app.packed.sql;

import java.util.Set;

// We need this database

// Required
// Optional<>  (Can be specified via Wirelets)
public interface SQLContract {
    
    Set<DatasourceContract> datasources();

    // Config/String/? toConfigExample();
    
    interface DatasourceContract {

        // fx.
        // .sql (.port) =
        // eller sql.default (.port) =
        // og sql.nr3 (.port)

        // Her aendrer vi jo lige pludselig opsaetning hvis vi tilfoejer en ny data source
        String configRoot();

        /** {@return the name of the datasource}. */
        String name();
    }
}
// Tillader vi altid override via Wirelets????