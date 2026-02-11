# CmGridExporter & erstelleExport — Complete Reference Guide

**Version:** 1.0  
**Date:** February 11, 2026  
**Status:** Complete Reference Documentation

---

## Table of Contents

### [Part 1 — Server Side: `erstelleExport` (PHP)](#part-1--server-side-erstelleexport-php-1)
- [1.1 Overview](#11-overview)
- [1.2 Field Types](#12-field-types)
- [1.3 Renderers](#13-renderers)
- [1.4 Field-Level Configuration](#14-field-level-configuration)
- [1.5 Global Configuration](#15-global-configuration)
- [1.6 Security](#16-security)
- [1.7 Excel-Specific Behavior](#17-excel-specific-behavior)
- [1.8 CSV-Specific Behavior](#18-csv-specific-behavior)
- [1.9 Processing Pipeline](#19-processing-pipeline)

### [Part 2 — Client Side: `Ext.ux.CmGridExporter` (JavaScript/ExtJS)](#part-2--client-side-extuxcmgridexporter-javascriptextjs-1)
- [2.1 Overview](#21-overview)
- [2.2 Column-Level Configuration](#22-column-level-configuration)
- [2.3 Data Scope](#23-data-scope)
- [2.4 Old vs. New Export Path](#24-old-vs-new-export-path)
- [2.5 Automatic Type Detection](#25-automatic-type-detection)
- [2.6 Client-Side CSV Template](#26-client-side-csv-template-createcsvtemplate)
- [2.7 Client-Side Excel XML Template](#27-client-side-excel-xml-template-createexceltemplate)
- [2.8 Client-Side Print Template](#28-client-side-print-template-createprinttemplate)
- [2.9 Charts](#29-charts-createchartwindow--createchartstore)
- [2.10 iCal Export](#210-ical-export-createicalevents--createicaltemplate)
- [2.11 Renderer Priority Summary](#211-renderer-priority-summary)

### [Part 3 — Frontend ↔ Backend Mapping](#part-3--frontend--backend-mapping-1)
- [3.1 Parameter Mapping Table](#31-parameter-mapping-table)
- [3.2 End-to-End Data Flow](#32-end-to-end-data-flow)

### [Appendix](#appendix-1)
- [A. Excel XML Style Reference](#a-excel-xml-style-reference)
- [B. Default Configuration Values](#b-default-configuration-values)
- [C. Security Notes](#c-security-notes)

---

# Part 1 — Server Side: `erstelleExport` (PHP)

## 1.1 Overview

The `erstelleExport` method is the core server-side component responsible for generating export files (Excel or CSV) from structured data arrays. It provides comprehensive type conversion, custom rendering, and formatting capabilities.

### Method Signature

```php
public static function erstelleExport(
    array $daten, 
    array $header_fields, 
    array $header_title, 
    array|null $config = null
): false|string
```

### Parameters

- **`$daten`** — A multidimensional associative array containing the data rows to be exported. Each element represents one row of data with field names as keys.
  
- **`$header_fields`** — An indexed array of field names that define which columns should be included in the export and in what order. These field names must match keys in the `$daten` array.
  
- **`$header_title`** — An associative array mapping field names to their human-readable display titles. These titles appear in the header row of the exported file.
  ```php
  ['field_name' => 'Display Title', 'user_id' => 'User ID']
  ```
  
- **`$config`** — An optional associative array containing additional configuration options that control export behavior, field types, renderers, formatting options, and output format. See sections 1.4 and 1.5 for detailed configuration options.

### Return Value

Returns the file content as a string on success, or `false` on failure.

---

## 1.2 Field Types

The system supports 23 distinct field types that control how data is formatted and rendered in the export:

| Type | Description |
|---|---|
| `string` | Plain text field. Automatically wraps in Excel if the value contains line breaks (`\r` or `\n`). |
| `int` | Integer number with no thousands separator. Displays as whole numbers. |
| `float` | Decimal number with configurable precision (default 2 decimal places). |
| `numeric` | Generic numeric value, treated as float in CSV exports. |
| `bool` | Boolean value rendered as "true" or "false" string in CSV format. |
| `date` | Date-only value, formatted according to locale settings (typically DD.MM.YYYY). |
| `datetime` | Date with time, formatted with both date and time components. |
| `datetime_with_day` | Date with time and weekday name. The weekday is skipped in CSV exports but shown in Excel. |
| `percent` | Numeric value multiplied by 100 and displayed with a percentage (%) suffix. |
| `null` | Renders the string "null" when the field value is actually null. |
| `mixed` | Auto-detects whether value is numeric; if yes, formats as number, otherwise as string. |
| `numeric_not_empty` | Formats as numeric only if the value is non-empty, otherwise returns empty string. |
| `yes_no` | Converts 'j'/'n' single characters to translated "Ja"/"Nein" (Yes/No) strings. |
| `gesichert` | Converts 's' to translated "Ja" and any other value to translated "Nein". |
| `mime` | File count display with singular/plural label (e.g., "1 Datei" or "3 Dateien"). |
| `bytes` | Raw byte value formatted as human-readable size (e.g., "1,5 MB", "256 KB"). |
| `bytes_per_second` | Same as bytes format but with "/s" suffix for transfer rates (e.g., "2,5 MB/s"). |
| `list` | Multiple field values from the same row joined by a configurable separator (default ", "). |
| `set` | Comma-separated enum values where each value is resolved to its human-readable label from database. |
| `enum` | Single enum value resolved to its human-readable label via database lookup or custom mapping. |
| `hashmap` | Key-value pairs with configurable separators for key-value and between rows. |
| `renderer` | Custom rendering via one of the 24 built-in `render_*` callable functions. |
| `dummy` | Placeholder type; the actual type is specified in the `field_subtype` configuration. |

---

## 1.3 Renderers

The system provides 24 specialized renderers for complex data formatting scenarios:

| Renderer | Description | Example Output |
|---|---|---|
| `render_verpackungsdaten` | Displays packaging information combining order unit and content details | `Bestelleinheit: Karton\nInhalt: 12 Stück` |
| `render_artikel_preise` | Tiered price list showing quantity thresholds and corresponding prices | `ab 1: 10,00 €\nab 10: 9,50 €\nab 100: 8,75 €` |
| `render_uom` | Unit of measure with unit code in parentheses | `Stück (ST)` |
| `render_position_kommentare` | Position comments with author attribution | `John Doe (jdoe): This is a comment` |
| `render_tickets_kommentare` | Ticket comments grouped by ticket number with metadata | `Ticket: 1234 Short description\nJohn Doe (jdoe): Comment 25.01.2026 14:30` |
| `render_numeric_with_unit` | Formatted number with singular or plural unit based on value | `3,50 kg` |
| `render_ist_gesperrt_user_name` | Username of the user who has locked a record, with full name | `jdoe (John Doe)` |
| `render_bewertungen` | Product rating/review with selected attributes displayed | `Erstellt am: 25.01.2026 14:30\nBenutzername: jdoe\nKomfort: Sehr gut` |
| `render_skonto` | Cash discount tiers loaded from comma-separated discount IDs | `10 Tage: 2,00%\n30 Tage: 3,50%` |
| `render_benutzername_mit_name` | One or more usernames displayed with their full names | `jdoe (John Doe),jsmith (Jane Smith)` |
| `render_beschaffer_benutzername_mit_name` | Single purchaser username with full name in parentheses | `jdoe (John Doe)` |
| `render_platzhalter_vom_datum` | Dynamic label retrieved from database with formatted date appended | `Liefertermin vom 25.01.2026 14:30` |
| `render_user_schreibschutz_felder` | Write-protected field names as comma-separated translated labels | `Vorname,Nachname,E-Mail-Adresse` |
| `render_html` | Converts HTML content to plain text while preserving line breaks as `\r\n` | `First line\r\nSecond line` |
| `render_staffel_preis_ab` | Single price tier with optional "ab" (from) prefix | `ab 10,00 €` |
| `render_currency` | Numeric value formatted as currency with proper symbol | `10,00 €` |
| `render_oci_felder_mapping` | OCI field mappings showing inbound and outbound directions | `Outbound:\nTARGET ← SOURCE\nInbound:\nSOURCE → TARGET` |
| `render_time_of_day` | Time formatted with "Uhr" suffix | `14:30 Uhr` |
| `render_historie_referenz_bezeichnung` | History reference with zero-padded order number and position | `00042 - 3` |
| `render_zeitplan` | Order transmission schedule entries with day, hour, minute | `Wochentag:Montag Stunde:08 Minute:30` |
| `render_download_historie` | Download history showing date/time and user information | `25.01.2026 14:30:\nJohn Doe` |
| `render_workflow_entscheidung_fall` | Workflow decision case ID resolved to human-readable label | `Genehmigung` |
| `render_workflow_delegation` | Workflow delegation chain showing direction with arrows | `25.01.2026 14:30: John Doe (jdoe) => Jane Smith (jsmith)` |
| `render_artikel_status` | Article statuses as dash-separated list with descriptions | `Aktiv (veröffentlicht) - Gesperrt (intern)` |

---

## 1.4 Field-Level Configuration

These configuration keys can be set per column to control detailed rendering behavior:

| Configuration Key | Description |
|---|---|
| `field_precision` | Custom decimal precision for float fields. Overrides the global default of 2 decimal places. |
| `field_subtype` | The actual field type when `field_type` is set to "dummy". Allows indirection in type specification. |
| `field_table` | Database table name for enum/set label resolution via database lookup. |
| `field_field` | Column name in the database table for enum/set label resolution. |
| `field_custom_mapping` | Associative array providing custom label mapping instead of database enum lookup. Keys are raw values, values are display labels. |
| `field_key` | Column name containing the key field for hashmap rendering. |
| `field_value` | Column name(s) containing value field(s) for hashmap/list rendering. Can be a string or an array of strings. |
| `field_key_value_separator` | Separator string between key and value in hashmap output (default: `": "`). |
| `field_row_separator` | Separator string between multiple rows/entries (default: `"\n"`). |
| `field_value_separator` | Separator string between multiple values in list output (default: `", "`). |
| `field_unit` | Column name containing the unit of measure for `render_numeric_with_unit`. |
| `field_unit_literal` | Hardcoded unit string for `render_numeric_with_unit` when unit is not from a database column. |
| `field_unit_expressions` | Array with 'singular' and 'plural' keys containing translation keys for unit expressions. |
| `field_render_int` | Boolean flag to force integer formatting in `render_numeric_with_unit` (no decimal places). |
| `field_platzhalter` | Column name containing the placeholder label for `render_platzhalter_vom_datum`. |
| `field_currency_field` | Column name containing the currency code for `render_currency`. |
| `field_brutto_field` | Boolean or column name flag for selecting gross vs net price in `render_artikel_preise`. |

---

## 1.5 Global Configuration

These configuration keys apply to the entire export:

| Configuration Key | Description |
|---|---|
| `format` | Output format specification. Valid values: `"excel"` (default) or `"csv"`. |
| `target_encoding` | Target character encoding for CSV exports. If set, data is converted using `mb_convert_encoding()`. If not set, UTF-8 BOM is automatically prepended. |
| `default_table` | Fallback database table name used for enum/set resolution when `field_table` is not specified for a field. |
| `felder` | Master list of all field/column names defining the order of columns in the export. This overrides the order in `$header_fields`. |

---

## 1.6 Security

The export system implements automatic password field protection:

- **Password Field Detection**: Any field whose name contains the substring `kennwort` or `passwort` (case-insensitive) is automatically detected as a password field.
  
- **Automatic Blanking**: Password field values are forcibly set to an empty string before export, regardless of their original value.

- **Security by Design**: This protection cannot be disabled or bypassed through configuration, ensuring passwords are never exposed in exported files.

---

## 1.7 Excel-Specific Behavior

When `format` is set to `"excel"` (the default), the following behaviors apply:

### Text Wrapping
- **Auto-detection**: The `wrap_text` property is automatically enabled for any column where at least one value contains `\r` or `\n` line break characters.
- **Vertical Alignment**: When any column has `wrap_text` enabled, all cells are set to vertical align top for consistent appearance.

### Cell Configuration
- **`cell_config_datenzeile`**: Per-column cell styling and type configuration array that controls Excel cell formatting.
- **Dummy Template Row**: A dummy row containing `time()` values is inserted first to establish proper column data types, then immediately removed after type detection.

### File Generation
- **Library**: Uses the `CM_PhpExcel` class for Excel file generation.
- **Format**: Generates Excel 2007+ format (.xlsx) files.
- **Styling**: Applies configured styles, number formats, and cell types based on field types.

---

## 1.8 CSV-Specific Behavior

When `format` is set to `"csv"`, the following behaviors apply:

### Character Encoding
- **UTF-8 BOM**: If `target_encoding` is not set, a UTF-8 BOM (byte sequence `EF BB BF`) is prepended to the file content. This ensures proper encoding detection in Excel and other applications.
- **Encoding Conversion**: If `target_encoding` is specified, each line is converted using `mb_convert_encoding()` from UTF-8 to the target encoding.

### Line Formatting
- **Line Endings**: CSV files use Windows-style `\r\n` (CRLF) line endings for maximum compatibility.
- **Field Separator**: Default separator is semicolon (`;`).
- **Field Delimiter**: Default delimiter is double quote (`"`).

### CSV Generation
- **Writer Function**: Uses `CM_CsvDatei::writeCSVLine()` for proper field quoting and escaping.
- **Type Handling**: All complex types are converted to strings; no native CSV type preservation.

---

## 1.9 Processing Pipeline

The `erstelleExport` method processes data through the following sequential pipeline:

### Step 1: Parse Configuration Options
- Merge provided `$config` with default values
- Extract format, encoding, and global settings
- Prepare per-field type and configuration maps

### Step 2: Build Header Row
- Use `$header_fields` to determine column order
- Look up display titles from `$header_title`
- Create header row array

### Step 3: Clean Data
- Remove fields from `$daten` that are not in `$header_fields`
- Reorder fields in each data row to match `$felder` order if specified
- Detect and blank any password fields (field names containing 'kennwort' or 'passwort')

### Step 4: Detect Types
- For fields without explicit type configuration, infer type from data
- Check for numeric patterns, date formats, boolean values
- Set default type to 'string' if no pattern matches

### Step 5: Build Conversion Rules
- Create per-field conversion rules based on field type
- Prepare renderer callables for 'renderer' type fields
- Set up enum/set lookup configurations

### Step 6: Apply Conversions
Conversions are applied in this specific order:
1. **hashmap** → Process key-value pair structures
2. **enum** → Resolve single enum values to labels
3. **set** → Resolve comma-separated enum values to labels
4. **list** → Join multiple field values with separator
5. **yes_no** → Convert 'j'/'n' to "Ja"/"Nein"
6. **gesichert** → Convert 's' to "Ja", others to "Nein"
7. **mime** → Format file count with singular/plural
8. **bytes** → Format byte values as human-readable sizes
9. **renderer** → Execute custom render function
10. **mixed** → Auto-detect and format as numeric or string
11. **numeric_not_empty** → Format non-empty values as numeric
12. **bool** → Convert to "true"/"false" string
13. **date** → Format date values
14. **datetime** → Format datetime values
15. **null** → Convert null to "null" string
16. **int** → Format as integer
17. **float** → Format with specified precision
18. **percent** → Multiply by 100 and add % suffix

### Step 7: Generate Output
- **For Excel**: Create file using `CM_PhpExcel`, apply styles, generate .xlsx binary
- **For CSV**: Build CSV string with proper encoding, line endings, and BOM if needed
- Return file content as string

---

# Part 2 — Client Side: `Ext.ux.CmGridExporter` (JavaScript/ExtJS)

## 2.1 Overview

`Ext.ux.CmGridExporter` is an ExtJS grid plugin that adds comprehensive export capabilities to grid components.

### Registration
```javascript
Ext.preg('cmgridexporter', Ext.ux.CmGridExporter)
```

### Key Features
- **Export Buttons**: Automatically adds export format buttons to the grid's bottom toolbar
- **Column Selection**: Adds export column selection checkboxes to the column header menu
- **Permission Check**: Requires `pruefeBenutzerRechte('cm_grid_export')` permission for the user
- **Format Support**: Controlled by `erlaubteExportformate` configuration

### Supported Formats
Default supported formats (configurable via `erlaubteExportformate` array):
- `'print'` — HTML print preview in popup window
- `'csv'` — CSV file download
- `'excel'` — Excel file download
- `'ical'` — iCalendar file for calendar events

---

## 2.2 Column-Level Configuration

Each grid column can be configured with the following export-related properties:

| Property | Type | Default | Description |
|---|---|---|---|
| `exportable` | Boolean | `true` | Whether the column can be selected for export. Set to `false` to hide from export column selector. |
| `doExport` | Boolean | `false` | Whether the column is currently selected/enabled for export. Can be set to `true` to include by default. |
| `printRenderer` | Boolean/Function | — | Custom renderer for print output. If `true`, uses the column's own renderer. If function, calls that function. |
| `csvRenderer` | Boolean/Function | — | Custom renderer for CSV export. If `true`, uses the column's own renderer. If function, calls that function. |
| `excelRenderer` | Boolean/String/Function | — | Custom renderer for Excel XML. Can be `true` (use own renderer), function (custom renderer), or string `'csv'` (reuse csvRenderer). |
| `exportType` | String | — | Maps directly to server-side `field_type`. Specifies how the field should be processed. |
| `exportDataIndex` | String | — | Override the column's dataIndex for the server export request. Useful when display and export data differ. |
| `exportConfig` | Object | — | Additional configuration object sent to server. Can include keys like `key`, `value`, `table`, `field`, `renderer`, `requires`, etc. |
| `exportStyleTyp` | String | — | Excel XML style type. Valid values: `'auto'`, `'text'`, `'float'`, `'int'`, `'percent'`. |

---

## 2.3 Data Scope

Every export operation allows the user to select one of three data scopes:

| Scope | Value | Description |
|---|---|---|
| Current Page | `aktuelle_seite` | Exports only the current page of data. Preserves pagination parameters (limit/start) in the server request. |
| All Pages | `alle_seite` | Exports all available data. Removes pagination parameters so the server returns the complete dataset. |
| Selection | `auswahl` | Exports only the currently selected rows in the grid. Automatically disabled if no rows are selected. |

The scope selection is presented to the user before export execution (except in some automated scenarios).

---

## 2.4 Old vs. New Export Path

The plugin supports two different export execution paths:

### Old Path (when `newExport` is not `true`)

1. **Create Temp Store**: Creates a temporary `Ext.data.Store` clone with export configuration
2. **Load Data**: Loads data from the server with export parameters
3. **Client Rendering**: Renders the export client-side using:
   - `createCsvTemplate()` for CSV
   - `createExcelTemplate()` for Excel XML
   - `createPrintTemplate()` for print HTML
4. **Submit**: Submits the rendered content via a hidden HTML form
5. **Limitation**: All data must fit in browser memory

### New Path (when `newExport === true`, CSV/Excel only)

1. **Build Form**: Constructs a single comprehensive form POST with all parameters including:
   - Store base parameters
   - Sort information
   - Grid filter definitions
   - Column definitions with types and configuration
   - Export field types and config
2. **Direct POST**: POSTs directly to the store's URL
3. **Server Processing**: Server performs the query and calls `erstelleExport()` to generate the file
4. **Download**: Browser receives the file as a direct download response
5. **Security**: Includes CSRF token from HTML meta tag
6. **Advantage**: No memory limitation, handles large datasets efficiently

---

## 2.5 Automatic Type Detection

When a column does not have an explicit `exportType` configured, the plugin automatically detects the appropriate type:

| Condition | Detected Type |
|---|---|
| `dataIndex` is `'datum_erstellt'` or `'datum_aktualisiert'` | `datetime_with_day` |
| Column has a `type` property | Uses that type directly |
| Column `xtype` is `'cmdatecolumn'` | `datetime_with_day` |
| Column `xtype` is `'cmcurrencycolumn'` | `float` (or `int` if `decimalPrecision === 0`) |
| Column `xtype` is `'cmnumbercolumn'` | `float` (or `int` if `decimalPrecision === 0` or `allowDecimals === false`) |
| Column `xtype` is `'cmcheckcolumn'` with `trueValue='j'` and `falseValue='n'` | `yes_no` |
| No pattern matches | `string` (default) |

---

## 2.6 Client-Side CSV Template (`createCsvTemplate`)

When using the old export path, the CSV is generated entirely in the browser:

### Processing
- Iterates through all selected columns and visible data rows
- Applies renderer chain (see below) to format each cell value
- Builds a CSV string with proper escaping

### Column Type Handling
- **default**: Standard data column
- **action**: Action column (skipped in CSV)
- **sel_model**: Selection model column (skipped in CSV)
- **cm_check**: Checkbox column (included if exportable)

### Renderer Chain (Priority Order)
1. If `csvRenderer === true`: Use column's own `renderer` function
2. If `csvRenderer` is a function: Call that function
3. Otherwise: Use raw field value from record

### Formatting Details
- **Separator**: `;` (semicolon)
- **Delimiter**: `"` (double quote)
- **Escaping**: Delimiter characters inside values are doubled (`"` becomes `""`)
- **HTML Decode**: All values are HTML-decoded before export
- **Line Ending**: `\r\n` (Windows CRLF)

### Output
Returns a complete CSV string ready for download.

---

## 2.7 Client-Side Excel XML Template (`createExcelTemplate`)

Generates Excel 2003 SpreadsheetML XML format in the browser:

### Structure
```xml
<?xml version="1.0"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet" ...>
  <DocumentProperties>...</DocumentProperties>
  <Styles>...</Styles>
  <Worksheet ss:Name="Export">
    <Table>
      <Column ss:Width="..." />
      <Row ss:StyleID="s01"><!-- Title Row --></Row>
      <Row ss:StyleID="s01"><!-- Header Row --></Row>
      <Row><!-- Data Rows --></Row>
    </Table>
  </Worksheet>
</Workbook>
```

### Renderer Chain (6-Level Priority)
1. If `excelRenderer === true`: Use column's own `renderer` function
2. If `excelRenderer` is a function: Call that function
3. If `excelRenderer === 'csv'`: Check csvRenderer (go to level 4)
4. If `csvRenderer === true`: Use column's own `renderer` function
5. If `csvRenderer` is a function: Call that function
6. Otherwise: Use raw value, HTML-encoded

### Styles (8 Pre-defined)
- **s01**: Header style (bold, centered, borders)
- **s02**: Left-aligned text
- **s03**: Right-aligned text
- **s04**: Left-aligned float
- **s05**: Right-aligned float
- **s06**: Left-aligned percent
- **s07**: Right-aligned percent
- **s08**: Title style (bold, larger font)

### Auto Type Detection (Per Cell)
- **Leading zeros** (e.g., "007"): Type = String
- **10+ digits**: Type = String (prevents scientific notation)
- **Numeric pattern** (regex match): Type = Number
- **Ends with %**: Type = Percent (value divided by 100)
- **Default**: Type = String

### Column Width
- Converts pixel width to points: `width × 72 ÷ 96`

---

## 2.8 Client-Side Print Template (`createPrintTemplate`)

Generates an HTML page for printing:

### Structure
Opens a new browser popup window containing:
- HTML document with table
- Print-friendly CSS styling
- Auto-triggers print dialog (optional)

### Renderer Chain (Priority Order)
1. If `printRenderer === true`: Use column's own `renderer` function
2. If `printRenderer` is a function: Call that function
3. Otherwise: Use raw field value

### Special Column Handling
- **Action columns**: Tooltips from action items joined with `<br>` tags
- **Selection model**: Renders `<input type="checkbox">` element
- **Check column**: Renders checked or unchecked `<input type="checkbox">`

### Template Engine
Uses `Ext.XTemplate` for generating HTML:
```javascript
new Ext.XTemplate(
    '<html><head><style>...</style></head>',
    '<body><table>',
    '<tpl for="."><tr>...</tr></tpl>',
    '</table></body></html>'
)
```

---

## 2.9 Charts (`createChartWindow` + `createChartStore`)

Provides data visualization capabilities:

### `createChartStore`
- **Purpose**: Aggregates grid data for chart display
- **Grouping**: Groups data by `groupField` configuration
- **Aggregation Functions**:
  - `'count'`: Counts records per group
  - `'sum'`: Sums values from `dataField` per group
- **Output**: Returns aggregated data store for chart consumption

### `createChartWindow`
- **Purpose**: Renders interactive charts using Chart.js library
- **Chart Types**: Pie chart or Bar chart
- **Canvas Check**: Verifies browser Canvas support before rendering
- **Colors**: Generates random colors for each data segment
- **Y-Axis**: For bar charts, step size = `round(max/100) × 10`
- **Window**: Displays chart in an `Ext.Window` popup

---

## 2.10 iCal Export (`createIcalEvents` + `createIcalTemplate`)

Exports grid data as iCalendar (.ics) events:

### Field Mapping (`createIcalEvents`)
Three mapping types:
- **String value**: Direct field lookup via `record.get(value)` or literal string
- **Function**: Custom callback `function(store, record, grid)` returning the value
- **Auto UID**: If UID not specified, generates `{kontext}_{id}@comfortmarket`

### Supported iCal Fields
- `uid` — Unique identifier
- `dtstamp` — Timestamp
- `dtstart` — Event start date/time
- `dtend` — Event end date/time
- `summary` — Event title/summary
- `description` — Event description
- `location` — Event location
- `resources` — Event resources
- `attendee` — Event attendees
- `organizer` — Event organizer

### Server Processing (`createIcalTemplate`)
- **Endpoint**: AJAX POST to `cm/ical_auflisten`
- **Server Role**: Formats event data into proper iCal format
- **Output**: Downloads .ics file to user's device

---

## 2.11 Renderer Priority Summary

Different export formats have different renderer priority chains:

| Priority | CSV | Excel XML | Print |
|---|---|---|---|
| **1st** | `csvRenderer=true` → use column's own renderer | `excelRenderer=true` → use column's own renderer | `printRenderer=true` → use column's own renderer |
| **2nd** | `csvRenderer` is function → call it | `excelRenderer` is function → call it | `printRenderer` is function → call it |
| **3rd** | Raw dataIndex value | `excelRenderer='csv'` → check csvRenderer | Raw dataIndex value |
| **4th** | — | `csvRenderer=true` → use column's own renderer | — |
| **5th** | — | `csvRenderer` is function → call it | — |
| **6th** | — | Raw value (HTML-encoded) | — |

**Note**: Excel has the most sophisticated fallback chain, allowing reuse of CSV renderers.

---

# Part 3 — Frontend ↔ Backend Mapping

## 3.1 Parameter Mapping Table

This table shows how client-side form fields map to server-side PHP configuration:

| Frontend Form Field | Backend Config Key |
|---|---|
| `export[field_type][n]` | `$config['field_type'][n]` |
| `export[field_subtype][n]` | `$config['field_subtype'][n]` |
| `export[field_precision][n]` | `$config['field_precision'][n]` |
| `export[field_table][n]` | `$config['field_table'][n]` |
| `export[field_field][n]` | `$config['field_field'][n]` |
| `export[field_custom_mapping][n][key]` | `$config['field_custom_mapping'][n][key]` |
| `export[field_key][n]` | `$config['field_key'][n]` |
| `export[field_value][n]` | `$config['field_value'][n]` |
| `export[field_key_value_separator][n]` | `$config['field_key_value_separator'][n]` |
| `export[field_row_separator][n]` | `$config['field_row_separator'][n]` |
| `export[field_value_separator][n]` | `$config['field_value_separator'][n]` |
| `export[field_renderer][n]` | `$config['field_renderer'][n]` |
| `export[field_unit][n]` | `$config['field_unit'][n]` |
| `export[field_unit_literal][n]` | `$config['field_unit_literal'][n]` |
| `export[field_unit_expressions][n][singular]` | `$config['field_unit_expressions'][n]['singular']` |
| `export[field_unit_expressions][n][plural]` | `$config['field_unit_expressions'][n]['plural']` |
| `export[field_render_int][n]` | `$config['field_render_int'][n]` |
| `export[field_platzhalter][n]` | `$config['field_platzhalter'][n]` |
| `export[field_currency_field][n]` | `$config['field_currency_field'][n]` |
| `export[field_brutto_field][n]` | `$config['field_brutto_field'][n]` |
| `export[format]` | `$config['format']` |
| `export[target_encoding]` | `$config['target_encoding']` |
| `export[dateiname]` | Used for download filename |
| `export[root]` | Store root property for data extraction |
| `export[requires][n]` | Additional data dependencies loaded |
| `felder[n]` | `$config['felder'][n]` |
| `export[header_fields][n]` | `$header_fields[n]` |
| `export[header_title][field]` | `$header_title[field]` |

**Note**: The `[n]` notation indicates array indices, typically corresponding to column index or field name.

---

## 3.2 End-to-End Data Flow

The complete data flow from user action to file download:

```
┌─────────────────────────────────────────────────────────────────┐
│ USER ACTION                                                     │
│ User clicks Export button in grid toolbar                      │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ CLIENT-SIDE: CmGridExporter.configurateStore()                 │
│ • Determine data scope (current page / all / selection)       │
│ • Collect column configurations (types, renderers, config)    │
│ • Build export parameter set                                  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
                  ┌───────────┴───────────┐
                  │                       │
        ┌─────────▼─────────┐   ┌────────▼────────┐
        │ NEW PATH          │   │ OLD PATH        │
        │ (newExport=true)  │   │ (legacy)        │
        └─────────┬─────────┘   └────────┬────────┘
                  │                       │
                  │                       │
        ┌─────────▼─────────┐   ┌────────▼────────┐
        │ Build form POST   │   │ Create temp     │
        │ • Store params    │   │ store clone     │
        │ • Sort info       │   │                 │
        │ • Grid filters    │   │ Load data from  │
        │ • Column defs     │   │ server          │
        │ • Field types     │   │                 │
        │ • Export config   │   │                 │
        │ • CSRF token      │   │                 │
        └─────────┬─────────┘   └────────┬────────┘
                  │                       │
                  │                       │
        ┌─────────▼─────────┐   ┌────────▼────────────┐
        │ POST to server    │   │ Client rendering:   │
        │ PHP controller    │   │ • createCsvTemplate │
        └─────────┬─────────┘   │ • createExcelTemplate│
                  │              │ • createPrintTemplate│
                  │              └────────┬────────────┘
                  │                       │
                  │                       │
┌─────────────────▼───────────────────────▼───────────────────────┐
│ SERVER-SIDE: erstelleExport($daten, $header_fields,            │
│                              $header_title, $config)           │
│                                                                │
│ Step 1: Parse config options                                  │
│ • Merge with defaults                                         │
│ • Extract format, encoding, global settings                   │
│                                                                │
│ Step 2: Build header row                                      │
│ • Use $header_fields for column order                         │
│ • Look up display titles from $header_title                   │
│                                                                │
│ Step 3: Clean and sort data                                   │
│ • Remove extra fields not in $header_fields                   │
│ • Reorder by $felder if specified                             │
│ • Blank password fields (kennwort/passwort)                   │
│                                                                │
│ Step 4: Detect field types                                    │
│ • Infer types from data if not explicit                       │
│ • Check patterns: numeric, date, boolean                      │
│                                                                │
│ Step 5: Build conversion rules                                │
│ • Create per-field conversion map                             │
│ • Prepare renderer callables                                  │
│ • Set up enum/set lookup config                               │
│                                                                │
│ Step 6: Apply conversions (in order)                          │
│ • hashmap → enum → set → list                                 │
│ • yes_no → gesichert → mime → bytes                           │
│ • renderer → mixed → numeric_not_empty                        │
│ • bool → date → datetime → null                               │
│ • int → float → percent                                       │
│                                                                │
│ Step 7: Generate output                                       │
│ • Excel: CM_PhpExcel, apply styles, generate .xlsx           │
│ • CSV: Build CSV with encoding, BOM, line endings            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ FILE DOWNLOAD                                                  │
│ • Server returns file binary                                  │
│ • Browser triggers download with specified filename           │
│ • User receives Excel or CSV file                             │
└─────────────────────────────────────────────────────────────────┘
```

**Alternative Path (Old Method)**:
```
Client Rendering → Submit via hidden form → Server receives pre-rendered content
```

---

# Appendix

## A. Excel XML Style Reference

The Excel XML template uses 8 pre-defined styles:

| Style ID | Name | Alignment | Border | Number Format | Font | Usage |
|---|---|---|---|---|---|---|
| `s01` | Header | Horizontal: Center<br>Vertical: Center | All borders: Continuous, Weight 1 | — | Bold: 1 | Header row cells |
| `s02` | Text Left | Horizontal: Left<br>Vertical: Top | — | @ (text format) | — | Left-aligned text data |
| `s03` | Text Right | Horizontal: Right<br>Vertical: Top | — | @ (text format) | — | Right-aligned text data |
| `s04` | Float Left | Horizontal: Left<br>Vertical: Top | — | #,##0.00 | — | Left-aligned decimal numbers |
| `s05` | Float Right | Horizontal: Right<br>Vertical: Top | — | #,##0.00 | — | Right-aligned decimal numbers |
| `s06` | Percent Left | Horizontal: Left<br>Vertical: Top | — | 0.00% | — | Left-aligned percentages |
| `s07` | Percent Right | Horizontal: Right<br>Vertical: Top | — | 0.00% | — | Right-aligned percentages |
| `s08` | Title | Horizontal: Center<br>Vertical: Center | All borders: Continuous, Weight 1 | — | Bold: 1<br>Size: 14 | Title row |

---

## B. Default Configuration Values

Summary of all default configuration values:

### Server-Side Defaults
- **`format`**: `'excel'`
- **`field_key_value_separator`**: `': '` (colon-space)
- **`field_row_separator`**: `"\n"` (newline)
- **`field_value_separator`**: `', '` (comma-space)
- **Float precision**: `2` decimal places

### Client-Side Defaults
- **`csvSeparator`**: `';'` (semicolon)
- **`csvDelimeter`**: `'"'` (double quote)
- **`storeTimeout`**: `900000` milliseconds (15 minutes)
- **`erlaubteExportformate`**: `['print', 'csv', 'excel', 'ical']`
- **`exportable`**: `true` (columns exportable by default)
- **`doExport`**: `false` (columns not selected by default)

### Encoding Defaults
- **CSV without target_encoding**: UTF-8 with BOM (`EF BB BF`)
- **CSV line ending**: `\r\n` (Windows CRLF)

---

## C. Security Notes

### Password Protection
- **Automatic Detection**: Field names containing `kennwort` or `passwort` are automatically identified
- **Forced Blanking**: These fields are set to empty string before export
- **No Bypass**: This security measure cannot be disabled through configuration
- **Case Insensitive**: Detection works regardless of case (KENNWORT, Passwort, etc.)

### CSRF Protection (New Export Path)
- **Token Source**: CSRF token retrieved from HTML meta tag
- **Inclusion**: Token automatically included in export form POST
- **Validation**: Server validates token before processing export request

### Permission Check
- **Required Permission**: `cm_grid_export` via `pruefeBenutzerRechte('cm_grid_export')`
- **Enforcement**: Export functionality disabled if user lacks permission
- **Scope**: Applies to all export formats (CSV, Excel, Print, iCal)

### Data Access
- **Respects Filters**: Export honors grid filters and user permissions
- **Scope Control**: User selects data scope (current page/all/selection)
- **No Bypass**: Cannot export data user doesn't have permission to view in grid

---

**END OF DOCUMENT**

*This reference guide covers all aspects of the CM export system. For implementation examples and troubleshooting, consult the source code and related documentation.*
