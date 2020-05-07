package hieusenpaj.com.whapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import hieusenpaj.com.whapp.R
import hieusenpaj.com.whapp.objet.Message
import kotlinx.android.synthetic.main.left_message_layout.view.*

class MessagerAdapter(private var context: Context,
                      private var list: List<Message>):RecyclerView.Adapter<MessagerAdapter.ViewHolder>() {
    companion object {
        val LEFT = 0
        val RIGHT = 1
    }
    var currentUserId =""
    var userRef: DatabaseReference?= null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagerAdapter.ViewHolder {

        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        if (viewType == RIGHT) {
            val v =
                LayoutInflater.from(context).inflate(R.layout.right_message_layout, parent, false)
            return ViewHolder(v)
        }else{
            val v =
                LayoutInflater.from(context).inflate(R.layout.left_message_layout, parent, false)
            return ViewHolder(v)
        }

    }

    override fun getItemCount(): Int = list.size


    override fun onBindViewHolder(holder: MessagerAdapter.ViewHolder, position: Int) {

        val message = list[position]
        val fromUserId =message.from
        val messager = message.message
        val type =message.type


        if(type == "text"){
            holder.iv_message.visibility = View.GONE
            holder.tv_receiver.visibility = View.VISIBLE
            holder.tv_receiver.text = messager


        }else if (type=="image"){
            holder.iv_message.visibility = View.VISIBLE
            holder.tv_receiver.visibility = View.GONE
            Glide.with(context.applicationContext).load(messager)
                .into(holder.iv_message)
        }
            if(currentUserId != fromUserId){
                userRef = FirebaseDatabase.getInstance().reference.child("Users").child(fromUserId)

                userRef!!.addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0!!.hasChild("image")){
                            Glide.with(context.applicationContext).load(p0.child("image").value.toString())
                                .apply(RequestOptions().placeholder(R.drawable.profile_image).error(R.drawable.profile_image))
                                .into(holder.iv)
                        }
                    }


                })
            }
//                holder.tv_receiver.visibility = View.GONE
//                holder.iv.visibility = View.GONE
//
//                holder.tv_sender.text = messager
//            }else{
//                holder.tv_sender.visibility = View.GONE
//                holder.tv_receiver.text = messager
//            }
//        }

    }

    class ViewHolder(v:View):RecyclerView.ViewHolder(v){
        val iv = v.iv_profile
        val tv_receiver = v.tv_message
        val iv_message = v.iv_message
//        val tv_sender= v.tv_sender_message



    }

    override fun getItemViewType(position: Int): Int {

        if(FirebaseAuth.getInstance().currentUser!!.uid  == list[position].from){
            return RIGHT
        }else{
            return LEFT
        }
    }
}