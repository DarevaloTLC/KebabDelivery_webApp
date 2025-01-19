package models;
import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;

@Entity
public class Restaurante extends Model{
    public String name;
    public String address;
    public String phone;

    @OneToMany(mappedBy = "restaurante")
    public List<Pedido> listaPedidosTienda;

    @ManyToMany
    public List<Comida> comidaList;

    public Restaurante(String name, String address, String phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.listaPedidosTienda = new ArrayList<>();
        this.comidaList = new ArrayList<>();
    }
    public Restaurante() {}
}
