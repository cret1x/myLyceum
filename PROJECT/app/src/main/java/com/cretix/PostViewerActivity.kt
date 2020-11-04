package com.cretix

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cretix.RoomComponents.PostRoom.VKPostEntity
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_post_viewer.*
import java.io.File
import java.lang.Exception
import java.net.URL
import kotlin.concurrent.thread

class PostViewerActivity : AppCompatActivity() {
    private val roomDB = MyApplication.instance.getDatabase()

    fun createPaletteAsync(bitmap: Bitmap, view: ImageView) {
        Palette.from(bitmap).generate { palette ->
            view.setBackgroundColor(palette!!.getDominantColor(getColor(R.color.colorPrimary)))
        }
    }

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
        this.movementMethod = LinkMovementMethod.getInstance()
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    private fun replaceLinks(res: MatchResult): CharSequence {
        val t = res.value
        val name = t.substring(t.indexOf('|')+1..t.length-2)
        return name
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

                // Тред подгрузки фона для картинок
                thread(start = true) {
                    try {
                        val url = URL(post.media[position])
                        val bitmap: Bitmap
                        val file: File = Glide.with(this)
                            .asFile()
                            .load(url)
                            .submit()
                            .get()
                        val path: String = file.path
                        bitmap = BitmapFactory.decodeFile(path)
                        kotlin.run { createPaletteAsync(bitmap, imageView); loader.visibility = View.INVISIBLE }
                    } catch (e: Exception) {
                        Log.d("IMAGE_BG", e.printStackTrace().toString())
                    }


                }

                // Загрузка самой картинки
                try {
                    Glide.with(this)
                        .load(post.media[position])
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(imageView)
                } catch (e: Exception) {
                    Log.d("IMAGE", e.printStackTrace().toString())
                }
            }

        // Заполняю базовые поля
        title_.text = post.title
        val sdf = java.text.SimpleDateFormat("HH:mm dd-MM-yyy")
        val date = java.util.Date(post.time*1000)
        time.text = sdf.format(date)

        // Парсинг текста и добавление ссылок куда надо
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
                    startActivity(openURL)
                })
            )
            Log.v("LINK: ", name + " -> " + link)
        }
        val formatted_string = regex.replace(post.text) {
            replaceLinks(it)
        }
        text.text = formatted_string
        text.makeLinks(*links.toTypedArray())


        if (post.addon == "NULL") {
            addon.visibility = View.GONE
            addon.isClickable = false
            addon.isEnabled = false
        } else {
            addon.setOnClickListener {
                Log.d("URL", post.addon)
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data = Uri.parse(post.addon)
                startActivity(openURL)
            }
        }

        // Если нет изображений то убираем карусель
        if (post.media.isEmpty()) {
            carousel.layoutParams.height = 0
            carousel.visibility = View.INVISIBLE
            loader.visibility = View.INVISIBLE
        }

        // Если всего одна картинка убераем индикатор
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

        // Хрень с добавлением в избранное
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
                    photos = post.media.joinToString(","),
                    addons = post.addon
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
        //  Грузица картинка в фото группы
        carousel.setImageListener(imageListener);
        Glide.with(this).load(post.icon).into(groupIcon);
        when(post.type) {
            0 -> Glide.with(this).load(R.drawable.ic_vk_blue_logo).into(brandIcon)
            1 -> Glide.with(this).load(R.drawable.ic_facebook).into(brandIcon)
            else -> Glide.with(this).load(R.drawable.ic_twitter_blue_logo).into(brandIcon)
        }
    }
}
