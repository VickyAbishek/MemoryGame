package com.example.mymemory

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns.BITRATE
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymemory.models.BoardSize
import com.example.mymemory.utils.BitmapScaler
import com.example.mymemory.utils.EXTRA_BOARD_SIZE
import com.example.mymemory.utils.isPermissionGranted
import com.example.mymemory.utils.requestPermission
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {

    companion object {
        private const val PICK_PHOTOS_CODE = 32
        private const val READ_EXTERNAL_PHOTOS_CODE = 312
        private const val READ_PHOTOS_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val TAG = "CreateActivity"
        private const val MIN_GAME_NAME_LENGTH = 3
        private const val MAX_GAME_NAME_LENGTH = 14
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

        btnSave.setOnClickListener {
            saveDataToFirebase()
        }
        etGameName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAME_NAME_LENGTH))
        etGameName.addTextChangedListener {
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    btnSave.isEnabled = shouldEnableSaveButton()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            } }


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
        if ( chosenImagesUris.size != numImagesRequired ||  etGameName.text.isBlank() || etGameName.text.length < MIN_GAME_NAME_LENGTH ) {
            return false
        }
        return true
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

    private fun saveDataToFirebase() {
        for( (index:Int, photoUri: Uri) in chosenImagesUris.withIndex() ) {
            var imageByteArray = getImageByteArray(photoUri)
        }
    }

    private fun getImageByteArray(photoUri: Uri): ByteArray {
        val originalBitmap = if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ) {
            val source = ImageDecoder.createSource(contentResolver, photoUri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
        }

        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 250)
        val byteOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
        return byteOutputStream.toByteArray()
    }

}