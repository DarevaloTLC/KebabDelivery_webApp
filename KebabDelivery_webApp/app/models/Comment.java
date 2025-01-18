package models;

import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;

@Entity
public class Comment extends Model {

    public String author;
    public Date postedAt;

    @Lob
    public String content;

    @ManyToOne
    public Pedido pedido;

    public Comment(Pedido pedido, String author, String content) {
        this.pedido = pedido;
        this.author = author;
        this.content = content;
        this.postedAt = new Date();
    }

}