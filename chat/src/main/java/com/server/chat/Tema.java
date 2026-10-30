package com.server.chat;

import javafx.scene.Scene;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tema visual compartido por el servidor y el cliente.
 * Guarda el CSS en un archivo temporal para no depender de rutas ni del classpath.
 */
public final class Tema {

    private Tema() {
    }

    private static final String CSS = """
            .root {
                -fx-background-color: #0b0d17;
                -fx-font-family: "Segoe UI", "Helvetica Neue", Arial, sans-serif;
                -fx-font-size: 13px;
            }

            /* ---------- Encabezado ---------- */
            .header {
                -fx-background-color: #11142a;
                -fx-padding: 16 20;
                -fx-border-color: transparent transparent rgba(34,211,238,0.35) transparent;
                -fx-border-width: 0 0 1 0;
            }
            .title {
                -fx-text-fill: white;
                -fx-font-size: 20px;
                -fx-font-weight: bold;
            }
            .subtitle {
                -fx-text-fill: #8b93b8;
                -fx-font-size: 12px;
            }
            .badge {
                -fx-background-color: rgba(139,92,246,0.18);
                -fx-text-fill: #c4b5fd;
                -fx-background-radius: 20;
                -fx-padding: 4 12;
                -fx-font-weight: bold;
            }

            /* ---------- Barras ---------- */
            .conn-bar {
                -fx-background-color: #0e1122;
                -fx-padding: 12 20;
            }
            .input-bar {
                -fx-background-color: #11142a;
                -fx-padding: 14 20;
                -fx-border-color: rgba(139,92,246,0.25) transparent transparent transparent;
                -fx-border-width: 1 0 0 0;
            }
            .label {
                -fx-text-fill: #aab1d6;
            }

            /* ---------- Campos de texto ---------- */
            .text-field {
                -fx-background-color: #151a33;
                -fx-text-fill: #e8ebff;
                -fx-prompt-text-fill: #5d6591;
                -fx-background-radius: 10;
                -fx-border-color: #262c52;
                -fx-border-radius: 10;
                -fx-padding: 8 12;
                -fx-highlight-fill: #8b5cf6;
            }
            .text-field:focused {
                -fx-border-color: #22d3ee;
            }
            .text-field:disabled {
                -fx-opacity: 0.5;
            }

            /* ---------- Botones ---------- */
            .button {
                -fx-background-color: linear-gradient(to right, #22d3ee, #8b5cf6);
                -fx-text-fill: #0b0d17;
                -fx-font-weight: bold;
                -fx-background-radius: 10;
                -fx-padding: 8 20;
                -fx-cursor: hand;
            }
            .button:hover {
                -fx-effect: dropshadow(gaussian, rgba(34,211,238,0.55), 14, 0.2, 0, 0);
            }
            .button:pressed {
                -fx-opacity: 0.85;
            }
            .button:disabled {
                -fx-opacity: 0.35;
            }
            .button.danger {
                -fx-background-color: transparent;
                -fx-border-color: #f43f5e;
                -fx-border-radius: 10;
                -fx-text-fill: #fb7185;
            }
            .button.danger:hover {
                -fx-background-color: rgba(244,63,94,0.15);
                -fx-effect: dropshadow(gaussian, rgba(244,63,94,0.45), 12, 0.2, 0, 0);
            }

            /* ---------- Registro del servidor ---------- */
            .text-area {
                -fx-background-color: #262c52, #0f1226;
                -fx-background-insets: 0, 1;
                -fx-background-radius: 12, 11;
                -fx-text-fill: #cfd5ff;
                -fx-font-family: "Consolas", "Menlo", "DejaVu Sans Mono", monospace;
                -fx-font-size: 13px;
                -fx-highlight-fill: #8b5cf6;
            }
            .text-area .content {
                -fx-background-color: #0f1226;
                -fx-background-radius: 11;
            }
            .text-area .viewport {
                -fx-background-color: #0f1226;
            }

            /* ---------- Chat (cliente) ---------- */
            .chat-scroll {
                -fx-background-color: transparent;
                -fx-background: transparent;
                -fx-padding: 0;
            }
            .chat-scroll > .viewport {
                -fx-background-color: #0b0d17;
            }
            .chat-box {
                -fx-background-color: #0b0d17;
                -fx-padding: 28 20 16 20;
            }

            /* ---------- Chip flotante de denuncia ---------- */
            .report-chip {
                -fx-background-color: #1a1024;
                -fx-background-radius: 14;
                -fx-border-color: rgba(244,63,94,0.55);
                -fx-border-radius: 14;
                -fx-padding: 3 6;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.65), 10, 0.2, 0, 2);
            }
            .button.chip-report {
                -fx-background-color: #f43f5e;
                -fx-text-fill: white;
                -fx-font-size: 11px;
                -fx-padding: 3 10;
                -fx-background-radius: 10;
            }
            .button.chip-report:hover {
                -fx-effect: dropshadow(gaussian, rgba(244,63,94,0.6), 10, 0.2, 0, 0);
            }
            .button.chip-no {
                -fx-background-color: transparent;
                -fx-text-fill: #aab1d6;
                -fx-font-size: 11px;
                -fx-padding: 3 8;
            }
            .button.chip-no:hover {
                -fx-text-fill: white;
                -fx-effect: null;
            }
            .report-done {
                -fx-text-fill: #fb7185;
                -fx-font-size: 11px;
                -fx-font-weight: bold;
                -fx-padding: 3 8;
            }
            .bubble-own {
                -fx-background-color: linear-gradient(to bottom right, #7c3aed, #4f46e5);
                -fx-background-radius: 16 16 4 16;
                -fx-padding: 8 12;
                -fx-effect: dropshadow(gaussian, rgba(124,58,237,0.35), 10, 0.1, 0, 2);
            }
            .bubble-other {
                -fx-background-color: #1a1f3d;
                -fx-background-radius: 16 16 16 4;
                -fx-border-color: #2a3160;
                -fx-border-radius: 16 16 16 4;
                -fx-padding: 8 12;
            }
            .msg-author {
                -fx-text-fill: #22d3ee;
                -fx-font-size: 11px;
                -fx-font-weight: bold;
            }
            .msg-text {
                -fx-text-fill: #e1e5ff;
                -fx-font-size: 14px;
            }
            .msg-text-own {
                -fx-text-fill: white;
                -fx-font-size: 14px;
            }
            .msg-time {
                -fx-text-fill: rgba(255,255,255,0.45);
                -fx-font-size: 10px;
            }
            .system-msg {
                -fx-text-fill: #7a82ab;
                -fx-font-size: 12px;
                -fx-background-color: rgba(255,255,255,0.04);
                -fx-background-radius: 12;
                -fx-padding: 3 12;
            }

            /* ---------- Barras de desplazamiento ---------- */
            .scroll-bar {
                -fx-background-color: transparent;
            }
            .scroll-bar .thumb {
                -fx-background-color: #262c52;
                -fx-background-radius: 6;
            }
            .scroll-bar .thumb:hover {
                -fx-background-color: #3a4280;
            }
            .scroll-bar .track,
            .scroll-bar .increment-button,
            .scroll-bar .decrement-button,
            .scroll-bar .increment-arrow,
            .scroll-bar .decrement-arrow {
                -fx-background-color: transparent;
                -fx-padding: 0;
            }
            """;

    public static void aplicar(Scene scene) {
        try {
            Path tmp = Files.createTempFile("chat-tema", ".css");
            Files.writeString(tmp, CSS);
            tmp.toFile().deleteOnExit();
            scene.getStylesheets().add(tmp.toUri().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}