package hieusenpaj.com.whapp.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import hieusenpaj.com.whapp.R
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {
    var currentUserId = ""
    var mAuth: FirebaseAuth? = null
    var rootRef: DatabaseReference? = null
    var userProfile: StorageReference? = null
    var loading: ProgressDialog? = null
    var image = ""
    var uri: Uri? = null
    var currentUser: FirebaseUser? = null
    var uriFull: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        currentUserId = FirebaseAuth.getInstance()!!.currentUser!!.uid
        rootRef = FirebaseDatabase.getInstance().reference
        userProfile = FirebaseStorage.getInstance().reference.child("Profile Image")

        loading = ProgressDialog(this)
        btn_update.setOnClickListener {
            updateSetting()
        }
        profile_image.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onStart() {
        super.onStart()


            rootRef!!.child("Users").child(currentUserId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0!!.hasChild("name")){
                            startActivity(Intent(this@SettingActivity, MainActivity::class.java))
                        }else{
                            rl_setting.visibility = View.VISIBLE

                        }
                    }

                })


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            uriFull = data.data

            CropImage.activity(uriFull)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this)

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                uri = result.uri
//                Toast.makeText(context,uri.toString(),Toast.LENGTH_SHORT).show()
                image = "hieu"
                Glide.with(this).load(uri)
                    .apply(RequestOptions().placeholder(R.drawable.profile_image).error(R.drawable.profile_image))
                    .into(profile_image)
            }
        }

    }
    private fun updateName(){
        val sharedPreferences = getSharedPreferences("hieu", Context.MODE_PRIVATE)
        val edit = sharedPreferences.edit()
        edit.putBoolean("have_name",true)
        edit.apply()
    }


    private fun updateSetting() {
        val name = ed_user_name.text.toString()


        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Chen du vao", Toast.LENGTH_SHORT).show()
        }
       else {

            if (image == "") {
                var hashMap: HashMap<String, Any> = HashMap()
                hashMap["uid"] = currentUserId
                hashMap["name"] = name

                rootRef!!.child("Users").child(currentUserId).setValue(hashMap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finishAffinity()
                            updateName()
                            Toast.makeText(this, "Update thanh cong", Toast.LENGTH_SHORT).show()

                        }
                    }
            } else {
                loading!!.setTitle("Cho ty")
                loading!!.setCancelable(false)
                loading!!.show()
                val path = userProfile!!.child("$currentUserId.jpg")
                val pathFull = userProfile!!.child("${currentUserId}Full.jpg")

                path.putFile(uri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        image = task.result.downloadUrl.toString()
                        pathFull.putFile(uriFull!!).addOnCompleteListener{
                            if (task.isSuccessful){
                                var hashMap: HashMap<String, Any> = HashMap()
                                hashMap["uid"] = currentUserId
                                hashMap["name"] = name
                                hashMap["image"] = image
                                hashMap["image_full"] = it.result.downloadUrl.toString()
                                rootRef!!.child("Users").child(currentUserId).setValue(hashMap)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            startActivity(Intent(this, MainActivity::class.java))
                                            finishAffinity()
                                            updateName()
                                            loading!!.dismiss()
                                            Toast.makeText(this, "Update thanh cong", Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    }
                            }

                        }

                    }
                }


            }
        }

    }


}
