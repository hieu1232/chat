package hieusenpaj.com.whapp.activity

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import hieusenpaj.com.whapp.R
import kotlinx.android.synthetic.main.activity_calling.*
import kotlinx.android.synthetic.main.activity_chat.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CallingActivity : AppCompatActivity() {
    var mAuth: FirebaseAuth? = null
    var senderUserId = ""
    var senderImage: String? = null
    var senderName: String? = null
    var receiverUserId = ""
    var receiverImage: String? = null
    var receiverName: String? = null
    var checker = ""
    var callingId = ""
    var receiveId = ""
    var userRef: DatabaseReference? = null
    var mediaPlayer: MediaPlayer? = null
    var messagePushId = ""
    var senId = ""
    var rootRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calling)

        mediaPlayer = MediaPlayer.create(this, R.raw.ringing)

        mAuth = FirebaseAuth.getInstance()
        senderUserId = mAuth!!.currentUser!!.uid
        rootRef = FirebaseDatabase.getInstance().reference
        receiverUserId = intent.extras!!.getString("id")!!
        senId = intent.extras!!.getString("id_call")!!
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        setUp()
        iv_cancel_call.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentDate = SimpleDateFormat("MMM dd, yyy")
            val saveCurrentDate = currentDate.format(calendar.time)

            val currentTime = SimpleDateFormat("hh:mm a")
            val saveCurrentTime = currentTime.format(calendar.time)

            sendMessage("$saveCurrentDate   $saveCurrentTime", "call_cancel", false)

        }
        iv_make_call.setOnClickListener {
            sendMessage("", "call", true)


//            val hashMap :HashMap<String,Any> = HashMap()
//            hashMap.put("picked","picked")
//            userRef!!.child(senderUserId).child("Ringing").updateChildren(hashMap)
//                .addOnCompleteListener{task ->
//                    if(task.isSuccessful){
//
//
//
//                    }
//                }
            mediaPlayer!!.stop()
        }
    }

    private fun sendMessage(string: String, status: String, make: Boolean) {

        val messageSender = "Messages/$senderUserId/$receiverUserId"
        val messageReceiver = "Messages/$receiverUserId/$senderUserId"


        val messageTextHM: HashMap<String, Any> = HashMap()
        messageTextHM["message"] = string
        messageTextHM["type"] = status
        messageTextHM["from"] = senId
        messageTextHM["to"] = receiverUserId
        messageTextHM["time"] = getTime()
        messageTextHM["date"] = getDate()
        val messageBodyTextHM: HashMap<String, Any> = HashMap()
        messageBodyTextHM["$messageSender/$messagePushId"] = messageTextHM
        messageBodyTextHM["$messageReceiver/$messagePushId"] = messageTextHM

        rootRef!!.updateChildren(messageBodyTextHM).addOnCompleteListener {
            if (it.isSuccessful) {
                if (make) {
                    val intent = Intent(this@CallingActivity, VideoChatActivity::class.java)
                    intent.putExtra("id", receiverUserId)
                    intent.putExtra("key", messagePushId)
                    intent.putExtra("id_call", senId)
                    startActivity(intent)
                } else {
                    checker = "clicked"
                    cancelCalling(false)
                    mediaPlayer!!.stop()
                }

            }

        }

    }

    private fun setUp() {
        userRef!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0!!.child(receiverUserId).exists()) {
                    receiverImage = p0.child(receiverUserId).child("image").value.toString()
                    receiverName = p0.child(receiverUserId).child("name").value.toString()
                    Glide.with(this@CallingActivity.applicationContext)
                        .load(receiverImage)
                        .into(iv)
                    tv.text = receiverName

                }
                if (p0.child(senderUserId).exists()) {
                    senderImage = p0.child(senderUserId).child("image").value.toString()
                    senderName = p0.child(senderUserId).child("name").value.toString()
                }

            }

        })
    }

    override fun onStart() {
        super.onStart()
        mediaPlayer!!.start()
        val userMessageRefKey =
            rootRef!!.child("Messages").child(senderUserId).child(receiverUserId)
                .push()
        messagePushId = userMessageRefKey.key
        userRef!!.child(receiverUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (checker != "clicked" && !p0!!.hasChild("Calling") && !p0.hasChild("Ringing")) {

                    val calling: HashMap<String, Any> = HashMap()
                    calling["calling"] = receiverUserId!!
                    calling["from"] = senId
                    calling["key"] = messagePushId
                    userRef!!.child(senderUserId).child("Calling").updateChildren(calling)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val ringing: HashMap<String, Any> = HashMap()
                                ringing["ringing"] = senderUserId
                                ringing["from"] = senId
                                ringing["key"] = messagePushId
                                userRef!!.child(receiverUserId).child("Ringing")
                                    .updateChildren(ringing)
                            }
                        }
                }
            }

        })
        userRef!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0!!.child(senderUserId).hasChild("Ringing") && !p0.child(senderUserId).hasChild(
                        "Calling"
                    )
                ) {
                    iv_make_call.visibility = View.VISIBLE
                }
                if (p0.child(receiverUserId).child("Ringing").hasChild("picked")) {
                    mediaPlayer!!.stop()
                    val intent = Intent(this@CallingActivity, VideoChatActivity::class.java)
                    startActivity(intent)

                }
            }

        })

    }

    private fun cancelCalling(make: Boolean) {
        userRef!!.child(senderUserId).child("Calling")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0!!.exists() && p0.hasChild("calling")) {
                        callingId = p0.child("calling").value.toString()

                        userRef!!.child(callingId).child("Ringing").removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    userRef!!.child(senderUserId).child("Calling").removeValue()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                if (!make) {
                                                    onBackPressed()
                                                } else {

                                                }
                                            }
                                        }
                                }
                            }
                    } else {
//                        if (!make) {
//                            onBackPressed()
//                        }else{
//                            startActivity(Intent(this@CallingActivity,VideoChatActivity::class.java))
//                        }
                    }
                }

            })



        userRef!!.child(senderUserId).child("Ringing")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0!!.exists() && p0.hasChild("ringing")) {
                        receiveId = p0.child("ringing").value.toString()

                        userRef!!.child(receiveId).child("Calling").removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    userRef!!.child(senderUserId).child("Ringing").removeValue()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                if (!make) {
                                                    onBackPressed()
                                                } else {
                                                }

                                            }
                                        }
                                }
                            }
                    } else {
//                        if (!make) {
//                            onBackPressed()
//                        }else{
//                            startActivity(Intent(this@CallingActivity,VideoChatActivity::class.java))
//                        }
                    }
                }

            })

    }
    private fun getTime():String{
        var saveCurrentTime = ""

        val calendar = Calendar.getInstance()


        val currentTime = SimpleDateFormat("hh:mm a")
        saveCurrentTime = currentTime.format(calendar.time)
        return saveCurrentTime
    }
    private fun getDate():String{

        var saveCurrentDate = ""

        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("MMM dd, yyy")
        saveCurrentDate = currentDate.format(calendar.time)


        return saveCurrentDate
    }
}
