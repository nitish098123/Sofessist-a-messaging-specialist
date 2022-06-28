package com.example.database_part_3.groups.view_media_section
import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.selection.SelectionTracker
import androidx.viewpager.widget.ViewPager
import com.example.database_part_3.R
import com.example.database_part_3.groups.view_media_section.image.image_fragment
import com.example.database_part_3.groups.view_media_section.vedios.vedios_fragement
import com.google.android.material.tabs.TabLayout

class whole_activity : AppCompatActivity(){

    private var tracker : SelectionTracker<Long>? = null      // for long pressed function in grid view

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_showing_layout)

        var tab_toolbar = findViewById<Toolbar>(R.id.toolbar_)
        var tab_viewpager = findViewById<ViewPager>(R.id.tab_viewpager)
        var tab_tablayout = findViewById<TabLayout>(R.id.tab_tablayout)

        setSupportActionBar(tab_toolbar)
        setupViewPager(tab_viewpager)

        tab_tablayout.setupWithViewPager(tab_viewpager)

    }


    private fun setupViewPager(viewpager: ViewPager) {
        var adapter: ViewPagerAdapter = ViewPagerAdapter(supportFragmentManager)


        adapter.addFragment(image_fragment(), "IMAGE")
        adapter.addFragment(vedios_fragement(), "VEDIOS")

        // setting adapter to view pager.
        viewpager.setAdapter(adapter)
    }

    class ViewPagerAdapter : FragmentPagerAdapter {
        private var tracker : SelectionTracker<Long>? = null      // for long pressed function in grid view
        private final var fragmentList1: ArrayList<Fragment> = ArrayList()
        private final var fragmentTitleList1: ArrayList<String> = ArrayList()

        // this is a secondary constructor of ViewPagerAdapter class.
        public constructor(supportFragmentManager: FragmentManager) : super(supportFragmentManager)

        // returns which item is selected from arraylist of fragments.
        override fun getItem(position: Int): Fragment {
            return fragmentList1.get(position)
        }

        // returns which item is selected from arraylist of titles.
        @Nullable
        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList1.get(position)
        }

        // returns the number of items present in arraylist.
        override fun getCount(): Int {
            return fragmentList1.size
        }

        // this function adds the fragment and title in 2 separate  arraylist.
        fun addFragment(fragment: Fragment, title: String){
            fragmentList1.add(fragment)
            fragmentTitleList1.add(title)

            tracker.let {

            }
        }
    }
}