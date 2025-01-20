import play.*;
import play.jobs.*;
import play.test.*;

import models.*;

@OnApplicationStart
public class Bootstrap extends Job {

    public void doJob() {
        if(User.count() == 0){
            User adminUser = new User("admin@admin.com", "admin", "admin");
            adminUser.isAdmin = true;
            User user1 = new User("user1@user.com", "user1", "user1");

            adminUser.save();
            user1.save();
        }

        if((Restaurante.count() == 0) & Comida.count() == 0){
            Restaurante restaurante1 = new Restaurante("Kebab Castelldefels", "c/KebabFavorito", "123 456 789");
            Restaurante restaurante2 = new Restaurante("Kebab Barcelona", "c/KebabDeBarcelona", "987 654 321");
            restaurante1.save();
            restaurante2.save();
            Comida comida1 = new Comida("Kebab Mixto", 1, 5.50);
            Comida comida2 = new Comida("Kebab Falafel", 1, 3.50);
            Comida comida3 = new Comida("Menú Kebab Familiar", 3, 14.50);
            comida1.save();
            comida2.save();
            comida3.save();

            restaurante1.comidaList.add(comida1);
            comida1.restauranteList.add(restaurante1);
            restaurante1.comidaList.add(comida2);
            comida2.restauranteList.add(restaurante1);

            restaurante2.comidaList.add(comida1);
            restaurante2.comidaList.add(comida2);
            restaurante2.comidaList.add(comida3);
            comida1.restauranteList.add(restaurante2);
            comida2.restauranteList.add(restaurante2);
            comida3.restauranteList.add(restaurante2);

            restaurante1.save();
            restaurante2.save();
            comida1.save();
            comida2.save();
            comida3.save();
        }


    }

}