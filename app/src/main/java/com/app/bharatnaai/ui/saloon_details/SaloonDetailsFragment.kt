package com.app.bharatnaai.ui.saloon_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import bharatnaai.R

class SaloonDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_saloon_details, container, false)
    }

    companion object {
        fun newInstance() = SaloonDetailsFragment()
    }
}
