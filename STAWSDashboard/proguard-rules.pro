# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#aws rules: https://github.com/aws-amplify/aws-sdk-android/blob/master/Proguard.md
# Class names are needed in reflection
-keepnames class com.amazonaws.**
-keepnames class com.amazon.**
# Request handlers defined in request.handlers
-keep class com.amazonaws.services.**.*Handler
# The following are referenced but aren't required to run
-dontwarn com.fasterxml.jackson.**
-dontwarn org.apache.commons.logging.**
# Android 6.0 release removes support for the Apache HTTP client
-dontwarn org.apache.http.**
# The SDK has several references of Apache HTTP client
-dontwarn com.amazonaws.http.**
-dontwarn com.amazonaws.metrics.**


-keepclasseswithmembernames class com.st.assetTracking.dashboard.communication.aws.AwsApiKey
-keepclasseswithmembernames class com.st.assetTracking.dashboard.communication.aws.AwsAssetTrackingService
-keepclasseswithmembernames class com.st.assetTracking.dashboard.communication.aws.AwsDeviceProfile
-keepclasseswithmembernames class com.st.assetTracking.dashboard.communication.aws.AwsEventData
-keepclasseswithmembernames class com.st.assetTracking.dashboard.communication.aws.AwsLocationData
-keepclasseswithmembernames class com.st.assetTracking.dashboard.communication.aws.AwsTelemetryData
-keepclasseswithmembernames class com.st.assetTracking.dashboard.model.ApiKey
-keepclasseswithmembernames class com.st.assetTracking.dashboard.model.Device
-keepclasseswithmembernames class com.st.assetTracking.dashboard.model.DeviceProfile
-keepclasseswithmembernames class com.st.assetTracking.dashboard.model.DeviceData
-keepclasseswithmembernames class com.st.assetTracking.dashboard.model.LastDeviceLocations
-keepclasseswithmembernames class com.st.assetTracking.dashboard.model.LocationData
-keepclasseswithmembernames class com.st.assetTracking.dashboard.persistance.AssetTrackingDashboardDB
-keepclasseswithmembernames class com.st.assetTracking.dashboard.persistance.DeviceDataDaoLowLevel
-keepclasseswithmembernames class com.st.assetTracking.dashboard.persistance.DeviceListDao
-keepclasseswithmembernames class com.st.assetTracking.dashboard.persistance.entity.EventSampleEntity
-keepclasseswithmembernames class com.st.assetTracking.dashboard.persistance.entity.LocationEntity
-keepclasseswithmembernames class com.st.assetTracking.dashboard.persistance.entity.SensorSampleEntity

-dontwarn com.amazonaws.mobile.auth.ui.AuthUIConfiguration
-dontwarn com.amazonaws.mobile.auth.ui.SignInUI
-dontwarn com.amazonaws.mobile.auth.facebook.FacebookButton
-dontwarn com.amazonaws.mobile.auth.google.GoogleButton
-dontwarn com.amazonaws.mobile.auth.facebook.FacebookSignInProvider
-dontwarn com.amazonaws.mobile.auth.google.GoogleSignInProvider
-dontwarn com.amazonaws.mobile.auth.userpools.CognitoUserPoolsSignInProvider
-dontwarn com.amazonaws.mobileconnectors.cognitoauth.**


### azure sdk ###
#azure sdk
-dontwarn org.apache.log4j.**
-dontwarn org.bouncycastle.**
-dontwarn com.microsoft.azure.storage.**
-dontwarn org.slf4j.*

#retrofit proguard rules http://square.github.io/retrofit/
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.-KotlinExtensions

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

#Keep Gson for ADAL https://github.com/google/gson/blob/master/examples/android-proguard-example/proguard.cfg
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.Signal
-dontwarn sun.misc.SignalHandler

-keep class com.google.gson.examples.android.model.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# [OKIO] Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# Other
-dontwarn javax.xml.stream.**
-dontwarn okhttp3.internal.platform.ConscryptPlatform
-dontwarn org.joda.convert.**
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.*
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE