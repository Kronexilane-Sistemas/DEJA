package Test;

import Test.db.dao.AgendaDAO;
import Test.db.entities.AgendaEntity;

public class Test {
    public static void main(String[] args) {
        // Conexión de Test
        AgendaDAO agDao = new AgendaDAO();
        AgendaEntity en;
        en = agDao.LoadByPK(10);
        System.out.println(en);
    }
}
