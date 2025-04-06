package com.itech.pos_shield_transfer

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class BuyCryptoDashboard : AppCompatActivity() {

    private lateinit var sendBitcoinEditText: TextInputEditText
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_buy_crypto_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fiatCurrencyDropdown = findViewById<AutoCompleteTextView>(R.id.fiat_currency)
        val bitcoinCurrencyDropdown = findViewById<AutoCompleteTextView>(R.id.bitcoin_currency)
        sendBitcoinEditText = findViewById(R.id.send_bitcoin_editText)

        val options = listOf("USD", "PHP")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options)
        fiatCurrencyDropdown.setAdapter(adapter)

        val bitcoinOptions = listOf("TRX", "USDTTRC20", "ETH", "USDDTRC20", "BNB-BSC")
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bitcoinOptions)
        bitcoinCurrencyDropdown.setAdapter(adapter2)

        // Show the popup when the user clicks the input field
        sendBitcoinEditText.setOnClickListener {
            showAmountInputPopup()
        }

        fiatCurrencyDropdown.setOnItemClickListener { _, _, _, _ ->
            convertAndSetValue()
        }

        bitcoinCurrencyDropdown.setOnItemClickListener { _, _, _, _ ->
            convertAndSetValue()
        }

        sendBitcoinEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) convertAndSetValue()
        }

        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.loadUrl("https://stealthex.io/exchange/new/?amount=50&from=usd&to=usdttrc20")

        val nextButton = findViewById<AppCompatButton>(R.id.next_bttn)
        nextButton.setOnClickListener {
            val currentUrl = webView.url
            val intent = Intent(this, WalletAddressActivity::class.java)
            intent.putExtra("webViewUrl", currentUrl)
            startActivity(intent)
        }


    }

    private fun convertAndSetValue() {
        val fiat = findViewById<AutoCompleteTextView>(R.id.fiat_currency).text.toString()
        val crypto = findViewById<AutoCompleteTextView>(R.id.bitcoin_currency).text.toString()
        val amountStr = sendBitcoinEditText.text.toString()

        if (fiat.isNotBlank() && crypto.isNotBlank() && amountStr.isNotBlank()) {
            val amount = amountStr.toDoubleOrNull() ?: return

            // Check minimum thresholds
            if (fiat == "USD" && amount < 50) {
                Toast.makeText(this, "The minimum amount is 50 dollars", Toast.LENGTH_SHORT).show()
                return
            }

            if (fiat == "PHP" && amount < 2871.0) {
                Toast.makeText(this, "The minimum amount is 2871.0 Philippine Peso", Toast.LENGTH_SHORT).show()
                return
            }

            val cryptoValue = getCryptoValue(fiat, crypto, amount)

            if (cryptoValue != null) {
                findViewById<AutoCompleteTextView>(R.id.bitcoin_currency).setText("$crypto ($cryptoValue)", false)
            } else {
                findViewById<AutoCompleteTextView>(R.id.bitcoin_currency).setText("$crypto (Not Available)", false)
            }

            // ✅ Update WebView URL dynamically
            val amountParam = amount.toString()
            val fromParam = when (fiat.uppercase()) {
                "USD" -> "usd"
                "PHP" -> "php"
                else -> ""
            }

            val toParam = when (crypto.uppercase()) {
                "TRX" -> "trx"
                "USDTTRC20" -> "usdttrc20"
                "ETH" -> "eth"
                "USDDTRC20" -> "usddtrc20"
                "BNB-BSC" -> "bsc"
                else -> ""
            }

            if (fromParam.isNotEmpty() && toParam.isNotEmpty()) {
                val newUrl =
                    "https://stealthex.io/exchange/new/?amount=$amountParam&from=$fromParam&to=$toParam"
                webView.loadUrl(newUrl)
            }
        }
    }


    private fun getCryptoValue(fiat: String, crypto: String, amount: Double): Double? {
        val conversions = mapOf(
            "USD" to mapOf(
                50.0 to mapOf("TRX" to 201.179783, "USDTTRC20" to 48.4698404, "ETH" to 0.02739561, "USDDTRC20" to 48.13086555, "BNB-BSC" to 0.08267299),
                51.0 to mapOf("TRX" to 205.203378, "USDTTRC20" to 49.4392372, "ETH" to 0.02794352, "USDDTRC20" to 49.09348286, "BNB-BSC" to 0.08432645),
                52.0 to mapOf("TRX" to 209.142955, "USDTTRC20" to 50.40863401, "ETH" to 0.02849143, "USDDTRC20" to 50.05610017, "BNB-BSC" to 0.08597991)
            ),
            "PHP" to mapOf(
                2871.0 to mapOf("TRX" to 201.406125, "USDTTRC20" to 48.4833034, "ETH" to 0.02752126, "USDDTRC20" to 48.14407414, "BNB-BSC" to 0.0829978),
                2872.0 to mapOf("TRX" to 201.476277, "USDTTRC20" to 48.50019065, "ETH" to 0.02753084, "USDDTRC20" to 48.16084323, "BNB-BSC" to 0.08302671),
                2873.0 to mapOf("TRX" to 201.546429, "USDTTRC20" to 48.51707791, "ETH" to 0.02754043, "USDDTRC20" to 48.17761233, "BNB-BSC" to 0.08305562)
            )
        )

        val fiatRates = conversions[fiat] ?: return null
        val keys = fiatRates.keys.sorted()

        // Edge cases
        if (amount <= keys.first()) {
            return fiatRates[keys.first()]?.get(crypto)
        } else if (amount >= keys.last()) {
                val last = keys[keys.size - 1]
                val secondLast = keys[keys.size - 2]

                val lastValue = fiatRates[last]?.get(crypto) ?: return null
                val secondLastValue = fiatRates[secondLast]?.get(crypto) ?: return null

                // Linear extrapolation
                val slope = (lastValue - secondLastValue) / (last - secondLast)
                val extrapolated = lastValue + slope * (amount - last)
                return extrapolated
            }


        // Find nearest lower and upper keys
        val lowerKey = keys.lastOrNull { it <= amount } ?: return null
        val upperKey = keys.firstOrNull { it >= amount && it != lowerKey } ?: return null

        val lowerValue = fiatRates[lowerKey]?.get(crypto) ?: return null
        val upperValue = fiatRates[upperKey]?.get(crypto) ?: return null

        // Linear interpolation
        val ratio = (amount - lowerKey) / (upperKey - lowerKey)
        return lowerValue + (upperValue - lowerValue) * ratio
    }


    private fun showAmountInputPopup() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.amount_input_layout, null)

        val container = FrameLayout(this)
        val marginInDp = 30
        val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(marginInPx, marginInPx, marginInPx, marginInPx)
        container.layoutParams = layoutParams
        container.addView(view)

        dialog.setContentView(container)
        dialog.setCancelable(false)


        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        val amountEditText = view.findViewById<TextInputEditText>(R.id.amount)

        val currentValue = sendBitcoinEditText.text.toString()
        if (currentValue.isNotEmpty()) {
            amountEditText.setText(currentValue)
        }

        // Setup buttons
        val numberIds = listOf(
            R.id.num_0, R.id.num_1, R.id.num_2, R.id.num_3, R.id.num_4,
            R.id.num_5, R.id.num_6, R.id.num_7, R.id.num_8, R.id.num_9, R.id.decimal_point
        )

        for (id in numberIds) {
            view.findViewById<Button>(id).setOnClickListener { btn ->
                val text = (btn as Button).text.toString()
                amountEditText.setText(amountEditText.text.toString() + text)
            }
        }

        view.findViewById<Button>(R.id.clear).setOnClickListener {
            amountEditText.setText("")
        }

        view.findViewById<ImageButton>(R.id.close_bttn).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.ok).setOnClickListener {
            sendBitcoinEditText.setText(amountEditText.text.toString())
            dialog.dismiss()
            convertAndSetValue()
        }

        dialog.show()
    }
}
