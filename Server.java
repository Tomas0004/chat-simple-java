import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servidor del chat.
 *
 * Protocolo (una línea de texto por mensaje):
 *   - La primera línea que manda el cliente es su nombre.
 *   - Después, cada línea es un mensaje.
 *   - El servidor reenvía a todos: "nombre: mensaje".
 */
public class Server extends Application {

    private ServerSocket serverSocket;
    private boolean corriendo = false;

    // Clientes conectados: cada socket con su "salida" para escribirle
    private final Map<Socket, PrintWriter> clientes = new ConcurrentHashMap<>();

    // Elementos de la pantalla
    private TextField campoPuerto;
    private Button boton;
    private TextArea log;

    public static void main(String[] args) {
        launch(args);
    }

    // ---------------------------------------------------------------
    // Pantalla
    // ---------------------------------------------------------------
    @Override
    public void start(Stage stage) {
        campoPuerto = new TextField("5555");
        campoPuerto.setPrefWidth(80);

        boton = new Button("Iniciar servidor");
        boton.setOnAction(e -> {
            if (corriendo) detener();
            else iniciar();
        });

        HBox barra = new HBox(10, new Label("Puerto:"), campoPuerto, boton);

        log = new TextArea();
        log.setEditable(false);

        VBox root = new VBox(10, barra, log);
        root.setPadding(new Insets(10));

        stage.setTitle("Servidor de Chat");
        stage.setScene(new Scene(root, 500, 400));
        stage.setOnCloseRequest(e -> detener());
        stage.show();
    }

    // ---------------------------------------------------------------
    // Iniciar y detener el servidor
    // ---------------------------------------------------------------
    private void iniciar() {
        try {
            int puerto = Integer.parseInt(campoPuerto.getText().trim());
            serverSocket = new ServerSocket(puerto);
        } catch (NumberFormatException e) {
            mostrar("Puerto inválido.");
            return;
        } catch (IOException e) {
            mostrar("No se pudo iniciar: " + e.getMessage());
            return;
        }

        corriendo = true;
        boton.setText("Detener servidor");
        campoPuerto.setDisable(true);
        mostrar("Servidor iniciado en el puerto " + serverSocket.getLocalPort());

        // Hilo que espera nuevos clientes
        Thread aceptador = new Thread(() -> {
            while (corriendo) {
                try {
                    Socket socket = serverSocket.accept();   // se queda esperando aquí
                    Thread hilo = new Thread(() -> atender(socket));
                    hilo.setDaemon(true);
                    hilo.start();
                } catch (IOException e) {
                    // al detener el servidor, accept() lanza error: es normal
                }
            }
        });
        aceptador.setDaemon(true);
        aceptador.start();
    }

    private void detener() {
        if (!corriendo) return;
        corriendo = false;

        try {
            serverSocket.close();
            for (Socket s : clientes.keySet()) s.close();
        } catch (IOException e) {
            // no importa
        }
        clientes.clear();

        boton.setText("Iniciar servidor");
        campoPuerto.setDisable(false);
        mostrar("Servidor detenido.");
    }

    // ---------------------------------------------------------------
    // Atender a un cliente (corre en su propio hilo)
    // ---------------------------------------------------------------
    private void atender(Socket socket) {
        String nombre = null;
        try {
            BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter salida = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            // 1) La primera línea es el nombre
            nombre = entrada.readLine();
            if (nombre == null || nombre.isBlank()) return;

            clientes.put(socket, salida);
            difundir(">> " + nombre + " se unió al chat");

            // 2) Las demás líneas son mensajes
            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                if (!mensaje.isBlank()) {
                    difundir(nombre + ": " + mensaje);
                }
            }
        } catch (IOException e) {
            // el cliente se desconectó
        } finally {
            boolean estabaRegistrado = clientes.remove(socket) != null;
            try {
                socket.close();
            } catch (IOException e) {
                // no importa
            }
            if (corriendo && estabaRegistrado) {
                difundir(">> " + nombre + " salió del chat");
            }
        }
    }

    // ---------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------

    /** Muestra el texto en el log y se lo manda a todos los clientes. */
    private synchronized void difundir(String texto) {
        mostrar(texto);
        for (PrintWriter salida : clientes.values()) {
            salida.println(texto);
        }
    }

    /** Escribe en el log (se puede llamar desde cualquier hilo). */
    private void mostrar(String texto) {
        Platform.runLater(() -> log.appendText(texto + "\n"));
    }
}