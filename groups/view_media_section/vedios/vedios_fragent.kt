package com.example.database_part_3.groups.view_media_section.vedios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.database_part_3.R

class vedios_fragement : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.vedios_layout, container, false
        )
    }
    // Here "layout_login" is a name of layout file
    // created for LoginFragment
}