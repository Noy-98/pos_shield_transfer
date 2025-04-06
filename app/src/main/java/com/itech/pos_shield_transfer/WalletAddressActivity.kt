package com.itech.pos_shield_transfer

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WalletAddressActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_wallet_address)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val webView = findViewById<WebView>(R.id.webView)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true


        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Inject JavaScript to scroll to the target div and disable scrolling
                val js = """
                    (function() {
                        const target = document.querySelector('div.sc-772149c3-0.hgMDTZ');
                        if (target) {
                            target.scrollIntoView({ behavior: 'smooth', block: 'start' });
            
                            // Disable scrolling after a short delay
                            setTimeout(function() {
                                document.body.style.overflow = 'hidden';
                                document.documentElement.style.overflow = 'hidden';
                            }, 1000);
                        }
                    })();
                """
                view?.evaluateJavascript(js, null)
            }
        }

        val url = intent.getStringExtra("webViewUrl")
        if (!url.isNullOrEmpty()) {
            webView.loadUrl(url)
        }
    }
}