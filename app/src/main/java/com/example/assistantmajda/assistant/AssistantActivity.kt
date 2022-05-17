package com.example.assistantmajda.assistant

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.ClipboardManager
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.assistantmajda.R
import com.example.assistantmajda.data.AssistantDatabase
import com.example.assistantmajda.databinding.ActivityAssistantBinding
import java.util.*


class AssistantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssistantBinding
    private lateinit var assistantViewModel: AssistantViewModel

    private lateinit var textToSpeach: TextToSpeech
    private lateinit var speechRecongnize: SpeechRecognizer
    private lateinit var recognizeIntent: Intent
    private lateinit var keeper: String

    private var REQUESTCALL = 1
    private var SENDSMS = 2
    private var READSMS = 3
    private var SHAREFILE = 4
    private var SHAREATEXTFILE = 5
    private var READCONTACTS = 6
    private var CAPTUREPHOTO = 7

    private var REQUEST_CODE_SELECT_DOC: Int = 100
    private var REQUEST_ENABLE_BT = 1000

    private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var cameraManager: CameraManager
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var cameraID: String
    private lateinit var ringtone: Ringtone


    private val logtts = "TTS"
    private val logsr = "SR"
    private val logkeeper = "Keeper"


    private var imageIndex: Int = 0
    private lateinit var imgUri: Uri
//    private lateinit var helper: OpenWeatherMapHelper

    @Suppress("DEPRECATION")
    private val imageDirectory =
        Environment.getExternalStorageState(Environment.DIRECTORY_PICTURES).toString()


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_assistant)
        overridePendingTransition(R.anim.non_movable, R.anim.non_movable)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_assistant)

        val application = requireNotNull(this).application
        val dataSource = AssistantDatabase.getInstance(application).assisstantDao
        val viewModelFactory = AssistantViewModelFactory(dataSource, application)

        assistantViewModel =
            ViewModelProvider(this, viewModelFactory).get(AssistantViewModel::class.java)

        val adapter = AssistantAdapter()
        binding.recyclerview.adapter = adapter

        assistantViewModel.messages.observe(this, {
            it?.let {
                adapter.data = it
            }
        })
        binding.setLifecycleOwner(this)
        //animation

        if (savedInstanceState == null) {
            binding.assistantConstraintLayout.setVisibility(View.INVISIBLE)
            val viewTreeObserver: ViewTreeObserver =
                binding.assistantConstraintLayout.getViewTreeObserver()

            if (viewTreeObserver.isAlive) {
                viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        circularReaalActivity()
                        binding.assistantConstraintLayout.getViewTreeObserver()
                            .removeOnGlobalLayoutListener(this)
                    }

                })
            }

        }
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            cameraID = cameraManager.cameraIdList[0]
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        ringtone = RingtoneManager.getRingtone(
            applicationContext,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        )

//        helper =  OpenWeatherMapHelper(getString(R.string.OPEN_WEATHER_MAP_API_KEY));

        textToSpeach = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result: Int = textToSpeach.setLanguage(Locale.ENGLISH)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(logtts, "languge  Not supported")
                } else {
                    Log.e(logtts, "languge  supported")
                }

            } else {
                Log.e(logtts, "initialization failed  ")
            }

        }

        speechRecongnize = SpeechRecognizer.createSpeechRecognizer(this)
        recognizeIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizeIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        recognizeIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        speechRecongnize.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SR", "started")
            }

            override fun onBeginningOfSpeech() {
            }

            override fun onRmsChanged(rmsdB: Float) {
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                TODO("Not yet implemented")
            }

            override fun onEndOfSpeech() {
                Log.d("SR", "onEndOfSpeech")
            }

            override fun onError(error: Int) {
                TODO("Not yet implemented")
            }

            override fun onResults(bundle: Bundle?) {

                val data = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                if (data != null) {

                    keeper = data[0]
                    Log.d(logkeeper, keeper)
                    when {
                        keeper.contains("thanks") -> speak("It's my job, let me know if there is somthing")
                        keeper.contains("welcome") -> speak("for what?")
                        keeper.contains("clear") -> assistantViewModel.onClear()
                        keeper.contains("date") -> getDate()
                        keeper.contains("time") -> getTime()
                        keeper.contains("phone call") -> makeAPhoneCall()
                        keeper.contains("send SMS") -> sendSMS()
                        keeper.contains("read my last SMS") -> readSMS()

                        keeper.contains("open Gmail") -> openGmail()
                        keeper.contains("phone call") -> openWhatsapp()


                    }
                    //end when


                }
            }//end en result

            override fun onPartialResults(partialResults: Bundle?) {
                TODO("Not yet implemented")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                TODO("Not yet implemented")
            }

        })//end speechRecongnize.setRecognitionListener

        binding.assistantActionButton.setOnTouchListener { view, motionEvent ->

            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    speechRecongnize.stopListening()
                }


                MotionEvent.ACTION_DOWN -> {
                    textToSpeach.stop()
                    speechRecongnize.startListening(recognizeIntent)
                }

            }
            false

        }//end binding.assistantActionButton

        checkIfSpeechRecognizerAviable()


    }//end on create

    private fun checkIfSpeechRecognizerAviable() {
        if(SpeechRecognizer.isRecognitionAvailable(this)){
            Log.d(logsr,"yes")
        }else{
            Log.d(logsr,"false")
        }
    }

    fun speak(){

    }


}