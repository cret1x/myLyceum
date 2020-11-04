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
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.Exception


class FeedFragment : Fragment() {
    private val TAG = "MyLyceum"
    private lateinit var recycler: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private var sources = ""
    private var offset = ""
    private val POST_COUNT = 10
    private val VK_SOURCE_PREFS_NAME = "vkSourcePrefs"
    private var feedFirstStart = true
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
            loadMoreVKPosts(true, false)
        }
        return root
    }

    // Is Error Loading = ISL
    fun loadMoreVKPosts(first: Boolean, IEL: Boolean) {
        var isErrorLoading = IEL
        // Делаю запрос к апи и подгружаю посты со сдвигом или без

        val disposable = VKAPICalls.getFeedFromSources(sources, offset, POST_COUNT, first)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { post ->
                // Если ошибка, то убираем штуку "загрузить еще"
                if (isErrorLoading) {
                    (recycler.adapter as PostAdapter).removeLast()
                }
                isErrorLoading = false

                // type -3 это объект для передачи следующего сдвига
                if (post.type_ == -3) {
                    offset = (post as PostItemOffset).data as String
                    // если в нем передается это, то выводим что постов нет
                    if (offset == "_no_post_") {
                        //Toast.makeText(context, getString(R.string.all_loaded), Toast.LENGTH_SHORT).show()
                        Snackbar.make(activity!!.findViewById(android.R.id.content), getString(R.string.all_loaded), Snackbar.LENGTH_SHORT).show()
                    }
                // type -4 тоже почему то означает пустой список постов
                } else if (post.type_ == -4) {
                    Snackbar.make(activity!!.findViewById(android.R.id.content), getString(R.string.all_loaded), Snackbar.LENGTH_SHORT).show()
                    //Toast.makeText(context, getString(R.string.all_loaded), Toast.LENGTH_SHORT).show()
                // type -4 означает что нужно вывести сообщение (в данном случае с ошибкой)
                } else if (post.type_ == -1) {
                    //Log.d("ERROR", "WAS ERROR LOADING FEED")
                    (recycler.adapter as PostAdapter).apply {  postList = mutableListOf() }
                    (recycler.adapter as PostAdapter).apply {  postList = mutableListOf(PostItemInformal(getString(R.string.error_posts))) }
                    //Log.d("ADAPTER CONTENT", (recycler.adapter as PostAdapter).postList.toString())
                    isErrorLoading = true
                // иначе тупо добавляем пост в ленту
                } else {
                    (recycler.adapter as PostAdapter).append(post as PostItem)
                }
            }
            // По завершению, добавляем кнопку "загрузить еще"
            .doOnComplete {
                loadingProgressBar.visibility = View.INVISIBLE
                //Log.d("IS ERROR LOADING", isErrorLoading.toString())
                //.d("ADAPTER CONTENT ON COM", (recycler.adapter as PostAdapter).postList.toString())
                if (!isErrorLoading) {
                    //Log.d("ON COMPLETE", "APPEND BUTTON")
                    (recycler.adapter as PostAdapter).append(PostItemButton(getString(R.string.load_more)) {
                        // Которая при нажатии удаляется и рекурсивно вызывает функцию загрузки постов
                        loadingProgressBar.visibility = View.VISIBLE
                        //Log.d("TAG", (recycler.adapter as PostAdapter).postList.toString())
                        try {
                            (recycler.adapter as PostAdapter).removeLast()
                            loadMoreVKPosts(false, isErrorLoading)
                        } catch (e: Exception) {
                            //Log.e("ERROR", "обибка тут")
                        }


                    })
                }
            }
            .doOnError {
                run {
                //Log.d("ERROR", "ERROR")

                //Toast.makeText(requireContext(),"Невозможно обновить ленту", Toast.LENGTH_LONG).show()
                Snackbar.make(activity!!.findViewById(android.R.id.content), getString(R.string.error_posts), Snackbar.LENGTH_SHORT).show()
                recycler.adapter = PostAdapter(requireContext(),requireActivity()).apply { mutableListOf(PostItemInformal(getString(R.string.error_posts))) }
                isErrorLoading = true
                }
            }
            .subscribe()
    }


    companion object {
        @JvmStatic
        fun newInstance() = FeedFragment()
    }
}
