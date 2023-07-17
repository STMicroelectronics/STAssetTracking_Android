package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.st.login.Configuration
import com.st.login.LoginManager
import com.st.login.loginprovider.LoginProviderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class STLoginExampleActivity : AppCompatActivity() {

    var previousCheckedCompoundButton: CompoundButton? = null

    private lateinit var tvResult: TextView
    private lateinit var layoutLogin: LinearLayout
    private lateinit var layoutResult: LinearLayout

    private lateinit var rbCOGNITO : RadioButton
    private lateinit var rbKEYCLOAK : RadioButton
    private lateinit var rbPREDICTIVE : RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        // Hide title bar
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {}

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_st_login_example)

        layoutLogin = findViewById(R.id.layout_login)
        layoutResult = findViewById(R.id.layout_result)

        rbKEYCLOAK = findViewById(R.id.rbKEYCLOAK)
        rbCOGNITO = findViewById(R.id.rbCOGNITO)
        rbPREDICTIVE = findViewById(R.id.rbPREDICTIVE)

        val btnSignIn = findViewById<Button>(R.id.btnSignInProvider)

        tvResult = findViewById(R.id.result)

        btnSignIn.setOnClickListener{
            if(rbCOGNITO.isChecked){
                startExampleAuthentication(
                    LoginProviderFactory.LoginProviderType.COGNITO,
                    Configuration.getInstance(applicationContext, com.st.login.R.raw.auth_config_cognito))
            }else if(rbKEYCLOAK.isChecked){
                startExampleAuthentication(
                    LoginProviderFactory.LoginProviderType.KEYCLOAK,
                    Configuration.getInstance(applicationContext, com.st.login.R.raw.auth_config_keycloak))
            }
            else if(rbPREDICTIVE.isChecked){
                startExampleAuthentication(
                    LoginProviderFactory.LoginProviderType.PREDMNT,
                    Configuration.getInstance(applicationContext, com.st.login.R.raw.auth_config_predictive))
            }
        }

        rbKEYCLOAK.setOnCheckedChangeListener(onRadioButtonCheckedListener)
        rbCOGNITO.setOnCheckedChangeListener(onRadioButtonCheckedListener)
        rbPREDICTIVE.setOnCheckedChangeListener(onRadioButtonCheckedListener)
    }

    private fun startExampleAuthentication(loginProviderType: LoginProviderFactory.LoginProviderType, configuration: Configuration){
        // Check user authenticated [ Fire & Forget ]
        CoroutineScope(Dispatchers.Main).launch {
            val authData = LoginManager(this@STLoginExampleActivity.activityResultRegistry, this@STLoginExampleActivity, applicationContext, loginProviderType, configuration).login()
            if(authData != null){
                layoutLogin.visibility = View.GONE
                layoutResult.visibility = View.VISIBLE
                tvResult.text = authData.toString()
                println("AUTH_DATA_COMPLETED: $authData")
            }
        }
    }

    override fun onBackPressed() {
        if (layoutResult.visibility == View.VISIBLE){
            layoutResult.visibility = View.GONE
            layoutLogin.visibility = View.VISIBLE
        }else {
            super.onBackPressed()
        }
    }

    private var onRadioButtonCheckedListener: CompoundButton.OnCheckedChangeListener =
        object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                if (!isChecked) return
                if (previousCheckedCompoundButton != null) {
                    previousCheckedCompoundButton!!.isChecked = false
                    previousCheckedCompoundButton = buttonView
                } else {
                    previousCheckedCompoundButton = buttonView
                }
            }
        }

}