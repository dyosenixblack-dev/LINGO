package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

object ImageMockGenerator {
    
    fun generateJapaneseExitSign(): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Background Green
        canvas.drawColor(Color.parseColor("#008F39"))
        
        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        // White exit runner frame
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        canvas.drawRect(20f, 20f, 380f, 280f, paint)
        
        // Text Japanese
        paint.style = Paint.Style.FILL
        paint.textSize = 50f
        canvas.drawText("非常口", 200f, 130f, paint)
        
        // Text English
        paint.textSize = 65f
        paint.isFakeBoldText = true
        canvas.drawText("EXIT", 200f, 220f, paint)
        
        return bitmap
    }

    fun generateItalianMenu(): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 350, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Papyrus cream color
        canvas.drawColor(Color.parseColor("#FDF5E6"))
        
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#5C4033")
        }
        
        // Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        canvas.drawRect(15f, 15f, 385f, 335f, paint)
        
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 30f
        paint.isFakeBoldText = true
        canvas.drawText("RISTORANTE ITALIANO", 200f, 60f, paint)
        
        paint.textSize = 20f
        paint.isFakeBoldText = false
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("1. Pizza Margherita .... €8.50", 40f, 120f, paint)
        canvas.drawText("2. Pasta Carbonara .... €12.00", 40f, 180f, paint)
        canvas.drawText("3. Tiramisu Classico .... €6.50", 40f, 240f, paint)
        canvas.drawText("4. Espresso Napoletano .. €2.00", 40f, 300f, paint)
        
        return bitmap
    }

    fun generateFrenchAirportBoard(): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Dark airport terminal screen background
        canvas.drawColor(Color.parseColor("#0F1423"))
        
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#FFCC00") // Yellow glowing text
        }
        
        // Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawRect(10f, 10f, 390f, 290f, paint)
        
        paint.style = Paint.Style.FILL
        paint.textSize = 28f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("AÉROPORT DE PARIS-CDG", 200f, 60f, paint)
        
        paint.textSize = 18f
        paint.isFakeBoldText = false
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.WHITE
        canvas.drawText("FLIGHT  TO       BOARD   STATUS", 30f, 120f, paint)
        
        paint.color = Color.parseColor("#FFCC00")
        canvas.drawText("AF122   LONDON   GATE 4  EMBARQUEMENT", 30f, 170f, paint)
        canvas.drawText("AF450   TOKYO    GATE 6  RETARD 20 MIN", 30f, 220f, paint)
        canvas.drawText("AF893   CAIRO    GATE 9  PRÉVU À 19:40", 30f, 270f, paint)
        
        return bitmap
    }

    fun generateGermanMetroSign(): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Blue german subway background
        canvas.drawColor(Color.parseColor("#033E8C"))
        
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }
        
        // Large "U" Subways Logo
        paint.textSize = 70f
        paint.isFakeBoldText = true
        canvas.drawText("U-Bahn", 200f, 100f, paint)
        
        // Line directions
        paint.textSize = 20f
        paint.isFakeBoldText = false
        canvas.drawText("Fahrkarten & Tickets hier enterten", 200f, 150f, paint)
        
        paint.color = Color.parseColor("#FFD700") // Gold
        canvas.drawText("Gleis 1: Alexanderplatz - Ruhleben", 200f, 215f, paint)
        canvas.drawText("Gleis 2: Warschauer Str. - Pankow", 200f, 265f, paint)
        
        return bitmap
    }
}
