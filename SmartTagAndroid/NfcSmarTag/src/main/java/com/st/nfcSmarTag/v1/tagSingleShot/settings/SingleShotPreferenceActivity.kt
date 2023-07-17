package com.st.nfcSmarTag.v1.tagSingleShot.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.st.nfcSmarTag.R

class SingleShotPreferenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager.commit {
            replace(R.id.singleShotPref_contentView, SingleShotPreferenceFragment())
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

}