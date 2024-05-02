package com.example.azarnumerico

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class HelpActivity : ComponentActivity() {

    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help)

        webView = findViewById(R.id.helpWebView)
        webView.loadUrl("https://www.google.com")

    }
}