package com.atestados.scrapingjuzgados;

/**
 * Interfaz para reportar progreso de operaciones de scraping.
 * Permite que los scrapers funcionen con diferentes implementaciones
 * de UI (ProgressDialog, CircularProgressPanel, etc.).
 */
public interface ProgressReporter {
    void setMessage(String message);
    boolean isCancelled();
}
