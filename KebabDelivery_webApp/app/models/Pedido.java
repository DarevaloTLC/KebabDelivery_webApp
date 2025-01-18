package models;

import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;

@Entity
public class Pedido extends Model {

    public double precio;
    public Date postedAt;

    @Lob
    public String content;

    @ManyToOne
    public User author;

    @ManyToOne
    public Restaurante restaurante;

    @ManyToMany
    public List<Comida> comidaList;


    public Pedido(User author, double precio, String content) {
        this.comidaList = new ArrayList<Comida>();
        this.author = author;
        this.precio = precio;
        this.content = content;
        this.postedAt = new Date();
    }

    public Pedido() {}


    // Método para agregar comida al pedido
    public void addComida(Comida comida) {
        if (comida != null) {
            // Agregar la comida a la lista del pedido
            this.comidaList.add(comida);
        }
    }

}
