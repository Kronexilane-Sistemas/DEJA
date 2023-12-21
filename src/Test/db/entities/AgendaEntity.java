package Test.db.entities;

import org.kronexilane.DEJA.Annotations.DEJAEntity;
import org.kronexilane.DEJA.Annotations.DEJAPrimaryKey;

import java.util.LinkedHashMap;

@DEJAEntity(name = "agenda")
public class AgendaEntity implements Cloneable {
    @DEJAPrimaryKey(autonumeric = true)
    private Integer idagenda;
    private String Nombre;
    private String Apellido1;
    private String Apellido2;
    private String Telefono;

    public Integer getIdagenda() {
        return idagenda;
    }

    public void setIdagenda(Integer idagenda) {
        this.idagenda = idagenda;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getApellido1() {
        return Apellido1;
    }

    public void setApellido1(String apellido1) {
        Apellido1 = apellido1;
    }

    public String getApellido2() {
        return Apellido2;
    }

    public void setApellido2(String apellido2) {
        Apellido2 = apellido2;
    }

    public String getTelefono() {
        return Telefono;
    }

    public void setTelefono(String telefono) {
        Telefono = telefono;
    }

    @Override
    public AgendaEntity clone() {
        try {
            return (AgendaEntity) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public String toString() {
        return "Ejemplar de AgendaEntity\n" +
                "========================\n" +
                "idagenda=" + idagenda +
                "\nNombre='" + Nombre + '\'' +
                "\nApellido1='" + Apellido1 + '\'' +
                "\nApellido2='" + Apellido2 + '\'' +
                "\nTelefono='" + Telefono + '\'';

    }
}
