package com.example.parsaniahardik.upload_image_volley_multipart

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.example.parsaniahardik.upload_image_volley_multipart.VolleyMultipartRequest.DataPart
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap

class MainActivity : AppCompatActivity() {

    lateinit var btn: Button
    lateinit var imageView: ImageView
    private val GALLERY = 1
    private val uploadURL = "http://192.168.2.8:9090/PostPokedex/upload_file"
    lateinit var rQueue: RequestQueue
//    private var arraylist: ArrayList<HashMap<String, String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestMultiplePermissions()

        btn = findViewById(R.id.btn)
        imageView = findViewById<View>(R.id.iv) as ImageView

        btn.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            startActivityForResult(galleryIntent, GALLERY)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val contentURI = data.data
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED) {
            return
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                try {

//                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(contentURI))
//                    imageView?.setImageBitmap(bitmap)
                    uploadImage(bitmap)

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadImage(bitmap: Bitmap) {

        val volleyMultipartRequest =  object : VolleyMultipartRequest(Method.POST, uploadURL,
                Response.Listener { response ->
                    Log.d("ressssssoo", String(response.data))
//                    rQueue.cache.clear()
                    try {
                        val jsonObject = JSONObject(String(response.data))
                        Toast.makeText(applicationContext, jsonObject.getString("success"), Toast.LENGTH_SHORT).show()

//                        jsonObject.toString().replace("\\\\", "")

                        if (jsonObject.getString("success") == "1") {
//                            Toast.makeText(this, "success", Toast.LENGTH_LONG).show()
                            if (jsonObject.getString("token") == "success upload") {
                                Log.d("Bisa upload", "sukses")
                            }

//                            arraylist = ArrayList()
//                            val dataArray = jsonObject.getJSONArray("data")

//                            var url = ""
//                            for (i in 0 until dataArray.length()) {
//                                val dataobj = dataArray.getJSONObject(i)
//                                url = dataobj.optString("pathToFile")
//                            }
//                            Picasso.get().load(url).into(imageView)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                    Log.d("error", "${error.message}")
                }) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
//            @Throws(AuthFailureError::class)
//            override fun getParams(): Map<String, String> {
//                val params = HashMap<String, String>()
////                add string parameters
//                params.put("image", "ccccc");
//                return HashMap()
//            }

            /*
             *pass files using below method
             * */
//            @Throws(AuthFailureError::class)
            override fun getByteData(): Map<String, DataPart> {
                val params = HashMap<String, DataPart>()
                val imagename = System.currentTimeMillis()
                params.put("image", DataPart("$imagename.png", getFileDataFromDrawable(bitmap)))
                return params
            }
        }


        volleyMultipartRequest.retryPolicy = DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        rQueue = Volley.newRequestQueue(this@MainActivity)
        rQueue.add(volleyMultipartRequest)
    }

     private fun getFileDataFromDrawable(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun requestMultiplePermissions() {
        Dexter.withActivity(this)
                .withPermissions(

                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(applicationContext, "All permissions are granted by user!", Toast.LENGTH_SHORT).show()
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied) {
                            // show alert dialog navigating to Settings

                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).withErrorListener { Toast.makeText(applicationContext, "Some Error! ", Toast.LENGTH_SHORT).show() }
                .onSameThread()
                .check()
    }

}
