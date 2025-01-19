package controllers;

import play.*;
import play.mvc.*;
import javax.persistence.*;
import models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Application extends Controller {

    @Before
    public static void initDB() {
        // Verificar si el usuario admin ya existe
        User admin = User.find("byEmail", "admin@admin.com").first();
        if (admin == null) {
            admin = new User("admin@admin.com", "admin", "admin");
            admin.isAdmin = true;  // Establecer como admin
            admin.save();
            Logger.info("Usuario admin creado: " + admin.email);
        }

        // Verificar si el restaurante "KebabCastelldefels" ya existe
        Restaurante restaurante = Restaurante.find("byName", "KebabCastefaMustafa").first();
        if (restaurante == null) {
            restaurante = new Restaurante("KebabCastefaMustafa", "CarrerMustafa", "666 666 666");
            restaurante.save();
            Logger.info("Restaurante creado: " + restaurante.name);
        }

        // Verificar si la comida "Kebab Mixto" ya existe
        Comida comida = Comida.find("byName", "KebabMixto").first();
        if (comida == null) {
            comida = new Comida("KebabMixto", 1, 5.50);
            comida.save();
            Logger.info("Comida creada: " + comida.name);
        }

        // Asociar la comida "Kebab Mixto" al restaurante "KebabCastelldefels"
        if (!restaurante.comidaList.contains(comida)) {
            restaurante.comidaList.add(comida);
            comida.restauranteList.add(restaurante);
            restaurante.save();
            comida.save();
            Logger.info("Comida asociada al restaurante: " + comida.name + " -> " + restaurante.name);
        } else {
            Logger.info("La comida ya está asociada al restaurante.");
        }
    }



    // Página principal
    public static void index() {
        User user = getLoggedUser();
        if (user != null) {
            render(user);
        } else {
            render();
        }
    }

    // Iniciar sesión
    public static void loginPage() {
        render();
    }

    // Registrar un nuevo usuario
    public static void registerPage() {
        render();
    }

    public static void mainPage() {
        List<Restaurante> restaurantes = Restaurante.findAll();
        if (restaurantes == null || restaurantes.isEmpty()) {
            renderText("No hay restaurantes disponibles.");
        } else {
            render(restaurantes); // Enviar lista de restaurantes a la vista.
        }
    }

    public static void menuPage(Long restauranteId) {
        Restaurante restaurante = Restaurante.find("byId", restauranteId).first();
        List<Comida> comidas = restaurante.comidaList;
        render(restaurante, comidas);
    }
    private static List<Comida> getComidasByRestauranteId(Long restauranteId) {
        Restaurante restaurante = Restaurante.findById(restauranteId);

        // Si no se encuentra el restaurante o su lista de comidas es null, retornar lista vacía
        if (restaurante == null || restaurante.comidaList == null) {
            return new ArrayList<>();
        }

        // Filtrar la lista para eliminar comidas nulas
        List<Comida> comidas = restaurante.comidaList;
        comidas.removeIf(Objects::isNull);

        // Retornar la lista filtrada
        return comidas;
    }









    public static void register(String email, String password, String fullname) {
        Logger.info("Intentando registrar usuario con correo: " + email);

        User existingUser = User.find("byEmail", email).first();
        if (existingUser != null) {
            Logger.warn("Correo ya registrado: " + email);
            renderText("El correo ya está registrado.");
        } else {
            new User(email, password, fullname).save();
            Logger.info("Usuario registrado exitosamente: " + email);
            flash.success("Te has registrado exitosamente. Ahora inicia sesión.");
            loginPage(); // Redirige a la página de inicio de sesión
        }
    }


    // Autenticar usuario e iniciar sesión
    public static void login() {
        String email = request.params.get("email");
        String password = request.params.get("password");
        User user = User.connect(email, password);
        Logger.info("Intentando login: " + email);
        if (user == null) {
            Logger.warn("Usuario no encontrado");
            renderText("Correo o contraseña incorrectos.");
        }
        Logger.info("Login exitosamente: " + email);
        session.put("userId", user.id);
        session.put("userEmail", user.email);
        renderText("success");

    }

    // Cerrar sesión
    public static void logout() {
        Logger.info("Usuario desconectado");
        session.clear();
        flash.success("Has cerrado sesión exitosamente.");
        index();
    }

    // Obtener el usuario autenticado
    private static User getLoggedUser() {
        String userId = session.get("userId");
        if (userId != null) {
            return User.findById(Long.parseLong(userId));
        }
        return null;
    }


}
