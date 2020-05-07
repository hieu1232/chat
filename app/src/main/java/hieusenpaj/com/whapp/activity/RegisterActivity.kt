package hieusenpaj.com.whapp.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import hieusenpaj.com.whapp.R
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    var mAuth: FirebaseAuth? = null
    var loading: ProgressDialog? = null
    var rootRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        rootRef = FirebaseDatabase.getInstance().reference
        loading = ProgressDialog(this)

        tv_sign_in.setOnClickListener {
            onBackPressed()
        }
        ll_sign_up.setOnClickListener {
            creatNewAccount()
        }
        iv_back.setOnClickListener {
            onBackPressed()
        }
        ed_password.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
               creatNewAccount()
                handled = true
            }
            handled
        })
    }

    private fun creatNewAccount() {
        val email = ed_email.text.toString()
        val password = ed_password.text.toString()
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "dien cho du eamail vao", Toast.LENGTH_SHORT).show()
        }
        else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "dien cho du password vao", Toast.LENGTH_SHORT).show()
        } else {
            if (password.length >= 8) {
                loading!!.setTitle("Cho ty")
                loading!!.setCancelable(false)
                loading!!.show()
                mAuth!!.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, SettingActivity::class.java))
                            finishAffinity()
                            Toast.makeText(this, "Dang ki thanh cong", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT)
                                .show()
                        }
                        loading!!.dismiss()
                    }
            } else {
                Toast.makeText(this, "mat khau phai chua it nhat 8 ki tu", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


}
