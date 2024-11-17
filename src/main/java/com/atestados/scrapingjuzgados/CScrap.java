
package com.atestados.scrapingjuzgados;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.*;

/**
 * Clase CScrap para realizar scraping de datos de una página web específica
 * sobre juzgados y generar un archivo SQL con la información recopilada.
 * Además, presenta los resultados en una tabla de interfaz gráfica.
 */
public class CScrap {

    private static String URL_DIRECTORIO = "https://www.poderjudicial.es/cgpj/es/Servicios/Directorio/ch.Directorio-de-Organos-Judiciales.formato3/?provincia=01";
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
     * Realiza el scraping de los datos de una provincia específica y los presenta
     * en una tabla, además de guardarlos en un archivo SQL.
     * 
     * @param url       URL específica de la provincia.
     * @param resultado JTable donde se presentarán los resultados.
     */
    public void ScrapSitioWeb(String url, JTable resultado) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE, true))) { // Modo append
            // Dividir la URL para obtener la provincia
            String[] parts = url.split("=");
            if (parts.length < 2 || parts[1].isEmpty()) {
                throw new IllegalArgumentException("La URL no tiene un formato válido para extraer la provincia.");
            }
            String provincia = parts[1].split("&")[0];

            System.out.println("La dirección donde buscar es: " + url);

            // Deshabilitar validación SSL para evitar problemas con certificados no confiables
            disableSSLValidation();

            // Conectar al sitio y obtener el contenido HTML
            Document doc = Jsoup.connect(url).get();
            System.out.println("Se ha obtenido la conexión");

            // Determinar el número total de páginas
            String pageTotal = doc.select("input[name=pageTotal]").attr("value");
            int totalPages = Integer.parseInt(pageTotal);
            System.out.println("Total de páginas: " + totalPages + " de la provincia " + PROVINCIAS[Integer.parseInt(provincia) - 1]);

            // Configurar modelo para mostrar datos en JTable
            DefaultTableModel modelo = new DefaultTableModel(new Object[]{
                "Municipio", "Juzgado", "Teléfono/s", "Dirección", "Código Postal"
            }, 0);
            resultado.setModel(modelo);

            // Crear cabecera de la tabla SQL
            writer.write("\n\n--   PROVINCIA DE " + PROVINCIAS[Integer.parseInt(provincia) - 1].toUpperCase() + " \n\n"
                    + "CREATE TABLE IF NOT EXISTS SEDES (\n"
                    + "    id_juzgado INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                    + "    municipio TEXT,\n"
                    + "    nombre TEXT,\n"
                    + "    direccion TEXT,\n"
                    + "    telefono TEXT,\n"
                    + "    codigo_postal TEXT\n"
                    + ");\n");

            // Procesar todas las páginas de resultados
            for (int j = 0; j < totalPages; j++) {
                int startAt = j * 10;
                int pag = j + 1;
                String pageUrl = url + "&vgnextlocale=es&startAt=" + startAt + "&pag01=" + pag;
                System.out.println("Página a consultar: " + pageUrl);

                doc = Jsoup.connect(pageUrl).get();
                Elements filas = doc.select("tbody > tr");

                // Procesar cada fila de resultados
                for (Element fila : filas) {
                    String municipio = fila.select("th[data-cabecera=Municipio] span").text();
                    String juzgado = fila.select("td[data-cabecera=Juzgado] span a").text();
                    String telefono = fila.select("td[data-cabecera=Teléfono/s] span").html().replace("<br>", ", ");
                    String direccion = fila.select("td[data-cabecera=Dirección] span").text();
                    String cp = fila.select("td[data-cabecera=Código Postal] span").text();

                    // Agregar datos al JTable
                    modelo.addRow(new Object[]{municipio, juzgado, telefono, direccion, cp});

                    // Escribir comando SQL
                    String insertSQL = String.format(
                            "INSERT INTO sedes (municipio, nombre, direccion, telefono, codigo_postal) VALUES ('%s', '%s', '%s', '%s', '%s');\n",
                            municipio.replace("'", "''"), // Escapar apóstrofos
                            juzgado.replace("'", "''"),
                            telefono.replace("'", "''"),
                            direccion.replace("'", "''"),
                            cp.replace("'", "''")
                    );
                    writer.write(insertSQL);
                }

                // Pausar para evitar sobrecarga del servidor
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Se ha producido un error con la conexión: " + e.getMessage());
            System.out.println("Ha habido un problema con la conexión: " + e.getMessage());
        }
    }

    /**
     * Deshabilita la validación SSL para permitir conexiones con servidores que
     * no tienen certificados SSL válidos.
     * 
     * @throws Exception en caso de error al configurar la validación SSL.
     */
    private static void disableSSLValidation() throws Exception {
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
    }

    /**
     * Realiza el scraping de todas las provincias utilizando la lista de provincias predefinida.
     * 
     * @param resultado JTable donde se presentarán los resultados.
     */
    public void obtenerTodos(JTable resultado) {
        String baseUrl = URL_DIRECTORIO.substring(0, URL_DIRECTORIO.lastIndexOf("=") + 1);

        for (int i = 1; i < 53; i++) {
            String provincia = (i < 10) ? "0" + i : String.valueOf(i);
            String urlCompleta = baseUrl + provincia;
            ScrapSitioWeb(urlCompleta, resultado);
        }
    }
}

