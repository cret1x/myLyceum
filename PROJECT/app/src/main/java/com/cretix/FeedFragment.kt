package com.cretix

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class FeedFragment : Fragment() {
    private val TAG = "MyLyceum"
    private lateinit var recycler: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private var sources = ""
    private var offset = ""
    private val POST_COUNT = 10
    private val VK_SOURCE_PREFS_NAME = "vkSourcePrefs"
    private var feedFirstStart = true
    private var isErrorLoading = false
    private lateinit var vkSourcePrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("FeedFragment", "OnCreate()")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("FeedFragment", "onDestroyView()")
        feedFirstStart = false
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FeedFragment", "onDestroy()")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("FeedFragment", "onDetach()")
    }

    override fun onStart() {
        super.onStart()
        Log.d("FeedFragment", "onStart()")
    }

    override fun onStop() {
        super.onStop()
        Log.d("FeedFragment", "onStop()")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("FeedFragment", "onAttach()")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("FeedFragment", "onCreateView()")
        val root = inflater.inflate(R.layout.fragment_feed, container, false)
        vkSourcePrefs = requireContext().getSharedPreferences(VK_SOURCE_PREFS_NAME, Context.MODE_PRIVATE)
        sources = vkSourcePrefs.getString("sources_vk", "")!!
        loadingProgressBar = root.findViewById(R.id.update_feed)
        recycler = root.findViewById(R.id.feed_recycler)
        recycler.layoutManager = LinearLayoutManager(activity?.applicationContext)
        recycler.adapter = PostAdapter(requireContext(),requireActivity())
        loadingProgressBar.visibility = View.VISIBLE
        Log.d(TAG, "sources: [$sources]")
        if (sources == "") {
            loadingProgressBar.visibility = View.INVISIBLE
            recycler.adapter = PostAdapter(requireContext(),requireActivity()).apply { postList = mutableListOf(PostItemInformal(getString(R.string.no_sources))) }
        } else {
            loadMoreVKPosts(true)
        }
        return root
    }


    fun loadMoreVKPosts(first: Boolean) {
        val disposable = VKAPICalls.getFeedFromSources(sources, offset, POST_COUNT, first)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { post ->
                if (isErrorLoading) {
                    (recycler.adapter as PostAdapter).removeLast()
                }
                isErrorLoading = false
                if (post.type_ == -3) {
                    offset = (post as PostItemOffset).data as String
                    if (offset == "_no_post_") {
                        Toast.makeText(context, getString(R.string.all_loaded), Toast.LENGTH_SHORT).show()
                    }
                } else if (post.type_ == -4) {
                    Toast.makeText(context, getString(R.string.all_loaded), Toast.LENGTH_SHORT).show()
                } else if (post.type_ == -1) {
                    (recycler.adapter as PostAdapter).apply {  postList = mutableListOf(PostItemInformal(getString(R.string.error_posts))) }
                    isErrorLoading = true
                } else {
                    (recycler.adapter as PostAdapter).append(post as PostItem)
                }
            }
            .doOnComplete {
                loadingProgressBar.visibility = View.INVISIBLE
                if (!isErrorLoading) {
                    (recycler.adapter as PostAdapter).append(PostItemButton(getString(R.string.load_more)) {

                        loadingProgressBar.visibility = View.VISIBLE
                        (recycler.adapter as PostAdapter).removeLast()
                        loadMoreVKPosts(false)
                    })
                }

            }
            .doOnError { run {
                Toast.makeText(requireContext(),"Невозможно обновить ленту", Toast.LENGTH_LONG).show()
                recycler.adapter = PostAdapter(requireContext(),requireActivity()).apply { mutableListOf(PostItemInformal(getString(R.string.error_posts))) }
                isErrorLoading = true
            } }
            .subscribe()
    }


    companion object {
        @JvmStatic
        fun newInstance() = FeedFragment()
    }
}
