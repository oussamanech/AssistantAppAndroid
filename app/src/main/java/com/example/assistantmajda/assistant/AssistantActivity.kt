package com.example.assistantmajda.assistant

import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.content.ClipboardManager
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.Ringtone
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.ViewTreeObserver
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.assistantmajda.R
import com.example.assistantmajda.data.Assistant
import com.example.assistantmajda.data.AssistantDatabase
import com.example.assistantmajda.databinding.ActivityAssistantBinding

class AssistantActivity : AppCompatActivity() {

    private  lateinit var binding:ActivityAssistantBinding
    private  lateinit var assistantViewModel: AssistantViewModel

    private  lateinit var textToSpeach: TextToSpeech
    private  lateinit var speechRecongnize: SpeechRecognizer
    private  lateinit var recognizeIntent: Intent
    private  lateinit var keeper: String

    private  var REQUESTCALL = 1
    private  var SENDSMS = 2
    private  var READSMS = 3
    private  var SHAREFILE = 4
    private  var SHAREATEXTFILE = 5
    private  var READCONTACTS = 6
    private  var CAPTUREPHOTO = 7

    private  var REQUEST_CODE_SELECT_DOC: Int = 100
    private  var REQUEST_ENABLE_BT = 1000

    private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var cameraManager: CameraManager
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var cameraID: String
    private lateinit var ringtone: Ringtone

    private var imageIndex: Int = 0
    private lateinit var imgUri: Uri
//    private lateinit var helper: OpenWeatherMapHelper

    @Suppress("DEPRECATION")
    private val imageDirectory = Environment.getExternalStorageState(Environment.DIRECTORY_PICTURES).toString()


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_assistant)
        overridePendingTransition(R.anim.non_movable,R.anim.non_movable)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_assistant)

        val application = requireNotNull(this).application
        val dataSource = AssistantDatabase.getInstance(application).assisstantDao
        val viewModelFactory = AssistantViewModelFactory(dataSource,application)

        assistantViewModel = ViewModelProvider(this,viewModelFactory).get(AssistantViewModel::class.java)

        val adapter = AssistantAdapter()
        binding.recyclerview.adapter = adapter

        assistantViewModel.messages.observe(this,{
            it?.let{
                adapter.data = it
            }
        })
        binding.setLifecycleOwner(this)
        //animation

        if(savedInstanceState == null){
            binding.assistantConstraintLayout.setVisibility(View.INVISIBLE)
            val viewTreeObserver: ViewTreeObserver = binding.assistantConstraintLayout.getViewTreeObserver()

            if(viewTreeObserver.isAlive)
            {
                viewTreeObserver.addOnGlobalLayoutListener( object : ViewTreeObserver.OnGlobalLayoutListener{
                    override fun onGlobalLayout() {
                       circularReaalActivity()
                        binding.assistantConstraintLayout.getViewTreeObserver()
                            .removeOnGlobalLayoutListener(this)
                    }

                })
            }

        }


    }



}