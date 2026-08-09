package com.atestados.scrapingjuzgados;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.*;

/**
 * Clase para realizar scraping de datos de juzgados desde el sitio web del
 * Poder Judicial. Genera un archivo SQL con la información recopilada y
 * presenta los resultados en una tabla.
 */
public class CScrap {

    private static final String BASE_URL = "https://www.poderjudicial.es/cgpj/es/Servicios/Directorio/ch.Directorio-de-Organos-Judiciales.formato3/?provincia=";
    private static final String OUTPUT_FILE = "juzgados_data.sql";
    private static final String[] PROVINCIAS = {
        "Araba/Álava", "Albacete", "Alicante/Alacant", "Almería", "Ávila",
        "Badajoz", "Balears, Illes", "Barcelona", "Burgos", "Cáceres",
        "Cádiz", "Castellón/Castelló", "Ciudad Real", "Córdoba",
        "Coruña, A", "Cuenca", "Girona", "Granada", "Guadalajara",
        "Gipuzkoa", "Huelva", "Huesca", "Jaén", "León", "Lleida",
        "Rioja, La", "Lugo", "Madrid", "Málaga", "Murcia", "Navarra",
        "Ourense", "Asturias", "Palencia", "Palmas, Las", "Pontevedra",
        "Salamanca", "Santa Cruz de Tenerife", "Cantabria", "Segovia",
        "Sevilla", "Soria", "Tarragona", "Teruel", "Toledo",
        "Valencia/València", "Valladolid", "Bizkaia", "Zamora",
        "Zaragoza", "Ceuta", "Melilla"
    };

    /**
     * Realiza el scraping de todas las provincias.
     *
     * @param resultado Tabla donde se presentarán los resultados.
     * @param progressReporter Diálogo para mostrar el progreso.
     */
    public void obtenerTodos(JTable resultado, ProgressReporter progressReporter) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                long startTime = System.currentTimeMillis();
                System.out.println("INICIANDO SCRAPING DE TODAS LAS PROVINCIAS");

                for (int i = 1; i <= 52 && !progressReporter.isCancelled(); i++) {
                    String provinciaId = String.format("%02d", i);
                    String nombreProvincia = PROVINCIAS[i - 1];
                    String url = BASE_URL + provinciaId;

                    progressReporter.setMessage(String.format("Procesando provincia %d/52: %s", i, nombreProvincia));
                    System.out.printf("\n=== PROCESANDO PROVINCIA %d/52: %s ===\n", i, nombreProvincia);

                    scrapSitioWeb(url, resultado, progressReporter);

                    if (i < 52 && !progressReporter.isCancelled()) {
                        TimeUnit.SECONDS.sleep(2); // Pausa entre provincias
                    }
                }

                if (!progressReporter.isCancelled()) {
                    long tiempoTotal = (System.currentTimeMillis() - startTime) / 1000;
                    progressReporter.setMessage("¡Completado! Tiempo: " + tiempoTotal + " segundos");
                    System.out.println("SCRAPING COMPLETADO - Tiempo total: " + tiempoTotal + " segundos");
                }
                return null;
            }
        };
        worker.execute();
    }

    /**
 * Realiza el scraping de los datos de una provincia específica.
 *
 * @param url URL de la provincia.
 * @param resultado Tabla donde se presentarán los resultados.
 * @param progressReporter Diálogo para mostrar el progreso.
 */
void scrapSitioWeb(String url, JTable resultado, ProgressReporter progressReporter) {
    String provinciaId = "desconocida"; // Valor por defecto para el caso de error
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE, true))) {
        String finalProvinciaId = url.split("=")[1].split("&")[0]; // Usar una variable final
        String nombreProvincia = PROVINCIAS[Integer.parseInt(finalProvinciaId) - 1];
        System.out.println("URL: " + url);

        progressReporter.setMessage("Conectando a: " + nombreProvincia);
        disableSSLValidation();
        Document doc = Jsoup.connect(url).get();

        String pageTotal = doc.select("input[name=pageTotal]").attr("value");
        int totalPages = Integer.parseInt(pageTotal);
        System.out.println("Total páginas: " + totalPages);

        progressReporter.setMessage(String.format("Procesando %s (%d páginas)", nombreProvincia, totalPages));

        // Inicializar modelo de tabla
        SwingUtilities.invokeLater(() -> {
            DefaultTableModel modelo = new DefaultTableModel(new Object[]{
                "Municipio", "Juzgado", "Teléfono/s", "Dirección", "Código Postal"
            }, 0);
            resultado.setModel(modelo);
        });

        // Escribir encabezado SQL
        writer.write("\n-- PROVINCIA: " + nombreProvincia.toUpperCase() + "\n");
        writer.write("CREATE TABLE IF NOT EXISTS sedes (\n" +
                    "    id_juzgado INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    municipio TEXT,\n" +
                    "    nombre TEXT,\n" +
                    "    direccion TEXT,\n" +
                    "    telefono TEXT,\n" +
                    "    codigo_postal TEXT\n" +
                    ");\n");

        for (int j = 0; j < totalPages && !progressReporter.isCancelled(); j++) {
            int startAt = j * 10;
            int pag = j + 1;
            String pageUrl = url + "&vgnextlocale=es&startAt=" + startAt + "&pag01=" + pag;
            System.out.println("Procesando página " + pag + "/" + totalPages);

            progressReporter.setMessage(String.format("Página %d/%d - %s", pag, totalPages, nombreProvincia));
            doc = Jsoup.connect(pageUrl).get();
            Elements filas = doc.select("tbody > tr");

            for (Element fila : filas) {
                if (progressReporter.isCancelled()) break;

                String municipio = fila.select("th[data-cabecera=Municipio] span").text();
                String juzgado = fila.select("td[data-cabecera=Juzgado] span a").text();
                String telefono = fila.select("td[data-cabecera=Teléfono/s] span").html().replace("<br>", ", ");
                String direccion = fila.select("td[data-cabecera=Dirección] span").text();
                String cp = fila.select("td[data-cabecera=Código Postal] span").text();

                // Añadir a la tabla
                SwingUtilities.invokeLater(() -> {
                    DefaultTableModel modelo = (DefaultTableModel) resultado.getModel();
                    modelo.addRow(new Object[]{municipio, juzgado, telefono, direccion, cp});
                    int lastRow = resultado.getRowCount() - 1;
                    resultado.scrollRectToVisible(resultado.getCellRect(lastRow, 0, true));
                });

                // Escribir en SQL
                String insertSQL = String.format(
                        "INSERT INTO sedes (municipio, nombre, direccion, telefono, codigo_postal) VALUES ('%s', '%s', '%s', '%s', '%s');\n",
                        municipio.replace("'", "''"),
                        juzgado.replace("'", "''"),
                        telefono.replace("'", "''"),
                        direccion.replace("'", "''"),
                        cp.replace("'", "''")
                );
                writer.write(insertSQL);
                System.out.println("Juzgado procesado: " + juzgado);
            }

            if (j < totalPages - 1 && !progressReporter.isCancelled()) {
                TimeUnit.SECONDS.sleep(1); // Pausa entre páginas
            }
        }

        if (!progressReporter.isCancelled()) {
            progressReporter.setMessage("Completado: " + nombreProvincia);
            System.out.println("Completado procesamiento de " + nombreProvincia);
        }
    } catch (Exception e) {
        final String errorProvinciaId = provinciaId; // Capturar el valor para la lambda
        System.err.println("ERROR en provincia " + errorProvinciaId + ": " + e.getMessage());
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, "Error en provincia " + errorProvinciaId + ": " + e.getMessage());
        });
    }
}

    /**
     * Deshabilita la validación SSL para conexiones con certificados no
     * confiables.
     */
    private static void disableSSLValidation() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            System.err.println("ERROR al deshabilitar SSL: " + e.getMessage());
        }
    }
}
