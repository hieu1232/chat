package hieusenpaj.com.whapp.fragment


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import hieusenpaj.com.whapp.R
import hieusenpaj.com.whapp.activity.CallingActivity
import hieusenpaj.com.whapp.activity.ChatActivity
import hieusenpaj.com.whapp.objet.Contact
import hieusenpaj.com.whapp.objet.Message
import kotlinx.android.synthetic.main.fragment_chats.*
import kotlinx.android.synthetic.main.item_user.view.*


/**
 * A simple [Fragment] subclass.
 */
class ChatsFragment : Fragment() {
    var currentUserId = ""
    var userRef: DatabaseReference? = null
    var messageRef: DatabaseReference? = null
    var callBy =""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        messageRef = FirebaseDatabase.getInstance().reference.child("Messages")
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onStart() {
        super.onStart()
        dislayIvUser()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.layoutManager = LinearLayoutManager(context)

        shimmer_view_container.startShimmerAnimation()
        checkForReceiverCall()
        ll_search.setOnClickListener {
            showKeybroad(it)
//            it.requestFocus()

            context!!.sendBroadcast(Intent("SEARCH"))
        }



        val options = FirebaseRecyclerOptions.Builder<Contact>()
            .setQuery(messageRef!!.child(currentUserId), Contact::class.java)
            .build()
        val firebaseListAdapter = object :
            FirebaseRecyclerAdapter<Contact, ContactViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ContactViewHolder {
                val v =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_user, parent, false)
                return ContactViewHolder(v)

            }


            override fun onBindViewHolder(holder: ContactViewHolder, p1: Int, p2: Contact) {
                val id = getRef(p1).key

                messageRef!!.child(currentUserId).child(id)
                    .addChildEventListener(object : ChildEventListener {
                        override fun onCancelled(p0: DatabaseError?) {

                        }

                        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                        }

                        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                        }

                        override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                            if(p0!!.exists()) {
                                val message = p0!!.getValue(Message::class.java)
                                if (message!!.type == "text") {
                                    holder.tv_status.text = message.message
                                } else {
                                    holder.tv_status.text = "Sent a image"
                                }
                                holder.tv_time.text = message.time
                            }

                        }

                        override fun onChildRemoved(p0: DataSnapshot?) {
                        }

                    })


                userRef!!.child(id).addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        var name = ""
                        var image = ""
                        if (p0!!.exists() && p0.hasChild("image")) {
                            name = p0.child("name").value.toString()
                            val status = p0.child("status").value.toString()
                            image = p0.child("image").value.toString()

                            holder.tv_name.text = name
                            Glide.with(context!!.applicationContext).load(image)
                                .apply(
                                    RequestOptions().placeholder(R.drawable.profile_image).error(
                                        R.drawable.profile_image
                                    )
                                )
                                .into(holder.iv)
                        } else {
                            name = p0.child("name").value.toString()
                            val status = p0.child("status").value.toString()


                            holder.tv_name.text = name
                        }
                        if (p0.child("userState").hasChild("state")) {
                            val state = p0.child("userState").child("state").value.toString()
                            val date = p0.child("userState").child("date").value.toString()
                            val time = p0.child("userState").child("time").value.toString()
                            if (state == "online") {
//                                holder.tv_time.visibility = View.GONE
                                holder.iv_online.visibility = View.VISIBLE
                            } else {
//                                holder.tv_time.apply {
//                                    visibility = View.VISIBLE
////                                    text = time
//                                }
                                holder.iv_online.visibility = View.GONE
//                                holder.tv_status.text = "Last Seen :$date $time"
                            }
                        } else {
//                            holder.tv_status.text = "Offline"
                        }

                        holder.iv.setOnClickListener {
                            val intent = Intent(context, ChatActivity::class.java)
                            intent.putExtra("id", id)
                            intent.putExtra("name", name)
                            intent.putExtra("image", image)
                            startActivity(intent)
                        }


                    }


                })


            }

        }
        recycler.adapter = firebaseListAdapter
        messageRef!!.child(currentUserId).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {

                    shimmer_view_container.stopShimmerAnimation()
                    shimmer_view_container.visibility = View.GONE

            }

        })
        firebaseListAdapter.startListening()

    }

    class ContactViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val iv = v.user_profile_image
        val tv_name = v.tv_user_name
        val tv_status = v.tv_user_status
        val iv_status = v.iv_status
        val tv_time = v.tv_time
        val iv_online = v.iv_online
    }
    private fun showKeybroad(v:View){
        val imm =
            activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }

    private fun checkForReceiverCall() {
        userRef!!.child(currentUserId).child("Ringing").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.hasChild("ringing")){
                    callBy = p0.child("ringing").value.toString()
                    val intent = Intent(context, CallingActivity::class.java)
                    intent.putExtra("id",callBy)
                    intent.putExtra("key",p0.child("key").value.toString())
                    intent.putExtra("id_call",p0.child("from").value.toString())
                    context!!.startActivity(intent)


                }
            }

        })
    }
    private fun dislayIvUser(){
        userRef!!.child(currentUserId).addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.hasChild("image")){
                    Glide.with(context!!.applicationContext).load(p0.child("image").value.toString())
                        .apply(
                            RequestOptions().placeholder(R.drawable.profile_image).error(
                                R.drawable.profile_image
                            )
                        )
                        .into(user_profile_image)
                }
            }

        })
    }
}
