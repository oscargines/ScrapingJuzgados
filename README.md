# CScrap: Scraping de Datos de Órganos Judiciales

CScrap es una aplicación en Java que realiza **scraping** de datos sobre órganos judiciales desde el [sitio web del Poder Judicial de España](https://www.poderjudicial.es/). Extrae información sobre juzgados, municipios, teléfonos, direcciones y códigos postales, y genera un archivo SQL con los datos recopilados. Además, muestra los resultados en una tabla gráfica interactiva.

## Características

- **Scraping de Datos**: Recopila información detallada de los órganos judiciales por provincias.
- **Generación de SQL**: Crea un archivo SQL con comandos `INSERT` para integrar los datos en una base de datos.
- **Soporte para Varias Provincias**: Incluye scraping para todas las provincias españolas, incluidas Ceuta y Melilla.
- **Interfaz Gráfica**: Muestra los resultados en una tabla para fácil visualización.
- **Manejo de SSL**: Deshabilita la validación SSL para evitar problemas con certificados no confiables durante las conexiones.

## Requisitos

- **Java**: JDK 8 o superior.
- **Librerías Externas**:
  - [Jsoup](https://jsoup.org/): Para la conexión y el análisis del contenido HTML.
  - **Swing**: Para la interfaz gráfica (parte del JDK estándar).

## Instalación

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/tu_usuario/cscrap.git
