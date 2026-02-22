package com.example.meshoptimizer

import android.content.*
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private val group1 = mapOf("А" to "A", "В" to "B", "Е" to "E", "К" to "K", "М" to "M", "Н" to "H", "О" to "O", "Р" to "P", "С" to "C", "Т" to "T", "Х" to "X", "а" to "a", "е" to "e", "ё" to "e", "о" to "o", "р" to "p", "с" to "c", "у" to "y", "х" to "x")
    private val group2 = mapOf("т" to "m", "п" to "n", "и" to "u")
    private val group3 = linkedMapOf("б" to "6", "З" to "3", "У" to "Y", "к" to "k", "г" to "r", "ч" to "4")

    private lateinit var editLimit: EditText
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        prefs = getSharedPreferences("GlyphZipPrefs", Context.MODE_PRIVATE)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            fitsSystemWindows = true
        }

        // Заголовок
        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 50)
            val back = ImageButton(this@SettingsActivity).apply {
                setImageResource(R.drawable.ic_back)
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener { finish() }
            }
            addView(back)
            addView(TextView(this@SettingsActivity).apply {
                text = "Настройки"
                textSize = 24f
                setTextColor(Color.WHITE)
                setPadding(30, 0, 0, 0)
            })
        }
        root.addView(header)

        // Поле лимита
        val limitBox = LinearLayout(this).apply {
            setPadding(40, 30, 40, 30)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1E1E1E"))
                cornerRadius = 20f
            }
            addView(TextView(this@SettingsActivity).apply { text = "Лимит: "; setTextColor(Color.WHITE); textSize = 16f })
            editLimit = EditText(this@SettingsActivity).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                setText(prefs.getInt("byte_limit", 200).toString())
                setTextColor(Color.parseColor("#67EA94"))
                background = null
                textSize = 16f
            }
            addView(editLimit)
            addView(TextView(this@SettingsActivity).apply { text = " байтов (символов ASCII)"; setTextColor(Color.GRAY) })
        }
        root.addView(limitBox)

        // Колонки
        val cols = LinearLayout(this).apply {
            setPadding(0, 50, 0, 0)
            weightSum = 2f
        }

        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -2, 1.1f)
        }
        val right = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -2, 0.9f)
        }

        addSection(left, "Омоглифы", group1)
        addSection(right, "À la курсив", group2)
        right.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(1, 70) })
        addSection(right, "Агрессивно", group3)

        cols.addView(left)
        cols.addView(right)
        root.addView(cols)

        // Подвал
        val footerText = TextView(this).apply {
            val textStr = "GlyphZip v1.0 © Le-francais.ru. Проверить обновление"
            val spannable = SpannableString(textStr)
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://le-francais.ru/apps/GlyphZip")))
                }
            }
            val startIndex = textStr.indexOf("Проверить обновление")
            spannable.setSpan(clickableSpan, startIndex, textStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(UnderlineSpan(), startIndex, textStr.length, 0)

            text = spannable
            movementMethod = android.text.method.LinkMovementMethod.getInstance()
            setLinkTextColor(Color.parseColor("#42A5F5"))
            setTextColor(Color.GRAY)
            textSize = 14f
        }

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
            setPadding(20, 60, 0, 0)
            addView(footerText)
        }
        root.addView(footer)


        setContentView(ScrollView(this).apply { 
            setBackgroundColor(Color.parseColor("#121212"))
            addView(root) 
        })
    }

    override fun onPause() {
        super.onPause()
        val limit = editLimit.text.toString().toIntOrNull() ?: 200
        prefs.edit().putInt("byte_limit", limit).apply()
    }

    private fun addSection(container: LinearLayout, title: String, map: Map<String, String>) {
        val current = prefs.getStringSet("enabled_chars", group1.keys) ?: group1.keys

        // Заголовок группы
        val head = LinearLayout(this).apply {
            setPadding(0, 10, 0, 15)
        }
        
        val headCheckBox = androidx.appcompat.widget.AppCompatCheckBox(this@SettingsActivity).apply {
            buttonTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#67EA94"))
            isChecked = map.keys.all { current.contains(it) }
        }

        headCheckBox.setOnClickListener {
            val isCh = (it as CheckBox).isChecked
            val set = (prefs.getStringSet("enabled_chars", group1.keys) ?: group1.keys).toMutableSet()
            if (isCh) set.addAll(map.keys) else set.removeAll(map.keys)
            prefs.edit().putStringSet("enabled_chars", set).apply()
            recreate()
        }
        
        head.addView(headCheckBox)
        head.addView(TextView(this@SettingsActivity).apply {
            text = title
            setTextColor(Color.parseColor("#67EA94"))
            typeface = Typeface.DEFAULT_BOLD
            textSize = 17f
        })
        container.addView(head)

        // Сетка букв с отступом
        val grid = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 0, 0, 0)
        }
        for (entry in map) {
            grid.addView(androidx.appcompat.widget.AppCompatCheckBox(this).apply {
                text = "${entry.key} → ${entry.value}"
                setTextColor(Color.WHITE)
                isChecked = current.contains(entry.key)
                buttonTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#67EA94"))
                textSize = 15f
                setOnCheckedChangeListener { _, isChecked ->
                    val set = (prefs.getStringSet("enabled_chars", group1.keys) ?: group1.keys).toMutableSet()
                    if (isChecked) set.add(entry.key) else set.remove(entry.key)
                    prefs.edit().putStringSet("enabled_chars", set).apply()
                    headCheckBox.isChecked = map.keys.all{ set.contains(it) }
                }
            })
        }
        container.addView(grid)
    }
}
