package com.atestados.scrapingjuzgados;

/**
 *
 * @author Oscar I. Ginés R.
 * 
 * Aplicación de escritorio que hace scraping de la web del directorio del 
 * Poder Judicial de España, con el fin de obtener ese mismo directorio en una 
 * base de datos de MSQLite. Es una herramienta creada por tener la necesidad 
 * de tener esta base de datos para otro proyecto.
 */
public class ScrapingJuzgados {

    public static void main(String[] args) {
        FormScraping objetForm = new FormScraping();        
        objetForm.setVisible(true);
        objetForm.setResizable(false);
        objetForm.setLocationRelativeTo(null);
    }
}
