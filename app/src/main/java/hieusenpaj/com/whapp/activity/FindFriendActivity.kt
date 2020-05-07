package hieusenpaj.com.whapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import hieusenpaj.com.whapp.R
import hieusenpaj.com.whapp.objet.Contact
import kotlinx.android.synthetic.main.activity_find_friend.*
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.item_user.*
import kotlinx.android.synthetic.main.item_user.view.*

class FindFriendActivity : AppCompatActivity() {
    var userRef: DatabaseReference? = null
    var currentUserId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friend)

        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

        setSupportActionBar(toolbar)
        tv_title.text = "Find Friend"

        recycler.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Contact>()
            .setQuery(userRef!!, Contact::class.java)
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

            override fun onBindViewHolder(p0: ContactViewHolder, p1: Int, p2: Contact) {
                    p0.tv_name.text = getRef(p1).key
                    Glide.with(this@FindFriendActivity).load(p2.image)
                        .apply(RequestOptions().placeholder(R.drawable.profile_image).error(R.drawable.profile_image))
                        .into(p0.iv)
                    p0.iv.setOnClickListener {
                        val intent =
                            Intent(this@FindFriendActivity, ProfileUserVisitActivity::class.java)
                        intent.putExtra("id", getRef(p1).key)
                        startActivity(intent)
                    }



            }

        }
        recycler.adapter = firebaseListAdapter
        firebaseListAdapter.startListening()
    }

    class ContactViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tv_name = v.tv_user_name
        val tv_status = v.tv_user_status
        val iv = v.user_profile_image
        val rl = v.rl
    }
}
