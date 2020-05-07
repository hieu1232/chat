package hieusenpaj.com.whapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import hieusenpaj.com.whapp.R
import hieusenpaj.com.whapp.objet.Add
import kotlinx.android.synthetic.main.item_add_friend.view.*

class AddFriendAdapter(private var context: Context,
                       private var arrayList: ArrayList<Add>,
                       private var userRef: DatabaseReference,
                       private var listener:ListenClick): RecyclerView.Adapter<AddFriendAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddFriendAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_add_friend,parent,false))
    }

    override fun getItemCount(): Int = arrayList.size


    override fun onBindViewHolder(holder: AddFriendAdapter.ViewHolder, position: Int) {
        userRef.child(arrayList[position].key).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.hasChild("image")){
                    val name = p0.child("name").value.toString()
                    val image = p0.child("image").value.toString()

                    holder.tv_name.text = name

                    Glide.with(context.applicationContext).load(image)
                        .apply(RequestOptions().placeholder(R.drawable.profile_image).error(R.drawable.profile_image))
                        .into(holder.iv)
                }else{

                    val name = p0.child("name").value.toString()
                    holder.tv_name.text = name
                }


            }


        })
        if(arrayList[position].isAdd){
            holder.iv_add.setImageDrawable(context.resources.getDrawable(R.drawable.cicle_add))

        }else{
            holder.iv_add.setImageDrawable(context.resources.getDrawable(R.drawable.circle))
        }
        holder.rl.setOnClickListener {
            listener.onClick(position)
        }

    }
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tv_name = v.tv_user_name
        val iv = v.user_profile_image
        val rl = v.rl
        val iv_add = v.iv_cicle_add
    }
    interface ListenClick{
        fun onClick(position: Int)
    }
}