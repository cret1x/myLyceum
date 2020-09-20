package com.cretix

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Parcelable
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.type.DateTime
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import jp.wasabeef.blurry.Blurry
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_source_update.view.*
import kotlinx.android.synthetic.main.item_empty_list.view.*
import kotlinx.android.synthetic.main.item_post_informal.view.*
import kotlinx.android.synthetic.main.item_post_layout.view.*
import kotlinx.android.synthetic.main.item_post_layout.view.text
import java.io.IOException
import java.net.URL
import kotlin.concurrent.thread


abstract class PostItemBase(val type_: Int)

@Parcelize
data class PostItem(val pid: String, val title: String, val text: String, val time: Long, val type: Int, val icon: String, val media: List<String>, val photo_count: Int) : PostItemBase(type), Parcelable

data class PostItemInfom(val message: String) : PostItemBase(-1)

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
            (holder as InformalPostViewHolder).onBind((postList[position] as PostItemInfom).message)
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

        fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
            val spannableString = SpannableString(this.text)
            for (link in links) {
                val clickableSpan = object : ClickableSpan() {

                    override fun updateDrawState(textPaint: TextPaint) {
                        textPaint.color = textPaint.linkColor
                        textPaint.isUnderlineText = true
                    }

                    override fun onClick(view: View) {
                        Selection.setSelection((view as TextView).text as Spannable, 0)
                        view.invalidate()
                        link.second.onClick(view)
                    }
                }
                val startIndexOfLink = this.text.toString().indexOf(link.first)
                spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            this.movementMethod = LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
            this.setText(spannableString, TextView.BufferType.SPANNABLE)
        }

        fun createPaletteAsync(bitmap: Bitmap, view: ImageView) {
            Palette.from(bitmap).generate { palette ->
                view.setBackgroundColor(palette!!.getDominantColor(activity.getColor(R.color.colorPrimary)))
            }
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
                    /*
                    thread(start = true) {
                        val url = URL(post.media[position])
                        val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                        kotlin.run { createPaletteAsync(bitmap, imageView); loader.visibility = View.INVISIBLE }
                    }*/
                    Glide.with(ctx)
                        .load(post.media[position])
                        .into(imageView)
                }
            title.text = post.title
            val sdf = java.text.SimpleDateFormat("HH:mm dd-MM-yyy")
            val date = java.util.Date(post.time*1000)

            // wrap text when over 120 symbols
            time.text = sdf.format(date)
            if (post.text.length > 120) {
                text.text = post.text.substring(0,120) + "...\n" + ctx.getString(R.string.show_more)
                text.makeLinks(Pair(ctx.getString(R.string.show_more), View.OnClickListener {
                    val i = Intent(activity, PostViewerActivity::class.java)
                    i.putExtra("post", post)
                    ctx.startActivity(i)
                }))
            } else {
                text.text = post.text
            }

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