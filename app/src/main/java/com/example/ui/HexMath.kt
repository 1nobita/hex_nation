package com.example.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.roundToInt

data class HexCoord(val q: Int, val r: Int)

object HexMath {
    val SQRT_3 = sqrt(3f)
    
    fun hexToPixel(q: Int, r: Int, size: Float): Offset {
        val x = size * SQRT_3 * (q + r / 2f)
        val y = size * (3f / 2f) * r
        return Offset(x, y)
    }

    fun pixelToHex(x: Float, y: Float, size: Float): HexCoord {
        val qFloat = (Math.sqrt(3.0) / 3 * x - 1.0 / 3 * y) / size
        val rFloat = (2.0 / 3 * y) / size
        return axialRound(qFloat.toFloat(), rFloat.toFloat())
    }

    private fun axialRound(q: Float, r: Float): HexCoord {
        val s = -q - r
        var rq = q.roundToInt()
        var rr = r.roundToInt()
        var rs = s.roundToInt()

        val qDiff = Math.abs(rq - q)
        val rDiff = Math.abs(rr - r)
        val sDiff = Math.abs(rs - s)

        if (qDiff > rDiff && qDiff > sDiff) {
            rq = -rr - rs
        } else if (rDiff > sDiff) {
            rr = -rq - rs
        }
        return HexCoord(rq, rr)
    }

    fun getBaseHexagonPath(size: Float): Path {
        val path = Path()
        for (i in 0..5) {
            val angleDeg = 60.0 * i - 30.0
            val angleRad = Math.PI / 180.0 * angleDeg
            val x = size * cos(angleRad).toFloat()
            val y = size * sin(angleRad).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }
}
