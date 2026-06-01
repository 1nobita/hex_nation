package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Hexagon
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

import com.example.data.HexOwnershipLog

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import com.example.data.FULL_NATIONS_LIST
import java.net.URLEncoder

@Composable
fun HexCanvasScreen(
    nationName: String,
    hexagons: List<Hexagon>,
    logs: List<HexOwnershipLog>,
    onHexSelected: (Int, Int) -> Unit,
    onPurchaseAction: (Int, Int, Long) -> Unit,
    onBack: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset(200f, 200f)) }
    val hexSize = 70f
    val basePath = remember(hexSize) { HexMath.getBaseHexagonPath(hexSize * 0.92f) }
    
    val hexMap = remember(hexagons) {
        hexagons.associateBy { (it.q.toLong() shl 32) or (it.r.toLong() and 0xFFFFFFFFL) }
    }

    var selectedQ by remember { mutableIntStateOf(0) }
    var selectedR by remember { mutableIntStateOf(0) }
    var hasSelection by remember { mutableStateOf(false) }
    var selectedPaintColor by remember { mutableStateOf(Color(0xFFFF3B30)) }
    val colorPalette = listOf(
        Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00), Color(0xFF4CD964),
        Color(0xFF5AC8FA), Color(0xFF007AFF), Color(0xFF5856D6), Color(0xFFFF2D55)
    )
    
    // Theme Colors
    val bgScreen = Color(0xFF1C1B1F)
    val bgCanvas = Color(0xFF000000)
    val textPrimary = Color(0xFFE6E1E5)
    val accentPurple = Color(0xFFD0BCFF)
    val deepPurple = Color(0xFF381E72)
    val borderColor = Color(0xFF49454F)
    val statsBg = Color(0xFF2B2930)
    
    Column(modifier = Modifier.fillMaxSize().background(bgScreen)) {
        // Status Bar & Header
        Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "NATIONAL INSTANCE",
                        color = accentPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Text(
                            text = nationName,
                            color = textPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF313033), RoundedCornerShape(6.dp))
                                .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("#091", color = textPrimary, fontSize = 10.sp)
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Brush.linearGradient(listOf(accentPurple, deepPurple)),
                            shape = CircleShape
                        )
                        .border(2.dp, borderColor, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("RA", color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            // Stats Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatCard(modifier = Modifier.weight(1f), title = "CLAIMED", value = "412,804", bg = statsBg, border = borderColor.copy(alpha = 0.3f), textColor = textPrimary)
                StatCard(modifier = Modifier.weight(1f), title = "MARKET CAP", value = "$1.4M", bg = statsBg, border = borderColor.copy(alpha = 0.3f), textColor = textPrimary)
                StatCard(modifier = Modifier.weight(1f), title = "FLOOR PRICE", value = "$1.21", bg = deepPurple, border = accentPurple.copy(alpha = 0.2f), textColor = Color(0xFFEADDFF), titleColor = Color(0xFFEADDFF))
            }
        }
        
        // Main Canvas Viewport (The Grid)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(bgCanvas)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            val nationInfo = remember(nationName) { FULL_NATIONS_LIST.find { it.name == nationName } }
            val iconicPlace = nationInfo?.iconicPlace ?: "Landscape"
            val prompt = "$iconicPlace, $nationName, beautiful iconic professional photography"
            val imageUrl = remember(prompt) { "https://image.pollinations.ai/prompt/${URLEncoder.encode(prompt, "UTF-8")}" }
            
            AsyncImage(
                model = imageUrl,
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = offset.x
                        translationY = offset.y
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                    }
            )

            val defaultStroke = remember { Stroke(width = 1f) }
            val selectedStroke = remember { Stroke(width = 3f) }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom)
                            scale = if (newScale.isNaN()) scale else newScale.coerceIn(0.8f, 5f)
                            
                            val newX = offset.x + pan.x
                            val newY = offset.y + pan.y
                            offset = Offset(
                                if (newX.isNaN()) offset.x else newX.coerceIn(-10000f, 10000f),
                                if (newY.isNaN()) offset.y else newY.coerceIn(-10000f, 10000f)
                            )
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { tapLoc ->
                            val tapX = (tapLoc.x - offset.x) / scale
                            val tapY = (tapLoc.y - offset.y) / scale
                            val hexCoord = HexMath.pixelToHex(tapX, tapY, hexSize)
                            selectedQ = hexCoord.q
                            selectedR = hexCoord.r
                            hasSelection = true
                            onHexSelected(hexCoord.q, hexCoord.r)
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                if (width.isNaN() || height.isNaN() || width <= 0f || height <= 0f) return@Canvas
                if (offset.x.isNaN() || offset.y.isNaN() || scale.isNaN() || scale <= 0f) return@Canvas

                val startX = (-offset.x) / scale
                val startY = (-offset.y) / scale
                val endX = (width - offset.x) / scale
                val endY = (height - offset.y) / scale
                
                translate(offset.x, offset.y) {
                    scale(scale, scale, Offset.Zero) {
                        val rowHeight = hexSize * 1.5f
                        val colWidth = hexSize * HexMath.SQRT_3
                        
                        val minR = (startY / rowHeight).toInt() - 2
                        val maxR = (endY / rowHeight).toInt() + 2
                        
                        val minQ = (startX / colWidth).toInt() - 2
                        val maxQ = (endX / colWidth).toInt() + 2
                        
                        for (r in minR..maxR) {
                            for (q in (minQ - (r/2)-1)..(maxQ - (r/2) + 2)) {
                                val center = HexMath.hexToPixel(q, r, hexSize)
                                
                                val key = (q.toLong() shl 32) or (r.toLong() and 0xFFFFFFFFL)
                                val userHex = hexMap[key]
                                val isSelected = hasSelection && selectedQ == q && selectedR == r
                                
                                translate(center.x, center.y) {
                                    if (userHex != null) {
                                        drawPath(basePath, Color(userHex.color.toInt()))
                                    } else {
                                        drawPath(basePath, borderColor, style = defaultStroke)
                                    }
                                    
                                    if (isSelected) {
                                        drawPath(basePath, accentPurple, style = selectedStroke)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Coordinates Floating Label
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text("27.1751° N, 78.0421° E", color = textPrimary, fontSize = 10.sp, letterSpacing = (-0.5).sp)
            }
            
            // Zoom Controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButtonControls("+", statsBg, borderColor, textPrimary) { scale = (scale * 1.2f).coerceAtMost(5f) }
                TextButtonControls("-", statsBg, borderColor, textPrimary) { scale = (scale / 1.2f).coerceAtLeast(0.2f) }
            }
        }
        
        // Interaction Layer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgScreen)
        ) {
            HorizontalDivider(color = borderColor, thickness = 1.dp)
            Column(modifier = Modifier.padding(16.dp)) {
                if (hasSelection) {
                    val q = selectedQ
                    val r = selectedR
                    val key = (q.toLong() shl 32) or (r.toLong() and 0xFFFFFFFFL)
                    val existingHex = hexMap[key]
                    val flips = existingHex?.numFlips ?: 0
                    val basePrice = 1.0
                    val price = basePrice * (1.1).pow(flips.toDouble())
                    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("SELECTED HEX #${q * 100 + r}", color = Color(0xFFCAC4D0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(formatter.format(price), color = textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("+10% Next", color = Color(0xFF00FF00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            if (existingHex != null) {
                                Text("Owned by: ${existingHex.ownerId}", color = Color(0xFFEADDFF), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("AVAILABILITY LOCK", color = Color(0xFFCAC4D0), fontSize = 10.sp)
                            Text("14:22:09", color = Color(0xFFFFB4AB), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (logs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("OWNERSHIP HISTORY", color = Color(0xFFCAC4D0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            logs.take(3).forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(statsBg, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(log.ownerId, color = textPrimary, fontSize = 12.sp)
                                    Text("${log.durationSeconds}s", color = accentPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("CHOOSE PAINT COLOR", color = Color(0xFFCAC4D0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colorPalette.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(color, CircleShape)
                                    .border(
                                        2.dp,
                                        if (selectedPaintColor == color) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedPaintColor = color }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth().height(56.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { 
                                onPurchaseAction(q, r, selectedPaintColor.toArgb().toLong())
                                hasSelection = false
                            },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentPurple, contentColor = deepPurple)
                        ) {
                            Text("Buy & Paint", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { hasSelection = false },
                            modifier = Modifier.width(56.dp).fillMaxHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = borderColor, contentColor = textPrimary),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("NO HEX SELECTED", color = Color(0xFFCAC4D0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Tap a hex on the canvas", color = textPrimary, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(56.dp).background(statsBg, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Text("Select a Hex to Interact", color = borderColor, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Special Event Ticker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(deepPurple, borderColor)),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, accentPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF9800), CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Flash Sale: Buy Entire Nation ($50.00)", color = Color(0xFFEADDFF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Ends in 08:41", color = accentPurple, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, title: String, value: String, bg: Color, border: Color, textColor: Color, titleColor: Color = textColor.copy(alpha = 0.6f)) {
    Column(
        modifier = modifier
            .background(bg, RoundedCornerShape(12.dp))
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = titleColor, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        Text(value, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TextButtonControls(text: String, bg: Color, border: Color, textColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(bg, RoundedCornerShape(12.dp))
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}
