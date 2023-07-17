package com.st.assetTracking.dashboard.communication.aws

class AwsErrorMessage(errorMessage: String?) {

    companion object {

        private lateinit  var message: String

        fun getErrorMessage() : String {
            return message
        }

    }

    init {
        if(errorMessage == null){
            message = ""
        }else{
            message = errorMessage
        }
    }
}