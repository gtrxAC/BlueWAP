# ProGuard configuration
# Comments start with a #. You can use C-style preprocessor directives (//#ifdef, //#ifndef, //#else, //#endif) in this file to apply different configuration settings to different builds of your app.

# Add libraries used by your app here
-libraryjars ../sdk/lib/midp20.jar
-libraryjars ../sdk/lib/cldc10.jar

# Not recommended to edit these
-injars ../bin/in.jar
-outjars ../bin/out.jar
-microedition
-target 1.2

# You can use the "DEBUG" define in build.json to compile a version that is not obfuscated
//#ifdef DEBUG
-dontoptimize
-dontobfuscate
//#else
# Comment/uncomment below: multiple passes will take much longer to compile, but can be used when publishing a release
# -optimizationpasses 5
-optimizations !code/simplification/object,!method/inlining/unique
//#endif

# Not recommended to edit these
-keep public class * extends javax.microedition.midlet.MIDlet
-dontnote
-dontusemixedcaseclassnames
-repackageclasses ''
-overloadaggressively
-allowaccessmodification