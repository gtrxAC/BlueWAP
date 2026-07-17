# BlueWAP
BlueWAP is a WAP (Wireless Application Protocol) browser for Java ME devices. It allows old phones to go online without a cellular or Wi-Fi connection. This works by connecting via Bluetooth to another device that has an internet connection.

![](/img/demo.jpg)

The browser consists of two apps, a client and a server. The client is run on the device that you want to go online on, and the server is run on a device that has an internet connection (2G/3G if available in your region, or 4G/5G, Wi-Fi, etc). Both apps require MIDP 2.0, CLDC 1.0, and JSR-82 Java Bluetooth API.

The client can also be used as a standalone WAP browser if your device has an internet connection.

## Status
BlueWAP currently supports most of the basic features of WML (Wireless Markup Language). As some WML and HTML elements are the same, this also allows for crude viewing of some HTML pages.

### Working
* Basic text paragraphs
* Hyperlinks
* Images
* Input fields and forms
* Variables

### Not implemented
* `<access>`
* `<select>`
* `<template>`
* Events (`onenterforward`/`backward`, timers)
* Text formatting
* Most optional attributes (e.g. `localsrc`, `accesskey`)