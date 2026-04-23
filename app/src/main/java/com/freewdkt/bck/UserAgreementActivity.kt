package com.freewdkt.bck

import android.os.Bundle
import androidx.activity.ComponentDialog
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.freewdkt.bck.databinding.ActivityMainBinding
import com.freewdkt.bck.databinding.ActivityUserAgreementBinding
import com.freewdkt.bck.requestconstants.ApiConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.noties.markwon.Markwon
import okhttp3.Request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


class UserAgreementActivity : AppCompatActivity() {
    val client = okhttp3.OkHttpClient()
    private lateinit var binding: ActivityUserAgreementBinding
    private lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserAgreementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dialogLoading = layoutInflater.inflate(R.layout.dialog_loading,null)
            loadingDialog = MaterialAlertDialogBuilder(this)
                .setView(dialogLoading)
                .create()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        loadingDialog.show()
        fetchUserAgreement()
    }

    private fun fetchUserAgreement() {
        val request = Request.Builder()
            .url(ApiConstants.USER_AGREEMENT)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                   loadingDialog.dismiss()
                    binding.userAgreementText.text = getString(R.string.load_fail)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val markdownText = response.body?.string() ?: ""
                    runOnUiThread {
                        loadingDialog.dismiss()
                        val markwon = Markwon.create(applicationContext)
                        markwon.setMarkdown(binding.userAgreementText, markdownText)
                    }
                } else {
                    runOnUiThread {
                        loadingDialog.dismiss()
                        binding.userAgreementText.text = "${getString(R.string.server_error)} ${response.code}"

                    }
                }
            }
        })
    }

}