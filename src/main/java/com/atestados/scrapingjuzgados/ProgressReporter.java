package com.atestados.scrapingjuzgados;

/**
 * Interfaz para reportar progreso de operaciones de scraping.
 * Permite que los scrapers funcionen con diferentes implementaciones
 * de UI (ScrapingProgressPanel, etc.).
 */
public interface ProgressReporter {
    void setMessage(String message);
    boolean isCancelled();

    /**
     * Notifica la adición de una fila durante el scraping.
     *
     * @param provincia Provincia a la que pertenece la fila
     * @param totalRows Número total de filas acumuladas hasta ahora
     */
    default void onRowAdded(String provincia, int totalRows) {
    }
}
