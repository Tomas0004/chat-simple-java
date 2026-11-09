package com.server.chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Cliente del chat.
 * Se conecta al servidor, manda primero su nombre y luego sus mensajes.
 * Todo lo que llega del servidor se muestra en el área de chat.
 */
public class Client extends Application {

    private Socket socket;
    private PrintWriter salida;
    private boolean conectado = false;

    // Saludos rápidos: al pulsarlos se escriben en el campo de mensaje
    private final String[] saludos = {
            "¡Hola a todos!", "Saludos", "Hola", "Todo bien, todo correcto"
    };

    // Elementos de la pantalla
    private TextField campoHost;
    private TextField campoPuerto;
    private TextField campoNombre;
    private Button botonConectar;
    private TextArea chat;
    private TextField campoMensaje;
    private Button botonEnviar;
    private HBox filaSaludos;

    public static void main(String[] args) {
        launch(args);
    }

    // ---------------------------------------------------------------
    // Pantalla
    // ---------------------------------------------------------------
    @Override
    public void start(Stage stage) {
        // Barra de conexión
        campoHost = new TextField("localhost");
        campoHost.setPrefWidth(110);
        campoPuerto = new TextField("5555");
        campoPuerto.setPrefWidth(60);
        campoNombre = new TextField();
        campoNombre.setPromptText("Tu nombre");
        campoNombre.setPrefWidth(110);

        botonConectar = new Button("Conectar");
        botonConectar.setOnAction(e -> {
            if (conectado) desconectar();
            else conectar();
        });

        HBox barraConexion = new HBox(10,
                new Label("Host:"), campoHost,
                new Label("Puerto:"), campoPuerto,
                campoNombre, botonConectar);

        // Área donde se ve la conversación
        chat = new TextArea();
        chat.setEditable(false);
        chat.setWrapText(true);
        VBox.setVgrow(chat, Priority.ALWAYS);

        // Barra para escribir y enviar
        campoMensaje = new TextField();
        campoMensaje.setPromptText("Escribe un mensaje...");
        campoMensaje.setOnAction(e -> enviar());
        HBox.setHgrow(campoMensaje, Priority.ALWAYS);

        botonEnviar = new Button("Enviar");
        botonEnviar.setOnAction(e -> enviar());

        HBox barraEnvio = new HBox(10, campoMensaje, botonEnviar);

        // Botones de saludos rápidos (se crean con un ciclo)
        filaSaludos = new HBox(10);
        for (String saludo : saludos) {
            Button b = new Button(saludo);
            b.setOnAction(e -> campoMensaje.setText(saludo));
            filaSaludos.getChildren().add(b);
        }

        VBox root = new VBox(10, barraConexion, chat, barraEnvio, filaSaludos);
        root.setPadding(new Insets(10));

        cambiarEstado(false);

        stage.setTitle("Cliente de Chat");
        stage.setScene(new Scene(root, 600, 500));
        stage.setOnCloseRequest(e -> desconectar());
        stage.show();
    }

    // ---------------------------------------------------------------
    // Conexión
    // ---------------------------------------------------------------
    private void conectar() {
        String host = campoHost.getText().trim();
        String nombre = campoNombre.getText().trim();
        int puerto;

        try {
            puerto = Integer.parseInt(campoPuerto.getText().trim());
        } catch (NumberFormatException e) {
            mostrar("Puerto inválido.");
            return;
        }
        if (nombre.isEmpty()) {
            mostrar("Escribe un nombre antes de conectar.");
            return;
        }

        // Conectamos en otro hilo para no congelar la pantalla
        Thread hilo = new Thread(() -> {
            try {
                socket = new Socket(host, puerto);
                salida = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader entrada = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                salida.println(nombre);   // el servidor espera el nombre primero
                conectado = true;
                Platform.runLater(() -> cambiarEstado(true));

                // Leer mensajes del servidor hasta que se cierre la conexión
                String linea;
                while ((linea = entrada.readLine()) != null) {
                    mostrar(linea);
                }
            } catch (IOException e) {
                if (conectado) mostrar("Error: " + e.getMessage());
                else if (socket == null || !socket.isConnected()) mostrar("No se pudo conectar: " + e.getMessage());
            } finally {
                conectado = false;
                Platform.runLater(() -> cambiarEstado(false));
                mostrar("Desconectado.");
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    private void desconectar() {
        conectado = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            // no importa
        }
    }

    private void enviar() {
        String texto = campoMensaje.getText().trim();
        if (!conectado || texto.isEmpty()) return;

        salida.println(texto);
        campoMensaje.clear();
    }

    // ---------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------

    /** Agrega una línea al chat (se puede llamar desde cualquier hilo). */
    private void mostrar(String texto) {
        Platform.runLater(() -> chat.appendText(texto + "\n"));
    }

    /** Activa o desactiva los controles según estemos conectados o no. */
    private void cambiarEstado(boolean conectadoAhora) {
        botonConectar.setText(conectadoAhora ? "Desconectar" : "Conectar");
        campoHost.setDisable(conectadoAhora);
        campoPuerto.setDisable(conectadoAhora);
        campoNombre.setDisable(conectadoAhora);
        campoMensaje.setDisable(!conectadoAhora);
        botonEnviar.setDisable(!conectadoAhora);
        filaSaludos.setDisable(!conectadoAhora);
    }
}