package com.example.database_part_3

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.example.database_part_3.message_holder.chat_activity
import java.util.concurrent.Executors

class finger_print_lock : AppCompatActivity() {

    val context_ : Context = this

    // for checking either the biometric fingerprint is available or not
    private fun checkBiometricSupport(): Boolean{
        val keyguardManager : KeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if(!keyguardManager.isKeyguardSecure) {
            Toast.makeText(this,"Fingerprint hs not been enabled in settings",Toast.LENGTH_SHORT).show()
            return false
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC)!= PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this,"Fingerprint hs not been enabled in settings.",Toast.LENGTH_SHORT).show()
            return false
        }
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            true
        } else true
    }

    override fun onCreate(savedInstanceState: Bundle?){     // this means u can take inputs from layouts directly
        super.onCreate(savedInstanceState)
        setContentView(R.layout.finger_print_lock)

        checkBiometricSupport()

        val executor = Executors.newSingleThreadExecutor()
        val activity: FragmentActivity = this // reference to activity
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    // user clicked negative button
                } else {
                    Toast.makeText(context_,"some Unexpected error has occured!!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val intent = Intent(context_, chat_activity::class.java)
                startActivity(intent)                                              // if the authentications is successed then allow to see the chats
                Toast.makeText(context_,"Authentications has success!!", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed(){
                super.onAuthenticationFailed()
                Toast.makeText(context_,"Authentications has failed!!!", Toast.LENGTH_SHORT).show()
                /* Turn back to the previous layout */
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Set the title to display.")
            .setSubtitle("Set the subtitle to display.")
            .setDescription("Set the description to display")
            .setNegativeButtonText("Negative Button")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }
}