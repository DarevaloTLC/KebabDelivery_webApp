package models;

import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;

@Entity
public class Comida extends Model {

    public String name;
    public int cantidad;
    public double precio;

    @ManyToMany
    public List<Restaurante> restauranteList;

    @ManyToMany
    public List<Pedido> pedidoList;

    public Comida(String name, int cantidad, double precio) {
        this.name = name;
        this.cantidad = cantidad;
        this.precio = precio;
        this.restauranteList = new ArrayList<>();
        this.pedidoList = new ArrayList<>();
    }
    public Comida() {}

}

