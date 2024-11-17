/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.atestados.scrapingjuzgados;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import java.util.List;

/**
 *
 * @author Oscar
 */
public class SeleniumScraper {
    public void seleniumScraper(){
        // Configurar la ruta del driver (actualiza el path según tu sistema)
        System.setProperty("webdriver.chrome.driver", "/path/to/chromedriver");

        // Iniciar ChromeDriver
        WebDriver driver = new ChromeDriver();

        try {
            // Navegar al sitio web
            driver.get("https://www.poderjudicial.es/cgpj/es/Servicios/Directorio/Directorio_de_Organos_Judiciales");

            // Esperar a que cargue la tabla (puedes usar un WebDriverWait para esperar elementos dinámicamente)
            Thread.sleep(5000); // No recomendado en producción, mejor usar WebDriverWait

            // Obtener filas de la tabla
            List<WebElement> filas = driver.findElements(By.cssSelector("tbody > tr"));

            for (WebElement fila : filas) {
                String municipio = fila.findElement(By.cssSelector("th[data-cabecera='Municipio'] span")).getText();
                String juzgado = fila.findElement(By.cssSelector("td[data-cabecera='Juzgado'] span a")).getText();
                String telefono = fila.findElement(By.cssSelector("td[data-cabecera='Teléfono/s'] span")).getText();
                String direccion = fila.findElement(By.cssSelector("td[data-cabecera='Dirección'] span")).getText();
                String cp = fila.findElement(By.cssSelector("td[data-cabecera='Código Postal'] span")).getText();

                // Imprimir los datos en consola
                System.out.println("Municipio: " + municipio);
                System.out.println("Juzgado: " + juzgado);
                System.out.println("Teléfono/s: " + telefono);
                System.out.println("Dirección: " + direccion);
                System.out.println("Código Postal: " + cp);
                System.out.println("-------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Cerrar el navegador
            driver.quit();
        }
    }
}