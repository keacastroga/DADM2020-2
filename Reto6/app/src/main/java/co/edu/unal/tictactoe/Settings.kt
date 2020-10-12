package co.edu.unal.tictactoe

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.*
import android.preference.Preference.OnPreferenceChangeListener
import android.util.Log
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class Settings : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)

        val difficultyLevelPref = findPreference("difficulty_level") as ListPreference
        val difficulty = prefs.getString("difficulty_level", resources.getString(R.string.difficulty_expert))
        difficultyLevelPref.summary = difficulty as CharSequence

        difficultyLevelPref.onPreferenceChangeListener =
            OnPreferenceChangeListener { _, newValue ->
                difficultyLevelPref.summary = newValue as CharSequence

                val ed = prefs.edit()
                ed.putString("difficulty_level", newValue.toString())
                ed.commit()
                true
            }

        val victoryMessagePref = findPreference("victory_message") as EditTextPreference
        val victoryMessage = prefs.getString("victory_message", resources.getString(R.string.result_human_wins))
        victoryMessagePref.summary = victoryMessage as CharSequence

        victoryMessagePref.onPreferenceChangeListener =
            OnPreferenceChangeListener{_, newValue ->
                victoryMessagePref.summary = newValue as CharSequence
                val ed = prefs.edit()
                ed.putString("victory_message", newValue.toString())
                ed.commit()
                true
            }

        val colorPref = findPreference("board_color") as Preference
        colorPref.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                ColorPickerDialog.Builder(this)
                    .setTitle("ColorPicker Dialog")
                    .setPreferenceName("MyColorPickerDialog")
                    .setPositiveButton("Accept",
                        ColorEnvelopeListener { envelope, _ ->
                            val ed = prefs.edit()
                            ed.putString("board_color", "#"+envelope.hexCode)
                            ed.commit()
                        })
                    .setNegativeButton("Cancel") { dialogInterface, i -> dialogInterface.dismiss() }
                    .attachAlphaSlideBar(true) // the default value is true.
                    .attachBrightnessSlideBar(true) // the default value is true.
                    .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                    .show()
                true
            }
    }
}