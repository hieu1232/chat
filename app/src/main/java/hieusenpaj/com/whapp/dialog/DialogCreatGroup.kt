package hieusenpaj.com.whapp.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import hieusenpaj.com.whapp.R
import kotlinx.android.synthetic.main.dialog_creat_group.*

class DialogCreatGroup(context: Context,private val listener: OnClickDialog) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window!!.setBackgroundDrawable(ColorDrawable(0))
        setContentView(R.layout.dialog_creat_group)
        window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        ed_creat.setSelectAllOnFocus(true)
        ed_creat.requestFocus();
        window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        tv_cancel.setOnClickListener {
            dismiss()
        }
        tv_creat.setOnClickListener {
            if(TextUtils.isEmpty(ed_creat.text.toString())){
                Toast.makeText(context,"Dien cho du ten vao",Toast.LENGTH_SHORT).show()
            }else {
                listener.onClick(ed_creat.text.toString())
                dismiss()
            }
        }
    }

    interface OnClickDialog {
        fun onClick(name:String)
    }
}