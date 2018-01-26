package com.yellowbytestudios.hazardagif.ducksauce

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaPlayer2: MediaPlayer
    private lateinit var editText: EditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar!!.hide()
        editText = findViewById(R.id.lyrics_et) as EditText
        radioGroup = findViewById(R.id.radio_group) as RadioGroup

        textToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(applicationContext, "This language is not supported", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(applicationContext, "Initialization failed", Toast.LENGTH_SHORT).show()
            }
            textToSpeech.setSpeechRate(1.2f)
        }, "com.google.android.tts")

        if (Build.VERSION.SDK_INT < 21) {//Setting gender only works in version 19 and up.
            radioGroup.visibility = View.GONE
        } else {
            radioGroup.check(R.id.male_radio)
        }

        findViewById(R.id.play_song_button).setOnClickListener { v ->
            if (editText.text.toString() != "") {
                if ((v as Button).text.toString() == "Cancel") {
                    v.text = "Play Song"
                    editText.isEnabled = true
                    radioGroup.isEnabled = true
                    mediaPlayer.stop()
                    mediaPlayer2.stop()
                    textToSpeech.stop()
                } else {
                    v.text = "Cancel"
                    editText.isEnabled = false
                    radioGroup.isEnabled = false
                    val selectedId = radioGroup.checkedRadioButtonId
                    setVoice(selectedId == R.id.male_radio)
                    mediaPlayer = MediaPlayer.create(applicationContext, R.raw.start)
                    mediaPlayer2 = MediaPlayer.create(applicationContext, R.raw.ending)
                    mediaPlayer.setOnCompletionListener { sayLyric() }
                    mediaPlayer.start()
                }
            }
        }

        findViewById(R.id.bg1).setOnClickListener(this)
        findViewById(R.id.bg2).setOnClickListener(this)
        findViewById(R.id.bg3).setOnClickListener(this)
        findViewById(R.id.bg4).setOnClickListener(this)
        findViewById(R.id.bg5).setOnClickListener(this)
        findViewById(R.id.bg6).setOnClickListener(this)
        findViewById(R.id.bg7).setOnClickListener(this)

        val pref = applicationContext.getSharedPreferences("MyPref", 0) // 0 - for private mode
        editor = pref.edit()

        setBgColor(pref.getInt("bg_color", Color.BLACK)) // getting boolean
    }

    fun setVoice(male: Boolean) {
        val voice: String
        if (male) {
            voice = "en-us-x-sfg#male_1-local"
        } else {
            voice = "en-us-x-sfg#female_1-local"
        }

        if (Build.VERSION.SDK_INT >= 21) {
            for (tmpVoice in textToSpeech.voices) {
                if (tmpVoice.name == voice) {
                    textToSpeech.voice = tmpVoice
                }
            }
        }
    }

    fun sayLyric() {
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {

            }

            override fun onDone(utteranceId: String) {
                mediaPlayer2.setOnCompletionListener { sayLyric() }

                mediaPlayer2.start()
            }

            override fun onError(utteranceId: String) {

            }
        })

        val lyrics = editText.text.toString()

        if (Build.VERSION.SDK_INT >= 21) {
            val utteranceId = this.hashCode().toString() + ""
            textToSpeech.speak(lyrics, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            val map = HashMap<String, String>()
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId")
            textToSpeech.speak(lyrics, TextToSpeech.QUEUE_FLUSH, map)
        }
    }

    fun setBgColor(id: Int) {
        findViewById(R.id.background).setBackgroundColor(id)
        editor.putInt("bg_color", id) // Storing long
        editor.commit() // commit changes
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.bg1, R.id.bg2, R.id.bg3, R.id.bg4, R.id.bg5, R.id.bg6, R.id.bg7 -> setBgColor((v.background as ColorDrawable).color)
        }
    }
}
