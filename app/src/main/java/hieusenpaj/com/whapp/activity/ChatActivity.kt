package hieusenpaj.com.whapp.activity

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.ObservableSnapshotArray
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import hieusenpaj.com.whapp.R
import hieusenpaj.com.whapp.adapter.MessagerAdapter
import hieusenpaj.com.whapp.objet.Message
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat.iv_profile
import kotlinx.android.synthetic.main.left_message_layout.*
import kotlinx.android.synthetic.main.left_message_layout.view.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {
    var name = ""
    var image = ""
    var receiverUserId = ""
    var senderUserId = ""
    var rootRef: DatabaseReference? = null
    var list = ArrayList<Message>()
    var messageAdapter: MessagerAdapter? = null
    var myUrl = ""
    var uriFile: Uri? = null
    var i = 0
    var messagePushId = ""
    var firebaseListAdapter: FirebaseRecyclerAdapter<Message, ViewHolder>? = null

    companion object {
        val LEFT = 0
        val RIGHT = 1
        val LOADMORE = -1
    }

    var userRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        rootRef = FirebaseDatabase.getInstance().reference
        senderUserId = FirebaseAuth.getInstance().currentUser!!.uid
        receiverUserId = intent.extras!!.getString("id").toString()
        name = intent.extras!!.getString("name").toString()
        image = intent.extras!!.getString("image").toString()


        setupToolbar()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        iv_send_photo.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 438)
        }
        setupRe()
        iv_call.setOnClickListener {
            val intent = Intent(this, CallingActivity::class.java)
            intent.putExtra("id", receiverUserId)
            intent.putExtra("id_call", senderUserId)
            startActivity(intent)
//            firebaseListAdapter!!.stopListening()


        }
        iv_back.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 438 && resultCode == Activity.RESULT_OK && data != null) {
            uriFile = data.data

            val storageRef = FirebaseStorage.getInstance().reference.child("Image Files")
            val messageSender = "Messages/$senderUserId/$receiverUserId"
            val messageReceiver = "Messages/$receiverUserId/$senderUserId"

            var userMessageRefKey =
                rootRef!!.child("Messages").child(senderUserId).child(receiverUserId)
                    .push()
            var messagePushId = userMessageRefKey.key
            val filePath = storageRef.child("$messagePushId.jpg")
            val uploadTask = filePath.putFile(uriFile!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> {
                return@Continuation filePath.downloadUrl

            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    myUrl = task.result.toString()

                    val messageTextHM: HashMap<String, Any> = HashMap()
                    messageTextHM["message"] = myUrl
                    messageTextHM["type"] = "image"
                    messageTextHM["from"] = senderUserId
                    messageTextHM["time"] = getTime()
                    messageTextHM["date"] = getDate()

                    val messageBodyTextHM: HashMap<String, Any> = HashMap()
                    messageBodyTextHM["$messageSender/$messagePushId"] = messageTextHM
                    messageBodyTextHM["$messageReceiver/$messagePushId"] = messageTextHM

                    rootRef!!.updateChildren(messageBodyTextHM).addOnCompleteListener {
                        if (it.isSuccessful) {

                        }
                        ed_send.text = Editable.Factory.getInstance().newEditable("")
                    }
                }
            }
        }
    }

    private fun setupRe() {
        var options = FirebaseRecyclerOptions.Builder<Message>()
            .setQuery(
                rootRef!!.child("Messages").child(senderUserId).child(receiverUserId)
                , Message::class.java
            )
            .build()


        firebaseListAdapter = object :
            FirebaseRecyclerAdapter<Message, ViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ViewHolder {
                return if (viewType == RIGHT) {
                    val v =
                        LayoutInflater.from(this@ChatActivity)
                            .inflate(R.layout.right_message_layout, parent, false)
                    ViewHolder(v)
                } else {
                    val v =
                        LayoutInflater.from(this@ChatActivity)
                            .inflate(R.layout.left_message_layout, parent, false)
                    ViewHolder(v)
                }

            }

            override fun getItemViewType(position: Int): Int {

                if (FirebaseAuth.getInstance().currentUser!!.uid == getItem(position).from) {
                    return RIGHT
                } else {
                    return LEFT
                }


            }


            override fun onBindViewHolder(holder: ViewHolder, p1: Int, message: Message) {
//                if( p1==0){
//                    if(i>0 ) {
//                        setupRe(i)
//
//                    }
//                    i++
//                    Toast.makeText(this@ChatActivity, i.toString(), Toast.LENGTH_SHORT).show()
//                }


                val fromUserId = message.from
                val messager = message.message
                val type = message.type



                if (type == "text") {
                    holder.iv_message.visibility = View.GONE
                    holder.tv_receiver.visibility = View.VISIBLE
                    holder.ll_call.visibility = View.GONE
                    holder.tv_receiver.text = messager


                } else if (type == "image") {
                    holder.iv_message.visibility = View.VISIBLE
                    holder.tv_receiver.visibility = View.GONE
                    holder.ll_call.visibility = View.GONE
                    Glide.with(this@ChatActivity).load(messager)
                        .into(holder.iv_message)
                } else {
                    holder.iv_message.visibility = View.GONE
                    holder.tv_receiver.visibility = View.GONE
                    holder.ll_call.visibility = View.VISIBLE
                    holder.tv_message_call.text = messager
                    holder.tv_call_again.setOnClickListener {
                        val intent = Intent(this@ChatActivity, CallingActivity::class.java)
                        intent.putExtra("id", receiverUserId)
                        intent.putExtra("id_call", senderUserId)
                        startActivity(intent)
//                        stopListening()

                    }

                }

                userRef =
                    FirebaseDatabase.getInstance().reference.child("Users").child(fromUserId)

                userRef!!.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        if (senderUserId != fromUserId) {
                            if (p0!!.hasChild("image")) {

                                Glide.with(this@ChatActivity)
                                    .load(p0.child("image").value.toString())
                                    .apply(
                                        RequestOptions().placeholder(R.drawable.profile_image).error(
                                            R.drawable.profile_image
                                        )
                                    )
                                    .into(holder.iv)
                            }
                            if (type == "call_cancel") {


                                holder.tv_status_call.text =
                                    "Bạn đã bỏ lỡ cuộc gọi của ${p0.child("name").value.toString()}"
                                holder.tv_status_call.setTextColor(Color.RED)
                            }
                        } else {
                            if (type == "call_cancel") {


                                holder.tv_status_call.text =
                                    "$name đã bỏ lỡ cuộc gọi của bạn"
                                holder.tv_status_call.setTextColor(Color.RED)
                            }
                        }

                    }


                })





                try {
                    val messageBack = getItem(p1 + 1)
                    Log.d("TAG", message.from + "/" + messageBack.from)
                    if (message.from == messageBack.from) {
                        holder.iv.visibility = View.INVISIBLE
                    } else {
                        holder.iv.visibility = View.VISIBLE
                    }


                } catch (e: Exception) {

                }


                if (p1 == itemCount - 1) {
                    holder.iv.visibility = View.VISIBLE
                    holder.tv_isseen.run {
                        visibility = View.VISIBLE
                        if (message.isseen == "true") {
                            text = "Đã xem"
                        } else if (message.isseen == "false") {
                            text = "Đã gửi"
                        }

                    }


                } else {
                    holder.tv_isseen.visibility = View.GONE
                }
            }

        }
        recyclerView.adapter = firebaseListAdapter
        firebaseListAdapter!!.startListening()
        recyclerView.smoothScrollToPosition(recyclerView.adapter!!.itemCount)

    }


    override fun onStart() {
        super.onStart()

        rootRef!!.child("Messages").child(senderUserId).child(receiverUserId).addChildEventListener(
            object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                }

                override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                }

                override fun onChildAdded(p0: DataSnapshot?, p1: String?) {

                    recyclerView.smoothScrollToPosition(recyclerView.adapter!!.itemCount)
                    firebaseListAdapter!!.notifyItemChanged(recyclerView.adapter!!.itemCount-2)


                }

                override fun onChildRemoved(p0: DataSnapshot?) {
                }

            }
        )
        rootRef!!.child("Messages").child(senderUserId).child(receiverUserId)
            .limitToLast(1).addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {


                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0!!.exists()) {
                        for (message in p0.children) {
                            if (message.child("from").value.toString() != senderUserId) {
                                val hashMap: HashMap<String, Any> = HashMap()
                                hashMap["isseen"] = "true"
                                rootRef!!.child("Messages").child(senderUserId)
                                    .child(receiverUserId)
                                    .child(message.key).updateChildren(hashMap)
                                rootRef!!.child("Messages").child(receiverUserId)
                                    .child(senderUserId)
                                    .child(message.key).updateChildren(hashMap)
                            }
                        }
//                    val message = p0.getValue(Message::class.java)

//                    if (message!!.from != senderUserId) {
//                        val hashMap: HashMap<String, Any> = HashMap()
//                        hashMap["isseen"] = "true"
//                        rootRef!!.child("Messages").child(senderUserId).child(receiverUserId)
//                            .child(message.key).updateChildren(hashMap)
//                    }
                    }
                }

            })







        rootRef!!.child("Users").child(receiverUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {


                    if (p0!!.child("userState").hasChild("state")) {
                        val state = p0.child("userState").child("state").value.toString()
                        val date = p0.child("userState").child("date").value.toString()
                        val time = p0.child("userState").child("time").value.toString()
                        if (state == "online") {
                            tv_status.text = "Online"
                        } else {
                            tv_status.text = "$date $time"
                        }
                    } else {
                        tv_status.text = "Offline"
                    }


                }


            })

        checkForReceiverCall()


    }

    private fun checkForReceiverCall() {
        rootRef!!.child("Users").child(senderUserId).child("Ringing")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0!!.hasChild("ringing")) {
                        val callBy = p0.child("ringing").value.toString()
                        val intent = Intent(this@ChatActivity, CallingActivity::class.java)
                        intent.putExtra("id", callBy)
                        intent.putExtra("key", p0.child("key").value.toString())
                        intent.putExtra("id_call", p0.child("from").value.toString())
                        startActivity(intent)


                    }
                }

            })
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val iv = v.iv_profile
        val tv_receiver = v.tv_message
        val iv_message = v.iv_message
        val ll_call = v.ll_call
        val tv_message_call = v.tv_message_call
        val tv_call_again = v.tv_call_again
        val tv_status_call = v.tv_status_call
        val tv_isseen = v.tv_isseen
//        val tv_sender= v.tv_sender_message


    }

    private fun setupToolbar() {
        Glide.with(this).load(image)
            .apply(RequestOptions().placeholder(R.drawable.profile_image).error(R.drawable.profile_image))
            .into(iv_profile)
        tv_name.text = name

        iv_send.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {

        val string = ed_send.text.toString()
        if (TextUtils.isEmpty(string)) {
            Toast.makeText(this, "Nhap tin nhan vao", Toast.LENGTH_SHORT).show()
        } else {


            val messageSender = "Messages/$senderUserId/$receiverUserId"
            val messageReceiver = "Messages/$receiverUserId/$senderUserId"

            var userMessageRefKey =
                rootRef!!.child("Messages").child(senderUserId).child(receiverUserId)
                    .push()
            messagePushId = userMessageRefKey.key

            val messageTextHM: HashMap<String, Any> = HashMap()
            messageTextHM["key"] = messagePushId
            messageTextHM["message"] = string
            messageTextHM["type"] = "text"
            messageTextHM["from"] = senderUserId
            messageTextHM["isseen"] = "false"
            messageTextHM["time"] = getTime()
            messageTextHM["date"] = getDate()

            val messageBodyTextHM: HashMap<String, Any> = HashMap()
            messageBodyTextHM["$messageSender/$messagePushId"] = messageTextHM
            messageBodyTextHM["$messageReceiver/$messagePushId"] = messageTextHM

            rootRef!!.updateChildren(messageBodyTextHM).addOnCompleteListener {
                if (it.isSuccessful) {

                }
                ed_send.text = Editable.Factory.getInstance().newEditable("")

            }
        }
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
