package com.example.selfieapp

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.selfieapp.activities_login.LoginActivity
import com.example.selfieapp.adapters.AdapterRecyclerMain
import com.example.selfieapp.helpers.Config
import com.example.selfieapp.models.Image
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var databaseReference: DatabaseReference
    var auth = FirebaseAuth.getInstance()

    val REQUEST_CODE_CAMERA = 101
    val REQUEST_CODE_GALLERY = 102

    lateinit var adapterRecyclerMain: AdapterRecyclerMain

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(auth.currentUser==null)
        {startActivity(Intent(this,LoginActivity::class.java)) }

        databaseReference = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_DATABASE_NAME)
        init()
    }

    private fun init() {
        setupToolBar()
        setupAdapter()
        updateRecyclerView()
        fab_take_photo.setOnClickListener(this)
        fab_gallery.setOnClickListener(this)
    }

    private fun setupAdapter() {
        adapterRecyclerMain = AdapterRecyclerMain(this)
        recycler_view_main.layoutManager = GridLayoutManager(this,2)
        recycler_view_main.adapter = adapterRecyclerMain
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab_take_photo ->{
                requestMultiplePermissions()
            }
            R.id.fab_gallery ->{
                choosePhotoFromGallery()
            }
        }
    }

    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY)
    }

    private fun requestMultiplePermissions() {
        Dexter.withActivity(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            )
            .withListener(object: MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    // check if all permissions are granted
                    if (report!!.areAllPermissionsGranted())
                    {
                        Toast.makeText(applicationContext,"All Permissions are granted", Toast.LENGTH_SHORT).show()
                        openCamera()
                    }
                    //check for permanent denial of any permission
                    if(report.isAnyPermissionPermanentlyDenied){
                        Toast.makeText(applicationContext,"permission denied permantly", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(//9:00 04_09
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .onSameThread()
            .check()
    }

    private fun openCamera() {
        var intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intentCamera,REQUEST_CODE_CAMERA)
    }

    private fun showDialogue(){
        var builder = AlertDialog.Builder(this)
        builder.setTitle("Need Permissions")
        builder.setMessage("To access the following feature, Please give permission")
        builder.setPositiveButton("go to setting", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
                openSettings()
            }
        })
        builder.setNegativeButton("Cancel",object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }
        })
        builder.show()
    }

    //navigate to your app settings
    private fun openSettings(){
        var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        var uri = Uri.fromParts("package", packageName,null)
        intent.setData(uri)
        startActivityForResult(intent,101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE_GALLERY){
            if(data !=null){
                var contentUri = data.data

                try{
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,contentUri)
                    Toast.makeText(applicationContext, "Gallery", Toast.LENGTH_SHORT).show()

                    var path =  getImagePath(bitmap)//change bitmap to path string
                    Log.i("pathLog", "$path")
                    pushToFirebase(path) //save it into the firebase
                    updateRecyclerView()  //update the recycler view

                } catch (e: IOException){
                    e.printStackTrace()
                    Toast.makeText(applicationContext,e.message,Toast.LENGTH_SHORT).show()
                }
            }
        }else if(requestCode == REQUEST_CODE_CAMERA){
            val thumbnail = data?.extras?.get("data") as Bitmap
            //change bitmap to path string
            var path =  getImagePath(thumbnail)
            Log.i("pathLog", "$path")

            pushToFirebase(path) //save the image into the firebase
            updateRecyclerView() //update the recycler view
            Toast.makeText(applicationContext,"Camera ",Toast.LENGTH_SHORT).show()
        }
    }

    fun getImagePath(inImage:Bitmap):String {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(this.getContentResolver(), inImage, "Title", null)
        return path
    }
    private fun pushToFirebase(path: String) {

        var imageId = databaseReference.push().key
        var image = Image(imageId, path)

        databaseReference.child(imageId!!).setValue(image)
    }

    private fun updateRecyclerView(){
        //read from database here , then update the recycler view
       databaseReference.addValueEventListener(object: ValueEventListener{
           override fun onCancelled(p0: DatabaseError) {TODO("Not yet implemented") }

           override fun onDataChange(dataSnapshot: DataSnapshot) {
               var imageList: ArrayList<Image> = ArrayList()
            for (data in dataSnapshot.children){
                var image: Image? = data.getValue(
                    Image::class.java)
                image?.imageId = data.key
                imageList.add(image!!)
             }
               adapterRecyclerMain.setData(imageList)
           }
       })
    }

    private fun setupToolBar(){
        var toolbar = myCustomToolbar
        toolbar.title = "SELFIE"
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_home ->{
            }
            R.id.menu_logout ->{
                auth.signOut()
                startActivity(Intent(this,LoginActivity::class.java))
            }
        }
        return true
    }
}