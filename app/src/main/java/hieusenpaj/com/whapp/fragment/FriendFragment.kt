package hieusenpaj.com.whapp.fragment


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import hieusenpaj.com.whapp.R
import hieusenpaj.com.whapp.activity.ProfileUserVisitActivity
import hieusenpaj.com.whapp.objet.Contact
import kotlinx.android.synthetic.main.fragment_contacts.*
import kotlinx.android.synthetic.main.item_user.view.*

/**
 * A simple [Fragment] subclass.
 */
class FriendFragment : Fragment() {
    var contactRef:DatabaseReference?=null
    var userRef:DatabaseReference?=null
    var currentUserId=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        currentUserId= FirebaseAuth.getInstance().currentUser!!.uid
        contactRef = FirebaseDatabase.getInstance().reference.child("Contacts").child(currentUserId)
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.layoutManager = LinearLayoutManager(context)
    }

    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Contact>()
            .setQuery(contactRef!!, Contact::class.java)
            .build()
        val firebaseListAdapter = object :
            FirebaseRecyclerAdapter<Contact, ContactViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ContactViewHolder {
                val v =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
                return ContactViewHolder(v)

            }

            override fun onBindViewHolder(holder: ContactViewHolder, p1: Int, p2: Contact) {
                val id = getRef(p1).key
                holder.tv_status.visibility= View.GONE
                holder.tv_time.visibility= View.GONE
                userRef!!.child(id).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0!!.hasChild("image")){
                            val name = p0.child("name").value.toString()
                            val status = p0.child("status").value.toString()
                            val image = p0.child("image").value.toString()

                            holder.tv_name.text = name
                            holder.tv_status.text = status
                            Glide.with(context).load(image)
                                .apply(RequestOptions().placeholder(R.drawable.profile_image).error(R.drawable.profile_image))
                                .into(holder.iv)
                        }else{

                            val name = p0.child("name").value.toString()
                            val status = p0.child("status").value.toString()
                            holder.tv_name.text = name
                            holder.tv_status.text = status
                        }
                        if(p0.child("userState").hasChild("state")){
                            val state = p0.child("userState").child("state").value.toString()
                            val date = p0.child("userState").child("date").value.toString()
                            val time = p0.child("userState").child("time").value.toString()
                            if(state == "online"){
                               holder.iv_online.visibility = View.VISIBLE
                            }else{
                                holder.iv_online.visibility = View.GONE
                            }
                        }else{
                            holder.iv_online.visibility = View.GONE
                        }
                        holder.iv.setOnClickListener {
                            val intent = Intent(context,ProfileUserVisitActivity::class.java)
                            intent.putExtra("id",p0.child("uid").value.toString())
                            startActivity(intent)
                        }
                    }

                })


            }

        }
        recycler.adapter = firebaseListAdapter
        firebaseListAdapter.startListening()
    }

    class ContactViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tv_name = v.tv_user_name
        val tv_status = v.tv_user_status
        val iv = v.user_profile_image
        val iv_status = v.iv_status
        val tv_time = v.tv_time
        val iv_online = v.iv_online
    }




}
