package hieusenpaj.com.whapp.adapter

import android.content.Context
import android.icu.text.CaseMap

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import hieusenpaj.com.whapp.R
import kotlinx.android.synthetic.main.tab_item.view.*

class TabAdapter(private var context:Context,
                 private var list: ArrayList<Fragment>,
                 private var arrTitle :ArrayList<String>, fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
    override fun getItem(p0: Int): Fragment = list.get(p0)



    override fun getCount(): Int =list.size
    fun addViewFragment(fragment: Fragment,s: String) {
        list.add(fragment)
        arrTitle.add(s)
    }

    fun getTabView(position: Int): View {
        val view = LayoutInflater.from(context).inflate(R.layout.tab_item, null)
        view.tv_tab.text =  arrTitle[position]
        return view
    }



}