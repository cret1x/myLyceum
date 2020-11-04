package com.cretix

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Parcelable
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.synnapps.carouselview.ImageListener
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.item_empty_list.view.*
import kotlinx.android.synthetic.main.item_post_layout.view.*
import kotlinx.android.synthetic.main.item_post_layout.view.text
import java.io.File
import java.lang.Exception
import java.net.URL
import kotlin.concurrent.thread


abstract class PostItemBase(val type_: Int)

@Parcelize
data class PostItem(val pid: String, val title: String, val text: String, val time: Long, val type: Int, val icon: String, val media: List<String>, val photo_count: Int, val addon: String) : PostItemBase(type), Parcelable

data class PostItemInformal(val message: String) : PostItemBase(-1)

data class PostItemButton(val message: String, val callback: () -> Unit) : PostItemBase(-2)

data class PostItemOffset(val data: Any): PostItemBase(-3)

data class PostItemEmptyFlag(val flag: Boolean) : PostItemBase(-4)

class PostAdapter(val ctx: Context, val activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_INFORMAL = -1
    private val TYPE_BUTTON = -2

    var postList: MutableList<PostItemBase> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun append(post: PostItemBase) {
        postList.add(post)
        notifyItemInserted(postList.size)
    }

    fun removeLast() {
        postList.removeAt(postList.size-1)
        notifyItemRemoved(postList.size)
    }

    override fun getItemViewType(position: Int): Int =
        postList[position].type_

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType >= 0) {
            return PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post_layout, parent, false))
        } else if (viewType == TYPE_INFORMAL){
            return InformalPostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post_informal, parent, false))
        } else if (viewType == TYPE_BUTTON) {
            return ButtonPostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post_action, parent, false))
        }
        return InformalPostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post_informal, parent, false))
    }

    override fun getItemCount(): Int = postList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val _type_ = postList[position].type_
        if (_type_ >= 0) {
            (holder as PostViewHolder).onBind(postList[position] as PostItem)
        } else if (_type_ == TYPE_INFORMAL) {
            (holder as InformalPostViewHolder).onBind((postList[position] as PostItemInformal).message)
        } else if (_type_ == TYPE_BUTTON) {
            (holder as ButtonPostViewHolder).onBind((postList[position] as PostItemButton).message, (postList[position] as PostItemButton).callback)
        }
    }

    inner class InformalPostViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val textView = root.text
        fun onBind(message: String) {
            textView.text = message
        }
    }

    inner class ButtonPostViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val view = root
        val label = root.label
        fun onBind(msg: String, callback: () -> Unit) {
            label.text = msg
            view.setOnClickListener {
                callback()
            }
        }
    }

    inner class PostViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val title = root.title
        private val loader = root.image_progress
        private val text = root.text
        private val time = root.time
        private val groupIcon = root.groupIcon
        private val brandIcon = root.brandIcon
        private val carousel = root.carousel
        private val view = root
        private val addon = root.btn_open_repost

        private fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
            val spannableString = SpannableString(this.text)
            for (link in links) {
                val clickableSpan = object : ClickableSpan() {

                    override fun updateDrawState(textPaint: TextPaint) {
                        textPaint.color = textPaint.linkColor
                        //textPaint.isUnderlineText = true
                    }

                    override fun onClick(view: View) {
                        Selection.setSelection((view as TextView).text as Spannable, 0)
                        view.invalidate()
                        link.second.onClick(view)
                    }
                }
                val startIndexOfLink = this.text.toString().indexOf(link.first)
                if (startIndexOfLink >= 0) {
                    spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

            }
            this.movementMethod = LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
            this.setText(spannableString, TextView.BufferType.SPANNABLE)
        }

        fun createPaletteAsync(bitmap: Bitmap, view: ImageView) {
            Palette.from(bitmap).generate { palette ->
                view.setBackgroundColor(palette!!.getDominantColor(activity.getColor(R.color.colorPrimary)))
            }
        }

        private fun replaceLinks(res: MatchResult): CharSequence {
            val t = res.value
            val name = t.substring(t.indexOf('|')+1..t.length-2)
            val link = t.substring(1..t.indexOf('|')-1)
            return name
        }

        fun onBind(post: PostItem) {
            view.setOnClickListener {
                val i = Intent(activity, PostViewerActivity::class.java)
                i.putExtra("post", post)
                ctx.startActivity(i)
            }
            val imageListener =
                ImageListener { position, imageView ->
                    imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                    // Тред подгрузки фона для картинок
                    thread(start = true) {
                        try {

                            val url = URL(post.media[position])
                            val bitmap: Bitmap
                            val file: File = Glide.with(activity)
                                .asFile()
                                .load(url)
                                .submit()
                                .get()
                            val path: String = file.path
                            bitmap = BitmapFactory.decodeFile(path)
                            kotlin.run { createPaletteAsync(bitmap, imageView); loader.visibility = View.INVISIBLE }
                        } catch (e:Exception) {
                            Log.d("IMAGE_BG", e.printStackTrace().toString())
                        }


                    }
                    // Загрузка самой картинки
                    try {
                        Glide.with(ctx)
                            .load(post.media[position])
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .into(imageView)
                    } catch (e:Exception) {

                    }

                }
            title.text = post.title
            text.setOnClickListener {
                val i = Intent(activity, PostViewerActivity::class.java)
                i.putExtra("post", post)
                ctx.startActivity(i)
            }
            if (post.addon == "NULL") {
                addon.visibility = View.GONE
                addon.isClickable = false
                addon.isEnabled = false
            } else {
                addon.setOnClickListener {
                    val openURL = Intent(Intent.ACTION_VIEW)
                    openURL.data = Uri.parse(post.addon)
                    ctx.startActivity(openURL)
                }
            }


            val sdf = java.text.SimpleDateFormat("HH:mm dd-MM-yyy")
            val date = java.util.Date(post.time*1000)

            // parse text to create links
            val links = mutableListOf<Pair<String, View.OnClickListener>>()
            val regex = Regex(pattern = "\\[+club+[\\d]+\\|+[\\w\\sа-яА-Я\\d!@#\$%^&*«»()_+=\\-\"№;:?\\/}{',.|<>]+\\]|\\[+id+[\\d]+\\|+[\\w\\sа-яА-Я\\d!@#\$%^&*()_+=\\-\"№;:?\\/}{',«».|<>]+\\]")
            val regex_links = regex.findAll(post.text)
            for (rl in regex_links) {
                val t = rl.value
                val name = t.substring(t.indexOf('|')+1..t.length-2)
                val link = t.substring(1..t.indexOf('|')-1)
                links.add(
                    Pair(name, View.OnClickListener {
                        val url = "https://vk.com/$link"
                        val openURL = Intent(Intent.ACTION_VIEW)
                        openURL.data = Uri.parse(url)
                        activity.startActivity(openURL)
                    })
                )
                Log.v("LINK: ", name + " -> " + link)
            }
            val formatted_string = regex.replace(post.text) {
                replaceLinks(it)
            }
            val textLength = 240
            val placableText: String
            if (formatted_string.length > textLength) {
                Log.v("WGFIYUGEF", post.text.length.toString())
                placableText = formatted_string.substring(0,textLength) + "...\n" + ctx.getString(R.string.show_more)
                links.add(
                    Pair(ctx.getString(R.string.show_more), View.OnClickListener {
                        val i = Intent(activity, PostViewerActivity::class.java)
                        i.putExtra("post", post)
                        ctx.startActivity(i)
                        }
                    )
                )
            } else {
                placableText = formatted_string
            }
            text.text = placableText
            text.makeLinks(*links.toTypedArray())

            // set post time
            time.text = sdf.format(date)

            // hide carousel when 0 photos
            if (post.photo_count == 0) {
                carousel.layoutParams.height = 0
                carousel.visibility = View.INVISIBLE
                loader.visibility = View.INVISIBLE
            }

            // carousel setup
            carousel.pageCount = post.media.size
            carousel.setImageListener(imageListener);

            // hide pagination when only 1 item
            if (post.photo_count == 1) {
                carousel.setIndicatorVisibility(View.INVISIBLE)
            }

            Glide.with(view).load(post.icon).into(groupIcon);
            when(post.type) {
                0 -> Glide.with(view).load(R.drawable.ic_vk_blue_logo).into(brandIcon)
                1 -> Glide.with(view).load(R.drawable.ic_facebook).into(brandIcon)
                else -> Glide.with(view).load(R.drawable.ic_twitter_blue_logo).into(brandIcon)
            }
        }

    }

}