package com.fivucsas.mobile.android.data.preferences

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fivucsas.shared.ui.theme.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ThemePreferencesTest {

    private lateinit var prefs: ThemePreferences

    @Before
    fun setUp() {
        clearUnderlyingPrefs()
        prefs = ThemePreferences(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        clearUnderlyingPrefs()
    }

    private fun clearUnderlyingPrefs() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        ctx.getSharedPreferences("fivucsas_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove("theme_mode")
            .commit()
    }

    @Test
    fun default_returns_system() {
        assertEquals(ThemeMode.SYSTEM, prefs.getThemeMode())
    }

    @Test
    fun set_then_get_round_trip_light() {
        prefs.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, prefs.getThemeMode())
    }

    @Test
    fun set_then_get_round_trip_dark() {
        prefs.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, prefs.getThemeMode())
    }

    @Test
    fun persists_across_instances() {
        prefs.setThemeMode(ThemeMode.DARK)
        val second = ThemePreferences(ApplicationProvider.getApplicationContext())
        assertEquals(ThemeMode.DARK, second.getThemeMode())
    }

    @Test
    fun corrupt_value_falls_back_to_system() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        ctx.getSharedPreferences("fivucsas_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("theme_mode", "NOT_A_VALID_ENUM")
            .commit()
        val fresh = ThemePreferences(ctx)
        assertEquals(ThemeMode.SYSTEM, fresh.getThemeMode())
    }

    @Test
    fun state_flow_emits_on_change() = runTest {
        // Confirm the initial value is SYSTEM, then emit LIGHT.
        assertEquals(ThemeMode.SYSTEM, prefs.themeMode.first())

        prefs.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, prefs.themeMode.first())

        prefs.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, prefs.themeMode.first())
    }

    @Test
    fun set_same_mode_is_idempotent() {
        prefs.setThemeMode(ThemeMode.DARK)
        prefs.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, prefs.getThemeMode())
    }
}
