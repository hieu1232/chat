package hieusenpaj.com.whapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import hieusenpaj.com.whapp.adapter.AddFriendAdapter
import hieusenpaj.com.whapp.objet.Add
import kotlinx.android.synthetic.main.activity_add_friend_group.*
import kotlinx.android.synthetic.main.item_add_friend.view.*

class AddFriendGroupActivity : AppCompatActivity() {
    var contactRef: DatabaseReference? = null
    var userRef: DatabaseReference? = null
    var currentUserId = ""
    var arr = ArrayList<Add>()
    var adapter: AddFriendAdapter? = null
    var i = 0
    var arrAdd = ArrayList<Add>()
    var groupName =""
    var groupId =""
    var rootRef:DatabaseReference?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend_group)


        groupName = intent.extras!!.getString("name")!!
        groupId = intent.extras!!.getString("id")!!

        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        contactRef = FirebaseDatabase.getInstance().reference.child("Contacts").child(currentUserId)
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        rootRef = FirebaseDatabase.getInstance().reference
        recycler.layoutManager = LinearLayoutManager(this)



    }

    override fun onStart() {
        super.onStart()
        readUser()

    }

    private fun readUser() {
        contactRef!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                for (dataSnapshot in p0!!.children) {

                    userRef!!.child(dataSnapshot.key).child("groups").addListenerForSingleValueEvent(object :ValueEventListener{
                        override fun onCancelled(p0: DatabaseError?) {

                        }

                        override fun onDataChange(p0: DataSnapshot?) {
                            if (p0!!.hasChild(groupId)){

                            }else{
                                arr.add(Add(dataSnapshot.key, false))

                                adapter = AddFriendAdapter(
                                    this@AddFriendGroupActivity,
                                    arr,
                                    userRef!!,
                                    object : AddFriendAdapter.ListenClick {
                                        override fun onClick(position: Int) {


                                            i++
                                            arr[position].isAdd = !arr[position].isAdd
                                            adapter?.notifyDataSetChanged()
                                            if (checkAddEmty()) {
                                                iv_add.visibility = View.GONE
                                                iv_cancel.visibility = View.GONE
                                                iv_back.visibility = View.VISIBLE
                                            } else {
                                                iv_add.visibility = View.VISIBLE
                                                iv_cancel.visibility = View.VISIBLE
                                                iv_back.visibility = View.GONE
                                            }
                                        }

                                    })
                                recycler.adapter = adapter
                            }
                        }

                    })


                }



            }

        })
        Toast.makeText(this@AddFriendGroupActivity,arr.size.toString(),Toast.LENGTH_SHORT).show()



        iv_back.setOnClickListener {
            onBackPressed()
        }
        iv_cancel.setOnClickListener {
            for (add in arr) {
                add.isAdd = false
            }
            adapter?.notifyDataSetChanged()
            iv_add.visibility = View.GONE
            iv_cancel.visibility = View.GONE
            iv_back.visibility = View.VISIBLE
        }
        iv_add.setOnClickListener {
            arrAdd.clear()
            for (add in arr) {
                if (add.isAdd) {
                    arrAdd.add(add)
                }
            }
            for (add in arrAdd){
                creatGroup(add.key)
            }

            startActivity(Intent(this,MainActivity::class.java))

        }


    }

    private fun creatGroup(userKey:String) {
        var hashMap: HashMap<String, Any> = HashMap()
        hashMap["id"] = userKey
        rootRef!!.child("GroupDetail").child(groupId).child("member").child(userKey).updateChildren(hashMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "tao group thanh cong", Toast.LENGTH_SHORT).show()
            }
        }
        val hashMapUser :HashMap<String,Any> = HashMap()
        hashMapUser["id"] = groupId
        rootRef!!.child("Users").child(userKey).child("groups").child(groupId).setValue(hashMapUser)
    }

    private fun checkAddEmty(): Boolean {
        for (add in arr) {
            if (add.isAdd) return false
        }
        return true
    }

//    override fun onStart() {
//        super.onStart()
//        val options = FirebaseRecyclerOptions.Builder<Add>()
//            .setQuery(contactRef!!, Add::class.java)
//            .build()
//        val firebaseListAdapter = object :
//            FirebaseRecyclerAdapter<Add, ContactViewHolder>(options) {
//            override fun onCreateViewHolder(
//                parent: ViewGroup,
//                viewType: Int
//            ): ContactViewHolder {
//                val v =
//                    LayoutInflater.from(parent.context).inflate(R.layout.item_add_friend, parent, false)
//                return ContactViewHolder(v)
//
//            }
//
//            override fun onBindViewHolder(holder: ContactViewHolder, p1: Int, p2: Add) {
//                val id = getRef(p1).key
//                userRef!!.child(id).addValueEventListener(object : ValueEventListener {
//                    override fun onCancelled(p0: DatabaseError?) {
//
//                    }
//
//                    override fun onDataChange(p0: DataSnapshot?) {
//                        if(p0!!.hasChild("image")){
//                            val name = p0.child("name").value.toString()
//                            val image = p0.child("image").value.toString()
//
//                            holder.tv_name.text = name
//
//                            Glide.with(this@AddFriendGroupActivity).load(image)
//                                .apply(RequestOptions().placeholder(R.drawable.profile_image).error(R.drawable.profile_image))
//                                .into(holder.iv)
//                        }else{
//
//                            val name = p0.child("name").value.toString()
//                            holder.tv_name.text = name
//                        }
//
//                        holder.rl.setOnClickListener {
//
//                        }
//                    }
//
//                })
//
//
//            }
//
//        }
//        recycler.adapter = firebaseListAdapter
//        firebaseListAdapter.startListening()
//    }
//
//    class ContactViewHolder(v: View) : RecyclerView.ViewHolder(v) {
//        val tv_name = v.tv_user_name
//        val iv = v.user_profile_image
//        val rl = v.rl
//    }
}
