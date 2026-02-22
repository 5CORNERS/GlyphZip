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
import androidx.core.view.isVisible
import android.content.res.ColorStateList

class SettingsActivity : AppCompatActivity() {

    private val group1 = mapOf("А" to "A", "В" to "B", "Е" to "E", "К" to "K", "М" to "M", "Н" to "H", "О" to "O", "Р" to "P", "С" to "C", "Т" to "T", "Х" to "X", "а" to "a", "е" to "e", "ё" to "e", "о" to "o", "р" to "p", "с" to "c", "у" to "y", "х" to "x")
    private val group2 = mapOf("т" to "m", "п" to "n", "и" to "u", "д" to "g")
    private val group3 = linkedMapOf( "б" to "6", "З" to "3", "У" to "Y", "Д" to "D", "к" to "k", "г" to "r", "т" to "t", "ш" to "w", "Ш" to "W", "ч" to "4", "ь" to "b")

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

        // Header
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

        // Limit field
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

        // Columns
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

        addSection(left, "Омоглифы", group1, true)
        addSection(left, "À la курсив", group2)
        right.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(1, 70) })
        addSection(right, "Агрессивно", group3)

        cols.addView(left)
        cols.addView(right)
        root.addView(cols)

        // Footer
        val footerText = TextView(this).apply {
            val textStr = "GlyphZip v1.1 © Le-francais.ru. Проверить обновление"
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

    private fun addSection(container: LinearLayout, title: String, map: Map<String, String>, expandable: Boolean = false) {
        val current = prefs.getStringSet("enabled_chars", group1.keys) ?: group1.keys
        val childCheckboxes = mutableListOf<androidx.appcompat.widget.AppCompatCheckBox>()

        // Group header
        val head = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 10, 0, 15)
            gravity = Gravity.CENTER_VERTICAL
        }

        val headCheckBox = androidx.appcompat.widget.AppCompatCheckBox(this@SettingsActivity).apply {
            buttonTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#67EA94"))
            updateCheckboxState(this, map.keys, current)
        }

        headCheckBox.setOnClickListener {
            val set = (prefs.getStringSet("enabled_chars", group1.keys) ?: group1.keys).toMutableSet()
            val allKeys = map.keys
            val enabledInGroupCount = set.intersect(allKeys).size

            if (enabledInGroupCount < allKeys.size) { // If indeterminate or all off, turn all on.
                set.addAll(allKeys)
            } else { // If all are on, turn all off.
                set.removeAll(allKeys)
            }

            prefs.edit().putStringSet("enabled_chars", set).apply()
            updateCheckboxState(headCheckBox, allKeys, set)
            childCheckboxes.forEach { checkbox ->
                val key = checkbox.tag as? String
                if (key != null) {
                    checkbox.isChecked = set.contains(key)
                }
            }
        }

        head.addView(headCheckBox)

        val titleTextView = TextView(this@SettingsActivity).apply {
            text = title
            setTextColor(Color.parseColor("#67EA94"))
            typeface = Typeface.DEFAULT_BOLD
            textSize = 17f
            setPadding(20, 0, 0, 0)
        }

        // Character grid with indentation
        val grid = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 0, 0, 0)
            isVisible = !expandable
        }

        if (expandable) {
            val chevron = ImageView(this).apply {
                setImageResource(R.drawable.outline_arrow_back)
                imageTintList = ColorStateList.valueOf(Color.parseColor("#67EA94"))
                rotation = 0f // Initially points left
                scaleX = 0.8f
                scaleY = 0.8f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginStart = 20 }
            }

            val expandableArea = LinearLayout(this@SettingsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(titleTextView)
                addView(chevron)
                setOnClickListener {
                    grid.isVisible = !grid.isVisible
                    chevron.rotation = if (grid.isVisible) 270f else 0f // 270 degrees for down
                }
            }
            head.addView(expandableArea)
        } else {
            head.addView(titleTextView)
        }

        container.addView(head)

        for (entry in map) {
            val checkbox = androidx.appcompat.widget.AppCompatCheckBox(this).apply {
                text = "${entry.key} → ${entry.value}"
                setTextColor(Color.WHITE)
                isChecked = current.contains(entry.key)
                buttonTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#67EA94"))
                textSize = 15f
                tag = entry.key
                setOnCheckedChangeListener { _, isChecked ->
                    val set = (prefs.getStringSet("enabled_chars", group1.keys) ?: group1.keys).toMutableSet()
                    if (isChecked) set.add(entry.key) else set.remove(entry.key)
                    prefs.edit().putStringSet("enabled_chars", set).apply()
                    updateCheckboxState(headCheckBox, map.keys, set)
                }
            }
            childCheckboxes.add(checkbox)
            grid.addView(checkbox)
        }
        container.addView(grid)
    }

    private fun updateCheckboxState(checkbox: androidx.appcompat.widget.AppCompatCheckBox, allKeys: Set<String>, enabledKeys: Set<String>) {
        val enabledInGroupCount = enabledKeys.intersect(allKeys).size
        when {
            enabledInGroupCount == 0 -> { // All disabled
                checkbox.isChecked = false
                checkbox.alpha = 1.0f
            }
            enabledInGroupCount == allKeys.size -> { // All enabled
                checkbox.isChecked = true
                checkbox.alpha = 1.0f
            }
            else -> { // Indeterminate
                checkbox.isChecked = true
                checkbox.alpha = 0.5f
            }
        }
    }
}
