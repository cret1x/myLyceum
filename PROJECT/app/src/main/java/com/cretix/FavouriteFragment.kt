package com.cretix

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class FavouriteFragment : Fragment() {
    private val roomDB = MyApplication.instance.getDatabase()
    private lateinit var recycler: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_favourite, container, false)
        recycler = root.findViewById(R.id.recycler)
        loadingProgressBar = root.findViewById(R.id.update_fav)
        recycler.layoutManager = LinearLayoutManager(activity?.applicationContext)
        loadingProgressBar.visibility = View.VISIBLE
        recycler.adapter = PostAdapter(requireContext(), requireActivity()).apply {
            postList = mutableListOf()
        }

        val disposable = roomDB.vkPostDao().getFavourite()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { list ->
                (recycler.adapter as PostAdapter).apply { postList = mutableListOf() }
                for (post in list) {
                    var photos: List<String>
                    if (post.photos.isEmpty()) {
                        photos = listOf()
                    } else {
                        photos = post.photos.split(",")
                    }
                    (recycler.adapter as PostAdapter).append(
                        PostItem(
                            post.id,
                            post.title,
                            post.text,
                            post.time,
                            0,
                            post.icon,
                            photos.toList(),
                            photos.size,
                            post.addons
                        )
                    )
                }
                if (list.isEmpty()) {
                    (recycler.adapter as PostAdapter).apply {
                        postList = mutableListOf(PostItemInformal(getString(R.string.no_fav)))
                    }
                }
                loadingProgressBar.visibility =  View.INVISIBLE
            }
            .doOnComplete { }
            .subscribe()
        return root
    }

    companion object {
        @JvmStatic
        fun newInstance() {
        }
    }
}
