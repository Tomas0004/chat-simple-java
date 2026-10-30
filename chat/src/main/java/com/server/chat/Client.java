package com.server.chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Cliente del chat. Se conecta al servidor y muestra los mensajes como burbujas:
 * los propios a la derecha (violeta) y los de otros a la izquierda.
 */
public class Client extends Application {

    private Socket socket;
    private PrintWriter salida;
    private volatile boolean conectado = false;
    private String nombreActual = "";

    private TextField campoHost;
    private TextField campoPuerto;
    private TextField campoNombre;
    private Button botonConectar;
    private TextField campoMensaje;
    private Button botonEnviar;
    private VBox mensajes;
    private ScrollPane scroll;
    private Label estado;
    private Circle indicador;

    private Button botonSaludo1;
    private Button botonSaludo2;
    private Button botonSaludo3;
    private Button botonSaludo4;

    private String SALUDO_1 = "¡Hola a todos!";
    private String SALUDO_2 = "Saludos";
    private String SALUDO_3 = "Hola";
    private String SALUDO_4 = "Todo bien, todo correcto";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // --- Encabezado ---
        Label titulo = new Label("Chat");
        titulo.getStyleClass().add("title");

        indicador = new Circle(5, Color.web("#f43f5e"));
        estado = new Label("Sin conexión");
        estado.getStyleClass().add("subtitle");
        HBox filaEstado = new HBox(8, indicador, estado);
        filaEstado.setAlignment(Pos.CENTER_LEFT);

        VBox textos = new VBox(3, titulo, filaEstado);
        HBox header = new HBox(textos);
        header.getStyleClass().add("header");

        // --- Barra de conexión ---
        campoHost = new TextField("localhost");
        campoHost.setPrefWidth(120);
        campoPuerto = new TextField("5555");
        campoPuerto.setPrefWidth(70);
        campoNombre = new TextField();
        campoNombre.setPromptText("Tu nombre");
        campoNombre.setPrefWidth(120);
        campoNombre.setOnAction(e -> alternarConexion());

        botonConectar = new Button("Conectar");
        botonConectar.setOnAction(e -> alternarConexion());

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox conexion = new HBox(10,
                new Label("Host"), campoHost,
                new Label("Puerto"), campoPuerto,
                campoNombre, espacio, botonConectar);
        conexion.getStyleClass().add("conn-bar");
        conexion.setAlignment(Pos.CENTER_LEFT);

        VBox superior = new VBox(header, conexion);

        // --- Área de mensajes ---
        mensajes = new VBox(10);
        mensajes.getStyleClass().add("chat-box");

        scroll = new ScrollPane(mensajes);
        scroll.getStyleClass().add("chat-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // Siempre baja hasta el último mensaje
        mensajes.heightProperty().addListener((obs, antes, ahora) -> scroll.setVvalue(1.0));

        // --- Barra de envío ---
        campoMensaje = new TextField();
        campoMensaje.setPromptText("Escribe un mensaje...");
        campoMensaje.setOnAction(e -> enviar());
        HBox.setHgrow(campoMensaje, Priority.ALWAYS);

        botonEnviar = new Button("Enviar");
        botonEnviar.setOnAction(e -> enviar());

        botonSaludo1 = new Button(SALUDO_1);
        botonSaludo1.setOnAction(e -> setCampoMensaje(SALUDO_1));

        botonSaludo2 = new Button(SALUDO_2);
        botonSaludo2.setOnAction(e -> setCampoMensaje(SALUDO_2));

        botonSaludo3 = new Button(SALUDO_3);
        botonSaludo3.setOnAction(e -> setCampoMensaje(SALUDO_3));

        botonSaludo4 = new Button(SALUDO_4);
        botonSaludo4.setOnAction(e -> setCampoMensaje(SALUDO_4));

        HBox envio = new HBox(10, campoMensaje, botonEnviar);
        envio.setAlignment(Pos.CENTER);

        HBox saludos = new HBox(21, botonSaludo1, botonSaludo2, botonSaludo3, botonSaludo4);
        saludos.setAlignment(Pos.CENTER_LEFT);

        VBox area_envio = new VBox(10, envio, saludos);
        area_envio.getStyleClass().add("input-bar");

        BorderPane root = new BorderPane();
        root.setTop(superior);
        root.setCenter(scroll);
        root.setBottom(area_envio);

        actualizarEstadoUI(false);

        Scene scene = new Scene(root, 600, 560);
        Tema.aplicar(scene);

        stage.setTitle("Cliente de Chat");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> desconectar());
        stage.show();
    }

    private void alternarConexion() {
        if (conectado) desconectar();
        else conectar();
    }

    private void conectar() {
        String host = campoHost.getText().trim();
        String nombre = campoNombre.getText().trim();
        int puerto;
        try {
            puerto = Integer.parseInt(campoPuerto.getText().trim());
        } catch (NumberFormatException ex) {
            agregarSistema("Puerto inválido.");
            return;
        }
        if (nombre.isEmpty()) {
            agregarSistema("Escribe un nombre antes de conectar.");
            return;
        }

        nombreActual = nombre;
        botonConectar.setDisable(true);
        estado.setText("Conectando...");

        Thread t = new Thread(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, puerto), 5000);
                salida = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                salida.println(nombre); // el servidor espera el nombre primero
                conectado = true;

                Platform.runLater(() -> {
                    botonConectar.setDisable(false);
                    actualizarEstadoUI(true);
                    estado.setText("Conectado a " + host + ":" + puerto + " como " + nombre);
                    campoMensaje.requestFocus();
                });

                try (BufferedReader entrada = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                    String linea;
                    while ((linea = entrada.readLine()) != null) {
                        procesar(linea);
                    }
                }
            } catch (IOException ex) {
                if (!conectado) agregarSistema("No se pudo conectar: " + ex.getMessage());
            } finally {
                boolean estabaConectado = conectado;
                conectado = false;
                Platform.runLater(() -> {
                    botonConectar.setDisable(false);
                    actualizarEstadoUI(false);
                });
                if (estabaConectado) agregarSistema("Desconectado del servidor.");
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void desconectar() {
        conectado = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }

    private void enviar() {
        String texto = campoMensaje.getText().trim();
        if (!conectado || texto.isEmpty()) return;
        salida.println(texto.replace("\n", " "));
        campoMensaje.clear();
    }

    private void setCampoMensaje(String text) {
        campoMensaje.setText(text);
    }

    /** Interpreta una línea del protocolo recibida del servidor. */
    private void procesar(String linea) {
        String[] p = linea.split("\\|", 4);
        if (p[0].equals("MSG") && p.length == 4) {
            agregarMensaje(p[1], p[2], p[3]);
        } else if (p[0].equals("SYS") && linea.length() > 4) {
            agregarSistema(linea.substring(4));
        } else {
            agregarSistema(linea);
        }
    }

    private void agregarMensaje(String hora, String nombre, String contenido) {
        Platform.runLater(() -> {
            boolean propio = nombre.equals(nombreActual);

            Label texto = new Label(contenido);
            texto.setWrapText(true);
            texto.setMaxWidth(380);
            texto.getStyleClass().add(propio ? "msg-text-own" : "msg-text");

            Label horaLabel = new Label(hora);
            horaLabel.getStyleClass().add("msg-time");
            horaLabel.setMaxWidth(Double.MAX_VALUE);
            horaLabel.setAlignment(Pos.CENTER_RIGHT);

            VBox burbuja = new VBox(3);
            if (!propio) {
                Label autor = new Label(nombre);
                autor.getStyleClass().add("msg-author");
                burbuja.getChildren().add(autor);
            }
            burbuja.getChildren().addAll(texto, horaLabel);
            burbuja.getStyleClass().add(propio ? "bubble-own" : "bubble-other");

            HBox fila = new HBox(burbuja);
            fila.setAlignment(propio ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            mensajes.getChildren().add(fila);
        });
    }

    private void agregarSistema(String texto) {
        Platform.runLater(() -> {
            Label aviso = new Label(texto);
            aviso.getStyleClass().add("system-msg");
            HBox fila = new HBox(aviso);
            fila.setAlignment(Pos.CENTER);
            mensajes.getChildren().add(fila);
        });
    }

    private void actualizarEstadoUI(boolean conectadoAhora) {
        botonConectar.setText(conectadoAhora ? "Desconectar" : "Conectar");
        if (conectadoAhora) {
            if (!botonConectar.getStyleClass().contains("danger"))
                botonConectar.getStyleClass().add("danger");
            indicador.setFill(Color.web("#22c55e"));
        } else {
            botonConectar.getStyleClass().remove("danger");
            indicador.setFill(Color.web("#f43f5e"));
            estado.setText("Sin conexión");
        }
        campoHost.setDisable(conectadoAhora);
        campoPuerto.setDisable(conectadoAhora);
        campoNombre.setDisable(conectadoAhora);
        campoMensaje.setDisable(!conectadoAhora);
        botonEnviar.setDisable(!conectadoAhora);
        botonSaludo1.setDisable(!conectadoAhora);
        botonSaludo2.setDisable(!conectadoAhora);
        botonSaludo3.setDisable(!conectadoAhora);
        botonSaludo4.setDisable(!conectadoAhora);
    }
}