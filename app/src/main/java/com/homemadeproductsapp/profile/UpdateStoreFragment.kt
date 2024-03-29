package com.homemadeproductsapp.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.Group
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.homemadeproductsapp.DB.Store
import com.homemadeproductsapp.DB.User
import com.homemadeproductsapp.FileSelectorFragment
import com.homemadeproductsapp.R
import com.mindorks.notesapp.data.local.pref.PrefConstant
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UpdateStoreFragment : Fragment() {
        private lateinit var editTextName: EditText
        private lateinit var editTextEmail: EditText
        private lateinit var editTextDescription: EditText
        private lateinit var imageViewStorePhoto: ImageView
        private lateinit var imageViewBack: ImageView

        private lateinit var buttonUpdateInfo: Button
        private lateinit var view1: View
        private var picturePath = ""
        private lateinit var imageLocation: File



        companion object {
            private const val MY_PERMISSION_CODE = 124
            private const val REQUEST_CODE_CAMERA = 1
            private const val REQUEST_CODE_GALLERY = 2
        }
        private lateinit var onMoveClick:onMoveClick1

        private lateinit var auth: FirebaseAuth
        private lateinit var dbReference: DatabaseReference
        private lateinit var firebaseDatabase: FirebaseDatabase
        private lateinit var  progressBar:ProgressBar
        private lateinit var group: Group
    private lateinit var textViewHelper:TextView

    private lateinit var datacommunication: datacommunication
        private lateinit var circularProgressDrawable:CircularProgressDrawable
    override fun onAttach(context: Context) {
        super.onAttach(context)
    onMoveClick=context as onMoveClick1
        datacommunication=context as datacommunication
    }

        override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view1= inflater.inflate(R.layout.fragment_update_store, container, false)
            return view1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    bindViews(view1)
        circularProgressDrawable = CircularProgressDrawable(requireContext())
        circularProgressDrawable.strokeWidth = 10f
        circularProgressDrawable.centerRadius = 50f
        circularProgressDrawable.start()
        if(datacommunication.store!=null) {

            getStoreData()
            setupClickListeners()
            requireActivity()
                    .onBackPressedDispatcher
                    .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            // Do custom work here

                            onMoveClick.onBack()

                        }
                    }
                    )

        }
    }
    private fun setupClickListeners() {
        imageViewStorePhoto.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (checkAndRequestPermissions()) {
                    openPicker()
                    val name = editTextName.text.toString()
                    val description = editTextDescription.text.toString()
                    datacommunication.store!!.store_name=name
                    datacommunication.store!!.store_description=description

                }
            }


        })
        buttonUpdateInfo.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                auth = FirebaseAuth.getInstance()
                val id = auth.currentUser!!.uid
                val name = editTextName.text.toString()
                val description = editTextDescription.text.toString()
                val category = datacommunication.store!!.mainCategoryName

                val storeid = datacommunication.store!!.store_id
                val path = datacommunication.store!!.store_logo
                if(name.length>30){
                    Toast.makeText(requireContext(),"Max number of character for store name is 30", Toast.LENGTH_SHORT).show()
                }
                else if(description.length>100){
                    Toast.makeText(requireContext(),"Max number of character for store description is 100", Toast.LENGTH_SHORT).show()
                }
                else {
                    val store = Store(storeid.toString(), name, path.toString(), description, category, id)
                    val lol = FirebaseDatabase.getInstance().getReference("Store")
                            .child(storeid!!).setValue(store)
                    onMoveClick.onBack()
                }
                }


        })
        imageViewBack.setOnClickListener(View.OnClickListener {
            onMoveClick.onBack()
        })

    }

    private fun bindViews(view1: View) {
        editTextName = view1.findViewById(R.id.editTextName)
        editTextDescription = view1.findViewById(R.id.editTextDescription)
        imageViewStorePhoto = view1.findViewById(R.id.storePic)
        buttonUpdateInfo = view1.findViewById(R.id.buttonUpdateProfile)
        imageViewBack=view1.findViewById(R.id.back)
        progressBar=view1.findViewById(R.id.progressBar)
        textViewHelper=view1.findViewById(R.id.helperText1)
        group=view1.findViewById(R.id.group1)
    }





    override fun onResume() {
        super.onResume()

        getStoreData()
    }


    private fun getStoreData() {




       editTextName.setText(datacommunication.store!!.store_name)
        editTextDescription.setText(datacommunication.store!!.store_description)

        val path= datacommunication.store!!.store_logo
        Log.d("bagga",path.toString()+"kuso")

            if (path.isNotEmpty()) {

                if ("firebasestorage" !in path) {
                    Log.d("kuso", "bagga")

                    progressBar.visibility = View.VISIBLE
                    group.visibility = View.GONE
                    textViewHelper.visibility = View.VISIBLE


                    uploadImageToFirebase(path.toUri())
                } else {
                    Glide.with(requireContext()).load(path).placeholder(circularProgressDrawable).into(imageViewStorePhoto)
                }
            }



    }




    private fun checkAndRequestPermissions(): Boolean {
        val permissionCAMERA =
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val listPermissionsNeeded = ArrayList<String>()
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionCAMERA != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                    requireActivity(), listPermissionsNeeded.toTypedArray<String>(),
                    MY_PERMISSION_CODE
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openPicker()
                }
            }
        }
    }

    private fun openPicker() {
        val dialog = FileSelectorFragment.newInstance()
        dialog.show(requireActivity().supportFragmentManager, FileSelectorFragment.TAG)
    }


    private fun uploadImageToFirebase(fileUri: Uri) {

        if (fileUri != null) {

            val fileName = UUID.randomUUID().toString() +".jpg"

            val database = FirebaseDatabase.getInstance()
            val refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")

            refStorage.putFile(fileUri)
                    .addOnSuccessListener(
                            OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                                taskSnapshot.storage.downloadUrl.addOnSuccessListener {

                                    val imageUrl = it
                                    picturePath=imageUrl.toString()
                                    Log.d("haha",imageUrl.toString())
                                    Glide.with(this).load(imageUrl).skipMemoryCache(false).placeholder(circularProgressDrawable).into(imageViewStorePhoto)
                                    datacommunication.store!!.store_logo=picturePath

                                    progressBar.visibility=View.GONE
                                    group.visibility=View.VISIBLE
                                    textViewHelper.visibility=View.GONE

                                }
                            })


                    ?.addOnFailureListener(OnFailureListener { e ->
                        print(e.message)
                    })
        }
    }

}
interface onMoveClick1 {
    fun onBack()


}

