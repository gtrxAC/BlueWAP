# J2ME project template
Template for developing and compiling Java ME (MIDP) applications with OpenJDK 8 and ProGuard. No IDE required. Uses shell/batch scripts and Node.js.

Supports Windows and Linux (macOS should work too, but not actively tested).

Based on the build system used by [Discord J2ME](https://github.com/gtrxAC/discord-j2me).

## Setup
1. Install [Node.js](https://nodejs.org).
2. Download [Temurin OpenJDK 8](https://adoptium.net/temurin/releases/?version=8&package=jdk) (zip or tar.gz). Extract the package into the `sdk` folder. Make sure there is a sub-folder named something like `sdk/jdk8u...`.
3. Download [ProGuard](https://github.com/Guardsquare/proguard/releases/latest). Extract the package and copy the extracted `lib/proguard.jar` file into the `sdk` folder.
4. To build your application, run `build.sh` (Linux) or `build.bat` (Windows). Output files are saved into the `bin` folder.
5. Running and testing your application is not currently covered by this template, but you can transfer the output JAR file to a real device or use an emulator such as [KEmulator nnmod](https://nnproject.cc/kem/).

## Build targets and preprocessor
j2me-template supports compiling multiple builds of an app, which can be useful when targeting various devices with different capabilities and quirks.

Conditional compilation is supported via C-style preprocessor directives, which can be used to include certain parts of your code only in certain builds of the app. Supported directives are `//#ifdef`, `//#ifndef`, `//#else`, and `//#endif`. These work similarly to their [equivalents in C and C++](https://www.w3schools.com/c/c_macros.php). They can be used in Java source files, the `manifest.mf` file, and the `midlets.pro` ProGuard configuration file.

### Example
A common use case for the preprocessor would be to exclude code that is not relevant for a certain device or platform. This reduces the JAR size and may also increase compatibility in some cases. For example, you could have two builds of an app, one with touchscreen support and one without.

In the `build.json` file, you can specify the `TOUCH_SUPPORT` define for one of the builds. In your Java code, you can then use a directive like:
```java
    public void example() {
        System.out.println("Hello!");
//#ifdef TOUCH_SUPPORT
        System.out.println("This line is only printed in the touch-enabled build");
//#endif
    }
```

### build.json file 
All of your build targets are specified in the `build.json` file. Each target includes a few details:
* `name`: The output JAR file name of this build.
* `defines`: Which preprocessor defines to use for this build.
* `bootclasspath`: Which Java libraries this build depends on. This should include a MIDP JAR (1.0, 2.0, or 2.1), a CLDC JAR (1.0 or 1.1), and optionally any extra APIs (for example JSR-75 for file system access).
* `excludes`: Which resource/asset files (in the `res` folder) are NOT included in this build. All files in the `res` folder are included in each JAR by default.

## MIDP and CLDC versions
By default, j2me-template targets MIDP 2.0 and CLDC 1.0. This means your app will run on devices with at least these MIDP and CLDC versions, which includes most devices released since around 2004.

Some devices from the early 2000s, notably older Symbian phones, do not support CLDC 1.1, which most importantly adds support for floating-point math (`float` and `double` data types). Older devices may also only support MIDP 1.0, which lacks several useful features, such as full-screen support and greater control over UI elements in Forms.

To change your MIDP and CLDC versions:
* Change the `-libraryjars` options in `midlets.pro`.
* Change the configuration and profile attributes in `manifest.mf`.
* Change the `bootclasspath` options in all of the build targets in `build.json`.

By using separate build targets and adding preprocessor directives to `midlets.pro` and `manifest.mf`, you can also compile builds that use different MIDP and CLDC versions.

## See also
* [J2ME Docs (documentation for various APIs)](https://nikita36078.github.io/J2ME_Docs)