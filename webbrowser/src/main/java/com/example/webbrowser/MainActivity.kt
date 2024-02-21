package com.example.webbrowser

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.SemanticsProperties.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.webbrowser.ui.theme.JetpackComposePracticeTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel= viewModel<MainViewModel>()
            HomeScreen(viewModel = viewModel)
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val focusManager = LocalFocusManager.current

    val (inputUrl, setUrl) = rememberSaveable {
        mutableStateOf("https://google.com")
    }
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "나만의 웹 브라우저") },
                actions = {
                    IconButton(onClick = {
                        viewModel.undo()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back",
                            tint = Color.White,
                        )
                    }
                    IconButton(onClick = {
                        viewModel.redo()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "forword",
                            tint = Color.White,
                        )
                    }
                }
            )
        },
        scaffoldState = scaffoldState
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = inputUrl,
                onValueChange = setUrl,
                label = {Text("https://")},
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    viewModel.url.value = inputUrl
                    focusManager.clearFocus()
                })
            )

            Spacer(modifier = Modifier.height(16.dp))

            MyWebView(viewModel = viewModel, scaffoldState = scaffoldState)
        }
    }
}

@Composable
fun MyWebView(viewModel: MainViewModel, scaffoldState: ScaffoldState) {
    val webView = rememberWebView()
    LaunchedEffect(Unit){
        viewModel.undoSharedFlow.collectLatest {
            if(webView.canGoBack()){
                webView.goBack()
            }else{
                scaffoldState.snackbarHostState.showSnackbar("더 이상 뒤로 갈 수 없음")
            }
        }
    }

    LaunchedEffect(Unit){
        viewModel.redoSharedFlow.collectLatest {
            if(webView.canGoForward()){
                webView.goForward()
            }else{
                scaffoldState.snackbarHostState.showSnackbar("더 이상 앞ㅡ로 갈 수 없음")
            }
        }
    }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { webView},
        update = { webView ->
            webView.loadUrl(viewModel.url.value)
        }
    )
}

@Composable
fun rememberWebView(): WebView {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            loadUrl("https://google.com")
        }
    }
    return webView
}