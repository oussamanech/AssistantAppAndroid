package com.example.assistantmajda

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.assistantmajda.assistant.AssistantActivity
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private lateinit var imageButton: ImageButton
    val RecordAudioRequestCode: Int = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageButton=findViewById(R.id.assistant_action_button_main)

        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.RECORD_AUDIO)
        != PackageManager.PERMISSION_GRANTED){
            checkPermission()

        }

        imageButton.setOnClickListener {
            startActivity(Intent(this,AssistantActivity::class.java))
        }

    }//end onCreate

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == RecordAudioRequestCode && grantResults.size > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permision Granted",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO),
            RecordAudioRequestCode)
        }
    }


}