package controllers;

import java.util.*;

import play.*;
import play.mvc.*;
import javax.persistence.*;
import models.*;

public class Application extends Controller {

    // Página principal
    public static void index() {
        // Verificar si hay un usuario autenticado
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

    public static void register(String email, String password, String fullname) {
        // Validar si el correo ya está registrado
        User existingUser = User.find("byEmail", email).first();
        if (existingUser != null) {
            flash.error("El correo ya está registrado.");
            registerPage();
        }

        // Crear y guardar el usuario
        new User(email, password, fullname).save();

        flash.success("Te has registrado exitosamente. Ahora inicia sesión.");
        loginPage();
    }

    // Autenticar usuario e iniciar sesión
    public static void login(String email, String password) {
        // Validar credenciales
        User user = User.connect(email, password);
        if (user == null) {
            flash.error("Correo o contraseña incorrectos.");
            loginPage();
        }

        // Guardar al usuario en la sesión
        session.put("userId", user.id);
        session.put("userEmail", user.email);

        flash.success("Bienvenido, " + user.fullname + "!");
        index(); // Redirigir a la página principal o dashboard
    }

    // Cerrar sesión
    public static void logout() {
        session.clear();
        flash.success("Has cerrado sesión exitosamente.");
        index(); // Redirigir a la página principal
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
