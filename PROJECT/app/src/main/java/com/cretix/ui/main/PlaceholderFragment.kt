package com.cretix.ui.main

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cretix.MyApplication
import com.cretix.R
import com.cretix.RoomComponents.SourceRoom.VKSourceEntity
import com.cretix.SourceAdapter
import com.cretix.SourceItem
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.android.schedulers.AndroidSchedulers


class PlaceholderFragment : Fragment() {
    private lateinit var recycler: RecyclerView
    private val authorizationPreferencesName = "authPrefs"
    private lateinit var authPrefs: SharedPreferences
    private lateinit var pageViewModel: PageViewModel
    private var fireDB: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val roomDB = MyApplication.instance.getDatabase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_source_select, container, false)
        authPrefs = requireContext().getSharedPreferences(authorizationPreferencesName, Context.MODE_PRIVATE)
        recycler = root.findViewById(R.id.source_recycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = SourceAdapter(requireContext(), requireActivity())
        var type = arguments?.getInt(ARG_SECTION_NUMBER) ?: 0
        type--

        val emptySource = SourceItem("empty","",false,false, false, "")
        val authSource = SourceItem("no_auth","",false,false, false, "")


        when (type) {
            0 -> {
                if (authPrefs.getBoolean("isVKAuth", false)) {
                    roomDB.vkSourceDao().getAll()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { result ->
                            run {
                                var items = mutableListOf<SourceItem>()
                                if (result.isEmpty()) {
                                    items.add(
                                       emptySource
                                    )
                                } else {
                                    for (source in result) {
                                        items.add(createVKSourceItem(source))
                                    }
                                }

                                (recycler.adapter as SourceAdapter).apply {
                                    sourceList = items
                                    innerType = 0
                                }
                            }
                        }
                } else {
                    (recycler.adapter as SourceAdapter).apply {
                        sourceList = mutableListOf(authSource)
                        innerType = 0
                    }
                }
            }
            1 -> (recycler.adapter as SourceAdapter).apply {
                sourceList = mutableListOf(authSource)
                innerType = 0
            }
            else -> (recycler.adapter as SourceAdapter).apply {
                sourceList = mutableListOf(authSource)
                innerType = 0
            }
        }
        return root
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    private fun createVKSourceItem(src: VKSourceEntity) : SourceItem {
        return SourceItem(src.id, src.title, !src.isClosed, src.isSelected, src.isNotify, src.groupIcon)
    }
}