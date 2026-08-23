package com.example.game.graphics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.R

class GameSpriteManager(private val context: Context) {

    var playerRunBitmap: ImageBitmap? = null
        private set
    var playerMoneyBagBitmap: ImageBitmap? = null
        private set
    var chaserRunBitmap: ImageBitmap? = null
        private set
    var roadBgBitmap: ImageBitmap? = null
        private set
    var caughtSceneBitmap: ImageBitmap? = null
        private set
    var homeBannerBitmap: ImageBitmap? = null
        private set
    var gameLogoBitmap: ImageBitmap? = null
        private set

    init {
        loadSprites()
    }

    private fun loadSprites() {
        try {
            // Load transparent PNG assets provided by user
            playerRunBitmap = loadTransparentBitmap(R.drawable.img_player_running)
                ?: loadTransparentBitmap(R.drawable.img_player_run)
            
            playerMoneyBagBitmap = loadTransparentBitmap(R.drawable.img_player_moneybag)
            
            chaserRunBitmap = loadTransparentBitmap(R.drawable.img_chaser_running)
                ?: loadTransparentBitmap(R.drawable.img_chaser_run)

            val opts = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 }
            roadBgBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_road_bg, opts)?.asImageBitmap()
            caughtSceneBitmap = loadTransparentBitmap(R.drawable.img_caught_character)
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.img_caught_scene, opts)?.asImageBitmap()
            homeBannerBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_home_banner, opts)?.asImageBitmap()
            gameLogoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_game_logo, opts)?.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadTransparentBitmap(resId: Int): ImageBitmap? {
        val opts = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bmp = BitmapFactory.decodeResource(context.resources, resId, opts) ?: return null
        return bmp.asImageBitmap()
    }
}
