package hieusenpaj.com.whapp.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import hieusenpaj.com.whapp.R
import hieusenpaj.com.whapp.objet.Message
import kotlinx.android.synthetic.main.activity_group_chat.*
import kotlinx.android.synthetic.main.left_message_layout.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class GroupChatActivity : AppCompatActivity() {
    companion object {
        val LEFT = 0
        val RIGHT = 1
        val LOADMORE = -1
    }

    var currentUserName = ""
    var mAuth: FirebaseAuth? = null
    var userRef: DatabaseReference? = null
    var groupRef: DatabaseReference? = null
    var groupNameRef: DatabaseReference? = null
    var groupMessageKeyRef: DatabaseReference? = null
    var currentUserId = ""
    var currentGroupName = ""
    var currentGroupId = ""
    var currentDate = ""
    var currentTime = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)
        setSupportActionBar(toolbar)
        currentGroupName = intent.extras!!.getString("name")!!
        currentGroupId = intent.extras!!.getString("id")!!

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth!!.currentUser!!.uid
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        groupRef =
            FirebaseDatabase.getInstance().reference.child("GroupMessages").child(currentGroupId)
        groupNameRef =
            FirebaseDatabase.getInstance().reference.child("Group").child(currentGroupName)


        tv_title.text = currentGroupName


        getUserInfo()

        iv_send.setOnClickListener {
            sendMessage()
        }

        iv_add.setOnClickListener {
            val intent = Intent(this, AddFriendGroupActivity::class.java)
            intent.putExtra("id", currentGroupId)
            intent.putExtra("name", currentGroupName)
            startActivity(intent)
        }
        iv_back.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        setupRe()


    }

    private fun setupRe() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        var options = FirebaseRecyclerOptions.Builder<Message>()
            .setQuery(
                groupRef!!
                , Message::class.java
            )
            .build()


        val firebaseListAdapter = object :
            FirebaseRecyclerAdapter<Message, ViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ViewHolder {
                return if (viewType == RIGHT) {
                    val v =
                        LayoutInflater.from(this@GroupChatActivity)
                            .inflate(R.layout.right_message_layout, parent, false)
                    ViewHolder(v)
                } else {
                    val v =
                        LayoutInflater.from(this@GroupChatActivity)
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
                    Glide.with(this@GroupChatActivity).load(messager)
                        .into(holder.iv_message)
                } else {
                    holder.iv_message.visibility = View.GONE
                    holder.tv_receiver.visibility = View.GONE
                    holder.ll_call.visibility = View.VISIBLE
                    holder.tv_message_call.text = messager


                }
                userRef =
                    FirebaseDatabase.getInstance().reference.child("Users").child(fromUserId)

                userRef!!.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        if (currentUserId != fromUserId) {
                            if (p0!!.hasChild("image")) {

                                Glide.with(applicationContext)
                                    .load(p0.child("image").value.toString())
                                    .apply(
                                        RequestOptions().placeholder(R.drawable.profile_image).error(
                                            R.drawable.profile_image
                                        )
                                    )
                                    .into(holder.iv)
                            }
                        }
                    }

                })



                try {
                    val messageBack = getItem(p1 + 1)

                    if (message.from == messageBack.from) {
                        holder.iv.visibility = View.INVISIBLE
                    } else {
                        holder.iv.visibility = View.VISIBLE
                    }


                } catch (e: Exception) {

                }


                if (p1 == itemCount - 1) {
                    holder.iv.visibility = View.VISIBLE
                }
            }

        }
        recyclerView.adapter = firebaseListAdapter
        firebaseListAdapter.startListening()
        recyclerView.smoothScrollToPosition(recyclerView.adapter!!.itemCount)

    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val iv = v.iv_profile
        val tv_receiver = v.tv_message
        val iv_message = v.iv_message
        val ll_call = v.ll_call
        val tv_message_call = v.tv_message_call
        val tv_call_again = v.tv_call_again
        val tv_status_call = v.tv_status_call
//        val tv_sender= v.tv_sender_message


    }

    private fun getUserInfo() {
        userRef!!.child(currentUserId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0!!.exists()) {
                    currentUserName = p0.child("name").value.toString()
                }
            }

        })
    }

    private fun sendMessage() {

        val string = ed_send.text.toString()
        if (TextUtils.isEmpty(string)) {
            Toast.makeText(this, "Nhap tin nhan vao", Toast.LENGTH_SHORT).show()
        } else {


            val userMessageRefKey =
                groupRef!!.push()
            val messagePushId = userMessageRefKey.key

            val messageTextHM: HashMap<String, Any> = HashMap()
            messageTextHM["message"] = string
            messageTextHM["type"] = "text"
            messageTextHM["from"] = currentUserId
            messageTextHM["time"] = getTime()
            messageTextHM["date"] = getDate()


            groupRef!!.child(messagePushId).updateChildren(messageTextHM)
                .addOnCompleteListener {
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
    private fun saveMessageDatabase() {
        val message = ed_send.text.toString()
        val messageKey = groupNameRef!!.push().key
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "nha tin nhan vao", Toast.LENGTH_SHORT).show()
        } else {
            val calendarDate = Calendar.getInstance()
            val currentDateFormat = SimpleDateFormat("MMM dd,yyyy")
            currentDate = currentDateFormat.format(calendarDate.time)

            val calendarTime = Calendar.getInstance()
            val currentTimeFormat = SimpleDateFormat("hh:mm a")
            currentTime = currentTimeFormat.format(calendarTime.time)

            val groupMessageKey: HashMap<String, Any> = HashMap()
            groupNameRef!!.updateChildren(groupMessageKey)

            groupMessageKeyRef = groupNameRef!!.child(messageKey)

            val messageInfo: HashMap<String, Any> = HashMap()
            messageInfo["name"] = currentUserName
            messageInfo["message"] = message
            messageInfo["date"] = currentDate
            messageInfo["time"] = currentTime
            groupMessageKeyRef!!.updateChildren(messageInfo)
        }
    }

    private fun dislayMessage(dataSnapshot: DataSnapshot) {

        val iterator = dataSnapshot.children.iterator()
        while (iterator.hasNext()) {
            val date = iterator.next().value as String
            val message = iterator.next().value as String
            val name = iterator.next().value as String
            val time = iterator.next().value as String

//


        }

    }

}
