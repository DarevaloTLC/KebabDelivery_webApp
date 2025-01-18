package models;

import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;

@Entity
public class Pedido extends Model {

    public String title;
    public Date postedAt;

    @Lob
    public String content;

    @ManyToOne
    public User author;


    @OneToMany(mappedBy="pedido", cascade=CascadeType.ALL)
    public List<Comment> comments;

    public Pedido(User author, String title, String content) {
        this.comments = new ArrayList<Comment>();
        this.author = author;
        this.title = title;
        this.content = content;
        this.postedAt = new Date();
    }

    public Pedido() {

    }

    public Pedido addComment(String author, String content) {
        Comment newComment = new Comment(this, author, content).save();
        this.comments.add(newComment);
        this.save();
        return this;
    }
}