package Test.db.datasources;

import org.kronexilane.DEJA.Connector.Connector;
import org.kronexilane.DEJA.Connector.ConnectorParam;

/**
 * Conector a la base de datos de test
 * de https://www.kronexilane.org
 */
public class ConnectorToTest extends Connector {
    @Override
    public ConnectorParam getParameters() {

        return new ConnectorParam
                (
                        "com.mysql.cj.jdbc.Driver",
                        "jdbc:mysql://www.kronexilane.org/test",
                        "TestUser",
                        "TestUser"
                );
    }
}
