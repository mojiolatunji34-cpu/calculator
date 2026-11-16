package com.diffy01.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = CalculationHistory(this)
        setContent { CalculatorApp(store) }
    }
}

private val Blue = Color(0xFF536DFE)
private val DarkBg = Color(0xFF10131C)
private val LightBg = Color(0xFFF4F6FB)

@Composable
private fun CalculatorApp(store: CalculationHistory) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }
    var error by remember { mutableStateOf("") }
    var degrees by remember { mutableStateOf(true) }
    var scientific by remember { mutableStateOf(true) }
    var dark by remember { mutableStateOf(true) }
    var showingHistory by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf(store.load()) }
    val bg = if (dark) DarkBg else LightBg
    val panel = if (dark) Color(0xFF1B2030) else Color.White
    val foreground = if (dark) Color(0xFFF4F6FF) else Color(0xFF172033)
    val muted = if (dark) Color(0xFF9AA5BD) else Color(0xFF68738A)
    val scheme = if (dark) darkColorScheme(primary = Blue, background = bg, surface = panel, onBackground = foreground, onSurface = foreground)
        else lightColorScheme(primary = Blue, background = bg, surface = panel, onBackground = foreground, onSurface = foreground)

    MaterialTheme(colorScheme = scheme) {
        Surface(Modifier.fillMaxSize(), color = bg) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 16.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("DIFFY 01", color = Blue, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text("Calculator", color = foreground, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(if (degrees) "DEG" else "RAD", color = foreground, modifier = Modifier.clickable { degrees = !degrees }.background(panel, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = 10.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (dark) "☀" else "☾", color = foreground, modifier = Modifier.clickable { dark = !dark }.background(panel, RoundedCornerShape(14.dp)).padding(10.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("↶", color = foreground, fontSize = 20.sp, modifier = Modifier.clickable { showingHistory = !showingHistory }.background(panel, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 7.dp))
                }

                if (showingHistory) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Calculation history", color = foreground, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("CLEAR", color = Blue, fontSize = 12.sp, modifier = Modifier.clickable { history = emptyList(); store.clear() }.padding(10.dp))
                        Text("CLOSE", color = muted, fontSize = 12.sp, modifier = Modifier.clickable { showingHistory = false }.padding(10.dp))
                    }
                    if (history.isEmpty()) Text("Your calculations will appear here.", color = muted, modifier = Modifier.padding(20.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(history) { item ->
                            Column(Modifier.fillMaxWidth().background(panel, RoundedCornerShape(14.dp)).clickable {
                                expression = item.expression
                                result = item.result
                                error = ""
                                showingHistory = false
                            }.padding(14.dp)) {
                                Text(item.expression, color = muted, fontSize = 14.sp)
                                Text(item.result, color = foreground, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.weight(0.12f))
                    Column(Modifier.fillMaxWidth().background(panel, RoundedCornerShape(24.dp)).padding(20.dp), horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (scientific) "SCIENTIFIC" else "STANDARD", color = muted, fontSize = 10.sp, letterSpacing = 1.5.sp, modifier = Modifier.weight(1f))
                            Text(if (scientific) "STANDARD" else "SCIENTIFIC", color = Blue, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { scientific = !scientific }.padding(6.dp))
                        }
                        Text(expression.ifEmpty { "0" }, color = foreground, fontSize = 30.sp, textAlign = TextAlign.End, maxLines = 2, modifier = Modifier.fillMaxWidth().padding(top = 20.dp))
                        Text(error.ifEmpty { result }, color = if (error.isNotEmpty()) Color(0xFFFF7070) else Blue, fontSize = 35.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, maxLines = 1, modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 8.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    if (scientific) {
                        val scientificRows = listOf(
                            listOf("sin(", "cos(", "tan(", "ln", "log"),
                            listOf("asin(", "acos(", "atan(", "sqrt(", "x²"),
                            listOf("π", "e", "(", ")", "xʸ"),
                            listOf("√", "∛", "!", "abs(", "⌫")
                        )
                        scientificRows.forEach { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                row.forEach { key -> KeyButton(key, Modifier.weight(1f), panel, foreground, muted, Blue) {
                                    when (key) {
                                        "⌫" -> { expression = expression.dropLast(1); error = "" }
                                        "x²" -> expression += "^2"
                                        "xʸ" -> expression += "^"
                                        "√" -> expression += "sqrt("
                                        "∛" -> expression += "cbrt("
                                        "!" -> expression += "!"
                                        "π" -> expression += "pi"
                                        "e" -> expression += "e"
                                        "ln", "log" -> expression += key + "("
                                        else -> expression += key
                                    }
                                    error = ""
                                } }
                            }
                            Spacer(Modifier.height(7.dp))
                        }
                    }
                    val rows = listOf(
                        listOf("AC", "(", ")", "÷"),
                        listOf("7", "8", "9", "×"),
                        listOf("4", "5", "6", "−"),
                        listOf("1", "2", "3", "+"),
                        listOf("%", "0", ".", "=")
                    )
                    rows.forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            row.forEach { key ->
                                val isAction = key in listOf("AC", "=", "÷", "×", "−", "+")
                                KeyButton(key, Modifier.weight(1f), panel, foreground, muted, Blue, isAction) {
                                    when (key) {
                                        "AC" -> { expression = ""; result = "0"; error = "" }
                                        "=" -> {
                                            runCatching { CalculatorEngine.evaluate(expression, degrees) }
                                                .onSuccess { value ->
                                                    val formatted = formatNumber(value)
                                                    history = listOf(HistoryItem(expression, formatted)) + history
                                                    store.save(history)
                                                    result = formatted
                                                    error = ""
                                                }
                                                .onFailure { error = it.message ?: "Invalid expression" }
                                        }
                                        else -> { expression += when (key) { "×" -> "*"; "÷" -> "/"; "−" -> "-"; else -> key }; error = "" }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(9.dp))
                    }
                    Text("Offline scientific calculator  •  Diffy 01", color = muted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 3.dp, bottom = 10.dp))
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    label: String,
    modifier: Modifier,
    panel: Color,
    foreground: Color,
    muted: Color,
    accent: Color,
    emphasized: Boolean = false,
    onClick: () -> Unit
) {
    val bg = if (label == "=" || label == "AC") accent else panel
    val color = if (label == "=" || label == "AC") Color.White else if (emphasized) accent else foreground
    Surface(modifier.height(52.dp).clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), color = bg, tonalElevation = if (emphasized) 2.dp else 0.dp) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = color, fontSize = if (label.length > 4) 13.sp else 19.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun formatNumber(value: Double): String {
    if (!value.isFinite()) return "Error"
    if (value == 0.0) return "0"
    val symbols = DecimalFormatSymbols(Locale.US)
    val pattern = if (kotlin.math.abs(value) >= 1e12 || kotlin.math.abs(value) < 1e-8) "0.##########E0" else "0.##########"
    return DecimalFormat(pattern, symbols).format(value)
}
