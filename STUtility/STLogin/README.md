# STLogin_Android
This repo holds the __STLogin__ module, which purpose is to provide a high-level library to be used in ST Android Applications to perform the user login procedure over a set of Identity Providers.

Current supported Identity Providers are:

* __Keycloak__
* __Cognito__

## Getting this module

You can get this module with ```git clone```:

```bash
git clone https://github.com/SW-Platforms/STLogin_Android.git
```

or using ```repo``` by adding the following lines to your manifest:

```xml
<!-- ...other repositories declarations... -->
<remote name="githubSwPlat"     fetch="https://github.com/SW-Platforms/" />

<!-- ...othe repo modules declarations... -->
<project name="STLogin_Android"   remote="githubSwPlat"   path="Application/STLogin"  revision="refs/heads/master" />
```

and then issuing the ```repo sync``` command.

## Declaring this module as a dependency
To link this module with an app:

1. Add the ```STLogin``` repository subfolder to the repo ```.gitignore```
2. Declare the module in the ```settings.gradle``` file:
```gradle
include ':STLogin'
```
3. Add the following repository to the root ```build.gradle```
```gradle
 allprojects {
     repositories {
         // other repos
        maven { url 'https://jitpack.io' }
     }
}
```
4. Declare the dependency in the ```app.gradle``` file:
```gradle
dependencies {
  // other dependencies
  implementation project(path: ':STLogin')
}
```
5. Declare the Auth Redirect Scheme according to the Identity Provider configuration using one of the following ways:

    * declare the ```appAuthRedirectScheme``` manifest placeholder:
    ```gradle
    android {
      // other definitions
      manifestPlaceholders = [
                      'appAuthRedirectScheme': 'net.openid.appauthdemo'
            ]
    }
    ```
    
    * declare the auth redirect scheme in the application manifest with an Intent Filter:
    ```xml
    <intent-filter>
      <action android:name="android.intent.action.VIEW"/>
      <category android:name="android.intent.category.DEFAULT"/>
      <category android:name="android.intent.category.BROWSABLE"/>
      <data android:scheme="stpredmntqa"/>
    </intent-filter>
    ```
    this latter method is preferred as it allows to support multiple auth redirect schemes in the same application.

## Using this module
To use the module, call the ```login()``` method as follows:

```kotlin
val resultRegistry = activity.activityResultRegistry
CoroutineScope(Dispatchers.Main).launch {

  val authData = LoginManager(
    resultRegistry,
    activity,
    context,
    LoginManager.LoginProviders.KEYCLOAK).login()

  Log.d(mTAG, authData.toString())
  
}
```

## License
COPYRIGHT(c) 2015 STMicroelectronics

 

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:
   1. Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
   2. Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
   3. Neither the name of STMicroelectronics nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
