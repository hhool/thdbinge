package com.wtn.hdbinge

import android.annotation.SuppressLint
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.wtn.hdbinge.ui.theme.HdbingeTheme
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.net.URISyntaxException

public interface ApiService {
    @GET("/")
    suspend fun getHomePage(): Response<ResponseBody>
}

class MainActivity : ComponentActivity() {
    private lateinit var apiService: ApiService
    private lateinit var webView: WebView
    private val TAG = "MainActivity"
    private var url = "https://vidbinge.com"
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView)

        // Enable hardware acceleration
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        // Enable JavaScript and other settings
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = true
            allowContentAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            allowContentAccess = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
            databaseEnabled = true
        }

        // Set WebViewClient to handle errors
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                // output the URL to the log

                return if (url.startsWith("intent://") || url.startsWith("customscheme://")) {
                    // Handle custom URL schemes
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        startActivity(intent)
                        true
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                        false
                    }
                } else if (url.startsWith(url)) {
                    // Let WebView handle the URL
                    true
                } else {
                    // output the URL to the log
                    true
                }
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                return super.shouldInterceptRequest(view, request)
            }
        }

        // Set WebChromeClient to handle video playback
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(view, callback)
                // Handle showing custom view for video playback
            }

            override fun onHideCustomView() {
                super.onHideCustomView()
                // Handle hiding custom view for video playback
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                // Handle progress change
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                // Handle received title
            }

            override fun onReceivedIcon(view: WebView?, icon: android.graphics.Bitmap?) {
                super.onReceivedIcon(view, icon)
                // Handle received icon
            }

            override fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean) {
                super.onReceivedTouchIconUrl(view, url, precomposed)
                // Handle received touch icon URL
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: android.webkit.ValueCallback<Array<android.net.Uri>>?,
                fileChooserParams: android.webkit.WebChromeClient.FileChooserParams?
            ): Boolean {
                return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
                // Handle showing file chooser
            }

            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                return super.onConsoleMessage(consoleMessage)
                // Handle console message
            }

            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
                return super.onJsAlert(view, url, message, result)
                // Handle JavaScript alert
            }

            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
                return super.onJsConfirm(view, url, message, result)
                // Handle JavaScript confirm
            }

            override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: android.webkit.JsPromptResult?): Boolean {
                return super.onJsPrompt(view, url, message, defaultValue, result)
                // Handle JavaScript prompt
            }

            override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                super.onPermissionRequest(request)
                // Handle permission request
            }

            override fun onPermissionRequestCanceled(request: android.webkit.PermissionRequest?) {
                super.onPermissionRequestCanceled(request)
                // Handle permission request canceled
            }

            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: android.webkit.GeolocationPermissions.Callback?) {
                super.onGeolocationPermissionsShowPrompt(origin, callback)
                // Handle geolocation permissions show prompt
            }

            override fun onGeolocationPermissionsHidePrompt() {
                super.onGeolocationPermissionsHidePrompt()
                // Handle geolocation permissions hide prompt
            }
        }
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .method(original.method(), original.body())
                .header("Origin", "https://www.proxy.com")
            val request = requestBuilder.build()
            chain.proceed(request)
        }.build()

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Load content
        loadContent()
    }

    private fun loadContent() {
        lifecycleScope.launch {
            try {
                val response = apiService.getHomePage()
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        val htmlContent = body.string()
                        webView.loadDataWithBaseURL(url, htmlContent, "text/html", "UTF-8", null)
                    }
                } else {
                    Log.e("MainActivity", "Error response code: ${response.code()}")
                    Log.e("MainActivity", "Error response message: ${response.message()}")
                    when (response.code()) {
                        400 -> loadLocalPageWithCode(400)
                        401 -> loadLocalPageWithCode(401)
                        402 -> loadLocalPageWithCode(402)
                        403 -> {
                            if (response.message().contains("Cloudflare")) {
                                // process human verification

                            } else
                                loadLocalPageWithCode(403)
                            }
                        404 -> loadLocalPageWithCode(404)
                        500 -> loadLocalPageWithCode(500)
                        else -> loadLocalGenericErrorPage()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Exception: ${e.message}")
                loadLocalGenericErrorPage()
            }
        }
    }

    private fun loadLocalGenericErrorPage() {
        webView.loadUrl("file:///android_asset/error.html")
    }

    private fun loadLocalPageWithCode(code: Int) {
        webView.loadUrl("file:///android_asset/$code.html")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HdbingeTheme {
        Greeting("Android")
    }
}