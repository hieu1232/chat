package hieusenpaj.com.whapp.fragment


import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ListPopupWindow
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

import hieusenpaj.com.whapp.R
import hieusenpaj.com.whapp.activity.FindFriendActivity
import hieusenpaj.com.whapp.activity.LoginActivity
import hieusenpaj.com.whapp.activity.SettingActivity
import hieusenpaj.com.whapp.adapter.ItemMainAdapter
import hieusenpaj.com.whapp.dialog.DialogCreatGroup
import hieusenpaj.com.whapp.objet.ItemMain
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.ed_user_name
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {
    var currentUserId = ""
    var mAuth: FirebaseAuth? = null
    var rootRef: DatabaseReference? = null
    var userProfile: StorageReference? = null
    var loading: ProgressDialog? = null
    var image = ""
    var uri: Uri? = null
    var uriFull: Uri? = null
    var edit = false
    var popup: ListPopupWindow? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance()
        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        rootRef = FirebaseDatabase.getInstance().reference
        userProfile = FirebaseStorage.getInstance().reference.child("Profile Image")
        context!!.registerReceiver(br, IntentFilter("CROP"))
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loading = ProgressDialog(context)
        user_profile_image.setOnClickListener {
            if(edit) {
                val intent = Intent()
                intent.action = Intent.ACTION_GET_CONTENT
                intent.type = "image/*"
                startActivityForResult(intent, 100)
            }
        }
        setupEdit()
        iv_log_out.setOnClickListener {
            showListPopupWindow(it)
        }


    }
    

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            uriFull = data.data

            CropImage.activity(uriFull)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(activity!!)

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                uri = result.uri
//                Toast.makeText(context,uri.toString(),Toast.LENGTH_SHORT).show()
                image = "hieu"

                loading!!.setTitle("Cho ty")
                loading!!.setCancelable(false)
                loading!!.show()
                val path = userProfile!!.child("$currentUserId.jpg")
                val pathFull = userProfile!!.child("${currentUserId}Full.jpg")

                path.putFile(uri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        image = task.result.downloadUrl.toString()
                        pathFull.putFile(uriFull!!).addOnCompleteListener {
                            if (task.isSuccessful) {
                                var hashMap: HashMap<String, Any> = HashMap()
                                hashMap["uid"] = currentUserId
                                hashMap["image"] = image
                                hashMap["image_full"] = it.result.downloadUrl.toString()
                                rootRef!!.child("Users").child(currentUserId).updateChildren(hashMap)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Glide.with(context!!.applicationContext).load(uri)
                                                .apply(RequestOptions().placeholder(R.drawable.profile_image).error(R.drawable.profile_image))
                                                .into(user_profile_image)
                                            Glide.with(context!!.applicationContext).load(uriFull)
                                                .apply(RequestOptions().placeholder(R.drawable.profile_image).error(R.drawable.profile_image))
                                                .into(iv_user_full)
                                            turnoffEdit()
                                            loading!!.dismiss()
                                            Toast.makeText(
                                                context,
                                                "Update thanh cong",
                                                Toast.LENGTH_SHORT
                                            )
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

    override fun onDestroy() {
        super.onDestroy()
        context!!.unregisterReceiver(br)
    }
    private var br = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            Toast.makeText(context,"hieu",Toast.LENGTH_SHORT).show()


        }

    }
    private fun setupEdit() {
        iv_edit.setOnClickListener {
            edit = true
            iv_edit.visibility = View.GONE
            ed_user_name.isEnabled = true
            iv_cancel.visibility = View.VISIBLE
            rl_change.visibility = View.VISIBLE
            rl_change.setOnClickListener {
                updateSetting()
            }


        }
        iv_cancel.setOnClickListener {
            edit = false
            turnoffEdit()



        }
    }

    private fun updateSetting() {
        val name = ed_user_name.text.toString()


        if (TextUtils.isEmpty(name)) {
            Toast.makeText(context, "Chen du vao", Toast.LENGTH_SHORT).show()
        } else {

                var hashMap: HashMap<String, Any> = HashMap()
                hashMap["uid"] = currentUserId
                hashMap["name"] = name


                rootRef!!.child("Users").child(currentUserId).updateChildren(hashMap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            turnoffEdit()
                        }
                    }

        }

    }

    private fun turnoffEdit() {
        ed_user_name.isEnabled = false
        iv_cancel.visibility = View.GONE
        iv_edit.visibility = View.VISIBLE
        rl_change.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        dislayIvUser()

    }

    private fun dislayIvUser() {
        rootRef!!.child("Users").child(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {


                        if (p0!!.hasChild("image")) {
                            Glide.with(context!!.applicationContext)
                                .load(p0.child("image").value.toString())
                                .apply(
                                    RequestOptions().placeholder(R.drawable.profile_image).error(
                                        R.drawable.profile_image
                                    )
                                )
                                .into(user_profile_image)
                        }
                        if (p0.hasChild("image_full")) {
                            Glide.with(context!!.applicationContext)
                                .load(p0.child("image_full").value.toString())
                                .apply(
                                    RequestOptions().placeholder(R.drawable.profile_image).error(
                                        R.drawable.profile_image
                                    )
                                )
                                .into(iv_user_full)
                        }


                    ed_user_name.text = Editable.Factory.getInstance().newEditable(p0!!.child("name").value.toString())

                }

            })
    }
    private fun showListPopupWindow(anchor: View) {
        popup = ListPopupWindow(context!!)

        val listPopupItems = ArrayList<ItemMain>()
        listPopupItems.add(ItemMain("Logout"))
//        val list = listOf("name", "date", "size")
//        for (i in list.indices) {
//            if (list[i] == sharedPreferences!!.getString("sort", "")) {
//                listPopupItems[i].image = R.drawable.ic_tick_click
//            }
//        }


        val listPopupWindow = createListPopupWindow(anchor, listPopupItems)
        listPopupWindow.show()

    }


    private fun createListPopupWindow(
        anchor: View,
        items: ArrayList<ItemMain>
    ): ListPopupWindow {

        val adapter = ItemMainAdapter(context!!, items, object : ItemMainAdapter.ItemListener {
            override fun onClick(position: Int) {
                when (position) {
                    0 -> {
                        updateUserStatus("offline")
                        val intent = Intent("OFF")
                        intent.putExtra("off",true)
                        context!!.sendBroadcast(intent)
                        startActivity(Intent(context, LoginActivity::class.java))

                        mAuth!!.signOut()


                    }


                }
//                showListPopupWindow(it)
            }

        })
        popup!!.anchorView = anchor
        popup!!.width = convertToPx(120)
        popup!!.height = convertToPx(35)
//        popup!!.setBackgroundDrawable(resources.getDrawable(R.drawable.popup))
        popup!!.setAdapter(adapter)



        return popup!!
    }
    private fun convertToPx(dp: Int): Int {
        // Get the screen's density scale
        val scale = resources.displayMetrics.density
        // Convert the dps to pixels, based on density scale
        return (dp * scale + 0.5f).toInt()
    }
    private fun updateUserStatus(state: String) {

            var saveCurrentTime = ""
            var saveCurrentDate = ""

            val calendar = Calendar.getInstance()
            val currentDate = SimpleDateFormat("MMM dd, yyy")
            saveCurrentDate = currentDate.format(calendar.time)

            val currentTime = SimpleDateFormat("hh:mm a")
            saveCurrentTime = currentTime.format(calendar.time)

            val hashMap: HashMap<String, Any> = HashMap()
            hashMap["time"] = saveCurrentTime
            hashMap["date"] = saveCurrentDate
            hashMap["state"] = state
            if (mAuth != null) {

                rootRef!!.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("userState").updateChildren(hashMap)
            }

        }

}
