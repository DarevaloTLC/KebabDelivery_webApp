package controllers;

import java.util.*;

import play.*;
import play.mvc.*;

import models.*;

public class Application extends Controller {

    public static void index() {
        Pedido frontPost = Pedido.find("order by postedAt desc").first();
        List<Pedido> olderPosts = Pedido.find(
                "order by postedAt desc"
        ).from(1).fetch(10);
        render(frontPost, olderPosts);
    }
}