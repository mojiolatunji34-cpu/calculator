package com.diffy01.calculator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val history = CalculationHistory(this)
        val vault = SecureVault(this)
        val prefs = getSharedPreferences("calculator_settings", Context.MODE_PRIVATE)
        setContent { CalculatorApp(history, vault, prefs) }
    }
}

private val Accent = Color(0xFF6C7CFF)
private val Night = Color(0xFF0B1020)
private val Day = Color(0xFFF3F5FB)

@Composable
private fun CalculatorApp(store: CalculationHistory, vault: SecureVault, prefs: android.content.SharedPreferences) {
    var expression by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("0") }
    var error by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf(prefs.getBoolean("degrees", true)) }
    var dark by remember { mutableStateOf(prefs.getBoolean("dark", true)) }
    var scientific by remember { mutableStateOf(true) }
    var tab by remember { mutableStateOf("CALC") }
    var history by remember { mutableStateOf(store.load()) }
    var memory by remember { mutableDoubleStateOf(0.0) }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val bg = if (dark) Night else Day
    val surface = if (dark) Color(0xFF171F33) else Color.White
    val fg = if (dark) Color(0xFFF4F6FF) else Color(0xFF17223A)
    val muted = if (dark) Color(0xFF9AA8C4) else Color(0xFF68738A)
    val scheme = if (dark) darkColorScheme(primary=Accent, background=bg, surface=surface, onBackground=fg, onSurface=fg)
        else lightColorScheme(primary=Accent, background=bg, surface=surface, onBackground=fg, onSurface=fg)

    MaterialTheme(colorScheme=scheme) {
        Surface(Modifier.fillMaxSize(), color=bg) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal=14.dp)) {
                Row(Modifier.fillMaxWidth().padding(vertical=12.dp), verticalAlignment=Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("DIFFY 01", color=Accent, fontSize=11.sp, fontWeight=FontWeight.ExtraBold, letterSpacing=2.sp)
                        Text("Scientific Suite", color=fg, fontSize=23.sp, fontWeight=FontWeight.Bold)
                    }
                    Text(if(degree) "DEG" else "RAD", color=fg, modifier=Modifier.background(surface, RoundedCornerShape(12.dp)).clickable { degree=!degree; prefs.edit().putBoolean("degrees",degree).apply() }.padding(12.dp))
                }
                Row(Modifier.fillMaxWidth().padding(bottom=10.dp), horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    listOf("CALC","TOOLS","GRAPH","VAULT","SETTINGS").forEach { item ->
                        val selected = tab == item
                        val tint by animateColorAsState(if(selected) Accent else surface, label="tab")
                        Surface(Modifier.weight(1f).clickable { tab=item }, color=tint, shape=RoundedCornerShape(12.dp)) {
                            Text(item, color=if(selected) Color.White else muted, fontSize=9.sp, fontWeight=FontWeight.Bold, textAlign=TextAlign.Center, modifier=Modifier.padding(vertical=12.dp))
                        }
                    }
                }
                when(tab) {
                    "CALC" -> CalculatorScreen(expression, { expression=it; error="" }, answer, { answer=it }, error, { error=it }, degree, scientific, { scientific=it }, history, { history=it; store.save(it) }, memory, { memory=it }, clipboard, context, surface, fg, muted)
                    "TOOLS" -> ToolsScreen(surface, fg, muted)
                    "GRAPH" -> GraphScreen(surface, fg, muted, degree)
                    "VAULT" -> VaultScreen(vault, surface, fg, muted)
                    "SETTINGS" -> SettingsScreen(surface, fg, muted, dark, { dark=it; prefs.edit().putBoolean("dark",it).apply() }, degree, { degree=it; prefs.edit().putBoolean("degrees",it).apply() }, { history=emptyList(); store.clear() })
                }
            }
        }
    }
}

@Composable
private fun CalculatorScreen(
    expression:String, setExpression:(String)->Unit, answer:String, setAnswer:(String)->Unit,
    error:String, setError:(String)->Unit, degree:Boolean, scientific:Boolean, setScientific:(Boolean)->Unit,
    history:List<HistoryItem>, setHistory:(List<HistoryItem>)->Unit, memory:Double, setMemory:(Double)->Unit,
    clipboard:androidx.compose.ui.platform.ClipboardManager, context:Context, surface:Color, fg:Color, muted:Color
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(Modifier.fillMaxWidth().background(surface,RoundedCornerShape(24.dp)).padding(18.dp), horizontalAlignment=Alignment.End) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text(if(scientific) "SCIENTIFIC MODE" else "STANDARD MODE", color=muted, fontSize=10.sp, letterSpacing=1.5.sp, modifier=Modifier.weight(1f))
                Text(if(scientific) "STANDARD" else "SCIENTIFIC", color=Accent, fontSize=11.sp, fontWeight=FontWeight.Bold, modifier=Modifier.clickable { setScientific(!scientific) }.padding(6.dp))
            }
            Text(expression.ifEmpty{"0"}, color=fg, fontSize=28.sp, textAlign=TextAlign.End, maxLines=3, modifier=Modifier.fillMaxWidth().padding(top=22.dp))
            Text(error.ifEmpty{answer}, color=if(error.isNotEmpty()) Color(0xFFFF6B75) else Accent, fontSize=35.sp, fontWeight=FontWeight.Bold, textAlign=TextAlign.End, maxLines=2, modifier=Modifier.fillMaxWidth().padding(top=8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.End) {
                SmallAction("COPY") { clipboard.setText(AnnotatedString(answer)) }
                SmallAction("SHARE") { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,"$expression = $answer"),"Share result")) }
                SmallAction("a/b") { runCatching { setAnswer(Fraction.fromDecimal(answer.toDouble()).toString()) }.onFailure { setError("Cannot convert this result to a fraction") } }
            }
        }
        Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(7.dp)) {
            listOf("MC","MR","M+","M-").forEach { key -> Key(key, Modifier.weight(1f), surface, fg, true) {
                when(key) {
                    "MC" -> setMemory(0.0)
                    "MR" -> setExpression(expression + AdvancedMath.format(memory))
                    "M+" -> setMemory(memory + (answer.toDoubleOrNull() ?: 0.0))
                    "M-" -> setMemory(memory - (answer.toDoubleOrNull() ?: 0.0))
                }
            } }
        }
        if(scientific) {
            val scienceRows = listOf(
                listOf("sin(","cos(","tan(","ln(","log("),
                listOf("asin(","acos(","atan(","sqrt(","cbrt("),
                listOf("sinh(","cosh(","tanh(","abs(","exp("),
                listOf("π","e","^","!","floor("),
                listOf("ceil(","("," ) ","%","⌫")
            )
            scienceRows.forEach { row -> KeyRow(row, surface, fg, muted) { key ->
                when(key.trim()) {
                    "π" -> setExpression(expression+"pi")
                    "e" -> setExpression(expression+"e")
                    "⌫" -> setExpression(expression.dropLast(1))
                    else -> setExpression(expression+key.trim())
                }
            } }
        }
        val rows = listOf(listOf("AC","(",")","÷"),listOf("7","8","9","×"),listOf("4","5","6","−"),listOf("1","2","3","+"),listOf("%","0",".","="))
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                row.forEach { key -> Key(key, Modifier.weight(1f), surface, fg, key in listOf("AC","=","÷","×","−","+")) {
                    when(key) {
                        "AC" -> { setExpression(""); setAnswer("0"); setError("") }
                        "⌫" -> setExpression(expression.dropLast(1))
                        "=" -> runCatching { CalculatorEngine.evaluate(expression,degree) }.onSuccess { v ->
                            val formatted=AdvancedMath.format(v)
                            setAnswer(formatted); setError("")
                            setHistory(listOf(HistoryItem(expression,formatted))+history)
                        }.onFailure { setError(it.message ?: "Check your expression") }
                        else -> setExpression(expression + when(key){"×"->"*";"÷"->"/";"−"->"-";else->key})
                    }
                    if(key!="=" && key!="AC") setError("")
                } }
            }
        }
        Text("Offline • Calculation history saved on this device", color=muted, fontSize=10.sp, modifier=Modifier.align(Alignment.CenterHorizontally).padding(12.dp))
        Text("RECENT", color=muted, fontSize=10.sp, fontWeight=FontWeight.Bold, letterSpacing=1.5.sp)
        history.take(4).forEach { item ->
            Row(Modifier.fillMaxWidth().clickable { setExpression(item.expression); setAnswer(item.result); setError("") }.padding(vertical=7.dp), verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(item.expression,color=muted,fontSize=12.sp); Text(item.result,color=fg,fontSize=16.sp,fontWeight=FontWeight.SemiBold) }
                Text("USE",color=Accent,fontSize=10.sp)
            }
        }
    }
}

@Composable private fun KeyRow(keys:List<String>, surface:Color, fg:Color, muted:Color, onKey:(String)->Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        keys.forEach { key -> Key(key.trim(),Modifier.weight(1f),surface,fg,false,onClick={onKey(key)}) }
    }
}
@Composable private fun Key(label:String, modifier:Modifier, surface:Color, fg:Color, action:Boolean, onClick:()->Unit) {
    val bg=if(label=="=" || label=="AC") Accent else surface
    Surface(modifier.padding(vertical=3.dp).height(48.dp).clickable { onClick() }, color=bg, shape=RoundedCornerShape(14.dp), tonalElevation=if(action) 2.dp else 0.dp) {
        Box(contentAlignment=Alignment.Center) { Text(label,color=if(label=="="||label=="AC") Color.White else if(action) Accent else fg,fontSize=if(label.length>5) 11.sp else 17.sp,fontWeight=FontWeight.SemiBold) }
    }
}
@Composable private fun SmallAction(label:String,onClick:()->Unit) {
    Text(label,color=Accent,fontSize=10.sp,fontWeight=FontWeight.Bold,modifier=Modifier.clickable { onClick() }.padding(start=14.dp,top=12.dp))
}

@Composable
private fun ToolsScreen(surface:Color,fg:Color,muted:Color) {
    var tool by remember { mutableStateOf("EQUATIONS") }
    var a by remember { mutableStateOf("1") }; var b by remember { mutableStateOf("5") }; var c by remember { mutableStateOf("6") }
    var values by remember { mutableStateOf("4, 7, 7, 2, 9") }
    var n by remember { mutableStateOf("5") }; var r by remember { mutableStateOf("2") }
    var amount by remember { mutableStateOf("1") }; var from by remember { mutableStateOf("km") }; var to by remember { mutableStateOf("m") }
    var dateA by remember { mutableStateOf("2026-01-01") }; var dateB by remember { mutableStateOf("2026-12-31") }
    var startTime by remember { mutableStateOf("09:00") }; var endTime by remember { mutableStateOf("17:30") }
    var z1r by remember { mutableStateOf("3") }; var z1i by remember { mutableStateOf("2") }
    var z2r by remember { mutableStateOf("1") }; var z2i by remember { mutableStateOf("-4") }
    var matrixA by remember { mutableStateOf("1,2;3,4") }; var matrixB by remember { mutableStateOf("5,6;7,8") }
    var output by remember { mutableStateOf("Choose a tool and enter values.") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("ADVANCED TOOLS",color=fg,fontSize=19.sp,fontWeight=FontWeight.Bold)
        Row(Modifier.fillMaxWidth().padding(vertical=10.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            listOf("EQUATIONS","STATISTICS","nPr / nCr","MATRIX","COMPLEX","CONVERTER","DATE","TIME").forEach { item ->
                Surface(Modifier.clickable { tool=item },color=if(tool==item) Accent else surface,shape=RoundedCornerShape(10.dp)) {
                    Text(item,color=if(tool==item) Color.White else muted,fontSize=9.sp,modifier=Modifier.padding(9.dp))
                }
            }
        }
        when(tool) {
            "EQUATIONS" -> {
                Text("Quadratic equation: ax² + bx + c = 0",color=muted,fontSize=13.sp)
                Field("Coefficient a",a){a=it}; Field("Coefficient b",b){b=it}; Field("Coefficient c",c){c=it}
                PrimaryAction("SOLVE EQUATION") { output=runCatching { AdvancedMath.solveQuadratic(a.toDouble(),b.toDouble(),c.toDouble()) }.getOrElse { it.message ?: "Invalid coefficients" } }
                PrimaryAction("SOLVE LINEAR ax + b = 0") { output=runCatching { AdvancedMath.solveLinear(a.toDouble(),b.toDouble()) }.getOrElse { it.message ?: "Invalid coefficients" } }
            }
            "STATISTICS" -> {
                Text("Enter numbers separated by commas",color=muted,fontSize=13.sp); Field("Dataset",values){values=it}
                PrimaryAction("ANALYZE DATA") { output=runCatching { AdvancedMath.statistics(values.split(",").map { it.trim().toDouble() }).entries.joinToString("\n") { "${it.key}: ${AdvancedMath.format(it.value)}" } }.getOrElse { it.message ?: "Invalid dataset" } }
            }
            "nPr / nCr" -> {
                Field("n",n){n=it}; Field("r",r){r=it}
                PrimaryAction("CALCULATE") { output=runCatching { val nn=n.toInt(); val rr=r.toInt(); "Permutation nPr = ${AdvancedMath.format(AdvancedMath.permutation(nn,rr))}\nCombination nCr = ${AdvancedMath.format(AdvancedMath.combination(nn,rr))}" }.getOrElse { it.message ?: "Enter whole numbers" } }
            }
            "MATRIX" -> {
                Text("Enter square matrices. Separate columns with commas and rows with semicolons.",color=muted,fontSize=12.sp)
                Field("Matrix A",matrixA){matrixA=it}; Field("Matrix B",matrixB){matrixB=it}
                PrimaryAction("DETERMINANT OF A") { output=runCatching { val m=matrixA.split(";").map { row -> row.split(",").map { it.trim().toDouble() } }; "det(A) = ${AdvancedMath.format(AdvancedMath.determinant(m))}" }.getOrElse { it.message ?: "Check matrix A" } }
                PrimaryAction("MULTIPLY A × B") { output=runCatching { val left: List<List<Double>> = parseMatrixInput(matrixA); val right: List<List<Double>> = parseMatrixInput(matrixB); val product: List<List<Double>> = AdvancedMath.multiply(left, right); product.joinToString("\n") { row: List<Double> -> row.joinToString("    ") { value: Double -> AdvancedMath.format(value) } } }.getOrElse { failure -> failure.message ?: "Check matrix dimensions" } }
            }
            "COMPLEX" -> {
                Text("Complex numbers z = a + bi",color=muted,fontSize=13.sp)
                Field("z1 real part",z1r){z1r=it}; Field("z1 imaginary part",z1i){z1i=it}
                Field("z2 real part",z2r){z2r=it}; Field("z2 imaginary part",z2i){z2i=it}
                PrimaryAction("CALCULATE COMPLEX OPERATIONS") {
                    output=runCatching {
                        val x=ComplexNumber(z1r.toDouble(),z1i.toDouble())
                        val y=ComplexNumber(z2r.toDouble(),z2i.toDouble())
                        "z1 + z2 = ${(x+y).format()}\nz1 - z2 = ${(x-y).format()}\nz1 * z2 = ${(x*y).format()}\nz1 / z2 = ${(x/y).format()}\n|z1| = ${AdvancedMath.format(x.magnitude())}\nconjugate(z1) = ${x.conjugate().format()}"
                    }.getOrElse { it.message ?: "Check complex-number values" }
                }
            }
            "CONVERTER" -> {
                Text("Length, mass, volume and temperature",color=muted,fontSize=13.sp)
                Field("Value",amount){amount=it}; Field("From unit (km, m, cm, mm, mi, ft, in, kg, g, lb, oz, L, mL, °C, °F, K)",from){from=it}; Field("To unit",to){to=it}
                PrimaryAction("CONVERT") { output=runCatching { AdvancedMath.format(AdvancedMath.convert(amount.toDouble(),from.trim(),to.trim()))+" "+to }.getOrElse { it.message ?: "Invalid conversion" } }
            }
            "DATE" -> {
                Text("Dates use YYYY-MM-DD format",color=muted,fontSize=13.sp); Field("Start date",dateA){dateA=it}; Field("End date",dateB){dateB=it}
                PrimaryAction("CALCULATE DAYS") { output=runCatching { val f=SimpleDateFormat("yyyy-MM-dd",Locale.US).apply { isLenient=false }; val d1=f.parse(dateA)!!; val d2=f.parse(dateB)!!; val days=(d2.time-d1.time)/86400000L; "${abs(days)} days between dates" }.getOrElse { "Use valid dates in YYYY-MM-DD format" } }
            }
            "TIME" -> {
                Text("Calculate elapsed time using 24-hour HH:mm format",color=muted,fontSize=13.sp)
                Field("Start time",startTime){startTime=it}; Field("End time",endTime){endTime=it}
                PrimaryAction("CALCULATE DURATION") { output=runCatching { val f=SimpleDateFormat("HH:mm",Locale.US).apply { isLenient=false }; val start=f.parse(startTime)!!; val end=f.parse(endTime)!!; var minutes=(end.time-start.time)/60000L; if(minutes<0) minutes+=24*60; "${minutes/60} hours ${minutes%60} minutes (${minutes} minutes)" }.getOrElse { "Use valid 24-hour times, e.g. 09:30" } }
            }
        }
        ResultPanel(output,surface,fg)
        Text("SCIENTIFIC CONSTANTS",color=fg,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=18.dp))
        listOf("π  = 3.141592653589793","e  = 2.718281828459045","c  = 299,792,458 m/s","g  = 9.80665 m/s²","h  = 6.62607015 × 10⁻³⁴ J·s","Nₐ = 6.02214076 × 10²³ mol⁻¹").forEach { Text(it,color=muted,fontSize=13.sp,modifier=Modifier.padding(vertical=5.dp)) }
        Text("Fraction tools are available from the calculator result using a/b. Matrix determinant and multiplication are provided by the maths engine and are being expanded into a dedicated matrix workspace.",color=muted,fontSize=11.sp,modifier=Modifier.padding(vertical=12.dp))
    }
}

@Composable
private fun GraphScreen(surface:Color,fg:Color,muted:Color,degree:Boolean) {
    var function by remember { mutableStateOf("sin(x)") }
    var range by remember { mutableStateOf("10") }
    Text("FUNCTION GRAPH",color=fg,fontSize=19.sp,fontWeight=FontWeight.Bold)
    Text("Enter a function using x, e.g. x^2, sin(x), or x^3-2*x",color=muted,fontSize=12.sp,modifier=Modifier.padding(vertical=8.dp))
    Field("f(x)",function){function=it}; Field("X range (+/-)",range){range=it}
    Surface(color=surface,shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth().height(260.dp).padding(vertical=10.dp)) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val w=size.width; val h=size.height
            drawLine(muted.copy(alpha=.5f),Offset(0f,h/2),Offset(w,h/2),1.dp.toPx())
            drawLine(muted.copy(alpha=.5f),Offset(w/2,0f),Offset(w/2,h),1.dp.toPx())
            val limit=range.toFloatOrNull()?.coerceIn(1f,100f) ?: 10f
            val path=Path(); var started=false
            for(i in 0..300) {
                val x=-limit+2*limit*i/300
                val y=runCatching { CalculatorEngine.evaluate(function.replace(Regex("\\bx\\b"),"($x)"),degree).toFloat() }.getOrNull()
                if(y!=null && y.isFinite() && abs(y)<limit*10) {
                    val px=i/300f*w; val py=h/2-y/(2*limit)*h
                    if(!started){path.moveTo(px,py);started=true}else path.lineTo(px,py)
                } else started=false
            }
            drawPath(path,Accent,style=androidx.compose.ui.graphics.drawscope.Stroke(width=2.dp.toPx()))
        }
    }
    Text(if(degree) "Trigonometric functions use degrees in this graph." else "Trigonometric functions use radians in this graph.",color=muted,fontSize=11.sp)
}

@Composable
private fun VaultScreen(vault:SecureVault,surface:Color,fg:Color,muted:Color) {
    var pin by remember { mutableStateOf("") }; var unlocked by remember { mutableStateOf(false) }
    var configured by remember { mutableStateOf(vault.isConfigured()) }
    var title by remember { mutableStateOf("") }; var secret by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(emptyList<VaultEntry>()) }; var message by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("PRIVATE VAULT",color=fg,fontSize=19.sp,fontWeight=FontWeight.Bold)
        Text("Encrypted on this device. Use a PIN you do not use elsewhere. This is a simple private notes vault, not a replacement for a dedicated password manager.",color=muted,fontSize=12.sp,modifier=Modifier.padding(vertical=10.dp))
        if(!unlocked) {
            Field(if(configured) "Enter your vault PIN" else "Create a 6–12 digit PIN",pin){pin=it.filter(Char::isDigit).take(12)}
            PrimaryAction(if(configured) "UNLOCK VAULT" else "CREATE PRIVATE VAULT") {
                runCatching {
                    if(!configured) { vault.setPin(pin.toCharArray()); configured=true; unlocked=true; message="Vault created. Keep your PIN safe." }
                    else if(vault.verifyPin(pin.toCharArray())) { entries=vault.readEntries(); unlocked=true; message="Vault unlocked." }
                    else message="Incorrect PIN. Try again."
                    pin=""
                }.onFailure { message=it.message ?: "Could not open vault" }
            }
        } else {
            Row(verticalAlignment=Alignment.CenterVertically) { Text("Your private items",color=fg,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f)); Text("LOCK",color=Accent,modifier=Modifier.clickable { unlocked=false; entries=emptyList() }.padding(8.dp)) }
            Field("Item title",title){title=it}; Field("Private note or secret",secret){secret=it}
            PrimaryAction("SAVE ENCRYPTED ITEM") {
                if(title.isBlank()||secret.isBlank()) message="Enter both a title and content."
                else { entries=entries+VaultEntry(title,secret); vault.writeEntries(entries); title="";secret="";message="Saved securely on this device." }
            }
            entries.forEachIndexed { index,item ->
                Column(Modifier.fillMaxWidth().background(surface,RoundedCornerShape(14.dp)).padding(12.dp)) {
                    Text(item.title,color=fg,fontWeight=FontWeight.Bold)
                    Text(item.secret,color=muted,fontSize=13.sp)
                    Text("DELETE",color=Color(0xFFFF737D),fontSize=10.sp,modifier=Modifier.align(Alignment.End).clickable { entries=entries.toMutableList().also { it.removeAt(index) }; vault.writeEntries(entries) }.padding(5.dp))
                }
                Spacer(Modifier.height(6.dp))
            }
        }
        if(message.isNotBlank()) Text(message,color=Accent,fontSize=12.sp,modifier=Modifier.padding(8.dp))
    }
}

@Composable
private fun SettingsScreen(surface:Color,fg:Color,muted:Color,dark:Boolean,onDark:(Boolean)->Unit,degree:Boolean,onDegree:(Boolean)->Unit,onClear:()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("PREFERENCES",color=fg,fontSize=19.sp,fontWeight=FontWeight.Bold)
        SettingRow("Dark appearance","Use the dark calculator theme",dark,onDark,fg,muted)
        SettingRow("Degree mode","Use degrees for trigonometry",degree,onDegree,fg,muted)
        PrimaryAction("CLEAR CALCULATION HISTORY",onClear)
        Text("Privacy",color=fg,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=18.dp))
        Text("Calculations and vault items are stored locally. The app does not need internet access for calculations. Keep your device protected and do not store highly sensitive credentials in a basic notes vault.",color=muted,fontSize=12.sp,modifier=Modifier.padding(vertical=8.dp))
        Text("DIFFY 01 • Scientific Suite • Version 1.1",color=muted,fontSize=11.sp,modifier=Modifier.padding(top=18.dp))
    }
}
@Composable private fun SettingRow(title:String,subtitle:String,value:Boolean,onChange:(Boolean)->Unit,fg:Color,muted:Color) {
    Row(Modifier.fillMaxWidth().padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title,color=fg,fontWeight=FontWeight.SemiBold); Text(subtitle,color=muted,fontSize=11.sp) }
        Switch(checked=value,onCheckedChange=onChange)
    }
}
@Composable private fun Field(label:String,value:String,onChange:(String)->Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical=5.dp)) {
        Text(label,color=MaterialTheme.colorScheme.onBackground,fontSize=11.sp)
        BasicTextField(value=value,onValueChange=onChange,modifier=Modifier.fillMaxWidth().padding(top=5.dp).background(MaterialTheme.colorScheme.surface,RoundedCornerShape(12.dp)).padding(13.dp),singleLine=true,textStyle=androidx.compose.ui.text.TextStyle(color=MaterialTheme.colorScheme.onSurface,fontSize=15.sp),decorationBox={ inner -> if(value.isEmpty()) Text("Enter value",color=MaterialTheme.colorScheme.onSurface.copy(alpha=.45f)); inner() })
    }
}
@Composable private fun PrimaryAction(label:String,onClick:()->Unit) {
    Button(onClick=onClick,modifier=Modifier.fillMaxWidth().padding(vertical=5.dp),shape=RoundedCornerShape(13.dp)) { Text(label,fontSize=11.sp,fontWeight=FontWeight.Bold) }
}
@Composable private fun ResultPanel(text:String,surface:Color,fg:Color) {
    Column(Modifier.fillMaxWidth().padding(top=10.dp).background(surface,RoundedCornerShape(14.dp)).padding(14.dp)) {
        Text("RESULT",color=Accent,fontSize=10.sp,fontWeight=FontWeight.Bold)
        Text(text,color=fg,fontSize=14.sp,modifier=Modifier.padding(top=5.dp))
    }
}


private fun parseMatrixInput(input: String): List<List<Double>> = input.split(";").map { row -> row.split(",").map { cell -> cell.trim().toDouble() } }
