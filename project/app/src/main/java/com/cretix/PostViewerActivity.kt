package com.cretix

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.cretix.RoomComponents.PostRoom.VKPostEntity
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_post_viewer.*
import java.net.URL
import kotlin.concurrent.thread

class PostViewerActivity : AppCompatActivity() {
    private val roomDB = MyApplication.instance.getDatabase()

    fun createPaletteAsync(bitmap: Bitmap, view: ImageView) {
        Palette.from(bitmap).generate { palette ->
            view.setBackgroundColor(palette!!.getDominantColor(getColor(R.color.colorPrimary)))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_viewer)

        val post  = intent.getParcelableExtra<PostItem>("post")!!

        roomDB.vkPostDao().isFavourite(post.pid)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { res ->  if (res) add_favourite.text = getString(R.string.del_fav) else add_favourite.text = getString(R.string.add_to_fav) }
            .subscribe()

        val imageListener =
            ImageListener { position, imageView ->
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                thread(start = true) {
                    val url = URL(post.media[position])
                    val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    kotlin.run { createPaletteAsync(bitmap, imageView)}
                }
                Glide.with(this)
                    .load(post.media[position])
                    .into(imageView)
            }
        title_.text = post.title
        val sdf = java.text.SimpleDateFormat("HH:mm dd-MM-yyy")
        val date = java.util.Date(post.time*1000)
        time.text = sdf.format(date)
        text.text = post.text
        if (post.media.isEmpty()) {
            carousel.layoutParams.height = 0
        }
        carousel.pageCount = post.media.size
        if (post.media.size == 1) {
            carousel.setIndicatorVisibility(View.INVISIBLE)
        }
        open_source.setOnClickListener {
            val link = "https://vk.com/wall${post.pid}"
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse(link)
            startActivity(openURL)
        }
        add_favourite.setOnClickListener {
            val fav = (add_favourite.text == getString(R.string.add_to_fav))
            if (fav) {
                val post_ = VKPostEntity(
                    title = post.title,
                    id = post.pid,
                    text = post.text,
                    time = post.time,
                    icon = post.icon,
                    isFavourite = fav,
                    photos = post.media.joinToString(",")
                )
                thread(start = true) {
                    roomDB.vkPostDao().addFavourite(post_)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete { kotlin.run { add_favourite.text = getString(R.string.del_fav) }}
                        .subscribe()
                }

            } else {
                thread(start  = true) {
                    roomDB.vkPostDao().removeFavourite(post.pid)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete { kotlin.run { add_favourite.text = getString(R.string.add_to_fav) }}
                        .subscribe()
                }

            }


        }
        carousel.setImageListener(imageListener);
        Glide.with(this).load(post.icon).into(groupIcon);
        when(post.type) {
            0 -> Glide.with(this).load(R.drawable.ic_vk_blue_logo).into(brandIcon)
            1 -> Glide.with(this).load(R.drawable.ic_facebook).into(brandIcon)
            else -> Glide.with(this).load(R.drawable.ic_twitter_blue_logo).into(brandIcon)
        }
    }
}
