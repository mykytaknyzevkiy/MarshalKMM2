# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Work\android\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class com.l1inc.viewer.elevation.ElevationDataLoadingListener #{
                                                                    #    public protected *;
                                                                    #}
 #{
                                                       #    public protected *;
                                                       #}
-keep class com.l1inc.viewer.Course3DRenderer{
    public static *** DEFAULT_OVERALL_MODE;
    public static *** ELEVATION;
    public static *** GPS_DETAILS;
    public static *** COURSE_ID;
}
-keep class com.l1inc.viewer.HoleWithinCourse{
    public protected *;
}

-keepclassmembers class com.l1inc.viewer.drawing.LayerPolygon {
    <fields>;
    <methods>;
}
-keep class com.l1inc.viewer.drawing.DrawElement
-keepclassmembers class com.l1inc.viewer.drawing.DrawElement{
    <fields>;
    <methods>;
}
-keepnames class com.l1inc.viewer.Course3DViewer { *; }
-keepparameternames
-keepclassmembers class com.l1inc.viewer.Course3DViewer {
    <methods>;
}
-keep class com.l1inc.viewer.elevation.ElevationHelper {
    public static *** getInstance();
    void parseElevationData(***);
    void prepare();
}
-keep class com.l1inc.viewer.elevation.ElevationDataLoadingListener{
    public protected *;
}
-keep class com.l1inc.viewer.PinPositionOverride{
    public protected *;
}
-keep public class com.l1inc.viewer.common.*
-keep class com.l1inc.viewer.textureset.DesertTextureSet
-keep class com.l1inc.viewer.textureset.DefaultTextureSet