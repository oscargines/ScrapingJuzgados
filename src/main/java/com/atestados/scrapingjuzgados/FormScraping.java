package com.atestados.scrapingjuzgados;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Dashboard SaaS de scraping del directorio del Poder Judicial.
 * Estructura: sidebar blanca + topbar con búsqueda + tarjetas KPI +
 * tabla con badges de estado + panel de progreso integrado.
 */
public class FormScraping extends JFrame {

    private static final Color BG_APP = new Color(0xF5F6F8);

    private final SidebarPanel sidebar;
    private final StatCard kpiRegistros;
    private final StatCard kpiProvincias;
    private final StatCard kpiExito;
    private final StatCard kpiEstado;
    private final ScrapingProgressPanel progressPanel;
    private final StatusBadgeRenderer statusRenderer = new StatusBadgeRenderer();
    private final Map<String, Integer> provinciaCounts = new HashMap<>();

    private JTable tbDatos;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;
    private long scrapeStart;

    public FormScraping() {
        super("Scraping Juzgados");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);

        kpiRegistros = new StatCard("Registros totales", "0", "▲ en tiempo real");
        kpiProvincias = new StatCard("Provincias", "0", "de 52 provincias");
        kpiExito = new StatCard("Tasa de éxito", "100%", "▲ sin errores");
        kpiEstado = new StatCard("Estado", "Preparado", "listo para empezar");
        progressPanel = new ScrapingProgressPanel(this::onScrapeCancelled);
        progressPanel.setVisible(false);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BG_APP);

        sidebar = new SidebarPanel();
        sidebar.setOnSelect(this::onSidebarSelect);
        sidebar.setOnNewScrape(this::promptNewScrape);
        getContentPane().add(sidebar, BorderLayout.WEST);

        getContentPane().add(buildMainArea(), BorderLayout.CENTER);
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_APP);
        main.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        main.add(buildTopBar(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        body.add(buildStatCards(), BorderLayout.NORTH);
        body.add(wrapInCard(buildTable()), BorderLayout.CENTER);

        main.add(body, BorderLayout.CENTER);
        main.add(progressPanel, BorderLayout.SOUTH);
        return main;
    }

    private JComponent buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setOpaque(false);

        searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar en resultados...");
        searchField.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #FFFFFF");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        top.add(searchField, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);

        JButton abrirCarpeta = new JButton("Abrir carpeta SQL");
        abrirCarpeta.putClientProperty(FlatClientProperties.STYLE,
                "arc: 999; background: #F3F4F6; foreground: #374151; borderWidth: 0; focusWidth: 0");
        abrirCarpeta.setPreferredSize(new Dimension(160, 38));
        abrirCarpeta.addActionListener(e -> abrirCarpetaSQL());
        acciones.add(abrirCarpeta);

        JButton nuevo = new JButton("＋ Nuevo scraping");
        nuevo.putClientProperty(FlatClientProperties.STYLE,
                "arc: 999; background: #22C55E; foreground: #FFFFFF; borderWidth: 0; focusWidth: 0");
        nuevo.setPreferredSize(new Dimension(170, 38));
        nuevo.addActionListener(e -> promptNewScrape());
        acciones.add(nuevo);

        top.add(acciones, BorderLayout.EAST);
        return top;
    }

    /**
     * Abre en el explorador la carpeta donde se guardan los archivos SQL
     * generados (el directorio de trabajo de la aplicación).
     */
    private void abrirCarpetaSQL() {
        try {
            File carpeta = new File("").getAbsoluteFile();
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(carpeta);
            } else if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("explorer.exe", carpeta.getAbsolutePath()).start();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se pudo abrir la carpeta automáticamente.\nLos archivos SQL se guardan en: " + carpeta.getAbsolutePath());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir la carpeta: " + e.getMessage());
        }
    }

    private JComponent buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 20, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));
        row.add(kpiRegistros);
        row.add(kpiProvincias);
        row.add(kpiExito);
        row.add(kpiEstado);
        return row;
    }

    private JComponent buildTable() {
        tbDatos = new JTable(new DefaultTableModel(0, 0));
        tbDatos.setFillsViewportHeight(true);
        tbDatos.setIntercellSpacing(new Dimension(0, 0));
        tbDatos.setShowGrid(true);
        tbDatos.getTableHeader().setReorderingAllowed(false);

        sorter = new TableRowSorter<>((DefaultTableModel) tbDatos.getModel());
        tbDatos.setRowSorter(sorter);
        return new JScrollPane(tbDatos);
    }

    private JComponent wrapInCard(JComponent content) {
        JPanel card = new JPanel(new BorderLayout());
        card.putClientProperty(FlatClientProperties.STYLE,
                "arc: 18; background: #FFFFFF; border: 1,1,1,1,#E5E7EB");
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void applyFilter() {
        if (sorter == null || searchField == null) {
            return;
        }
        String text = searchField.getText().trim();
        sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
    }

    private void onSidebarSelect(String option) {
        if (SidebarPanel.OPTION_ORGANOS.equals(option)) {
            startScrape("Órganos judiciales", r -> new CScrap().obtenerTodos(tbDatos, r));
        } else if (SidebarPanel.OPTION_TODOS.equals(option)) {
            startScrape("Todos los juzgados", r -> new PJScraper().obtenerTodosPartidos(tbDatos, r));
        } else if (SidebarPanel.OPTION_DEMARCACION.equals(option)) {
            startScrape("Demarcaciones", r -> new PJScraper().obtenerTodosPartidos(tbDatos, r));
        }
    }

    private void promptNewScrape() {
        if (!sidebar.isEnabled()) {
            return;
        }
        String url = JOptionPane.showInputDialog(this,
                "Introduzca la URL del directorio de órganos judiciales:",
                "Nuevo scraping", JOptionPane.PLAIN_MESSAGE);
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        startScrape("Nuevo scraping", r -> new CScrap().scrapSitioWeb(url.trim(), tbDatos, r));
    }

    private interface ScrapeTask {
        void run(ProgressReporter reporter);
    }

    private void startScrape(String title, ScrapeTask task) {
        sidebar.setEnabled(false);
        resetData();
        kpiEstado.setValue("Trabajando");
        kpiEstado.setDelta("iniciando...");
        
        LoadingSlider slider = new LoadingSlider(6);
        kpiEstado.setAnimatedComponent(slider);
        slider.start();
        progressPanel.setMessage("Conectando...");
        progressPanel.reset();
        progressPanel.setVisible(true);
        scrapeStart = System.currentTimeMillis();

        ScrapeReporter reporter = new ScrapeReporter();
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                task.run(reporter);
                return null;
            }

            @Override
            protected void done() {
                long secs = (System.currentTimeMillis() - scrapeStart) / 1000;
                progressPanel.stop();
                progressPanel.setMessage("Scraping finalizado");
                progressPanel.setVisible(false);
                sidebar.setEnabled(true);
                slider.stop();
                kpiEstado.clearAnimatedComponent();
                if (progressPanel.isCancelled()) {
                    kpiEstado.setValue("Cancelado");
                    kpiEstado.setDelta("detenido por el usuario");
                } else {
                    kpiEstado.setValue("Completado");
                    kpiEstado.setDelta("tiempo total: " + secs + " s");
                    if (reporter.lastMessage != null && reporter.lastMessage.contains("Script SQL generado en:")) {
                        JOptionPane.showMessageDialog(FormScraping.this,
                                "Scraping completado.\n\n" + reporter.lastMessage
                                + "\n\nPuedes abrir la carpeta con el botón \"Abrir carpeta SQL\".",
                                "Scraping completado", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        };
        worker.execute();
    }

    private void onScrapeCancelled() {
        kpiEstado.setValue("Cancelando...");
        kpiEstado.setDelta("deteniendo el proceso");
    }

    private void resetData() {
        provinciaCounts.clear();
        tbDatos.setModel(new DefaultTableModel(0, 0));
        sorter.setModel((DefaultTableModel) tbDatos.getModel());
        sorter.setRowFilter(null);
        kpiRegistros.setValue("0");
        kpiProvincias.setValue("0");
    }

    /**
     * Reporter que actualiza el panel de progreso, los KPIs y las pastillas de
     * estado de la tabla. onRowAdded se invoca siempre en el EDT.
     */
    private class ScrapeReporter implements ProgressReporter {
        private volatile String lastMessage = "";

        @Override
        public void setMessage(String message) {
            lastMessage = message;
            progressPanel.setMessage(message);
            kpiEstado.setDelta(message);
        }

        @Override
        public boolean isCancelled() {
            return progressPanel.isCancelled();
        }

        @Override
        public void onRowAdded(String provincia, int totalRows) {
            int count = provinciaCounts.merge(provincia, 1, Integer::sum);
            kpiRegistros.setValue(String.valueOf(totalRows));
            kpiProvincias.setValue(String.valueOf(provinciaCounts.size()));
            int cols = tbDatos.getColumnCount();
            if (cols > 0) {
                tbDatos.getColumnModel().getColumn(cols - 1).setCellRenderer(statusRenderer);
            }
        }
    }

    public static void installTheme() {
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 16);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("@accentColor", new Color(0x22C55E));
        UIManager.put("Component.accentColor", new Color(0x22C55E));
        UIManager.put("Component.focusColor", new Color(0x22C55E));
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.gridColor", new Color(0xE5E7EB));
        UIManager.put("Table.rowHeight", 44);
        UIManager.put("Panel.background", BG_APP);
    }

    public static void main(String[] args) {
        installTheme();
        SwingUtilities.invokeLater(() -> new FormScraping().setVisible(true));
    }
}
