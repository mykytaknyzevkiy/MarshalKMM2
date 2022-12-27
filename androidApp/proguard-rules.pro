-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
   static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
   static **$* *;
}
-keepclassmembers class <2>$<3> {
   kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
   public static ** INSTANCE;
}
-keepclassmembers class <1> {
   public static <1> INSTANCE;
   kotlinx.serialization.KSerializer serializer(...);
}

-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-keep class com.l1inc.viewer.elevation.ElevationDataLoadingListener

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

-keepattributes *Annotation*
# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

-keep class com.l1inc.viewer.** { <fields>; }
-keep class com.l1inc.viewer.** { *; }