package hieusenpaj.com.whapp.fragment


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import hieusenpaj.com.whapp.R
import hieusenpaj.com.whapp.objet.Contact
import kotlinx.android.synthetic.main.fragment_request.*
import kotlinx.android.synthetic.main.item_request.view.*

/**
 * A simple [Fragment] subclass.
 */
class NotificationFragment : Fragment() {
    var currentUserId = ""
    var userRef: DatabaseReference? = null
    var chatRequestRef: DatabaseReference? = null
    var contactRef: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        chatRequestRef = FirebaseDatabase.getInstance().reference.child("Chat Request")
        contactRef = FirebaseDatabase.getInstance().reference.child("Contacts")

        return inflater.inflate(R.layout.fragment_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.layoutManager = LinearLayoutManager(context)
    }

    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<Contact>()
            .setQuery(chatRequestRef!!.child(currentUserId), Contact::class.java)
            .build()
        val firebaseListAdapter = object :
            FirebaseRecyclerAdapter<Contact, ContactViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ContactViewHolder {
                val v =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_request, parent, false)
                return ContactViewHolder(v)

            }

            override fun onBindViewHolder(holder: ContactViewHolder, p1: Int, p2: Contact) {
                val id = getRef(p1).key
                val ref = getRef(p1).child("request_type").ref

                ref.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        if (dataSnapshot!!.exists()) {
                            val type = dataSnapshot.value.toString()
                            Toast.makeText(context, type, Toast.LENGTH_SHORT).show()
                            if (type == "received") {
                                holder.btnAccept.setOnClickListener {
                                    acceptChatRequest(currentUserId, id)
                                }
                                holder.tv_status.text = "want to connect with you"
                            } else {
                                holder.btnAccept.setOnClickListener {
                                    cancelRequestMessage(currentUserId, id)
                                }
                                holder.btnAccept.text = "Cancel"
                                holder.btnCancel.visibility = View.GONE
                                holder.tv_status.text = "you have sent a request"

                            }

                            userRef!!.child(id).addValueEventListener(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError?) {

                                }

                                override fun onDataChange(p0: DataSnapshot?) {
                                    if (p0!!.exists() && p0.hasChild("image")) {
                                        val name = p0.child("name").value.toString()
                                        val status = p0.child("status").value.toString()
                                        val image = p0.child("image").value.toString()

                                        holder.tv_name.text = name
                                        Glide.with(context!!.applicationContext).load(image)
                                            .apply(
                                                RequestOptions().placeholder(R.drawable.profile_image).error(
                                                    R.drawable.profile_image
                                                )
                                            )
                                            .into(holder.iv)
                                    } else {
                                        val name = p0.child("name").value.toString()
                                        val status = p0.child("status").value.toString()


                                        holder.tv_name.text = name
                                    }

                                    holder.btnCancel.setOnClickListener {
                                        cancelRequestMessage(currentUserId, id)
                                    }
                                }

                            })
                        }
                    }


                })


            }

        }
        recycler.adapter = firebaseListAdapter
        firebaseListAdapter.startListening()

        dislayNumberNoti()
    }

    private fun dislayNumberNoti() {
        chatRequestRef!!.child(currentUserId).addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists()) {
                    val i = p0.children.count()
                    val intent = Intent("SIZE")
                    intent.putExtra("size", i)
                    context!!.applicationContext.sendBroadcast(intent)
                }else{
                    val intent = Intent("SIZE")
                    intent.putExtra("size", 0)
                    activity!!.applicationContext.sendBroadcast(intent)
                }
            }

        })
    }

    class ContactViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tv_name = v.tv_user_name
        val tv_status = v.tv_user_status
        val btnAccept = v.btn_accept
        val btnCancel = v.btn_cancel
        val iv = v.user_profile_image
    }

    private fun acceptChatRequest(senderUserId: String, receiverUserId: String) {
        contactRef!!.child(senderUserId).child(receiverUserId).child("Contacts").setValue("saved")
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    contactRef!!.child(receiverUserId).child(senderUserId).child("Contacts")
                        .setValue("saved").addOnCompleteListener {
                            if (it.isSuccessful) {
                                cancelRequestMessage(senderUserId, receiverUserId)
                            }
                        }
                }
            }
    }

    private fun cancelRequestMessage(senderUserId: String, receiverUserId: String) {
        chatRequestRef!!.child(senderUserId).child(receiverUserId).removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    chatRequestRef!!.child(receiverUserId).child(senderUserId).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                            }
                        }
                }
            }
    }
}
