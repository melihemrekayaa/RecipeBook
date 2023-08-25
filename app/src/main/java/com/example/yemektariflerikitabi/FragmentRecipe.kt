package com.example.yemektariflerikitabi

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.yemektariflerikitabi.databinding.FragmentRecipeBinding
import java.io.ByteArrayOutputStream
import java.lang.Exception


class FragmentRecipe : Fragment() {

    private lateinit var binding: FragmentRecipeBinding

    var selectedImage: Uri ?= null
    var selectedBitmap: Bitmap ?= null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentRecipeBinding.inflate(inflater, container ,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectImageIcon.setOnClickListener {
            selectImage(it)
        }
        binding.saveBtn.setOnClickListener {
            save(it)
        }

        arguments?.let {
            val incomingInformation = FragmentRecipeArgs.fromBundle(it).information

            if (incomingInformation == "menu"){
                binding.foodNameText.setText("")
                binding.foodItemText.setText("")
                binding.saveBtn.visibility = View.VISIBLE

                val selectImageBackground = BitmapFactory.decodeResource(context?.resources,R.drawable.select_image)
                binding.selectImageIcon.setImageBitmap(selectImageBackground)
            }
            else{
                binding.saveBtn.visibility = View.INVISIBLE
            }

            val selectedId = FragmentRecipeArgs.fromBundle(it).id

            context?.let {
                try {
                    val db = it.openOrCreateDatabase("Meals",Context.MODE_PRIVATE,null)
                    val cursor = db.rawQuery("SELECT * FROM meals WHERE id = ?", arrayOf(selectedId.toString()))

                    val mealNameIndex = cursor.getColumnIndex("mealName")
                    val mealItemsIndex = cursor.getColumnIndex("mealItems")
                    val imageIndex = cursor.getColumnIndex("image")

                    while (cursor.moveToNext()){
                        binding.foodNameText.setText(cursor.getString(mealNameIndex))
                        binding.foodItemText.setText(cursor.getString(mealItemsIndex))

                        val byteArray = cursor.getBlob(imageIndex)
                        val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                        binding.selectImageIcon.setImageBitmap(bitmap)

                    }
                    cursor.close()
                }
                catch (e:Exception){
                    e.printStackTrace()
                }

            }
        }
    }

    fun save(view: View){
        val mealName = binding.foodNameText.text.toString()
        val mealItems = binding.foodItemText.text.toString()

        if (selectedBitmap != null){
            val smallBitmap = createSmallBitmap(selectedBitmap!!,300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            try{
                context?.let {

                    val database = it.openOrCreateDatabase("Meals",Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS meals (" +
                            "id INTEGER PRIMARY KEY," +
                            "mealName VARCHAR," +
                            "mealItems VARCHAR," +
                            "image BLOB)")

                    val sqlString = "INSERT INTO meals (mealName,mealItems,image) VALUES (?,?,?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1,mealName)
                    statement.bindString(2,mealItems)
                    statement.bindBlob(3,byteArray)
                    statement.execute()

                }
            }
            catch (e:Exception){
                e.printStackTrace()
            }

            val action = FragmentRecipeDirections.actionFragmentRecipe2ToFragmentList()
            Navigation.findNavController(view).navigate(action)
        }
    }
    fun selectImage(view: View) {
            activity?.let {

                if (ContextCompat.checkSelfPermission(it.applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
                }
                else{
                    val galleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent,2)
                    }
            }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1){
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val galleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent,2)
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){

            selectedImage = data.data

            try {
                context?.let {

                    if (selectedImage != null){
                        if (Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(it.contentResolver,selectedImage!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.selectImageIcon.setImageBitmap(selectedBitmap)
                        }
                        else{
                            selectedBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,selectedImage)
                            binding.selectImageIcon.setImageBitmap(selectedBitmap)
                        }
                    }
                }
            }
            catch (e: Exception){
                e.printStackTrace()
            }

        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun createSmallBitmap(bitmapByUser: Bitmap, maxSize: Int) :Bitmap{
        var width = bitmapByUser.width
        var height = bitmapByUser.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1){
            width = maxSize
            val shortenedHeight = width / bitmapRatio
            height = shortenedHeight.toInt()
        }
        else{
            height = maxSize
            val shortenedWidth = height * bitmapRatio
            width = shortenedWidth.toInt()
        }

        return Bitmap.createScaledBitmap(bitmapByUser,width ,height,true)
    }




}