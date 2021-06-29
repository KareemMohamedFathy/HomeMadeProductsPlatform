package com.homemadeproductsapp.MyStore.ItemsAndFeed


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*

import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.homemadeproductsapp.BuildConfig
import com.homemadeproductsapp.DB.Local.StoreSession
import com.homemadeproductsapp.DB.Product
import com.homemadeproductsapp.FileSelectorFragment
import com.homemadeproductsapp.MyStore.MyStoreActivity
import com.homemadeproductsapp.MyStore.OnOptionClickListener
import com.homemadeproductsapp.R
import com.mindorks.notesapp.data.local.pref.PrefConstant
import kotlinx.android.synthetic.main.activity_create_item.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CreateItemActivity : AppCompatActivity(), OnOptionClickListener,AdapterView.OnItemSelectedListener {
    companion object {
        private const val MY_PERMISSION_CODE = 124
        private const val REQUEST_CODE_CAMERA = 1
        private const val REQUEST_CODE_GALLERY = 2
    }
    private var picturePath = ""

    private lateinit var imageLocation: File
    private lateinit var editTextPrice:TextView
    private lateinit var editTextName:TextView
    private lateinit var editTextCopies:TextView
    private lateinit var editTextDescription:TextView
    private lateinit var buttonAddItem:Button
    private lateinit var store_id:String
    private lateinit var dbReference: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var imageViewAddItem: ImageSwitcher
    private lateinit var imageViewAddImageView: ImageView

    private lateinit var mainCategory:String
    private  var subCategories= arrayListOf<String>()
    private lateinit var subcategory:String

    private  val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private  var curUser=auth.currentUser!!.uid

    private var images: ArrayList<Uri?>? = null
    private lateinit var nextBtn:ImageView
    private lateinit var  spin:Spinner

    private lateinit var previousBtn:ImageView
    private  var stringUri=ArrayList<String>()

    //current position/index of selected images
    private var position = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_item)

        bindViews()
        images = ArrayList()

        //    imageViewImageSwitcher.setFactory { imageViewAddItem}
         imageViewImageSwitcher.setFactory(ViewSwitcher.ViewFactory { // TODO Auto-generated method stub

  // Create a new ImageView and set it's properties
             imageViewAddImageView  = ImageView(applicationContext)
             imageViewAddImageView.scaleType = ImageView.ScaleType.FIT_XY
             imageViewAddImageView
          })
      /*  imageViewImageSwitcher.setFactory(ViewSwitcher.ViewFactory { // TODO Auto-generated method stub

// Create a new ImageView and set it's properties
            val imageView = ImageView(applicationContext)
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            imageView.layoutParams = FrameLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT)
            imageView
        })*/
    setupSharedPreference()
        setupToolbarText()
        getIntentData()
     setupClickListeners()

    }
    private fun setupSharedPreference() {
        StoreSession.init(this)
    }
    private fun setupToolbarText() {
        if (supportActionBar != null) {
            getSupportActionBar()!!.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar()!!.setCustomView(R.layout.actionbar);
            val view = supportActionBar!!.customView
            var textViewTitle: TextView =view.findViewById(R.id.action_bar_title)
            textViewTitle.setText("Add Your Prodcut")
            var back: ImageView =view.findViewById(R.id.action_bar_Image)
            back.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    val intent: Intent = Intent(this@CreateItemActivity, MyStoreActivity::class.java)
                   startActivity(intent)
                    finish()
                }
            }
            )

        }
    }

    private fun getIntentData() {
        val intent = intent

        if (intent.hasExtra("store_id")) {
            store_id= intent.getStringExtra("store_id").toString()
        }
        if(!store_id.isEmpty()) {
            mainCategory = StoreSession.readString(PrefConstant.MAINCATEGORY)!!
            getSubCategories(mainCategory)

        }
    }

    private fun getSubCategories(mainCategory: String) {
        val reference = FirebaseDatabase.getInstance().reference

        val query = reference.child("Category").orderByChild("mainCategory").equalTo(mainCategory)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                subCategories.add("select subcategory")
                if (dataSnapshot.exists()) {
                    for (dsp in dataSnapshot.children) {

                        val subCategory = dsp.child("subCategory").value.toString()
                        subCategories.add(subCategory)

                    }
                }
               setupSpinners()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        )


    }

    private fun setupSpinners() {
        spin = findViewById<Spinner>(R.id.spinnerSubCategory)
        spin.setPrompt("Pick One");


        spin.onItemSelectedListener = this
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                subCategories)
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item)

        spin.adapter = adapter
    }

    private fun setupClickListeners() {
    val clickAction=object : View.OnClickListener{
        override fun onClick(v: View?) {
            val name = editTextName.text.toString()
            val price = editTextPrice.text.toString()
            val copies = editTextCopies.text.toString()
            val description = editTextDescription.text.toString()

            firebaseDatabase = FirebaseDatabase.getInstance()
            dbReference = firebaseDatabase.getReference("Product")
            val productId = dbReference.push().key.toString()
            for (uri in images!!) {
                stringUri.add(uri.toString())
            }
            if (images!!.size==0) {
                Toast.makeText(this@CreateItemActivity, "Plz add at least 1 photo", Toast.LENGTH_SHORT).show()
            }
        else    if (subcategory == "select subcategory") {
                Toast.makeText(this@CreateItemActivity, "Plz choose category", Toast.LENGTH_SHORT).show()
            }
            else    if (TextUtils.isEmpty(name)||TextUtils.isEmpty(price)||TextUtils.isEmpty(description)) {
                Toast.makeText(this@CreateItemActivity, "Plz fill all data", Toast.LENGTH_SHORT).show()
            }
          else  if(name.length>30){
                Toast.makeText(this@CreateItemActivity,"Max number of character for store name is 30",Toast.LENGTH_SHORT).show()
            }
            else if(description.length>150){
                Toast.makeText(this@CreateItemActivity,"Max number of character for store description is 150",Toast.LENGTH_SHORT).show()
            }


            else{
                val p: Product = Product(name, productId, copies.toInt(), "Yes", price.toDouble(), description, picturePath, store_id, subcategory, stringUri)
                dbReference.child(productId).setValue(p)

                intent = Intent(this@CreateItemActivity, MyStoreActivity::class.java)
                startActivity(intent)
                finish()

            }
        }
    }
        buttonAddItem.setOnClickListener(clickAction)
        imageViewAddItem.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (checkAndRequestPermissions()) {
                    openPicker()
                }
            }


        })
        nextBtn.setOnClickListener {
            if (position < images!!.size-1){
                position++
                imageViewAddItem.setImageURI(images!![position])
            }
            else{
                position=0;
                //no more images
                imageViewAddItem.setImageURI(images!![position])

            }
        }

        //switch to previous image clicking this button
        previousBtn.setOnClickListener {
            if (position > 0){
                position--
                imageViewAddItem.setImageURI(images!![position])
            }
            else{
                position=images!!.size-1
                imageViewAddItem.setImageURI(images!![position])

            }

        }

    }

    private fun openPicker() {
   val dialog=FileSelectorFragment.newInstance()
     dialog.show(supportFragmentManager, FileSelectorFragment.TAG)
    }

    private fun bindViews() {
        editTextPrice=findViewById(R.id.editTextPrice)
        editTextCopies=findViewById(R.id.editTextCopies)
        editTextDescription=findViewById(R.id.editTextDescription)
        editTextName=findViewById(R.id.editTextName)
        buttonAddItem=findViewById(R.id.submit_button)
        imageViewAddItem=findViewById(R.id.imageViewImageSwitcher)
        imageViewAddImageView=findViewById(R.id.imageViewAddItem)
        nextBtn=findViewById(R.id.ImageViewNext)
        previousBtn=findViewById(R.id.ImageViewBack)
    }
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val mFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(mFileName, ".jpg", storageDir)
    }
    override fun onCameraClick() {

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {

            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(this@CreateItemActivity, BuildConfig.APPLICATION_ID + ".provider", photoFile)
                imageLocation = photoFile
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA)
            }
        }
    }
    private fun checkAndRequestPermissions(): Boolean {
        val permissionCAMERA = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        val listPermissionsNeeded = ArrayList<String>()
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionCAMERA != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray<String>(), MY_PERMISSION_CODE)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openPicker()
                }
            }
        }
    }

    override fun onGalleryClick() {

            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_GALLERY);


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        images=ArrayList<Uri?>()

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CAMERA -> {
                               picturePath = imageLocation.path.toString()
                    images!!.add(picturePath.toUri())
                    imageViewAddItem.setImageURI(images!![0])

                }
                REQUEST_CODE_GALLERY -> {
                    if (data?.getClipData() != null) {
                        var count = data.clipData!!.itemCount
                        for (i in 0 until count) {

                            var imageUri: Uri = data.clipData!!.getItemAt(i).uri
                            images!!.add(imageUri)
                            val contentResolver = applicationContext.contentResolver
                            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                            imageUri?.let { contentResolver.takePersistableUriPermission(it, takeFlags) }


                            //    imageViewAddItem.setImageURI(imageUri)
                        }
                        imageViewAddItem.setImageURI(images!![0])
                        position = 0;


                    } else if (data?.getData() != null) {
                        // if single image is selected

                        var imageUri: Uri = data.data!!
                        images!!.add(imageUri)

                        imageViewAddItem.setImageURI(images!![0])

                        position = 0;

                    }

                }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        subcategory=subCategories[position]
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

   }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent: Intent = Intent(this@CreateItemActivity, MyStoreActivity::class.java)
        startActivity(intent)
        finish()
    }

}
