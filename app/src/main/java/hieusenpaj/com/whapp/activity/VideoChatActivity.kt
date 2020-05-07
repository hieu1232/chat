package hieusenpaj.com.whapp.activity

import android.content.Intent
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.opentok.android.*
import hieusenpaj.com.whapp.R
import kotlinx.android.synthetic.main.activity_video_chat.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.HashMap

class VideoChatActivity : AppCompatActivity(), Session.SessionListener,
    PublisherKit.PublisherListener {
    companion object {
        val API_Key = "46514442"
        val SESSION_ID = "1_MX40NjUxNDQ0Mn5-MTU4MTk5MjQ4NjM0OX5TbmExOWFwclh0ekRJeUNjdDFRUktheWZ-fg"
        val TOKEN =
            "T1==cGFydG5lcl9pZD00NjUxNDQ0MiZzaWc9NjcwZWViYjQ3ZGQyZjUwYzAwMmEzOGMxZTI0YmUyMGQ0ODYxOGMxNzpzZXNzaW9uX2lkPTFfTVg0ME5qVXhORFEwTW41LU1UVTRNVGs1TWpRNE5qTTBPWDVUYm1FeE9XRndjbGgwZWtSSmVVTmpkREZSVWt0aGVXWi1mZyZjcmVhdGVfdGltZT0xNTgxOTkyNTAwJm5vbmNlPTAuNjUyOTMwMTE0ODM0Njc2NCZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTg0NTgwODk4JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9"
        val LOG_TAG = VideoChatActivity::class.java.simpleName
        val RC_PER = 1234

    }
    var messagePushId=""
    var callingId = ""
    var receiveId = ""
    var senderUserId = ""
    var mAuth: FirebaseAuth? = null
    var userId: String? = null
    var userRef: DatabaseReference? = null
    var rootRef: DatabaseReference? = null
    var session: Session? = null
    var publisher: Publisher? = null
    var subscriber: Subscriber? = null
    var receiverUserId = ""
    var senId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat)

        mAuth = FirebaseAuth.getInstance()
        senderUserId = mAuth!!.currentUser!!.uid
        rootRef = FirebaseDatabase.getInstance().reference
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        receiverUserId = intent.extras!!.getString("id")!!
        try {
            senId = intent.extras!!.getString("id_call")!!
            messagePushId = intent.extras!!.getString("key")!!
        }catch (e:Exception){

        }

        iv_cancel_call.setOnClickListener {
            cancelCalling(false)
            if (publisher != null) {
                publisher!!.destroy()
            }
            if (subscriber != null) {
                subscriber!!.destroy()
            }

        }

        session = Session.Builder(this, API_Key, SESSION_ID).build()
        session!!.setSessionListener(this)
        session!!.connect(TOKEN)

    }

    //session

    override fun onStreamDropped(p0: Session?, p1: Stream?) {
        if (subscriber != null) {
            subscriber == null
            subsciber_container.removeAllViews()
        }

    }

    override fun onStreamReceived(p0: Session?, p1: Stream?) {
        if (subscriber == null) {
            subscriber = Subscriber.Builder(this, p1).build()

            session!!.subscribe(subscriber)
            subsciber_container.addView(subscriber!!.view)
            if (subscriber!!.view is GLSurfaceView) {
                (subscriber!!.view as GLSurfaceView).setZOrderOnTop(true)

            }
        }

    }

    override fun onConnected(p0: Session?) {
        publisher = Publisher.Builder(this).build()
        publisher!!.setPublisherListener(this)
        publisher_container.addView(publisher!!.view)

        if (publisher!!.view is GLSurfaceView) {
            (publisher!!.view as GLSurfaceView).setZOrderOnTop(true)

        }
        session!!.publish(publisher)
    }

    override fun onDisconnected(p0: Session?) {
    }

    override fun onError(p0: Session?, p1: OpentokError?) {
    }


    //publisher

    override fun onStreamCreated(p0: PublisherKit?, p1: Stream?) {


    }

    override fun onStreamDestroyed(p0: PublisherKit?, p1: Stream?) {

    }

    override fun onError(p0: PublisherKit?, p1: OpentokError?) {
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {
        super.onPointerCaptureChanged(hasCapture)
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
                                                    sendMessage()
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
                                                    sendMessage()
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

    private fun sendMessage() {
        val messageSender = "Messages/$senderUserId/$receiverUserId"
        val messageReceiver = "Messages/$receiverUserId/$senderUserId"

        var userMessageRefKey =
            rootRef!!.child("Messages").child(senderUserId).child(receiverUserId)
                .push()


        val messageTextHM: HashMap<String, Any> = HashMap()
        messageTextHM["message"] = "${getDate()}   ${getTime()}"
        messageTextHM["type"] = "call"
        messageTextHM["from"] = senId
        messageTextHM["isseen"] = "null"
        messageTextHM["to"] = receiverUserId
        messageTextHM["time"] = getTime()
        messageTextHM["date"] = getDate()
        val messageBodyTextHM: HashMap<String, Any> = HashMap()
        messageBodyTextHM["$messageSender/$messagePushId"] = messageTextHM
        messageBodyTextHM["$messageReceiver/$messagePushId"] = messageTextHM

        rootRef!!.updateChildren(messageBodyTextHM).addOnCompleteListener {
            if (it.isSuccessful) {

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


