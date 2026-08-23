package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.game.audio.SoundManager
import com.example.game.engine.GameEngine
import com.example.game.model.ScreenState
import com.example.game.storage.GamePreferences
import com.example.game.ui.screens.CaughtGameOverDialog
import com.example.game.ui.screens.LeaderboardScreen
import com.example.game.ui.screens.LevelCompleteDialog
import com.example.game.ui.screens.LevelSelectScreen
import com.example.game.ui.screens.MainGameScreen
import com.example.game.ui.screens.MainMenuScreen
import com.example.game.ui.screens.MissionsScreen
import com.example.game.ui.screens.PauseDialog
import com.example.game.ui.screens.SettingsScreen
import com.example.game.ui.screens.ShopScreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private var soundManager: SoundManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = GamePreferences(this)
        val sound = SoundManager().also {
            it.isSoundEnabled = preferences.soundEnabled
            it.isMusicEnabled = preferences.musicEnabled
        }
        soundManager = sound

        val engine = GameEngine(sound, preferences)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkSurface
                ) {
                    GameRootApp(engine)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        soundManager?.startBgm()
    }

    override fun onPause() {
        super.onPause()
        soundManager?.stopBgm()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager?.release()
    }
}

@Composable
fun GameRootApp(engine: GameEngine) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                if (engine.screenState == ScreenState.PLAYING) {
                    engine.pauseGame()
                }
                engine.soundManager.stopBgm()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                if (engine.preferences.musicEnabled && engine.screenState == ScreenState.PLAYING) {
                    engine.soundManager.startBgm()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Handle System Back Button
    BackHandler(enabled = true) {
        when (engine.screenState) {
            ScreenState.PLAYING -> engine.pauseGame()
            ScreenState.PAUSED -> engine.resumeGame()
            ScreenState.CAUGHT_GAMEOVER, ScreenState.LEVEL_COMPLETE -> engine.goToMainMenu()
            ScreenState.LEVEL_SELECT, ScreenState.SHOP, ScreenState.MISSIONS,
            ScreenState.LEADERBOARD, ScreenState.SETTINGS -> engine.goToMainMenu()
            ScreenState.MENU -> {
                // Let system exit app
            }
        }
    }

    Crossfade(
        targetState = engine.screenState,
        label = "screen_transition"
    ) { state ->
        when (state) {
            ScreenState.MENU -> {
                MainMenuScreen(
                    engine = engine,
                    onPlayEndless = { engine.startEndlessGame() },
                    onOpenLevelSelect = { engine.screenState = ScreenState.LEVEL_SELECT },
                    onOpenShop = { engine.screenState = ScreenState.SHOP },
                    onOpenMissions = { engine.screenState = ScreenState.MISSIONS },
                    onOpenLeaderboard = { engine.screenState = ScreenState.LEADERBOARD },
                    onOpenSettings = { engine.screenState = ScreenState.SETTINGS }
                )
            }

            ScreenState.LEVEL_SELECT -> {
                LevelSelectScreen(
                    engine = engine,
                    onSelectLevel = { level -> engine.startLevel(level) },
                    onBack = { engine.goToMainMenu() }
                )
            }

            ScreenState.PLAYING, ScreenState.PAUSED, ScreenState.CAUGHT_GAMEOVER, ScreenState.LEVEL_COMPLETE -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    MainGameScreen(
                        engine = engine,
                        onPauseClick = { engine.pauseGame() }
                    )

                    // Overlays
                    if (state == ScreenState.PAUSED) {
                        PauseDialog(
                            onResume = { engine.resumeGame() },
                            onRestart = { engine.restartCurrentGame() },
                            onHome = { engine.goToMainMenu() },
                            isBangla = engine.preferences.languageIsBangla
                        )
                    } else if (state == ScreenState.CAUGHT_GAMEOVER) {
                        CaughtGameOverDialog(
                            engine = engine,
                            onTryAgain = { engine.restartCurrentGame() },
                            onHome = { engine.goToMainMenu() }
                        )
                    } else if (state == ScreenState.LEVEL_COMPLETE) {
                        LevelCompleteDialog(
                            engine = engine,
                            onNextLevel = {
                                val nextLvlNum = (engine.currentLevel?.levelNumber ?: 1) + 1
                                val allLevels = engine.preferences.getLevels()
                                val nextLvl = allLevels.find { it.levelNumber == nextLvlNum }
                                if (nextLvl != null) {
                                    engine.startLevel(nextLvl)
                                } else {
                                    engine.goToMainMenu()
                                }
                            },
                            onHome = { engine.goToMainMenu() }
                        )
                    }
                }
            }

            ScreenState.SHOP -> {
                ShopScreen(
                    engine = engine,
                    onBack = { engine.goToMainMenu() }
                )
            }

            ScreenState.MISSIONS -> {
                MissionsScreen(
                    engine = engine,
                    onBack = { engine.goToMainMenu() }
                )
            }

            ScreenState.LEADERBOARD -> {
                LeaderboardScreen(
                    engine = engine,
                    onBack = { engine.goToMainMenu() }
                )
            }

            ScreenState.SETTINGS -> {
                SettingsScreen(
                    engine = engine,
                    onBack = { engine.goToMainMenu() }
                )
            }
        }
    }
}

