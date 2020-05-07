package hieusenpaj.com.whapp.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import hieusenpaj.com.whapp.R
import hieusenpaj.com.whapp.adapter.TabAdapter
import hieusenpaj.com.whapp.fragment.*
import hieusenpaj.com.whapp.objet.Contact
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_user.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    val fragment1: Fragment = GroupsFragment()
    val fragment2: Fragment = ChatsFragment()
    val fragment3: Fragment = NotificationFragment()
    val fragment4: Fragment = FriendFragment()
    val fragment5: Fragment = ProfileFragment()
    val fm: FragmentManager = supportFragmentManager
    var active = fragment2

    companion object {
        private const val ID_HOME = 1
        private const val ID_EXPLORE = 2
        private const val ID_MESSAGE = 3
        private const val ID_NOTIFICATION = 4
        private const val ID_ACCOUNT = 5
    }

    var currentUser: FirebaseUser? = null
    var currentUserId = ""
    var mAuth: FirebaseAuth? = null
    var rootRef: DatabaseReference? = null
    var userRef: DatabaseReference? = null
    var tabAdapter: TabAdapter? = null
    var arrFragment = ArrayList<Fragment>()
    var arrTitle = ArrayList<String>()
    var popup: ListPopupWindow? = null
    var i = 0
    var isOff = false
    private var str=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser
        currentUserId = currentUser!!.uid
        rootRef = FirebaseDatabase.getInstance().reference
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        registerReceiver(br, IntentFilter("SIZE"))
        registerReceiver(brOff, IntentFilter("OFF"))
        registerReceiver(brSearch, IntentFilter("SEARCH"))
        recycler.layoutManager = LinearLayoutManager(this)

        showBottom()
        setUpBack()
    }

    private fun setUpBack() {
        iv_back.setOnClickListener {
            rl_container.visibility = View.VISIBLE
            rl_search.visibility = View.GONE
            hideKeybroad()
            ed_search.text.clear()
        }
    }

    override fun onStart() {
        super.onStart()
//
        updateUserStatus("online")
        setupSearch()
//        }
    }

    override fun onStop() {
        super.onStop()
        if (i == 0) {
            if (currentUser != null) {
                rootRef = FirebaseDatabase.getInstance().reference

                updateUserStatus("offline")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fragment5.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (i == 0) {
            if (currentUser != null) {
                rootRef = FirebaseDatabase.getInstance().reference

                updateUserStatus("offline")
            }

        }
        unregisterReceiver(br)
        unregisterReceiver(brOff)
        unregisterReceiver(brSearch)

    }

    private var br = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val size = p1!!.getIntExtra("size", 0)
            if (size > 0) {
                bottomNavigation.setCount(ID_NOTIFICATION, size.toString())
            } else {

                bottomNavigation.clearCount(ID_NOTIFICATION)
            }
        }

    }
    private var brOff = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val Off = p1!!.getBooleanExtra("off", false)
            isOff = Off

        }

    }
    private var brSearch = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            Toast.makeText(this@MainActivity,"br",Toast.LENGTH_SHORT).show()

            rl_container.visibility = View.GONE
            rl_search.visibility = View.VISIBLE
            ed_search.setSelectAllOnFocus(true)
            ed_search.requestFocus()

            ed_search.addTextChangedListener( object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                        str = p0.toString()
                        onStart()

                }

            })

        }

    }


    private fun setupSearch() {
        var options: FirebaseRecyclerOptions<Contact> ?=null
        if(str == ""){
            options = FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(userRef!!,Contact::class.java)
                .build()
        }else{
            options = FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(userRef!!.orderByChild("name").startAt(str),Contact::class.java)
                .build()
        }
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


                    p0.tv_date.visibility = View.GONE
                    p0.tv_name.text = p2.name
                    p0.tv_status.visibility = View.GONE
                    Glide.with(this@MainActivity).load(p2.image)
                        .apply(RequestOptions().placeholder(R.drawable.profile_image).error(R.drawable.profile_image))
                        .into(p0.iv)
                    p0.iv.setOnClickListener {
                        val intent = Intent(this@MainActivity, ProfileUserVisitActivity::class.java)
                        intent.putExtra("id", getRef(p1).key)
                        startActivity(intent)
                        hideKeybroad()
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
        val tv_date = v.tv_time
        val rl = v.rl
    }

    override fun onBackPressed() {
        if(rl_search.visibility == View.VISIBLE){
            rl_search.visibility = View.GONE
            rl_container.visibility = View.VISIBLE
            ed_search.text.clear()
//            hideKeybroad()
//            Toast.makeText(this,"hihi",Toast.LENGTH_SHORT).show()

        }else {
//            Toast.makeText(this,"hihi",Toast.LENGTH_SHORT).show()
            finishAffinity()
        }
    }

    private fun showBottom() {
        bottomNavigation.apply {


            add(MeowBottomNavigation.Model(ID_HOME, R.drawable.ic_home))
            add(MeowBottomNavigation.Model(ID_EXPLORE, R.drawable.ic_explore))
            add(MeowBottomNavigation.Model(ID_MESSAGE, R.drawable.ic_message))
            add(MeowBottomNavigation.Model(ID_NOTIFICATION, R.drawable.ic_notification))
            add(MeowBottomNavigation.Model(ID_ACCOUNT, R.drawable.ic_account))

//            setCount(ID_NOTIFICATION, "115")

            setOnShowListener {
                val name = when (it.id) {
                    ID_HOME -> "HOME"
                    ID_EXPLORE -> "EXPLORE"
                    ID_MESSAGE -> "MESSAGE"
                    ID_NOTIFICATION -> "NOTIFICATION"
                    ID_ACCOUNT -> "ACCOUNT"
                    else -> ""
                }

//                tvSelected.text = getString(R.string.main_page_selected, name)
            }

            setOnClickMenuListener {
                when (it.id) {
                    ID_HOME -> {
                        fm.beginTransaction().hide(active).show(fragment5).commit()
                        active = fragment5
                    }
                    ID_EXPLORE -> {
                        fm.beginTransaction().hide(active).show(fragment1).commit()
                        active = fragment1

                    }
                    ID_MESSAGE -> {
                        fm.beginTransaction().hide(active).show(fragment2).commit()
                        active = fragment2
                    }
                    ID_NOTIFICATION -> {
                        fm.beginTransaction().hide(active).show(fragment3).commit()
                        active = fragment3
                    }
                    ID_ACCOUNT -> {
                        fm.beginTransaction().hide(active).show(fragment4).commit()
                        active = fragment4
                    }

                }
                val name = when (it.id) {
                    ID_HOME -> "HOME"
                    ID_EXPLORE -> "EXPLORE"
                    ID_MESSAGE -> "MESSAGE"
                    ID_NOTIFICATION -> "NOTIFICATION"
                    ID_ACCOUNT -> "ACCOUNT"
                    else -> ""
                }
//                Toast.makeText(context, name, Toast.LENGTH_LONG).show()
            }

            setOnReselectListener {
                Toast.makeText(context, "item ${it.id} is reselected.", Toast.LENGTH_LONG).show()
            }
            show(3)

        }
        fm.beginTransaction().add(R.id.frame, fragment5, "5").hide(fragment5).commit()
        fm.beginTransaction().add(R.id.frame, fragment4, "4").hide(fragment4).commit()
        fm.beginTransaction().add(R.id.frame, fragment3, "3").hide(fragment3).commit()
        fm.beginTransaction().add(R.id.frame, fragment1, "1").hide(fragment1).commit()
        fm.beginTransaction().add(R.id.frame, fragment2, "2").commit()
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun handlePermission() {
        val perms = arrayOf(
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(perms, 3)
        } else {


        }

    }


    private fun updateUserStatus(state: String) {
        if (!isOff) {
            var saveCurrentTime = ""
            var saveCurrentDate = ""

            val calendar = Calendar.getInstance()
            val currentDate = SimpleDateFormat("MMM dd, yyy")
            saveCurrentDate = currentDate.format(calendar.time)

            val currentTime = SimpleDateFormat("hh:mm a")
            saveCurrentTime = currentTime.format(calendar.time)

            val hashMap: HashMap<String, Any> = HashMap()
            hashMap["time"] = saveCurrentTime
            hashMap["date"] = saveCurrentDate
            hashMap["state"] = state
            if (mAuth != null) {

                rootRef!!.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("userState").updateChildren(hashMap)
            }

        }
    }

    private fun hideKeybroad(){
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(ed_search.windowToken, 0)
    }

}
