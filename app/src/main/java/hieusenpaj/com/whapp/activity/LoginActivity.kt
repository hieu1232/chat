package hieusenpaj.com.whapp.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import hieusenpaj.com.whapp.R
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    var currentUser:FirebaseUser?=null
    var mAuth :FirebaseAuth?=null
    var loading : ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        handlePermission()
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser
        loading = ProgressDialog(this)

        tv_sign_up.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }

        ll_login.setOnClickListener {
            allowLogin()
        }
        ed_password.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                allowLogin()
                handled = true
            }
            handled
        })



    }



    override fun onStart() {
        super.onStart()

        if(currentUser != null){
//            Toast.makeText(this,"hieu",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,SettingActivity::class.java))
            finishAffinity()
        }
    }
    private fun allowLogin() {
        loading!!.setTitle("Cho ty")
        loading!!.setCancelable(false)
        loading!!.show()
        val email = ed_email.text.toString()
        val password = ed_password.text.toString()
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"dien cho du eamail vao",Toast.LENGTH_SHORT).show()
            loading!!.dismiss()
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"dien cho du password vao",Toast.LENGTH_SHORT).show()
            loading!!.dismiss()
        }else{
            mAuth!!.signInWithEmailAndPassword(email,password).addOnCompleteListener{task ->
                if(task.isSuccessful){
                    startActivity(Intent(this,SettingActivity::class.java))
                    finishAffinity()
                }else {
                    Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
                loading!!.dismiss()
            }
        }
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
}
