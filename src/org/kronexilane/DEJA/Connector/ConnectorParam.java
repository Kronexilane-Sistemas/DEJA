package org.kronexilane.DEJA.Connector;

/**
 * Clase que representa los parámetros de conexión
 * a la base de datos.
 *
 * @author SERGIO
 */
public class ConnectorParam {
    private final String driver;
    private final String url;
    private final String user;
    private final String password;

    public ConnectorParam(String driver,
                          String url,
                          String user,
                          String password) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }


    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
