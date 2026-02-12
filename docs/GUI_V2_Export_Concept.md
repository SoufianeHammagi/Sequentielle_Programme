# GUI V2 — Export Concept

**Version:** 1.0  
**Date:** February 12, 2026  
**Status:** Concept & Implementation Plan

---

## Table of Contents

- [1. Background & Problem Statement](#1-background--problem-statement)
  - [1.1 GUI V1 Export (ExtJS)](#11-gui-v1-export-extjs)
  - [1.2 Limitations & Motivation for V2](#12-limitations--motivation-for-v2)
- [2. Goals & Requirements](#2-goals--requirements)
  - [2.1 Core Goals](#21-core-goals)
  - [2.2 Functional Requirements](#22-functional-requirements)
  - [2.3 Non-Functional Requirements](#23-non-functional-requirements)
- [3. Architecture Overview](#3-architecture-overview)
  - [3.1 High-Level Data Flow](#31-high-level-data-flow)
  - [3.2 Frontend (Webix)](#32-frontend-webix)
  - [3.3 Backend (PHP)](#33-backend-php)
  - [3.4 Separation of Concerns: GUI V1 vs GUI V2](#34-separation-of-concerns-gui-v1-vs-gui-v2)
- [4. Client-Side vs Server-Side Export](#4-client-side-vs-server-side-export)
  - [4.1 Webix Built-In Export](#41-webix-built-in-export)
  - [4.2 Server-Side Export (Our Approach)](#42-server-side-export-our-approach)
  - [4.3 Decision Matrix](#43-decision-matrix)
- [5. Technical Design](#5-technical-design)
  - [5.1 Frontend Component: CMExporter](#51-frontend-component-cmexporter)
  - [5.2 Export Action in CMActionCollection](#52-export-action-in-cmactioncollection)
  - [5.3 Request Schema](#53-request-schema)
  - [5.4 Column & Renderer Concept](#54-column--renderer-concept)
  - [5.5 Backend API / Endpoint](#55-backend-api--endpoint)
  - [5.6 Response & Download](#56-response--download)
- [6. UI / UX Design](#6-ui--ux-design)
  - [6.1 Export Button Placement](#61-export-button-placement)
  - [6.2 Export Menu Options](#62-export-menu-options)
  - [6.3 Column Selection Dialog (Future)](#63-column-selection-dialog-future)
  - [6.4 Responsiveness](#64-responsiveness)
- [7. Existing Implementation (Work in Progress)](#7-existing-implementation-work-in-progress)
  - [7.1 CMExporter.js](#71-cmexporterjs)
  - [7.2 CMActionCollection.js — daten_exportieren Action](#72-cmactioncollectionjs--daten_exportieren-action)
  - [7.3 LaenderVerwaltung.js — Reference Integration](#73-laenderverwaltungjs--reference-integration)
  - [7.4 CMIcons.css — New Icons](#74-cmiconscss--new-icons)
  - [7.5 CMMultiDataView.js — Batch & SplitButton Support](#75-cmmultidataviewjs--batch--splitbutton-support)
- [8. Third-Party Tool Research & Evaluation](#8-third-party-tool-research--evaluation)
  - [8.1 Webix Data Export](#81-webix-data-export)
  - [8.2 SheetJS (xlsx)](#82-sheetjs-xlsx)
  - [8.3 FileSaver.js](#83-filesaverjs)
  - [8.4 Server-Side Libraries (PHP)](#84-server-side-libraries-php)
  - [8.5 Recommendation](#85-recommendation)
- [9. Request Schema Reference](#9-request-schema-reference)
  - [9.1 Global Parameters](#91-global-parameters)
  - [9.2 Per-Field Parameters](#92-per-field-parameters)
  - [9.3 Filter Parameters](#93-filter-parameters)
  - [9.4 Example POST Body](#94-example-post-body)
- [10. Implementation Roadmap (TOPs)](#10-implementation-roadmap-tops)
  - [TOP 1 — Requirements & Goal Definition](#top-1--requirements--goal-definition)
  - [TOP 2 — Architecture & Core Concept](#top-2--architecture--core-concept)
  - [TOP 3 — Third-Party Research & Evaluation](#top-3--third-party-research--evaluation)
  - [TOP 4 — Technical Detail Design](#top-4--technical-detail-design)
  - [TOP 5 — Implementation](#top-5--implementation)
  - [TOP 6 — Documentation & Outlook](#top-6--documentation--outlook)
- [11. Future Extensibility](#11-future-extensibility)
- [12. References](#12-references)

---

## 1. Background & Problem Statement

### 1.1 GUI V1 Export (ExtJS)

In GUI V1, built with ExtJS, a mature export system exists via the `Ext.ux.CmGridExporter` plugin. This plugin provides:

- **Column Selection**: Users can pick which columns to include in the export (`exportable` / `doExport` flags per column).
- **Data Scope**: Three export modes:
  - **Current Page** — Only the data currently visible on the paginated page.
  - **All Results** — All data matching current filters and sorting.
  - **Selection** — Only the rows the user has manually selected.
- **Server-Side Export** via the "new export" path (`newExport: true`), where a POST request with structured parameters (`export[format]`, `export[field_type]`, `export[field_renderer]`, filters, sorting, etc.) is sent to the server.
- **Server-Side Export Engine**: `CM_Common::erstelleExport()` handles the heavy lifting — type conversion, rendering, Excel/CSV generation.
- **Multiple Formats**: CSV, Excel (.xlsx), Print/HTML, iCal, Charts.
- **Rich Type & Renderer System**: 23 field types and 24 renderers for complex data formatting.

**Reference files in GUI V1:**
- `CmGridExporter.js` — Frontend export plugin
- `CM_Common::erstelleExport()` — Server-side export engine (PHP)
- Example module: `profitcenter_verwalten` with `cmGridExporterExtraConfig: { newExport: true }` and per-field export configurations

### 1.2 Limitations & Motivation for V2

- **Webix vs ExtJS**: GUI V2 uses Webix, not ExtJS. The V1 plugin cannot be reused directly.
- **Client-Side Only (Webix)**: Webix provides [client-side data export](https://docs.webix.com/datatable__export.html) features (toCSV, toExcel, toPDF), but these have limitations for enterprise use:
  - Cannot handle very large datasets (browser memory constraints).
  - No server-side rendering/formatting (data consistency issues).
  - Missing advanced features (custom renderers, server-managed types).
- **Need for Consistency**: A standardized export solution for all GUI V2 modules ensures uniform behavior, reduces per-module boilerplate, and enables central improvements.

---

## 2. Goals & Requirements

### 2.1 Core Goals

1. **Server-Side Export** — CSV, Excel, and future formats generated on the server for scalability and data consistency.
2. **Filter / Sort / Column Selection / Scope** — Faithful reproduction of the V1 capabilities: current page, all results, or selected rows.
3. **Extensibility by Design** — Architecture supports adding new formats (PDF, iCal, Charts, Templates) and features (custom renderers, type extensions) without redesign.
4. **Uniform Standard** — A single reusable export component for all GUI V2 modules.

### 2.2 Functional Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-01 | Export button in grid toolbar (SplitButton with dropdown) | Must |
| FR-02 | Export scope: Current Page / All Results / Selection | Must |
| FR-03 | CSV export format | Must |
| FR-04 | Excel (.xlsx) export format | Must |
| FR-05 | Server-side export generation | Must |
| FR-06 | Send column metadata (field names, titles, types, renderers) to server | Must |
| FR-07 | Send current filter state to server | Must |
| FR-08 | Send current sort state to server | Must |
| FR-09 | Send selected row IDs for "Selection" scope | Must |
| FR-10 | Download response as file | Must |
| FR-11 | Column selection dialog (user picks columns before export) | Should |
| FR-12 | Per-column export configuration (exportType, exportConfig, exportField) | Must |
| FR-13 | Respect `exportable` and `hidden` flags on columns | Must |
| FR-14 | Support `exportField` mapping (virtual columns exporting different data fields) | Must |
| FR-15 | Configurable filename pattern | Should |
| FR-16 | Target encoding for CSV (e.g., CP1252 for legacy compatibility) | Should |
| FR-17 | Print/HTML export | Could |
| FR-18 | PDF export | Could |
| FR-19 | iCal export | Could |
| FR-20 | Chart export | Could |
| FR-21 | Responsive UI for export controls | Must |

### 2.3 Non-Functional Requirements

| ID | Requirement |
|----|-------------|
| NFR-01 | Export must work for datasets with 100,000+ rows (server-side) |
| NFR-02 | Export UI must be accessible on mobile/tablet viewports |
| NFR-03 | Export action must be permission-controlled |
| NFR-04 | CSRF token must be included in export requests |
| NFR-05 | Password fields must be excluded/blanked automatically |
| NFR-06 | Export must respect user's current grid state (filters, sort, page) |

---

## 3. Architecture Overview

### 3.1 High-Level Data Flow

```
┌──────────────────────────────────────────────────────────────────┐
│  USER ACTION: Clicks export button / selects format & scope     │
└──────────────────────────┬───────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────────┐
│  FRONTEND: CMExporter                                           │
│                                                                  │
│  1. Collect visible columns + header titles + export mapping     │
│  2. Collect current filter & sort state from Store               │
│  3. Collect selected row IDs (if scope = "selection")            │
│  4. Build POST parameter set (aligned with V1 newExport schema)  │
│  5. Submit via hidden HTML form (target=_blank)                  │
└──────────────────────────┬───────────────────────────────────────┘
                           │
                           ▼  POST request.php
┌──────────────────────────────────────────────────────────────────┐
│  BACKEND: PHP Controller                                         │
│                                                                  │
│  1. Parse export parameters                                      │
│  2. Load data from DB using module/function + filters + sort     │
│  3. Apply pagination (current page) or selection filter          │
│  4. Call CM_Common::erstelleExport($data, $headers, $titles,     │
│     $config) with field types, renderers, format options         │
│  5. Generate CSV or Excel file                                   │
│  6. Return file as download response                             │
└──────────────────────────┬───────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────────┐
│  FILE DOWNLOAD: Browser receives and saves the file              │
└──────────────────────────────────────────────────────────────────┘
```

### 3.2 Frontend (Webix)

The frontend responsibility is limited to:
- **UI**: Presenting the export menu (format + scope options) and optional column selection.
- **State Collection**: Reading the current grid state — visible columns, filters, sorting, pagination, selected IDs.
- **Parameter Assembly**: Building a structured POST payload that maps to the backend's expected request schema.
- **Submission**: Submitting via a dynamically created hidden `<form>` element for a seamless file download experience.

The frontend does **not** perform any data rendering or file generation.

### 3.3 Backend (PHP)

The backend leverages the existing `CM_Common::erstelleExport()` engine, which already supports:
- 23 field types (string, int, float, date, datetime, enum, set, hashmap, renderer, etc.)
- 24 specialized renderers (currency, HTML-to-text, packaging data, price tiers, etc.)
- Excel generation via `CM_PhpExcel`
- CSV generation with configurable encoding, BOM, separators
- Password field blanking, data sorting, type auto-detection

No major backend changes are required — the V2 frontend simply needs to send the same parameter schema that V1's `newExport` path already uses.

### 3.4 Separation of Concerns: GUI V1 vs GUI V2

| Aspect | GUI V1 (ExtJS) | GUI V2 (Webix) |
|--------|----------------|-----------------|
| Frontend Plugin | `Ext.ux.CmGridExporter` | `CMExporter` (AMD module) |
| UI Framework | ExtJS Grid Plugin | Webix SplitButton + CMActionCollection |
| Parameter Assembly | `configurateStore()` | `CMExporter._prepareExportParams()` |
| Export Engine (Backend) | `CM_Common::erstelleExport()` | `CM_Common::erstelleExport()` (same) |
| Request Schema | `export[format]`, `export[field_type]`, etc. | Same schema (compatible) |
| Submission | Hidden form POST | Hidden form POST (same pattern) |

---

## 4. Client-Side vs Server-Side Export

### 4.1 Webix Built-In Export

Webix provides client-side data export out of the box:

```javascript
// Webix examples
webix.toCSV($$("myTable"));
webix.toExcel($$("myTable"));
webix.toPDF($$("myTable"));
```

**Capabilities:**
- Quick CSV/Excel/PDF generation in the browser.
- Works with data currently loaded in the DataTable.
- Can include custom templates and formatting.
- [Documentation: https://docs.webix.com/datatable__export.html](https://docs.webix.com/datatable__export.html)

**Limitations:**
- Only exports data loaded in the client (pagination means partial data).
- No server-side field type/renderer processing (e.g., enum lookups, hashmap expansions).
- Large datasets can crash the browser or cause memory issues.
- No password field protection or server-enforced security.
- Excel output is limited compared to server-generated XLSX via PhpSpreadsheet.

### 4.2 Server-Side Export (Our Approach)

Our chosen approach sends export parameters to the server, where `CM_Common::erstelleExport()` handles everything:

**Advantages:**
- Scales to 100,000+ rows without browser constraints.
- Consistent data formatting (database enum lookups, custom renderers, type conversions).
- Centralized security (permission checks, password blanking, CSRF).
- Richer Excel output (styles, formatting, multi-sheet support potential).
- Single source of truth — server data, not stale client cache.

### 4.3 Decision Matrix

| Criterion | Client-Side (Webix) | Server-Side (CM_Common) |
|-----------|---------------------|------------------------|
| Large datasets | Limited by browser memory | Scales well |
| Data consistency | Depends on loaded data | Always fresh from DB |
| Custom renderers | Limited | Full support (24 renderers) |
| Enum/Set resolution | Not available | Database lookups |
| Security | No server validation | Permission + CSRF + password blanking |
| Implementation effort | Low (built-in) | Medium (already exists from V1) |
| Offline capability | Yes | No |
| Format quality | Basic | Professional (PhpExcel styles) |

**Decision**: Server-side export is the primary path. Client-side Webix export could be offered as a lightweight fallback for simple cases, but is **not** the strategic direction.

> **Note**: It is worth evaluating whether Webix's client-side export can be extended to support some V1 features (e.g., column selection, scope) for simpler use cases. However, the core export strategy remains server-side.

---

## 5. Technical Design

### 5.1 Frontend Component: CMExporter

`CMExporter` is an AMD module (`cm/CMExporter`) responsible for orchestrating the export process.

**Constructor Configuration:**

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `store` | Object | — | The CMStore instance containing data and state |
| `columns` | Array | — | Column/field configuration from the MDV config (`felder`) |
| `format` | String | `'csv'` | Export format: `'csv'` or `'excel'` |
| `encoding` | String | `'CP1252'` | Target encoding for CSV files |
| `exportType` | String | `'alle'` | Scope: `'alle'` (all), `'aktuelle'` (current page), `'auswahl'` (selection) |
| `selectedIds` | Array | — | Array of selected row IDs (for `'auswahl'` scope) |
| `newExport` | Boolean | `true` | Use the new server-side export path |
| `filename` | String | Auto-generated | Custom filename (auto: `YYYY-MM-DD_HH-mm-ss.ext`) |

**Key Methods:**

| Method | Description |
|--------|-------------|
| `execute()` | Entry point. Validates config and dispatches to the appropriate export handler. |
| `_prepareExportParams(store)` | Collects base parameters from the store: module, function, filters, sort, pagination, CSRF token. |
| `_processColumnsForExport(params, columns)` | Iterates columns, builds `felder[]`, `export[header_fields][]`, `export[header_title][]`, `export[field_type][]`, and processes per-column `exportConfig`. |
| `_processExportConfig(params, column, fieldIndex)` | Delegates to type-specific config handlers based on `exportType` / `subtype`. |
| `_configureHashmapExport(params, config, fieldIndex)` | Builds hashmap-specific params (key, value, separators). |
| `_configureEnumExport(params, config, fieldIndex)` | Builds enum/set-specific params (table, field, customMapping). |
| `_configureListExport(params, config, fieldIndex)` | Builds list-specific params (values, separator). |
| `_configureRendererExport(params, config, fieldIndex)` | Builds renderer-specific params (renderer name, unit, placeholders, currency, etc.). |
| `_submitExportForm(params)` | Creates a hidden `<form>`, populates it with `<input type="hidden">` fields, and submits via POST. |

### 5.2 Export Action in CMActionCollection

The export action is registered as `daten_exportieren` in `CMActionCollection.js`:

```javascript
daten_exportieren: {
  actionColumn: false,
  contextMenu: false,
  batch: 'tableContainer',
  mainToolbar: {
    enableOn: {
      alle_csv: 'notEmpty',
      alle_excel: 'notEmpty',
      aktuelle_csv: 'notEmpty',
      aktuelle_excel: 'notEmpty',
      auswahl_csv: 'select',
      auswahl_excel: 'select'
    }
  },
  type: 'splitbutton',
  splitButtonConfig: {
    options: [
      // CSV - All Pages, Current Page, Selection
      // Excel - All Pages, Current Page, Selection
    ]
  },
  handler: function(config) {
    const action = config.selectedOption.split('_');
    new CMExporter({
      store: this.getMdv().store,
      columns: this.getMdv().config.felder,
      format: action[1] || 'csv',
      encoding: 'CP1252',
      exportType: action[0] || 'alle',
      selectedIds: config.selectedIds
    }).execute();
  }
}
```

**Key behaviors:**
- `type: 'splitbutton'` renders a dropdown button with 6 options (CSV/Excel x All/Current/Selection).
- `enableOn` controls button state:
  - `'notEmpty'` — enabled when the grid has data.
  - `'select'` — enabled only when rows are selected.
- `batch: 'tableContainer'` — places the button in the table toolbar area.

### 5.3 Request Schema

The request schema is **intentionally aligned with the V1 `newExport` path** to maximize backend compatibility. Below is the full parameter structure sent as a POST to `request.php`:

#### Global Parameters

| Parameter | Example | Description |
|-----------|---------|-------------|
| `module` | `'cm'` | Target module |
| `function` | `'land_auflisten'` | Data-loading function on the backend |
| `export[format]` | `'csv'` or `'excel'` | Output format |
| `export[dateiname]` | `'2026-02-12_14-30-00.csv'` | Desired output filename |
| `export[target_encoding]` | `'CP1252'` | CSV character encoding |
| `export[root]` | `'daten'` | JSON root property for data extraction |
| `sql_calc_found_rows_under_root` | `1` | Instructs backend to calculate total rows |
| `_csrf` | `'...'` | CSRF token from `<meta name="_csrf">` |
| `limitstart` | `0` | Pagination start (current page scope only) |
| `limitlaenge` | `25` | Pagination count (current page scope only) |

#### Per-Field Parameters (indexed by field position)

| Parameter | Example | Description |
|-----------|---------|-------------|
| `felder[0]` | `'iso_3166_1_alpha_2'` | Data field name |
| `export[header_fields][0]` | `'iso_3166_1_alpha_2'` | Header field identifier |
| `export[header_title][iso_3166_1_alpha_2]` | `'ISO Alpha-2'` | Display title |
| `export[field_type][0]` | `'string'` | Field type for formatting |
| `export[field_subtype][0]` | `'hashmap'` | Sub-type for special rendering |
| `export[field_renderer][0]` | `'render_currency'` | Renderer function name |
| `export[field_key][0]` | `'currency_code'` | Hashmap key field |
| `export[field_value][0]` | `'currency_name'` | Hashmap value field |
| `export[field_table][0]` | `'cm_status'` | Enum lookup table |
| `export[field_field][0]` | `'status'` | Enum lookup column |
| `export[field_custom_mapping][0][key]` | `'Active'` | Custom enum mapping |
| `export[field_key_value_separator][0]` | `': '` | Hashmap key-value separator |
| `export[field_row_separator][0]` | `'\n'` | Hashmap row separator |
| `export[field_value_separator][0]` | `', '` | List value separator |
| `export[field_unit][0]` | `'kg'` | Unit string |
| `export[field_unit_literal][0]` | `true` | Unit is a literal (not a language key) |
| `export[field_unit_expressions][0][singular]` | `'item'` | Singular unit |
| `export[field_unit_expressions][0][plural]` | `'items'` | Plural unit |
| `export[field_render_int][0]` | `1` | Render as integer |
| `export[field_platzhalter][0]` | `'delivery_date'` | Placeholder field |
| `export[field_currency_field][0]` | `'waehrung'` | Currency source field |
| `export[requires][0]` | `'additional_data'` | Additional data dependency |

#### Filter Parameters

| Parameter | Example | Description |
|-----------|---------|-------------|
| `filter[sprache]` | `'de'` | Static/base filters from store config |
| `ext_gui_filter[0][field]` | `'bezeichnung'` | Dynamic grid filter — field name |
| `ext_gui_filter[0][type]` | `'string'` | Filter type |
| `ext_gui_filter[0][value]` | `'Deutschland'` | Filter value |
| `ext_gui_filter[0][comparison]` | `'eq'` | Comparison operator |
| `ext_gui_filter[0][comparison_operator]` | `'like'` | SQL comparison operator |

### 5.4 Column & Renderer Concept

Each column in the GUI V2 field configuration (`felder`) can define export behavior:

```javascript
{
  id: 'bezeichnungen',          // Column ID
  label: 'Designation',         // Display header
  exportField: 'bezeichnung',   // Maps to a different data field for export
  exportable: true,             // Whether column can be exported (default: true)
  hidden: false,                // Hidden columns are excluded from export
  type: 'string',               // Base type for export formatting
  exportType: 'hashmap',        // Sub-type for specialized rendering
  exportConfig: {               // Detailed export configuration
    subtype: 'hashmap',
    key: 'code',
    value: ['name', 'description'],
    keyValueSeparator: ': ',
    rowSeparator: '\n',
    valueSeparator: ', ',
    requires: ['additional_data_source']
  }
}
```

**Supported Export Sub-Types:**

| Sub-Type | Description | Key Config Properties |
|----------|-------------|----------------------|
| `hashmap` | Key-value pair rendering | `key`, `value`, `keyValueSeparator`, `rowSeparator`, `valueSeparator` |
| `enum` | Single enum value resolved via DB lookup | `table`, `field`, `customMapping` |
| `set` | Comma-separated enum values resolved via DB | `table`, `field`, `customMapping` |
| `list` | Multiple values joined by separator | `value`, `valueSeparator` |
| `renderer` | Custom server-side renderer function | `renderer`, `unit`, `unitLiteral`, `unitExpressions`, `renderInt`, `platzhalter`, `currencyField` |

**Supported Field Types (Server-Side):**

`string`, `int`, `float`, `numeric`, `bool`, `date`, `datetime`, `datetime_with_day`, `percent`, `null`, `mixed`, `numeric_not_empty`, `yes_no`, `gesichert`, `mime`, `bytes`, `bytes_per_second`, `list`, `set`, `enum`, `hashmap`, `renderer`, `dummy`

### 5.5 Backend API / Endpoint

- **Endpoint**: `request.php` (existing standard entry point)
- **Method**: POST
- **Content-Type**: `application/x-www-form-urlencoded` (submitted via hidden form)
- **Response**: Binary file download with appropriate headers (`Content-Type`, `Content-Disposition`)

The backend routing uses the `module` and `function` parameters to determine which data to load. The `export[...]` parameters are extracted and passed to `CM_Common::erstelleExport()`.

No new endpoint is required — the existing `request.php` controller already handles export requests when export parameters are present.

### 5.6 Response & Download

The server responds with:
- **Content-Type**: `text/csv; charset=CP1252` or `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- **Content-Disposition**: `attachment; filename="2026-02-12_14-30-00.csv"`
- **Body**: The raw file content

Since the form is submitted with `target="_blank"`, the download opens in a new tab (which immediately closes after the browser starts the download).

---

## 6. UI / UX Design

### 6.1 Export Button Placement

The export button is placed in the **main toolbar** of the grid (table container area), as a **SplitButton**:

```
┌─────────────────────────────────────────────────────────────┐
│  [+ Add]  [Edit]  [Delete]  ...  [▼ Export Data]            │
└─────────────────────────────────────────────────────────────┘
```

The SplitButton shows the last-used export action as the primary button and reveals a dropdown with all 6 options on click.

### 6.2 Export Menu Options

The dropdown provides 6 export options organized by format and scope:

| Icon | Label | Scope | Format | Enabled When |
|------|-------|-------|--------|-------------|
| file-table-outline | CSV - All Pages | All | CSV | Grid has data |
| file-table-outline | CSV - Current Page | Current Page | CSV | Grid has data |
| file-table-outline | CSV - Selection | Selection | CSV | Rows are selected |
| file-excel-outline | Excel - All Pages | All | Excel | Grid has data |
| file-excel-outline | Excel - Current Page | Current Page | Excel | Grid has data |
| file-excel-outline | Excel - Selection | Selection | Excel | Rows are selected |

**Icons used:**
- `cmi-file-table-outline` (Unicode: `\F0C7F`) — for CSV
- `cmi-file-excel-outline` (Unicode: `\F102D`) — for Excel

### 6.3 Column Selection Dialog (Future)

A planned enhancement is an interactive column selection dialog that lets the user:
- See all available columns (exportable ones).
- Toggle individual columns on/off for the export.
- Reorder columns via drag-and-drop.
- Save column selection preferences.

This mirrors GUI V1's column selection feature and is marked as a "Should" priority.

### 6.4 Responsiveness

The export UI must adapt to different viewport sizes:
- **Desktop**: Full SplitButton with labels and icons.
- **Tablet**: Condensed SplitButton (icons with tooltips).
- **Mobile**: Icon-only button or inclusion in a "More" overflow menu.

The `batch: 'tableContainer'` property ensures the export button participates in the toolbar's responsive layout system.

---

## 7. Existing Implementation (Work in Progress)

The following files have already been created or modified as part of the initial implementation:

### 7.1 CMExporter.js

**Path**: `gui/v2/js/cm/CMExporter.js`  
**Status**: New file — core export logic implemented.

The `CMExporter` module is fully functional for the `newExport` path with CSV and Excel formats. It:
- Accepts configuration via constructor (store, columns, format, encoding, exportType, selectedIds).
- Validates the export format against an allow-list (`gridexport_erlaubte_formate`).
- Prepares export parameters from the store's base params and current state.
- Processes columns, building the full parameter set including field types, headers, and per-field export configuration.
- Submits via a hidden form POST.
- Handles all five export sub-types: hashmap, enum/set, list, renderer.

### 7.2 CMActionCollection.js — daten_exportieren Action

**Path**: `gui/v2/js/cm/CMActionCollection.js`  
**Status**: Modified — new action added.

A new `daten_exportieren` action is added to the standard action collection. It:
- Renders as a SplitButton with 6 options (CSV/Excel x All/Current/Selection).
- Conditionally enables sub-options based on grid state (notEmpty, select).
- Instantiates `CMExporter` with the appropriate configuration and calls `execute()`.

### 7.3 LaenderVerwaltung.js — Reference Integration

**Path**: `gui/v2/js/cm/LaenderVerwaltung.js`  
**Status**: Modified — serves as the first integration example.

Changes:
- Added `filter: { sprache: Sprache.getActive() }` to store base params.
- Added `exportField: 'bezeichnung'` to the `bezeichnungen` column (maps a virtual display column to the actual data field for export).
- Enabled the export action: `daten_exportieren: true` in the permissions/actions config.

### 7.4 CMIcons.css — New Icons

**Path**: `gui/v2/css/CMIcons.css`  
**Status**: Modified — two new icon classes.

```css
.cmi.cmi-file-excel-outline::before {
  content: '\F102D';
}
.cmi.cmi-file-table-outline::before {
  content: '\F0C7F';
}
```

### 7.5 CMMultiDataView.js — Batch & SplitButton Support

**Path**: `gui/v2/js/gui/CMMultiDataView.js`  
**Status**: Modified — propagates `batch` property.

The `batch` property is now passed through to toolbar buttons, icons, and SplitButton components so that the export button is properly placed in the `tableContainer` batch area of the toolbar.

---

## 8. Third-Party Tool Research & Evaluation

### 8.1 Webix Data Export

- **What**: Built-in `webix.toCSV()`, `webix.toExcel()`, `webix.toPDF()`.
- **Pros**: Zero setup, works with loaded data, supports column templates.
- **Cons**: Client-side only, limited to loaded data, no server-side rendering.
- **Verdict**: Useful for quick lightweight exports. Not suitable as the primary export mechanism.

### 8.2 SheetJS (xlsx)

- **What**: JavaScript library for reading/writing Excel files.
- **Pros**: Powerful, runs in browser and Node.js, supports XLSX/CSV/ODS/etc.
- **Cons**: Large bundle size (~500KB), client-side still has dataset limitations, Community Edition lacks some formatting features.
- **Verdict**: Could be useful for client-side Excel generation if needed as a fallback. Not needed for server-side approach.

### 8.3 FileSaver.js

- **What**: Client-side file saving utility (implements `saveAs()`).
- **Pros**: Simple API, handles browser differences.
- **Cons**: Only useful for client-side generated files.
- **Verdict**: Not needed — our approach uses hidden form submission which triggers native browser download.

### 8.4 Server-Side Libraries (PHP)

- **PhpSpreadsheet** (successor to PHPExcel): Already in use via `CM_PhpExcel`. Handles Excel XLSX generation with styles, formatting, multi-sheet support.
- **League\Csv**: Clean CSV reading/writing library. May be considered for cleaner CSV generation.
- **TCPDF / Dompdf**: For future PDF export support.

### 8.5 Recommendation

Stick with the **server-side approach** using the existing `CM_Common::erstelleExport()` and `CM_PhpExcel`. No additional third-party frontend libraries are required for the core implementation. Evaluate Webix client-side export only for simple, non-critical quick-export scenarios.

---

## 9. Request Schema Reference

### 9.1 Global Parameters

```
module=cm
function=land_auflisten
export[format]=csv
export[dateiname]=2026-02-12_14-30-00.csv
export[target_encoding]=CP1252
export[root]=daten
sql_calc_found_rows_under_root=1
_csrf=abc123token
```

### 9.2 Per-Field Parameters

```
felder[0]=iso_3166_1_alpha_2
export[header_fields][0]=iso_3166_1_alpha_2
export[header_title][iso_3166_1_alpha_2]=ISO Alpha-2
export[field_type][0]=string

felder[1]=bezeichnung
export[header_fields][1]=bezeichnung
export[header_title][bezeichnung]=Designation
export[field_type][1]=string

felder[2]=waehrung
export[header_fields][2]=waehrung
export[header_title][waehrung]=Currency
export[field_type][2]=string
export[field_subtype][2]=enum
export[field_table][2]=cm_waehrung
export[field_field][2]=bezeichnung
```

### 9.3 Filter Parameters

```
filter[sprache]=de
ext_gui_filter[0][field]=bezeichnung
ext_gui_filter[0][type]=string
ext_gui_filter[0][value]=Deutschland
ext_gui_filter[0][comparison]=like
```

### 9.4 Example POST Body

Below is a complete example POST body for exporting the LaenderVerwaltung grid as CSV (all pages):

```
module=cm
&function=land_auflisten
&filter[sprache]=de
&export[format]=csv
&export[dateiname]=2026-02-12_14-30-00.csv
&export[target_encoding]=CP1252
&export[root]=daten
&sql_calc_found_rows_under_root=1
&_csrf=abc123token
&felder[0]=iso_3166_1_alpha_2
&export[header_fields][0]=iso_3166_1_alpha_2
&export[header_title][iso_3166_1_alpha_2]=ISO Alpha-2
&export[field_type][0]=string
&felder[1]=bezeichnung
&export[header_fields][1]=bezeichnung
&export[header_title][bezeichnung]=Designation
&export[field_type][1]=string
&felder[2]=iso_3166_1_alpha_3
&export[header_fields][2]=iso_3166_1_alpha_3
&export[header_title][iso_3166_1_alpha_3]=ISO Alpha-3
&export[field_type][2]=string
```

---

## 10. Implementation Roadmap (TOPs)

### TOP 1 — Requirements & Goal Definition

**Objective**: Clearly define what the export must support.

| Task | Status | Notes |
|------|--------|-------|
| Define export formats (short-term: CSV, Excel; long-term: PDF, iCal, Charts) | Done | See Section 2 |
| Define scope modes (Page / All / Selection) | Done | See Section 2 |
| Decide server-side vs. client-side | Done | Server-side primary |
| Compare V1 export UX with desired V2 UX | Done | See Section 3.4 |
| Define request schema (aligned with V1 newExport) | Done | See Section 9 |
| Decide: export via store URL vs. dedicated endpoint | Done | Via `request.php` (existing) |
| Define reusability approach (GUI V2 standard) | Done | Single `CMExporter` module |
| Project proposal | Pending | — |

### TOP 2 — Architecture & Core Concept

**Objective**: Define how the export works at a fundamental level.

| Task | Status | Notes |
|------|--------|-------|
| Frontend architecture (Webix): parameter collection | Done | `CMExporter` module |
| Backend architecture (PHP): `CM_Common::erstelleExport` | Done | Existing, reused |
| Data flow: Request → Export → Download | Done | See Section 3.1 |
| GUI V1 ↔ GUI V2 boundary definition | Done | See Section 3.4 |

### TOP 3 — Third-Party Research & Evaluation

**Objective**: Evaluate available third-party solutions.

| Task | Status | Notes |
|------|--------|-------|
| Evaluate Webix built-in export | Done | See Section 8.1 |
| Evaluate SheetJS (xlsx) | Done | See Section 8.2 |
| Evaluate FileSaver.js | Done | See Section 8.3 |
| Evaluate server-side PHP libraries | Done | See Section 8.4 |
| Final recommendation | Done | See Section 8.5 |

### TOP 4 — Technical Detail Design

**Objective**: Concrete technical specifications.

| Task | Status | Notes |
|------|--------|-------|
| Request schema (aligned with V1 newExport) | Done | See Section 9 |
| Column & renderer concept | Done | See Section 5.4 |
| Backend API / endpoint definition | Done | See Section 5.5 |
| CMExporter class design | Done | See Section 5.1 |
| CMActionCollection integration design | Done | See Section 5.2 |

### TOP 5 — Implementation

**Objective**: Build and deliver the export functionality.

| Task | Status | Notes |
|------|--------|-------|
| CMExporter.js — core export module | Done | See Section 7.1 |
| CMActionCollection.js — `daten_exportieren` action | Done | See Section 7.2 |
| CMIcons.css — export icons | Done | See Section 7.4 |
| CMMultiDataView.js — batch/SplitButton support | Done | See Section 7.5 |
| LaenderVerwaltung.js — reference integration | Done | See Section 7.3 |
| CSV export end-to-end test | Pending | — |
| Excel export end-to-end test | Pending | — |
| "Current Page" scope verification | Pending | — |
| "Selection" scope verification | Pending | — |
| Column selection dialog (future) | Planned | See Section 6.3 |
| Print/HTML export (future) | Planned | See Section 11 |
| Allowed formats config enforcement | Done | `gridexport_erlaubte_formate` |
| Responsive toolbar behavior | Pending | See Section 6.4 |
| Error handling & user feedback | Pending | — |

### TOP 6 — Documentation & Outlook

**Objective**: Complete documentation and plan future enhancements.

| Task | Status | Notes |
|------|--------|-------|
| Concept document (this file) | Done | — |
| V1 reference documentation (CmGridExporter) | Done | See `CmGridExporter_Reference.md` |
| Wiki/handbook: usage guide | Pending | — |
| Wiki/handbook: per-column config examples | Pending | — |
| Wiki/handbook: adding new export formats | Pending | — |
| Project closure documentation | Pending | — |

---

## 11. Future Extensibility

The export architecture is designed to accommodate future features without redesign:

| Feature | How It Fits |
|---------|-------------|
| **PDF Export** | Add `format: 'pdf'` support in `CMExporter`. Backend: integrate TCPDF/Dompdf in `erstelleExport()`. |
| **iCal Export** | Add `format: 'ical'` option. Backend: generate `.ics` file from date/event fields. |
| **Chart Export** | Add `format: 'chart'` option. Backend or frontend: generate chart images from data. |
| **Print/HTML Template** | Add `format: 'print'` option. Backend: render HTML template, frontend: open in print dialog. |
| **Custom Export Templates** | Allow per-module template override via `exportConfig.template`. |
| **Column Selection UI** | Add a dialog before export that lets users toggle columns on/off. The `exportable` flag already supports this. |
| **Saved Export Presets** | Store user-defined export configurations (columns, format, scope) and recall them. |
| **Scheduled Exports** | Backend cron job triggers export and emails the file. Same parameter schema. |
| **Bulk Export (Multiple Modules)** | Queue multiple export requests, deliver as ZIP. |
| **Custom Renderers** | Register new `render_*` functions in `CM_Common`. Frontend sends renderer name; backend processes. |
| **New Field Types** | Add new type handlers in `erstelleExport()`. Frontend sends the type name; backend processes. |

---

## 12. References

| Reference | Description |
|-----------|-------------|
| `gui/v2/js/cm/CMExporter.js` | GUI V2 export module (new) |
| `gui/v2/js/cm/CMActionCollection.js` | GUI V2 action definitions (modified) |
| `gui/v2/js/cm/LaenderVerwaltung.js` | GUI V2 reference integration (modified) |
| `gui/v2/css/CMIcons.css` | GUI V2 icon definitions (modified) |
| `gui/v2/js/gui/CMMultiDataView.js` | GUI V2 multi data view (modified) |
| `CmGridExporter.js` (V1) | GUI V1 ExtJS export plugin |
| `CM_Common::erstelleExport()` (PHP) | Server-side export engine |
| `profitcenter_verwalten` (V1) | V1 example with `cmGridExporterExtraConfig: { newExport: true }` |
| [Webix Data Export Docs](https://docs.webix.com/datatable__export.html) | Webix built-in client-side export |
| `CmGridExporter_Reference.md` | Comprehensive V1 export reference (see docs branch) |

---

*This document serves as the concept and implementation guide for the GUI V2 export system. It is intended to be updated as the implementation progresses and new features are added.*
