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
 * Clase para realizar scraping de partidos judiciales y sus municipios desde el
 * sitio web del CGPJ. Procesa la información y genera un archivo SQL con los
 * datos obtenidos.
 */
public class PJScraper {

    private static final String OUTPUT_FILE = "partidos_judiciales.sql";
    private static final String BASE_URL = "https://www.poderjudicial.es/cgpj/es/Servicios/Directorio/ch.Directorio-de-Organos-Judiciales.formato3/?provincia=";
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
     * Obtiene todos los partidos judiciales de todas las provincias (ID 1-52).
     *
     * @param resultado Tabla donde se mostrarán los resultados.
     * @param progressReporter Diálogo para mostrar el progreso de la operación.
     */
    public void obtenerTodosPartidos(JTable resultado, ProgressReporter progressReporter) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                long startTime = System.currentTimeMillis();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
                    writer.write("CREATE TABLE IF NOT EXISTS partidos_judiciales (\n" +
                                "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                                "    provincia TEXT,\n" +
                                "    partido_judicial TEXT,\n" +
                                "    municipio TEXT\n" +
                                ");\n\n");

                    disableSSLValidation();

                    for (int provinciaId = 1; provinciaId <= 52 && !progressReporter.isCancelled(); provinciaId++) {
                        String nombreProvincia = PROVINCIAS[provinciaId - 1];
                        progressReporter.setMessage(String.format("Procesando provincia %d/52: %s", provinciaId, nombreProvincia));
                        System.out.printf("\n=== PROCESANDO PROVINCIA %d/52: %s ===\n", provinciaId, nombreProvincia);

                        obtenerPartidosDeProvincia(provinciaId, nombreProvincia, resultado, progressReporter, writer);

                        if (provinciaId < 52 && !progressReporter.isCancelled()) {
                            TimeUnit.SECONDS.sleep(2);
                        }
                    }

                    if (!progressReporter.isCancelled()) {
                        long tiempoTotal = (System.currentTimeMillis() - startTime) / 1000;
                        progressReporter.setMessage("¡Completado! Tiempo: " + tiempoTotal + " segundos");
                        System.out.println("\nSCRAPING COMPLETADO - Tiempo total: " + tiempoTotal + " segundos");
                    }
                } catch (Exception e) {
                    System.err.println("ERROR en el proceso general: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "Error general: " + e.getMessage());
                    });
                }
                return null;
            }

            @Override
            protected void done() {
            }
        };
        worker.execute();
    }

    /**
     * Obtiene todos los juzgados de una provincia específica, procesando todas las páginas.
     */
    private void obtenerPartidosDeProvincia(int provinciaId, String nombreProvincia, JTable resultado, ProgressReporter progressReporter, BufferedWriter writer) {
        try {
            String provinciaIdStr = String.format("%02d", provinciaId);
            String provinciaUrl = BASE_URL + provinciaIdStr;
            System.out.println("URL de provincia: " + provinciaUrl);

            progressReporter.setMessage("Conectando a provincia: " + nombreProvincia);
            Document doc = Jsoup.connect(provinciaUrl)
                    .timeout(30000)
                    .get();
            System.out.println("Conexión establecida para provincia ID: " + provinciaId);

            String pageTotalStr = doc.select("input[name=pageTotal]").attr("value");
            int totalPages = pageTotalStr.isEmpty() ? 1 : Integer.parseInt(pageTotalStr);
            System.out.println("Total páginas: " + totalPages);

            SwingUtilities.invokeLater(() -> {
                DefaultTableModel modelo = (DefaultTableModel) resultado.getModel();
                if (modelo.getColumnCount() == 0) {
                    modelo.setColumnIdentifiers(new Object[]{"Provincia", "Partido Judicial", "Municipios"});
                }
            });

            writer.write("\n-- PROVINCIA: " + nombreProvincia.toUpperCase() + "\n");

            for (int page = 0; page < totalPages && !progressReporter.isCancelled(); page++) {
                int startAt = page * 10;
                int pag = page + 1;
                String pageUrl = provinciaUrl + "&vgnextlocale=es&startAt=" + startAt + "&pag01=" + pag;

                System.out.println("Procesando página " + pag + "/" + totalPages);
                progressReporter.setMessage(String.format("%s - Página %d/%d", nombreProvincia, pag, totalPages));

                Document pageDoc;
                if (page == 0) {
                    pageDoc = doc;
                } else {
                    pageDoc = Jsoup.connect(pageUrl).timeout(30000).get();
                    TimeUnit.SECONDS.sleep(1);
                }

                Elements filas = pageDoc.select("tbody > tr");
                System.out.println("Filas encontradas: " + filas.size());

                for (Element fila : filas) {
                    if (progressReporter.isCancelled()) break;

                    String municipio = fila.select("th[data-cabecera=Municipio] span").text();
                    String juzgado = fila.select("td[data-cabecera=Juzgado] span a").text();

                    if (!municipio.isEmpty() && !juzgado.isEmpty()) {
                        final String finalMunicipio = municipio;
                        final String finalJuzgado = juzgado;

                        SwingUtilities.invokeLater(() -> {
                            DefaultTableModel modelo = (DefaultTableModel) resultado.getModel();
                            modelo.addRow(new Object[]{nombreProvincia, finalJuzgado, finalMunicipio});
                            int lastRow = resultado.getRowCount() - 1;
                            resultado.scrollRectToVisible(resultado.getCellRect(lastRow, 0, true));
                        });

                        String insertSQL = String.format(
                                "INSERT INTO partidos_judiciales (provincia, partido_judicial, municipio) VALUES ('%s', '%s', '%s');\n",
                                nombreProvincia.replace("'", "''"),
                                juzgado.replace("'", "''"),
                                municipio.replace("'", "''")
                        );
                        writer.write(insertSQL);
                        System.out.println("Juzgado: " + juzgado + " | Municipio: " + municipio);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR en provincia ID " + provinciaId + ": " + e.getMessage());
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "Error en provincia ID " + provinciaId + ": " + e.getMessage());
            });
        }
    }

    /**
     * Deshabilita la validación SSL para conexiones con certificados no confiables.
     */
    private static void disableSSLValidation() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
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
