package hieusenpaj.com.whapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import hieusenpaj.com.whapp.R
import kotlinx.android.synthetic.main.activity_profile_user_visit.*
import ru.whalemare.sheetmenu.ActionItem
import ru.whalemare.sheetmenu.SheetMenu
import ru.whalemare.sheetmenu.layout.LinearLayoutProvider

class ProfileUserVisitActivity : AppCompatActivity() {
    var receiverUserId = ""
    var current_state = ""
    var senderUserId = ""
    var userRef: DatabaseReference? = null
    var chatRequestRef: DatabaseReference? = null
    var contactRef: DatabaseReference? = null
    var name = ""
    var image = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_user_visit)
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        chatRequestRef = FirebaseDatabase.getInstance().reference.child("Chat Request")
        contactRef = FirebaseDatabase.getInstance().reference.child("Contacts")

        receiverUserId = intent.extras!!.getString("id").toString()
        senderUserId = FirebaseAuth.getInstance().currentUser!!.uid
        current_state = "new"

        receiverUserInfo()

        iv_message.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("id", receiverUserId)
            intent.putExtra("name", name)
            intent.putExtra("image", image)
            startActivity(intent)

        }
    }

    private fun receiverUserInfo() {
        userRef!!.child(receiverUserId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0!!.exists() && p0.hasChild("image")) {
                    name = p0.child("name").value.toString()
                    val status = p0.child("status").value.toString()
                    image = p0.child("image").value.toString()

                    tv_name.text = name
                    Glide.with(applicationContext).load(image)
                        .apply(RequestOptions().placeholder(R.drawable.profile_image).error(R.drawable.profile_image))
                        .into(profile_image)
                } else {
                    name = p0.child("name").value.toString()
                    val status = p0.child("status").value.toString()


                    tv_name.text = name
                }
                if (p0.hasChild("image_full")) {
                    Glide.with(this@ProfileUserVisitActivity.applicationContext)
                        .load(p0.child("image_full").value.toString())
                        .apply(
                            RequestOptions().placeholder(R.drawable.profile_image).error(
                                R.drawable.profile_image
                            )
                        )
                        .into(iv_user_full)
                }
                managerChat()
            }

        })
    }

    private fun managerChat() {

        chatRequestRef!!.child(senderUserId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0!!.hasChild(receiverUserId)) {

                    val request_type =
                        p0.child(receiverUserId).child("request_type").value.toString()
                    if (request_type == "sent") {
                        current_state = "request_sent"
                        tv_request.text = "Cancel Friend Request"
                        Glide.with(this@ProfileUserVisitActivity.applicationContext)
                            .load(R.drawable.friend_sent)
                            .into(iv_request)
                    } else if (request_type == "received") {
                        current_state = "request_received"
                        tv_request.text = "Respond"
                        Glide.with(this@ProfileUserVisitActivity.applicationContext)
                            .load(R.drawable.friend_request)
                            .into(iv_request)

                    }
                } else {
                    contactRef!!.child(senderUserId)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError?) {

                            }

                            override fun onDataChange(p0: DataSnapshot?) {
                                if (p0!!.hasChild(receiverUserId)) {
                                    current_state = "friend"
                                    tv_request.text = "Friend"
                                    Glide.with(this@ProfileUserVisitActivity.applicationContext)
                                        .load(R.drawable.friend_accpect)
                                        .into(iv_request)
                                }
                            }

                        })
                }
            }

        })
        if (senderUserId != receiverUserId) {
            btn_send_message.setOnClickListener {
                if (current_state == "new") {
                    sendRequestMessage()
                }else if(current_state == "friend"){
                    setUpBottomDialog(true)
                }else if (current_state == "request_sent"){
                    cancelRequestMessage(chatRequestRef!!, "new", "Add Friend")

                }
                else{
                    setUpBottomDialog(false)
                }
//                } else if (current_state == "request_sent") {
//
//                } else if (current_state == "request_received") {
////                    acceptChatRequest()
//                } else if (current_state == "friend") {
////                    removeContact()
//                }

            }
        } else {
            rl_request.visibility = View.GONE
//            iv_message.visibility = View.INVISIBLE
//            btn_send_message.visibility = View.VISIBLE
        }

    }

    private fun removeContact() {
        cancelRequestMessage(contactRef!!, "new", "Add Friend")
    }

    private fun acceptChatRequest() {
        contactRef!!.child(senderUserId).child(receiverUserId).child("Contacts").setValue("saved")
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    contactRef!!.child(receiverUserId).child(senderUserId).child("Contacts")
                        .setValue("saved").addOnCompleteListener {
                            if (it.isSuccessful) {
                                cancelRequestMessage(chatRequestRef!!, "friend", "Friend")

                            }
                        }
                }
            }
    }


    private fun sendRequestMessage() {
        chatRequestRef!!.child(senderUserId).child(receiverUserId)
            .child("request_type").setValue("sent").addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    chatRequestRef!!.child(receiverUserId).child(senderUserId)
                        .child("request_type").setValue("received").addOnCompleteListener {
                            if (it.isSuccessful) {
                                btn_send_message.isEnabled = true
                                current_state = "request_sent"
                                tv_request.text = "Cancel Friend Request"
                                Glide.with(this@ProfileUserVisitActivity.applicationContext)
                                    .load(R.drawable.friend_sent)
                                    .into(iv_request)
                            }
                        }
                }
            }
    }

    private fun cancelRequestMessage(ref: DatabaseReference, state: String, s: String) {
        ref.child(senderUserId).child(receiverUserId).removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    ref.child(receiverUserId).child(senderUserId).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                btn_send_message.isEnabled = true
                                current_state = state
                                tv_request.text = s
                                btn_delice.visibility = View.GONE
                                if (state != "friend") {
                                    Glide.with(this@ProfileUserVisitActivity.applicationContext)
                                        .load(R.drawable.add_friend)
                                        .into(iv_request)
                                } else {
                                    Glide.with(this@ProfileUserVisitActivity.applicationContext)
                                        .load(R.drawable.friend_accpect)
                                        .into(iv_request)
                                }
                            }
                        }
                }
            }
    }

    fun setUpBottomDialog(isFriend: Boolean) {
        SheetMenu(
            title = null,
            actions = getSheetItems(isFriend),
            layoutProvider = LinearLayoutProvider(),
            onClick = { item ->
            if(isFriend){
                removeContact()

            }else{
                if(item.id==0){
                    acceptChatRequest()
                }else{
                    cancelRequestMessage(chatRequestRef!!, "new", "Add Friend")
                }

            }}
        ).show(this)
    }

    fun getSheetItems(isFriend:Boolean): List<ActionItem> {
        val arr = ArrayList<String>()
        if (!isFriend) {
            arr.add("Confirm")
            arr.add("Delete Request")
            return (0..1).map { index ->

                return@map ActionItem(index, arr[index], null)
            }
        }else{
            arr.add("Unfriend")
            return (0..0).map { index ->

                return@map ActionItem(0, arr[0], null)
            }
        }

    }
}
