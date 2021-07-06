package com.example.mymemory

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymemory.models.BoardSize
import com.example.mymemory.utils.EXTRA_BOARD_SIZE
import com.example.mymemory.utils.isPermissionGranted
import com.example.mymemory.utils.requestPermission

class CreateActivity : AppCompatActivity() {

    companion object {
        private const val PICK_PHOTOS_CODE = 32
        private const val READ_EXTERNAL_PHOTOS_CODE = 312
        private const val READ_PHOTOS_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val TAG = "CreateActivity"
    }

    private lateinit var boardSize: BoardSize
    private var numImagesRequired = -1
    private val chosenImagesUris = mutableListOf<Uri>()
    private lateinit var imagePickerAdapter: ImagePickerAdapter

    private lateinit var rvImagePicker: RecyclerView
    private lateinit var etGameName: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        numImagesRequired = boardSize.getNumPairs()
        supportActionBar?.title = "Choose pics 0/$numImagesRequired"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvImagePicker = findViewById(R.id.rvImagePicker)
        etGameName = findViewById(R.id.etGameName)
        btnSave = findViewById(R.id.btnSave)

        imagePickerAdapter = ImagePickerAdapter(this,chosenImagesUris, boardSize, object: ImagePickerAdapter.ImageClickListener{
            override fun onPlaceholderClicked() {
                if( isPermissionGranted(this@CreateActivity, READ_PHOTOS_PERMISSION) ) {
                    launchIntentForPhotos()
                } else {
                    requestPermission(this@CreateActivity, READ_PHOTOS_PERMISSION, READ_EXTERNAL_PHOTOS_CODE)
                }
            }
        })

        rvImagePicker.adapter = imagePickerAdapter
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if( requestCode == READ_EXTERNAL_PHOTOS_CODE ) {
            if (  grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                launchIntentForPhotos()
            } else {
                Toast.makeText(this, "Permission required to upload photos", Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if( requestCode != PICK_PHOTOS_CODE || resultCode != Activity.RESULT_OK || data == null ) {
            Log.w( TAG, "No response back from launch activity")
            return
        }
        val selectedUri: Uri? = data.data
        val clipData: ClipData? = data.clipData
        if ( clipData != null ) {
            Log.i(TAG, "clipData numImages ${clipData.itemCount} : $clipData")
            for(i in 0 until clipData.itemCount ) {
                val clipItem = clipData.getItemAt(i)
                if( chosenImagesUris.size < numImagesRequired ) {
                    chosenImagesUris.add(clipItem.uri)
                }
            }
        } else if ( selectedUri != null ) {
            Log.i(TAG, "data: $selectedUri")
            chosenImagesUris.add(selectedUri)
        }
        imagePickerAdapter.notifyDataSetChanged()
        supportActionBar?.title = "Choose pics ${chosenImagesUris.size} / $numImagesRequired"
        btnSave.isEnabled = shouldEnableSaveButton()
    }

    private fun shouldEnableSaveButton(): Boolean {
        return chosenImagesUris.size == numImagesRequired
    }

    private fun launchIntentForPhotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Choose Pics"), PICK_PHOTOS_CODE)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if ( item.itemId == android.R.id.home ) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}