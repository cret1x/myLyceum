package com.cretix

import android.util.Log
import com.vk.sdk.api.*
import io.reactivex.*
import org.intellij.lang.annotations.Flow
import org.json.JSONException
import org.json.JSONObject
import kotlin.concurrent.thread

abstract class VKAPICalls {

    class Group(gid: Long) {

        private lateinit var obj: JSONObject

        init {
            val request = VKApi.groups().getById(VKParameters.from(
                "group_id", (gid * -1).toString()
            ))
            request.executeSyncWithListener(object: VKRequest.VKRequestListener() {
                override fun onComplete(response: VKResponse?) {
                    super.onComplete(response)
                    obj = response!!.json.getJSONArray("response").get(0) as JSONObject
                }

                override fun onError(error: VKError?) {
                    super.onError(error)
                    Log.e(TAG, error.toString())
                }
            })
        }
        private fun Int.toBoolean() = this == 1

        fun getIcon() : String {
            return obj.getString("photo_100")
        }

        fun isClosed() : Boolean {
            return obj.getInt("is_closed").toBoolean()
        }

    }

    companion object {
        val TAG = "VK_API"
        private val roomDB = MyApplication.instance.getDatabase()


        fun getFeedFromSources(sources: String, offset: String, count: Int, isInitRequest: Boolean) : Flowable<PostItemBase> {
            return Flowable.create(FlowableOnSubscribe {
                val request = VKRequest(
                    "newsfeed.get", VKParameters.from(
                        VKApiConst.FILTERS, "post",
                        "source_ids", sources,
                        VKApiConst.COUNT, 10,
                        "start_from", offset
                    )
                )
                request.executeWithListener(object : VKRequest.VKRequestListener() {
                    override fun onComplete(response: VKResponse) {
                        val posts = response.json.getJSONObject("response").getJSONArray("items")
                        if (posts.length() == 0) {
                            it.onNext(PostItemEmptyFlag(true))
                            it.onComplete()
                            return
                        }
                        val nextFrom = response.json.getJSONObject("response").getString("next_from")
                        if (nextFrom == "" && !isInitRequest) {
                            it.onNext(PostItemOffset("_no_post_"))
                            it.onComplete()
                        }
                        it.onNext(PostItemOffset(nextFrom))

                        thread(start = true) {
                            for (i in 0 until posts.length()) {
                                val item = posts.getJSONObject(i)
                                val pid = item.getLong("source_id").toString() + "_" + item.getLong("post_id").toString()
                                val title = roomDB.vkSourceDao().getTitleByGid(item.getLong("source_id"))
                                val iconUrl = roomDB.vkSourceDao().getIconById(item.getLong("source_id"))
                                val photos = mutableListOf<String>()
                                val documents = mutableListOf<String>()
                                if (posts.getJSONObject(i).has("attachments")) {
                                    val attachments = posts.getJSONObject(i).getJSONArray("attachments")
                                    for (j in 0 until  attachments.length()) {
                                        val attachment = attachments[j] as JSONObject
                                        if (attachment.getString("type") == "photo") {
                                            val photo_ = attachment.getJSONObject("photo")
                                            var photo_link = ""
                                            try {
                                                photo_link = photo_.getString("photo_1280")
                                            } catch (e: JSONException) {
                                                try {
                                                    photo_link = photo_.getString("photo_807")
                                                } catch (e: JSONException) {
                                                    try {
                                                        photo_link = photo_.getString("photo_604")
                                                    } catch (e: JSONException) {
                                                        try {
                                                            photo_link = photo_.getString("photo_130")
                                                        } catch (e: JSONException) {
                                                            photo_link = "https://vk.com/images/camera_200.png"
                                                        }
                                                    }
                                                }
                                            }

                                            photos.add(photo_link)
                                        }
                                    }
                                }
                                it.onNext(PostItem(pid, title, item.getString("text"), item.getLong("date"), 0, iconUrl, photos.toList(), photos.size))
                            }
                            it.onComplete()
                        }

                    }
                    override fun onError(error: VKError) {
                        Log.e(TAG, error.toString())
                    }
                    override fun attemptFailed(
                        request: VKRequest,
                        attemptNumber: Int,
                        totalAttempts: Int
                    ) {
                        Log.e(TAG, "failed")
                    }
                })
            }, BackpressureStrategy.BUFFER)
        }
    }
}