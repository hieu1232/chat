package hieusenpaj.com.whapp.fragment


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import hieusenpaj.com.whapp.activity.GroupChatActivity
import hieusenpaj.com.whapp.dialog.DialogCreatGroup
import hieusenpaj.com.whapp.objet.Group
import hieusenpaj.com.whapp.objet.GroupUser
import hieusenpaj.com.whapp.objet.Message
import kotlinx.android.synthetic.main.fragment_groups.*
import kotlinx.android.synthetic.main.item_group.view.*


/**
 * A simple [Fragment] subclass.
 */
class GroupsFragment : Fragment() {
    var groupRef: DatabaseReference? = null
    var rootRef: DatabaseReference? = null
    var groupMessRef: DatabaseReference? = null
    var arr = ArrayList<String>()
    var userRef: DatabaseReference? = null
    var currentId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_groups, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.layoutManager = LinearLayoutManager(context)
        rootRef = FirebaseDatabase.getInstance().reference
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        groupRef = FirebaseDatabase.getInstance().reference.child("GroupDetail")
        groupMessRef = FirebaseDatabase.getInstance().reference.child("GroupMessages")
        currentId = FirebaseAuth.getInstance().currentUser!!.uid

        iv_add.setOnClickListener {
            val dialog = DialogCreatGroup(context!!,object : DialogCreatGroup.OnClickDialog{
                override fun onClick(name: String) {
                    createNewGroup(name)
                }

            })
            dialog.show()
        }
//
//

    }

    class GroupViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val iv = v.user_profile_image
        val tv_name = v.tv_user_name
        val tv_status = v.tv_user_status
        val iv_status = v.iv_status
        val tv_time = v.tv_time
        val iv_online = v.iv_online

    }

    override fun onStart() {
        super.onStart()
        readGroup()
        dislayIvUser()

    }

    private fun readGroup() {
        val options = FirebaseRecyclerOptions.Builder<GroupUser>()
            .setQuery(userRef!!.child(currentId).child("groups"), GroupUser::class.java)
            .build()
        val firebaseListAdapter = object :
            FirebaseRecyclerAdapter<GroupUser, GroupViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): GroupViewHolder {
                val v =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
                return GroupViewHolder(v)

            }

            override fun onBindViewHolder(holder: GroupViewHolder, p1: Int, p2: GroupUser) {

                val key = p2.id


                groupRef!!.child(key).addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        if (p0!!.exists()) {
                            holder.tv_name.text = p0.child("name").value.toString()
                            holder.iv.setOnClickListener {
                                val intent = Intent(context, GroupChatActivity::class.java)
                                intent.putExtra("name", p0.child("name").value.toString())
                                intent.putExtra("id", p0.child("id").value.toString())
                                context!!.startActivity(intent)
                            }
                        }

                    }

                })
                groupRef!!.child(key).child("member")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError?) {

                        }

                        override fun onDataChange(p0: DataSnapshot?) {
                            arr.clear()
                            for ((i, member) in p0!!.children.withIndex()) {

                                userRef!!.child(member.key)
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError?) {

                                        }

                                        override fun onDataChange(p0: DataSnapshot?) {

                                            if (p0!!.hasChild("image")) {
//                                                Toast.makeText(
//                                                    context,
//                                                    arr.size.toString(),
//                                                    Toast.LENGTH_SHORT
//                                                ).show()
//                                                arr.add(p0.child("image").value.toString())

                                                holder.iv.putImages(p0.child("image").value.toString(),i+1)

                                            }
                                        }

                                    })
                            }

//
                        }

//


                    })
//                holder.tv_name.text = arr.size.toString()

                groupMessRef!!.child(key).addChildEventListener(object : ChildEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                    }

                    override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                    }

                    override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                        if (p0!!.exists()) {
                            val message = p0.getValue(Message::class.java)
                            holder.tv_time.text = message!!.time
                            userRef!!.child(message.from)
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError?) {

                                    }

                                    override fun onDataChange(p0: DataSnapshot?) {
                                        val name = p0!!.child("name").value.toString()
                                        if (message.type == "text") {
                                            holder.tv_status.text = "$name : ${message.message}"
                                        } else {
                                            holder.tv_status.text = "$name : Sent a image"
                                        }

                                        if(p0.child("uid").value.toString()!= currentId) {

                                            if (p0.child("userState").hasChild("state")) {
//                                                val time =
//                                                    p0.child("userState").child("time")
//                                                        .value.toString()
//                                                holder.tv_time.apply {
//                                                    text = time
//                                                    visibility = View.VISIBLE
//                                                }
                                                if(p0.child("userState").child("state").value.toString()=="online"){
                                                    holder.iv_online.visibility = View.VISIBLE
                                                }else{
                                                    holder.iv_online.visibility = View.GONE
                                                }
                                            }
                                        }else{
//                                            holder.tv_time.visibility = View.GONE
                                        }
                                    }

                                })


                        }
                    }

                    override fun onChildRemoved(p0: DataSnapshot?) {
                    }

                })


            }

        }
        recycler.adapter = firebaseListAdapter
        firebaseListAdapter.startListening()
    }

    private fun dislayIvUser() {
        userRef!!.child(currentId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                Toast.makeText(context,"hieu",Toast.LENGTH_SHORT).show()
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
            }

        })
    }
    private fun createNewGroup(name: String) {
        val key = rootRef!!.child("GroupDetail").push().key
        var hashMap: HashMap<String, Any> = HashMap()
        hashMap["id"] = key
        hashMap["created"]= currentId
        hashMap["name"] = name
        hashMap["member/$currentId/id"]= currentId
//
        rootRef!!.child("GroupDetail").child(key).updateChildren(hashMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context!!, "tao group thanh cong", Toast.LENGTH_SHORT).show()
            }
        }
        val hashMapUser :HashMap<String,Any> = HashMap()
        hashMapUser["id"] = key
        rootRef!!.child("Users").child(currentId).child("groups").child(key).setValue(hashMapUser)

    }
}
