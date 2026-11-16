package com.diffy01.calculator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.VideoView
import android.widget.MediaController
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        window.decorView.systemUiVisibility = 0
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
    var calculationSteps by remember { mutableStateOf(emptyList<String>()) }
    var error by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf(prefs.getBoolean("degrees", true)) }
    var dark by remember { mutableStateOf(prefs.getBoolean("dark", true)) }
    var scientific by remember { mutableStateOf(false) }
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

        Surface(Modifier.fillMaxSize(), color=Color.Black) {
            if(tab=="CALC") {
                CalculatorScreen(
                    expression, { expression=it; error=""; calculationSteps=emptyList() },
                    answer, { answer=it }, error, { error=it },
                    calculationSteps, { calculationSteps=it }, degree, scientific, { scientific=it },
                    history, { history=it; store.save(it) }, memory, { memory=it },
                    clipboard, context, Color(0xFF101418), Color(0xFFAFCBF4), Color(0xFF9AA8C4),
                    onOpenTools={ tab="TOOLS" }
                )
            } else {
                Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal=14.dp)) {
                    Row(Modifier.fillMaxWidth().padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
                        Text("‹",color=fg,fontSize=28.sp,modifier=Modifier.clickable { tab="CALC" }.padding(end=12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("DIFFY 01",color=Accent,fontSize=11.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=2.sp)
                            Text("Scientific Suite",color=fg,fontSize=23.sp,fontWeight=FontWeight.Bold)
                        }
                        Text(if(degree) "DEG" else "RAD",color=fg,modifier=Modifier.background(surface,RoundedCornerShape(12.dp)).clickable { degree=!degree; prefs.edit().putBoolean("degrees",degree).apply() }.padding(12.dp))
                    }
                    Row(Modifier.fillMaxWidth().padding(bottom=10.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                        listOf("TOOLS","GRAPH","VAULT","SETTINGS").forEach { item ->
                            val selected=tab==item
                            Surface(Modifier.weight(1f).clickable { tab=item },color=if(selected) Accent else surface,shape=RoundedCornerShape(12.dp)) {
                                Text(item,color=if(selected) Color.White else muted,fontSize=9.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center,modifier=Modifier.padding(vertical=12.dp))
                            }
                        }
                    }
                    when(tab) {
                        "TOOLS" -> ToolsScreen(surface,fg,muted)
                        "GRAPH" -> GraphScreen(surface,fg,muted,degree)
                        "VAULT" -> VaultScreen(vault,surface,fg,muted,initiallyUnlocked=false)
                        "SETTINGS" -> SettingsScreen(surface,fg,muted,dark,{dark=it;prefs.edit().putBoolean("dark",it).apply()},degree,{degree=it;prefs.edit().putBoolean("degrees",it).apply()},{history=emptyList();store.clear()},vault)
                    }
                }
            }
        }
}
}

@Composable
private fun CalculatorScreen(
    expression:String, setExpression:(String)->Unit, answer:String, setAnswer:(String)->Unit,
    error:String, setError:(String)->Unit, steps:List<String>, setSteps:(List<String>)->Unit, degree:Boolean, scientific:Boolean, setScientific:(Boolean)->Unit,
    history:List<HistoryItem>, setHistory:(List<HistoryItem>)->Unit, memory:Double, setMemory:(Double)->Unit,
    clipboard:androidx.compose.ui.platform.ClipboardManager, context:Context, surface:Color, fg:Color, muted:Color,
    onOpenTools:()->Unit
) {
    Column(Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal=18.dp), verticalArrangement=Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth().padding(top=12.dp,bottom=5.dp),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("DIFFY 01",color=Accent,fontSize=11.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=2.2.sp)
                Text("SCIENTIFIC CALCULATOR",color=fg,fontSize=16.sp,fontWeight=FontWeight.Bold,letterSpacing=.6.sp)
            }
            Surface(color=surface,shape=RoundedCornerShape(12.dp)) {
                Text(if(degree) "DEG MODE" else "RAD MODE",color=Accent,fontSize=9.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(horizontal=11.dp,vertical=9.dp))
            }
        }
        Surface(color=surface,shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth().weight(1f).padding(vertical=4.dp)) {
            Column(Modifier.fillMaxSize().padding(horizontal=18.dp,vertical=16.dp),verticalArrangement=Arrangement.Bottom,horizontalAlignment=Alignment.End) {
                Text("EXPRESSION",color=Accent,fontSize=9.sp,fontWeight=FontWeight.Bold,modifier=Modifier.fillMaxWidth())
                if(expression.isNotEmpty()) Text(expression,color=muted,fontSize=24.sp,textAlign=TextAlign.End,maxLines=3,modifier=Modifier.fillMaxWidth().padding(top=8.dp))
                Text(if(error.isNotEmpty()) error else if(expression.isEmpty()) "0" else answer,color=if(error.isNotEmpty()) Color(0xFFFF737D) else fg,fontSize=if(answer.length>12) 34.sp else 46.sp,fontWeight=FontWeight.SemiBold,textAlign=TextAlign.End,maxLines=2,modifier=Modifier.fillMaxWidth().padding(top=8.dp))
            }
        }
        if(steps.isNotEmpty()) Column(Modifier.fillMaxWidth().background(surface,RoundedCornerShape(12.dp)).padding(8.dp)) {
            Text("WORKING",color=Accent,fontSize=9.sp,fontWeight=FontWeight.Bold)
            steps.take(3).forEach { Text(it,color=muted,fontSize=10.sp) }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal=6.dp,vertical=4.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
            SmallAction("◷") { if(history.isNotEmpty()) { setExpression(history.first().expression);setAnswer(history.first().result);setError("");setSteps(emptyList()) } }
            SmallAction("▥") { onOpenTools() }
            SmallAction("√π") { setScientific(!scientific) }
            SmallAction("⌫") { if(expression.isNotEmpty()) { setExpression(expression.dropLast(1));setError("") } }
        }
        Divider(color=muted.copy(alpha=.35f),thickness=1.dp)
        if(scientific) {
            val scienceRows=listOf(listOf("π",if(degree) "Deg" else "Rad","√","|x|"),listOf("sin","cos","tan","π"),listOf("ln","log","1/x","e"),listOf("eˣ","x²","xʸ",""))
            scienceRows.forEach { row -> Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    if(key.isEmpty()) Spacer(Modifier.weight(1f))
                    else Key(key,Modifier.weight(1f),surface,fg,false,compact=true) {
                        when(key) {
                            "π" -> setExpression(expression+"pi")
                            "Deg","Rad" -> Unit
                            "√" -> setExpression(expression+"sqrt(")
                            "|x|" -> setExpression(expression+"abs(")
                            "sin","cos","tan","ln","log" -> setExpression(expression+key+"(")
                            "π" -> setExpression(expression+"pi")
                            "e" -> setExpression(expression+"e")
                            "1/x" -> setExpression("1/("+expression+")")
                            "eˣ" -> setExpression(expression+"exp(")
                            "x²" -> setExpression(expression+"^2")
                            "xʸ" -> setExpression(expression+"^")
                        }
                        setError("")
                    }
                }
            } }
        }
        val rows=listOf(listOf("C","()","%","÷"),listOf("7","8","9","×"),listOf("4","5","6","−"),listOf("1","2","3","+"),listOf("+/−","0",".","="))
        rows.forEach { row -> Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            row.forEach { key -> Key(key,Modifier.weight(1f),surface,fg,key in listOf("C","=","÷","×","−","+"),compact=scientific) {
                when(key) {
                    "C" -> { setExpression("");setAnswer("0");setError("");setSteps(emptyList()) }
                    "()" -> setExpression(expression+if(expression.count{it=='('}>expression.count{it==')'}) ")" else "(")
                    "%" -> setExpression(expression+"%")
                    "+/−" -> setExpression(if(expression.startsWith("-")) expression.drop(1) else "-("+expression+")")
                    "=" -> {
                        runCatching { CalculatorEngine.evaluateWithSteps(expression,degree) }.onSuccess { calculation ->
                            val formatted=AdvancedMath.format(calculation.value)
                            setAnswer(formatted);setError("");setSteps(calculation.steps)
                            setHistory(listOf(HistoryItem(expression,formatted))+history)
                        }.onFailure { setError(it.message ?: "Check your expression");setSteps(emptyList()) }
                    }
                    else -> setExpression(expression+when(key){"×"->"*";"÷"->"/";"−"->"-";else->key})
                }
                if(key!="=" && key!="C") setError("")
            } }
        } }

    }
}


@Composable
private fun Key(label:String,modifier:Modifier,surface:Color,fg:Color,action:Boolean,compact:Boolean=false,onClick:()->Unit) {
    val bg=if(label=="=") Accent else if(action) Color(0xFF202B43) else surface
    Surface(
        modifier=modifier.padding(vertical=if(compact) 2.dp else 3.dp)
            .height(if(compact) 46.dp else 70.dp)
            .semantics { contentDescription="$label key"; role=Role.Button }
            .clickable { onClick() },
        color=bg,
        shape=if(compact) RoundedCornerShape(30.dp) else RoundedCornerShape(50),
        tonalElevation=0.dp
    ) {
        Box(contentAlignment=Alignment.Center) {
            Text(label,color=if(label=="=") Color.White else if(action) Color(0xFFB9C5FF) else Color(0xFFAFCBF4),
                fontSize=if(compact) 17.sp else 27.sp,fontWeight=FontWeight.Medium)
        }
    }
}

@Composable
private fun SmallAction(label:String,onClick:()->Unit) {
    Text(label,color=Color(0xFFAFCBF4),fontSize=if(label.length>3) 10.sp else 22.sp,
        fontWeight=FontWeight.Medium,modifier=Modifier.clickable { onClick() }.padding(horizontal=10.dp,vertical=8.dp))
}

@Composable
private fun ToolsScreen(surface:Color,fg:Color,muted:Color) {
    var tool by remember { mutableStateOf("EQUATIONS") }
    var a by remember { mutableStateOf("1") }; var b by remember { mutableStateOf("5") }; var c by remember { mutableStateOf("6") }
    var values by remember { mutableStateOf("4, 7, 7, 2, 9") }
    var n by remember { mutableStateOf("5") }; var r by remember { mutableStateOf("2") }
    var amount by remember { mutableStateOf("1") }; var from by remember { mutableStateOf("km") }; var to by remember { mutableStateOf("m") }; var precision by remember { mutableStateOf("6") }
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
                var scalar by remember { mutableStateOf("2") }
                var exponent by remember { mutableStateOf("2") }
                Text("Rows use semicolons; columns use commas. Example: 1,2;3,4. Rectangular matrices are supported where valid.",color=muted,fontSize=12.sp)
                Field("Matrix A",matrixA){matrixA=it}
                Field("Matrix B",matrixB){matrixB=it}
                Field("Scalar k",scalar){scalar=it}
                Field("Power n (0–8)",exponent){exponent=it}
                PrimaryAction("A + B") { output=runCatching { formatMatrix(AdvancedMath.addMatrices(parseMatrixInput(matrixA),parseMatrixInput(matrixB))) }.getOrElse { it.message ?: "Check dimensions" } }
                PrimaryAction("A − B") { output=runCatching { formatMatrix(AdvancedMath.subtractMatrices(parseMatrixInput(matrixA),parseMatrixInput(matrixB))) }.getOrElse { it.message ?: "Check dimensions" } }
                PrimaryAction("A × B") { output=runCatching { formatMatrix(AdvancedMath.multiply(parseMatrixInput(matrixA),parseMatrixInput(matrixB))) }.getOrElse { it.message ?: "Check dimensions" } }
                PrimaryAction("k × A") { output=runCatching { formatMatrix(AdvancedMath.scaleMatrix(parseMatrixInput(matrixA),scalar.toDouble())) }.getOrElse { it.message ?: "Enter a valid scalar" } }
                PrimaryAction("A POWER n") { output=runCatching { formatMatrix(AdvancedMath.matrixPower(parseMatrixInput(matrixA),exponent.toInt())) }.getOrElse { it.message ?: "Power requires square A and integer 0–8" } }
                PrimaryAction("DETERMINANT OF A") { output=runCatching { "det(A) = ${AdvancedMath.format(AdvancedMath.determinant(parseMatrixInput(matrixA)))}" }.getOrElse { it.message ?: "Enter a square matrix" } }
                PrimaryAction("INVERSE OF A") { output=runCatching { formatMatrix(AdvancedMath.inverse(parseMatrixInput(matrixA))) }.getOrElse { it.message ?: "Matrix is singular or not square" } }
                PrimaryAction("TRANSPOSE A") { output=runCatching { formatMatrix(AdvancedMath.transpose(parseMatrixInput(matrixA))) }.getOrElse { it.message ?: "Check matrix A" } }
                PrimaryAction("RANK OF A") { output=runCatching { "rank(A) = ${AdvancedMath.rank(parseMatrixInput(matrixA))}" }.getOrElse { it.message ?: "Check matrix A" } }
                PrimaryAction("TRACE OF A") { output=runCatching { "trace(A) = ${AdvancedMath.format(AdvancedMath.trace(parseMatrixInput(matrixA)))}" }.getOrElse { it.message ?: "Trace requires square A" } }
                PrimaryAction("SYMMETRIC EIGENVALUES") { output=runCatching { AdvancedMath.symmetricEigenvalues(parseMatrixInput(matrixA)).mapIndexed { index, value -> "λ${index+1} = ${AdvancedMath.format(value)}" }.joinToString("\n") }.getOrElse { it.message ?: "Enter a real symmetric matrix" } }
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
                Text("Length, mass, volume, temperature, time, pressure and energy",color=muted,fontSize=13.sp)
                Text("Units: m, km, cm, mm, mi, ft, in • kg, g, lb, oz • L, mL, μL, m³ • °C, °F, K • s, min, h, day • Pa, kPa, bar, atm • J, kJ, cal",color=muted,fontSize=10.sp)
                Field("Value",amount){amount=it}; Field("From unit",from){from=it}; Field("To unit",to){to=it}; Field("Decimal places (0-15)",precision){precision=it}
                PrimaryAction("CONVERT") { output=runCatching { AdvancedMath.format(AdvancedMath.convert(amount.toDouble(),from.trim(),to.trim()),precision.toInt())+" "+to }.getOrElse { it.message ?: "Invalid conversion" } }
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
        Text("Matrix tools support addition, subtraction, multiplication, scalar operations, powers, determinant, inverse, transpose, rank, trace and symmetric eigenvalues.",color=muted,fontSize=11.sp,modifier=Modifier.padding(vertical=12.dp))
    }
}

@Composable
private fun GraphScreen(surface:Color,fg:Color,muted:Color,degree:Boolean) {
    var function by remember { mutableStateOf("sin(x)") }
    var secondFunction by remember { mutableStateOf("x^2/10") }
    var range by remember { mutableStateOf("10") }
    var center by remember { mutableStateOf("0") }
    var yRange by remember { mutableStateOf("10") }
    var showSecond by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("FUNCTION GRAPH",color=fg,fontSize=19.sp,fontWeight=FontWeight.Bold)
        Text("Plot f(x) and g(x). Use x, powers (^), brackets and functions such as sin(x), cos(x), sqrt(x) and ln(x).",color=muted,fontSize=12.sp,modifier=Modifier.padding(vertical=8.dp))
        Field("f(x)",function){function=it}
        Field("g(x)",secondFunction){secondFunction=it}
        Row(verticalAlignment=Alignment.CenterVertically) { Text("Show second function",color=muted,modifier=Modifier.weight(1f)); Switch(checked=showSecond,onCheckedChange={showSecond=it}) }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) { Field("X half-range",range){range=it} }
            Column(Modifier.weight(1f)) { Field("X center",center){center=it} }
        }
        Field("Y half-range",yRange){yRange=it}
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick={center=((center.toDoubleOrNull() ?: 0.0)-10.0).toString()},modifier=Modifier.weight(1f)){Text("← Pan")}
            OutlinedButton(onClick={center=((center.toDoubleOrNull() ?: 0.0)+10.0).toString()},modifier=Modifier.weight(1f)){Text("Pan →")}
            OutlinedButton(onClick={range=((range.toDoubleOrNull() ?: 10.0)/2.0).coerceAtLeast(.1).toString()},modifier=Modifier.weight(1f)){Text("Zoom +")}
            OutlinedButton(onClick={range=((range.toDoubleOrNull() ?: 10.0)*2.0).coerceAtMost(1000.0).toString()},modifier=Modifier.weight(1f)){Text("Zoom −")}
        }
        Surface(color=surface,shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth().height(300.dp).padding(vertical=10.dp)) {
            Canvas(Modifier.fillMaxSize().padding(12.dp)) {
                val w=size.width; val h=size.height
                val xSpan=range.toFloatOrNull()?.takeIf{it.isFinite()}?.coerceIn(.1f,1000f) ?: 10f
                val ySpan=yRange.toFloatOrNull()?.takeIf{it.isFinite()}?.coerceIn(.1f,100000f) ?: 10f
                val xCenter=center.toFloatOrNull()?.takeIf{it.isFinite()} ?: 0f
                val xMin=xCenter-xSpan; val xMax=xCenter+xSpan
                fun px(x:Float)=((x-xMin)/(xMax-xMin))*w
                fun py(y:Float)=h-((y+ySpan)/(2f*ySpan))*h
                val grid=muted.copy(alpha=.16f)
                for(i in 0..10) { val gx=w*i/10f; val gy=h*i/10f; drawLine(grid,Offset(gx,0f),Offset(gx,h),1.dp.toPx()); drawLine(grid,Offset(0f,gy),Offset(w,gy),1.dp.toPx()) }
                if(xMin<=0f && xMax>=0f) drawLine(muted.copy(alpha=.8f),Offset(px(0f),0f),Offset(px(0f),h),1.5.dp.toPx())
                drawLine(muted.copy(alpha=.8f),Offset(0f,py(0f)),Offset(w,py(0f)),1.5.dp.toPx())
                fun plot(expr:String,color:Color) {
                    val path=Path(); var active=false; var previousY=0f
                    for(i in 0..1000) {
                        val x=xMin+(xMax-xMin)*i/1000f
                        val value=runCatching { CalculatorEngine.evaluate(expr.replace(Regex("\\bx\\b"),"($x)"),degree).toFloat() }.getOrNull()
                        if(value!=null && value.isFinite() && abs(value)<=ySpan) {
                            val y=py(value)
                            if(!active || abs(y-previousY)>h*.45f) path.moveTo(px(x),y) else path.lineTo(px(x),y)
                            active=true; previousY=y
                        } else active=false
                    }
                    drawPath(path,color,style=androidx.compose.ui.graphics.drawscope.Stroke(width=2.4.dp.toPx()))
                }
                plot(function,Accent); if(showSecond) plot(secondFunction,Color(0xFF23BFA5))
            }
        }
        Text("X: ${(center.toDoubleOrNull() ?: 0.0)-(range.toDoubleOrNull() ?: 10.0)} to ${(center.toDoubleOrNull() ?: 0.0)+(range.toDoubleOrNull() ?: 10.0)}    Y: ±$yRange",color=muted,fontSize=11.sp)
        Text(if(degree) "Trigonometric functions use degrees." else "Trigonometric functions use radians.",color=muted,fontSize=11.sp)
        Text("Blue: f(x)     Teal: g(x)",color=muted,fontSize=11.sp,modifier=Modifier.padding(top=5.dp))
    }
}

@Composable
private fun VaultScreen(vault:SecureVault,surface:Color,fg:Color,muted:Color,initiallyUnlocked:Boolean=false) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mediaVault = remember { SecureMediaVault(context) }
    var mediaItems by remember { mutableStateOf(mediaVault.listItems()) }
    var previewItem by remember { mutableStateOf<SecureMediaItem?>(null) }
    var previewFile by remember { mutableStateOf<java.io.File?>(null) }
    var previewBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var videoView by remember { mutableStateOf<VideoView?>(null) }
    fun closePreview() {
        videoView?.stopPlayback()
        videoView = null
        previewFile?.delete()
        previewFile = null
        previewBitmap = null
        previewItem = null
    }
    val hostActivity = context as? Activity
    DisposableEffect(hostActivity) {
        hostActivity?.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            hostActivity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            closePreview()
        }
    }
    var message by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }; var unlocked by remember { mutableStateOf(initiallyUnlocked) }
    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                val name = context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                    ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                    ?: "Private file"
                val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                mediaVault.importFile(uri, name, mime)
                mediaItems = mediaVault.listItems()
                message = "Encrypted copy added to your private gallery."
            }.onFailure { message = it.message ?: "Could not import this file." }
        }
    }
    var configured by remember { mutableStateOf(vault.isConfigured()) }
    var title by remember { mutableStateOf("") }; var secret by remember { mutableStateOf("") }
    var currentPin by remember { mutableStateOf("") }; var newPin by remember { mutableStateOf("") }
    var recoveryInput by remember { mutableStateOf("") }; var recoveryNewPin by remember { mutableStateOf("") }
    var recoveryCodeToSave by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(emptyList<VaultEntry>()) }
    DisposableEffect(lifecycleOwner, unlocked) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && unlocked) {
                closePreview()
                unlocked = false
                entries = emptyList()
                mediaItems = emptyList()
                title = ""; secret = ""; currentPin = ""; newPin = ""; recoveryInput = ""; recoveryNewPin = ""; recoveryCodeToSave = ""
                message = "Private gallery locked when the app was left."
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(unlocked) {
        if (unlocked) {
            delay(5 * 60 * 1000L)
            unlocked = false
            entries = emptyList()
            mediaItems = emptyList()
            closePreview()
            title = ""; secret = ""; currentPin = ""; newPin = ""; recoveryInput = ""; recoveryNewPin = ""; recoveryCodeToSave = ""
            message = "Private gallery locked automatically after five minutes."
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("PRIVATE VAULT",color=fg,fontSize=19.sp,fontWeight=FontWeight.Bold)
        Text("Encrypted on this device. Use a PIN you do not use elsewhere. This is a simple private notes vault, not a replacement for a dedicated password manager.",color=muted,fontSize=12.sp,modifier=Modifier.padding(vertical=10.dp))
        if(!unlocked) {
            PinField(if(configured) "Enter your private access code" else "Create a 4- or 6-digit code",pin){pin=it.filter(Char::isDigit).take(12)}
            PrimaryAction(if(configured) "UNLOCK PRIVATE GALLERY" else "CREATE PRIVATE GALLERY") {
                runCatching {
                    if(!configured) { recoveryCodeToSave=vault.setPin(pin.toCharArray()); configured=true; unlocked=true; message="Vault created. Write down the one-time recovery code shown below and keep it somewhere safe." }
                    else if(vault.verifyPin(pin.toCharArray())) { entries=vault.readEntries(); mediaItems=mediaVault.listItems(); unlocked=true; message="Vault unlocked." }
                    else message="Incorrect PIN. Try again."
                    pin=""
                }.onFailure { message=it.message ?: "Could not open vault" }
            }
            if(configured) {
                Text("Forgot your PIN? Use your recovery code to set a new one.",color=muted,fontSize=12.sp,modifier=Modifier.padding(top=12.dp))
                PinField("12-digit recovery code",recoveryInput){recoveryInput=it.filter(Char::isDigit).take(12)}
                PinField("New PIN (4 or 6 digits)",recoveryNewPin){recoveryNewPin=it.filter(Char::isDigit).take(6)}
                PrimaryAction("RESET PIN WITH RECOVERY CODE") {
                    runCatching {
                        recoveryCodeToSave=vault.resetPinWithRecoveryCode(recoveryInput.toCharArray(),recoveryNewPin.toCharArray())
                        recoveryInput=""; recoveryNewPin=""; pin=""
                        message="PIN reset. Write down your new recovery code below."
                    }.onFailure { recoveryInput="";recoveryNewPin="";message=it.message ?: "Could not reset PIN" }
                }
            }
        } else {
            Row(verticalAlignment=Alignment.CenterVertically) { Text("Your private items",color=fg,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f)); Text("LOCK",color=Accent,modifier=Modifier.clickable { closePreview(); unlocked=false; entries=emptyList(); mediaItems=emptyList(); title="";secret="";currentPin="";newPin="";recoveryInput="";recoveryNewPin="";recoveryCodeToSave="" }.padding(8.dp)) }
            Text("PRIVATE FILES",color=fg,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=8.dp))
            Text("Import photos, videos, PDFs and other files. Imported copies are encrypted in this app's private storage.",color=muted,fontSize=12.sp,modifier=Modifier.padding(vertical=6.dp))
            PrimaryAction("ADD PRIVATE PHOTOS / VIDEOS / FILES") {
                mediaPicker.launch(arrayOf("image/*","video/*","application/pdf","text/*","application/octet-stream"))
            }
            if (mediaItems.isNotEmpty()) {
                Text("${mediaItems.size} encrypted file(s)",color=Accent,fontSize=11.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(vertical=5.dp))
                mediaItems.forEach { item ->
                    Row(Modifier.fillMaxWidth().background(surface,RoundedCornerShape(12.dp)).padding(10.dp),verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name,color=fg,fontSize=13.sp,fontWeight=FontWeight.SemiBold,maxLines=2)
                            Text("${item.mimeType} • ${SecureMediaVault.displaySize(item.sizeBytes)}",color=muted,fontSize=10.sp)
                        }
                        if (item.mimeType.startsWith("image/") || item.mimeType.startsWith("video/")) {
                            Text("OPEN",color=Accent,fontSize=10.sp,modifier=Modifier.clickable {
                                runCatching {
                                    closePreview()
                                    val decrypted = mediaVault.decryptToCache(item.id)
                                    previewFile = decrypted
                                    previewItem = item
                                    if (item.mimeType.startsWith("image/")) {
                                        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                                        BitmapFactory.decodeFile(decrypted.absolutePath, bounds)
                                        var sample = 1
                                        while (bounds.outWidth / sample > 2048 || bounds.outHeight / sample > 2048) sample *= 2
                                        previewBitmap = BitmapFactory.decodeFile(
                                            decrypted.absolutePath,
                                            BitmapFactory.Options().apply { inSampleSize = sample }
                                        ) ?: error("This image format cannot be previewed.")
                                    }
                                    message = ""
                                }.onFailure { closePreview(); message=it.message ?: "Could not preview this file." }
                            }.padding(8.dp))
                        }
                        Text("DELETE",color=Color(0xFFFF737D),fontSize=10.sp,modifier=Modifier.clickable {
                            runCatching { closePreview(); mediaVault.deleteItem(item.id); mediaItems=mediaVault.listItems(); message="Private file deleted." }
                                .onFailure { message=it.message ?: "Could not delete file." }
                        }.padding(8.dp))
                    }
                    Spacer(Modifier.height(5.dp))
                }
            } else {
                Text("No private files yet.",color=muted,fontSize=11.sp,modifier=Modifier.padding(vertical=5.dp))
            }
            if (previewItem != null && previewFile != null) {
                Column(Modifier.fillMaxWidth().background(surface,RoundedCornerShape(14.dp)).padding(10.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Text(previewItem!!.name,color=fg,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f))
                        Text("CLOSE",color=Accent,fontSize=10.sp,modifier=Modifier.clickable { closePreview() }.padding(8.dp))
                    }
                    if (previewItem!!.mimeType.startsWith("image/")) {
                        previewBitmap?.asImageBitmap()?.let { bitmap ->
                            Image(bitmap=bitmap,contentDescription="Private photo preview",modifier=Modifier.fillMaxWidth().heightIn(max=380.dp))
                        }
                    } else if (previewItem!!.mimeType.startsWith("video/")) {
                        AndroidView(
                            factory = { viewContext ->
                                VideoView(viewContext).apply {
                                    videoView = this
                                    val controller = MediaController(viewContext)
                                    controller.setAnchorView(this)
                                    setMediaController(controller)
                                    setVideoPath(previewFile!!.absolutePath)
                                    setOnPreparedListener { it.isLooping = false }
                                    setOnErrorListener { _, _, _ -> message = "This video format cannot be played on this Android device."; true }
                                    start()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(240.dp)
                        )
                    }
                    Text("The temporary decrypted preview is removed when you close or lock the gallery.",color=muted,fontSize=10.sp,modifier=Modifier.padding(top=6.dp))
                }
            }
            Divider(color=muted.copy(alpha=.2f),modifier=Modifier.padding(vertical=10.dp))
            Text("PRIVATE NOTES",color=fg,fontWeight=FontWeight.Bold)
            Field("Item title",title){title=it}; Field("Private note or secret",secret){secret=it}
            PrimaryAction("SAVE ENCRYPTED ITEM") {
                if(title.isBlank()||secret.isBlank()) message="Enter both a title and content."
                else { entries=entries+VaultEntry(title,secret); vault.writeEntries(entries); title="";secret="";message="Saved securely on this device." }
            }
            Text("PIN SECURITY",color=fg,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=12.dp))
            PinField("Current PIN",currentPin){currentPin=it.filter(Char::isDigit).take(12)}
            PinField("New PIN (4 or 6 digits)",newPin){newPin=it.filter(Char::isDigit).take(6)}
            PrimaryAction("CHANGE VAULT PIN") {
                runCatching { recoveryCodeToSave=vault.changePin(currentPin.toCharArray(),newPin.toCharArray()); currentPin="";newPin="";message="Vault PIN changed. Save your new recovery code below. Existing notes were preserved." }
                    .onFailure { currentPin="";newPin="";message=it.message ?: "Could not change PIN" }
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
        if(recoveryCodeToSave.isNotBlank()) SelectionContainer { Text("RECOVERY CODE — SAVE THIS: $recoveryCodeToSave",color=fg,fontWeight=FontWeight.Bold,fontSize=14.sp,modifier=Modifier.fillMaxWidth().background(surface,RoundedCornerShape(12.dp)).padding(12.dp)) }
    }
}

@Composable
private fun SettingsScreen(surface:Color,fg:Color,muted:Color,dark:Boolean,onDark:(Boolean)->Unit,degree:Boolean,onDegree:(Boolean)->Unit,onClear:()->Unit,vault:SecureVault) {
    var configured by remember { mutableStateOf(vault.isConfigured()) }
    var createCode by remember { mutableStateOf("") }
    var confirmCode by remember { mutableStateOf("") }
    var currentCode by remember { mutableStateOf("") }
    var newCode by remember { mutableStateOf("") }
    var confirmNewCode by remember { mutableStateOf("") }
    var recoveryCode by remember { mutableStateOf("") }
    var privateMessage by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("PREFERENCES",color=fg,fontSize=19.sp,fontWeight=FontWeight.Bold)
        SettingRow("Dark appearance","Use the dark calculator theme",dark,onDark,fg,muted)
        SettingRow("Degree mode","Use degrees for trigonometry",degree,onDegree,fg,muted)
        PrimaryAction("CLEAR CALCULATION HISTORY",onClear)
        Divider(color=muted.copy(alpha=.2f),modifier=Modifier.padding(vertical=14.dp))
        Text("Private access code",color=fg,fontWeight=FontWeight.Bold)
        Text("Create your own 4- or 6-digit code. Open the PRIVATE VAULT tab and enter your code. There is no preset code.",color=muted,fontSize=12.sp,modifier=Modifier.padding(vertical=8.dp))
        if (!configured) {
            PinField("Create your private code",createCode){createCode=it.filter(Char::isDigit).take(6)}
            PinField("Confirm private code",confirmCode){confirmCode=it.filter(Char::isDigit).take(6)}
            PrimaryAction("SAVE PRIVATE CODE") {
                if (createCode.length != 4 && createCode.length != 6) privateMessage="Use exactly 4 or 6 digits."
                else if (createCode != confirmCode) privateMessage="The codes do not match."
                else runCatching { recoveryCode=vault.setPin(createCode.toCharArray()); configured=true; createCode=""; confirmCode=""; privateMessage="Private code saved. Keep the recovery code below somewhere safe." }
                    .onFailure { privateMessage=it.message ?: "Could not save the code." }
            }
        } else {
            PinField("Current private code",currentCode){currentCode=it.filter(Char::isDigit).take(12)}
            PinField("New private code",newCode){newCode=it.filter(Char::isDigit).take(6)}
            PinField("Confirm new code",confirmNewCode){confirmNewCode=it.filter(Char::isDigit).take(6)}
            PrimaryAction("CHANGE PRIVATE CODE") {
                if (newCode.length != 4 && newCode.length != 6) privateMessage="Use exactly 4 or 6 digits."
                else if (newCode != confirmNewCode) privateMessage="The new codes do not match."
                else runCatching { recoveryCode=vault.changePin(currentCode.toCharArray(),newCode.toCharArray()); currentCode="";newCode="";confirmNewCode="";privateMessage="Private code changed. Save the new recovery code below." }
                    .onFailure { currentCode="";newCode="";confirmNewCode="";privateMessage=it.message ?: "Could not change the code." }
            }
        }
        if (privateMessage.isNotBlank()) Text(privateMessage,color=Accent,fontSize=12.sp,modifier=Modifier.padding(vertical=6.dp))
        if (recoveryCode.isNotBlank()) SelectionContainer { Text("RECOVERY CODE — SAVE THIS: $recoveryCode",color=fg,fontWeight=FontWeight.Bold,fontSize=13.sp,modifier=Modifier.fillMaxWidth().background(surface,RoundedCornerShape(12.dp)).padding(12.dp)) }
        Text("Privacy",color=fg,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=18.dp))
        Text("Calculations and vault items are stored locally. The app does not need internet access for calculations. Keep your device protected and do not store highly sensitive credentials in a basic notes vault.",color=muted,fontSize=12.sp,modifier=Modifier.padding(vertical=8.dp))
        Text("DIFFY 01 • Scientific Suite • Version 1.4.0",color=muted,fontSize=11.sp,modifier=Modifier.padding(top=18.dp))
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
@Composable private fun PinField(label:String,value:String,onChange:(String)->Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical=5.dp)) {
        Text(label,color=MaterialTheme.colorScheme.onBackground,fontSize=11.sp)
        BasicTextField(
            value=value,
            onValueChange=onChange,
            modifier=Modifier.fillMaxWidth().padding(top=5.dp).background(MaterialTheme.colorScheme.surface,RoundedCornerShape(12.dp)).padding(13.dp),
            singleLine=true,
            visualTransformation=PasswordVisualTransformation(),
            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),
            textStyle=androidx.compose.ui.text.TextStyle(color=MaterialTheme.colorScheme.onSurface,fontSize=15.sp),
            decorationBox={ inner -> if(value.isEmpty()) Text("Enter PIN",color=MaterialTheme.colorScheme.onSurface.copy(alpha=.45f)); inner() }
        )
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

private fun formatMatrix(matrix: List<List<Double>>): String =
    matrix.joinToString("\n") { row -> row.joinToString("    ") { AdvancedMath.format(it) } }
