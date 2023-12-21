package Test.db.dao;

import Test.db.entities.AgendaEntity;
import org.kronexilane.DEJA.Connector.Connector;
import org.kronexilane.DEJA.Connector.ConnectorFactory;
import org.kronexilane.DEJA.DAO.DAO;

public class AgendaDAO extends DAO<AgendaEntity, Integer> {
    @Override
    public Connector Connector() {
        return ConnectorFactory.getInstance().getDBEngine("default");
    }
}
