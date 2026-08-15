List of supported WML tags and attributes.

## Tag support
| Symbol | Description | Count
|-|-|-
| ✅ | Fully supported | 9 (25%)
| 🟡 | Partially supported | 13 (37%)
| ❌ | Broken or unsupported | 13 (37%)

## Attribute support
19 attributes (35%) are supported out of the 54 attributes tracked by this list. Unsupported tags have more attributes that are not currently listed here.

Attributes common to all elements (`class`, `id`, `xml:lang`) are ignored.

## Table
| Tag | Supported | Supported attr. | Unsupported attr. | Notes
|-|-|-|-|-
| `<a>`         | 🟡 | `href` | `title` | Nested images are shown as text only
| `<access>`    | ❌ |  |  |
| `<anchor>`    | 🟡 |  | `title` | Nested images are shown as text only
| `<b>`         | ❌ |  |  |
| `<big>`       | ❌ |  |  |
| `<br>`        | ✅ |  |  |
| `<card>`      | 🟡 |  | `newcontext`<br>`onenterbackward`<br>`onenterforward`<br>`ontimer`<br>`ordered`<br>`title` |
| `<do>`        | 🟡 | `type` | `name`<br>`optional` | No special handling for different types
| `<em>`        | ❌ |  |  |
| `<fieldset>`  | ❌ |  | `title` | Seems to be broken; not meant to have special handling, acts as HTML `<div>`
| `<go>`        | 🟡 | `href`<br>`method` | `accept-charset`<br>`sendreferer` |
| `<head>`      | ✅ |  |  |
| `<i>`         | ❌ |  |  |
| `<img>`       | 🟡 | `alt`<br>`localsrc`<br>`src` | `align`<br>`height`<br>`hspace`<br>`vspace`<br>`width` | Text-only when nested in links
| `<input>`     | 🟡 | `maxlength`<br>`name`<br>`value` | `emptyok`<br>`format`<br>`size`<br>`tabindex`<br>`title`<br>`type` |
| `<meta>`      | ❌ |  |  |
| `<noop>`      | ✅ |  |  |
| `<onevent>`   | ❌ |  |  |
| `<optgroup>`  | 🟡 |  | `title` | No special handling; all `<option>` tags are shown as one list
| `<option>`    | 🟡 | `value` | `onpick`<br>`title` |
| `<p>`         | 🟡 | `align` | `mode` |
| `<postfield>` | ✅ | `name`<br>`value` |  |
| `<prev>`      | ✅ |  |  |
| `<refresh>`   | 🟡 |  |  | Reloads the page
| `<select>`    | 🟡 | `iname`<br>`name`<br>`value` | `ivalue`<br>`multiple`<br>`tabindex`<br>`title` |
| `<setvar>`    | ✅ | `name`<br>`value` |  |
| `<small>`     | ❌ |  |  |
| `<strong>`    | ❌ |  |  |
| `<table>`     | 🟡 |  | `align`<br>`column`<br>`title` | Columns are shown stacked with divider lines for rows
| `<td>`        | ✅ |  |  |
| `<template>`  | ❌ |  |  |
| `<timer>`     | ❌ |  |  |
| `<tr>`        | ✅ |  |  |
| `<u>`         | ❌ |  |  |
| `<wml>`       | ✅ |  |  |