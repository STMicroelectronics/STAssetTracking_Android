package com.st.login.loginprovider

import android.app.Activity
import android.content.Context
import android.content.res.Configuration

class LoginProviderFactory {

    enum class LoginProviderType {
        COGNITO,
        KEYCLOAK,
        PREDMNT,
        AZURE_AUTH_D
    }

    companion object {
        fun getLoginProvider(activity: Activity, ctx: Context, loginProviderType: LoginProviderType, configuration: com.st.login.Configuration) : ILoginProvider{

            return when(loginProviderType) {
                LoginProviderType.COGNITO -> CognitoLoginProvider(activity, ctx, configuration)
                LoginProviderType.KEYCLOAK -> KeycloakLoginProvider(activity, ctx, configuration)
                LoginProviderType.PREDMNT -> PredMntLoginProvider(activity, ctx, configuration)
                else -> throw Exception("invalid login provider")
            }

        }
    }

}