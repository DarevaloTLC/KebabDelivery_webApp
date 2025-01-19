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

    public static void restaurantPage() {
        List<Restaurante> restaurantes = Restaurante.findAll();
        render(restaurantes);
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

        Logger.info("Intentando login: " + email);

        // Validar las credenciales del usuario
        User user = User.connect(email, password);

        if (user == null) {
            Logger.warn("Credenciales incorrectas para: " + email);
            // Respuesta en texto plano con un mensaje de error
            renderText("Correo o contraseña incorrectos.");
            return;
        }

        Logger.info("Inicio de sesión exitoso para: " + email);
        session.put("userId", user.id);
        session.put("userEmail", user.email);

        if (user.isAdmin) {
            // Redirigir a la página de administración
            renderText("successAdmin");
        } else {
            // Redirigir a la página principal del usuario normal
            renderText("success");
        }
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

    public static void addToCart(Long comidaId) {
        User user = getLoggedUser();
        if (user == null) {
            flash.error("Debes iniciar sesión para agregar productos al carrito.");
            loginPage();
        }

        // Obtener la comida seleccionada
        Comida comida = Comida.findById(comidaId);
        if (comida == null) {
            flash.error("Comida no encontrada.");
            Logger.warn("Comida no encontrada.");
            mainPage(); // Redirigir a la página principal
        }

        // Obtener el restaurante de la comida seleccionada
        Restaurante restaurante = comida.restauranteList.get(0);  // Suponiendo que hay al menos un restaurante asociado a la comida

        // Obtener o crear un pedido (usando el ID almacenado en la sesión)
        String pedidoId = session.get("pedidoId");
        Pedido pedido;
        if (pedidoId != null) {
            pedido = Pedido.findById(Long.parseLong(pedidoId));
            if (pedido == null) {
                pedido = new Pedido(user, 0, "Pedido temporal");
                pedido.save();
                session.put("pedidoId", pedido.id.toString()); // Guardar el ID del pedido en la sesión
            }
        } else {
            pedido = new Pedido(user, 0, "Pedido temporal");
            pedido.save();
            session.put("pedidoId", pedido.id.toString()); // Guardar el ID del pedido en la sesión
        }

        // Agregar la comida al pedido
        pedido.addComida(comida);

        // Actualizar el precio total
        pedido.precio += comida.precio;

        // Asociar el restaurante al pedido si no está asociado
        if (pedido.restaurante == null) {
            pedido.restaurante = restaurante;
        }

        // Asegurarse de que el restaurante actualice su lista de pedidos
        if (!restaurante.listaPedidosTienda.contains(pedido)) {
            restaurante.listaPedidosTienda.add(pedido);
            restaurante.save();
        }

        // Guardar cambios en el pedido
        pedido.save();

        flash.success("Comida añadida al carrito.");
        menuPage(restaurante.id); // Redirigir al menú del restaurante
    }



    public static void carrito() {
        User user = getLoggedUser();
        if (user == null) {
            flash.error("Debes iniciar sesión para ver tu carrito.");
            loginPage();
        }

        // Recuperar el ID del pedido de la sesión
        String pedidoId = session.get("pedidoId");
        Logger.info("Pedido ID: "+ pedidoId);
        if (pedidoId == null) {
            flash.error("No hay productos en tu carrito.");
            mainPage(); // Redirigir a la página principal
        }

        // Recuperar el pedido desde la base de datos
        Pedido pedido = Pedido.findById(Long.parseLong(pedidoId));
        if (pedido == null || pedido.comidaList.isEmpty()) {
            flash.error("Tu carrito está vacío.");
            mainPage();
        }

        // Pasar el pedido a la vista
        render(pedido);
    }


    public static void finalizarPedido(String direccion) {
        User user = getLoggedUser();
        if (user == null) {
            flash.error("Debes iniciar sesión para finalizar el pedido.");
            renderText("ERROR: Debes iniciar sesión para finalizar el pedido.");
        }

        String pedidoId = session.get("pedidoId");
        if (pedidoId == null) {
            flash.error("No hay productos en tu carrito.");
            renderText("ERROR: No hay productos en tu carrito.");
        }

        Pedido pedido = Pedido.findById(Long.parseLong(pedidoId));
        if (pedido == null || pedido.comidaList.isEmpty()) {
            flash.error("Tu carrito está vacío.");
            renderText("ERROR: Tu carrito está vacío.");
        }

        if (direccion == null || direccion.trim().isEmpty()) {
            flash.error("Debes proporcionar una dirección de entrega.");
            renderText("ERROR: Debes proporcionar una dirección de entrega.");
        }

        // Actualizar el pedido con la dirección
        pedido.content = direccion;
        pedido.save();

        // Eliminar el pedido de la sesión
        session.remove("pedidoId");

        // Notificar éxito
        flash.success("Pedido realizado con éxito. ¡Gracias por tu compra!");
        renderText("SUCCESS: Pedido realizado con éxito. ¡Gracias por tu compra!");
    }




    public static void removeFromCart(Long comidaId) {
        User user = getLoggedUser();
        if (user == null) {
            flash.error("Debes iniciar sesión para eliminar productos del carrito.");
            loginPage();
        }

        // Recuperar el ID del pedido de la sesión
        String pedidoId = session.get("pedidoId");
        if (pedidoId == null) {
            flash.error("No hay productos en tu carrito.");
            mainPage();
        }

        // Recuperar el pedido desde la base de datos
        Pedido pedido = Pedido.findById(Long.parseLong(pedidoId));
        if (pedido == null || pedido.comidaList.isEmpty()) {
            flash.error("Tu carrito está vacío.");
            mainPage();
        }

        // Buscar la comida que se desea eliminar
        Comida comida = Comida.findById(comidaId);
        if (comida == null || !pedido.comidaList.contains(comida)) {
            flash.error("La comida no está en tu carrito.");
            carrito();  // Redirigir al carrito
        }

        // Eliminar la comida de la lista de comidas del pedido
        pedido.comidaList.remove(comida);

        // También eliminar el pedido de la lista de pedidos de la comida
        comida.pedidoList.remove(pedido);

        // Actualizar el precio total del pedido
        pedido.precio -= comida.precio;

        // Guardar los cambios en la base de datos
        comida.save();
        pedido.save();

        flash.success("Comida eliminada del carrito.");
        carrito();  // Redirigir al carrito
    }

    public static void viewOrdersPage(Long restauranteId) {
        // Obtener el restaurante por su ID
        Restaurante restaurante = Restaurante.findById(restauranteId);

        if (restaurante == null) {
            renderText("Restaurante no encontrado.");
            return;
        }

        // Obtener los pedidos del restaurante, basados en la relación ManyToOne
        List<Pedido> pedidos = restaurante.listaPedidosTienda;

        // Si no hay pedidos asociados al restaurante
        if (pedidos.isEmpty()) {
            renderText("No hay pedidos para este restaurante.");
            return;
        }

        // Pasar los pedidos a la vista
        render(pedidos,restaurante);
    }



}
