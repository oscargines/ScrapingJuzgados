# ScrapingJuzgados: Scraping de Directorio Judicial de España

Aplicación de escritorio en Java que realiza **scraping** del directorio del Poder Judicial de España, obteniendo información sobre órganos judiciales, partidos judiciales y municipios. Genera archivos SQL con los datos recopilados y los muestra en una tabla gráfica interactiva.

## Características

- **Scraping de Órganos Judiciales**: Recopila nombre, municipio, teléfono, dirección y código postal de cada juzgado.
- **Scraping de Partidos Judiciales**: Obtiene la estructura de partidos judiciales y sus municipios por provincia.
- **Generación de SQL**: Crea un archivo SQL con la tabla `sedes` lista para importar.
- **Progreso Visual**: Indicador circular animado integrado en la ventana con mensajes en tiempo real (ej: *"Procesando provincia 28/52: Madrid"*).
- **52 Provincias**: Soporta todas las provincias españolas, incluidas Ceuta y Melilla.
- **Paginación Automática**: Maneja automáticamente las páginas de resultados del sitio web.
- **Cancelación**: Botón para cancelar la operación en cualquier momento.

## Requisitos

- **Java**: JDK 22 o superior
- **Maven**: Para gestión de dependencias (incluye Maven Wrapper)
- **Librerías**:
  - [Jsoup](https://jsoup.org/) 1.18.1 - Conexión y análisis HTML
  - [Selenium](https://www.selenium.dev/) 4.10.0 - Scraping adicional (opcional)

## Instalación y Ejecución

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/oscargines/ScrapingJuzgados.git
   cd ScrapingJuzgados
   ```

2. **Compilar**:
   ```bash
   mvn clean compile
   ```

3. **Ejecutar**:
   ```bash
   mvn compile exec:java
   ```

   O desde NetBeans: Botón Run → Run Project

## Estructura del Proyecto

```
ScrapingJuzgados/
├── src/main/java/com/atestados/scrapingjuzgados/
│   ├── ScrapingJuzgados.java      # Clase principal (entry point)
│   ├── FormScraping.java          # Ventana principal con interfaz gráfica
│   ├── ProgressDialog.java        # Diálogo de progreso modal
│   ├── CircularProgressPanel.java # Panel de progreso circular integrado
│   ├── ProgressReporter.java      # Interfaz para reportar progreso
│   ├── CScrap.java                # Scraper de órganos judiciales (datos completos)
│   ├── PJScraper.java             # Scraper de sedes judiciales (misma salida juzgados_data.sql)
│   └── SeleniumScraper.java       # Scraper alternativo con Selenium
├── creaciontablasinserciondatos.sql # Script SQL: tabla tipos_juzgados
├── juzgados_data.sql              # Datos: sedes (órganos judiciales) (generado)
└── pom.xml                        # Configuración Maven
```

## Tablas SQL Generadas

### `sedes` (Órganos Judiciales)
| Columna | Tipo | Descripción |
|---------|------|-------------|
| id_juzgado | INTEGER | ID autoincremental |
| municipio | TEXT | Municipio del juzgado |
| nombre | TEXT | Nombre del órgano judicial |
| direccion | TEXT | Dirección postal |
| telefono | TEXT | Teléfono/s de contacto |
| codigo_postal | TEXT | Código postal |

### `tipos_juzgados` (Catálogo)
| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | INTEGER | ID autoincremental |
| denominacion | TEXT | Tipo de órgano judicial |

Contiene 34 tipos genéricos: Tribunal Supremo, Audiencia Nacional, Audiencia Provincial, Tribunal de Instancia, Tribunales Superiores de Justicia, Registro Civil, etc.

## Tipos de Órganos Judiciales

El filtro de la web del CGPJ clasifica los órganos en 34 categorías:

- Tribunal Supremo (y sus Salas)
- Audiencia Nacional (y sus Salas)
- Tribunal Central de Instancia (Secciones)
- Tribunal Superior de Justicia (y sus Salas)
- Audiencia Provincial
- Tribunal de Instancia (y sus Secciones)
- Registro Civil Central / Registro Civil

## Fuente de Datos

[Directorio de Órganos Judiciales - CGPJ](https://www.poderjudicial.es/cgpj/es/Servicios/Directorio/ch.Directorio-de-Organos-Judiciales.formato3/)

## Licencia

Proyecto académico de investigación. Los datos pertenecen al Consejo General del Poder Judicial de España.
