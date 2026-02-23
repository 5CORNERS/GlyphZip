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
    private val checkboxRegistry = mutableMapOf<String, CheckBox>()

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

        addSection(left, "Омоглифы", "enabled_group1", group1, true)
        addSection(left, "À la курсив", "enabled_group2", group2)
        right.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(1, 70) })
        addSection(right, "Агрессивно", "enabled_group3", group3)

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

    private fun addSection(container: LinearLayout, title: String, prefKey: String, map: Map<String, String>, expandable: Boolean = false) {
        val default = if(prefKey == "enabled_group1") map.keys else emptySet()
        val current = prefs.getStringSet(prefKey, default) ?: default

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
        checkboxRegistry[prefKey] = headCheckBox

        headCheckBox.setOnClickListener {
            val editor = prefs.edit()
            val currentSet = (prefs.getStringSet(prefKey, default) ?: default).toMutableSet()
            val allKeys = map.keys
            val enabledInGroupCount = currentSet.intersect(allKeys).size
            
            val shouldTurnOff = enabledInGroupCount > 0
            val newSet = if (shouldTurnOff) mutableSetOf() else allKeys.toMutableSet()

            if (prefKey == "enabled_group3" && !shouldTurnOff) {
                if (prefs.getStringSet("enabled_group2", emptySet())?.contains("т") == true) {
                    newSet.remove("т")
                }
            }
            
            editor.putStringSet(prefKey, newSet).apply()
            updateCheckboxState(headCheckBox, allKeys, newSet)
            updateChildCheckboxes(prefKey, newSet)

            if (!shouldTurnOff && newSet.contains("т")) {
                handleExclusion(prefKey, "т", editor)
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

        val grid = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 0, 0, 0)
            isVisible = !expandable
        }

        if (expandable) {
            val chevron = ImageView(this).apply {
                setImageResource(R.drawable.outline_arrow_back)
                imageTintList = ColorStateList.valueOf(Color.parseColor("#67EA94"))
                rotation = 0f
                scaleX = 0.8f
                scaleY = 0.8f
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginStart = 20 }
            }

            val expandableArea = LinearLayout(this@SettingsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(titleTextView)
                addView(chevron)
                setOnClickListener { 
                    grid.isVisible = !grid.isVisible
                    chevron.rotation = if (grid.isVisible) 270f else 0f
                }
            }
            head.addView(expandableArea)
        } else {
            head.addView(titleTextView)
        }

        container.addView(head)

        for (entry in map) {
            val checkbox = androidx.appcompat.widget.AppCompatCheckBox(this@SettingsActivity).apply {
                text = "${entry.key} → ${entry.value}"
                setTextColor(Color.WHITE)
                isChecked = current.contains(entry.key)
                buttonTintList = ColorStateList.valueOf(Color.parseColor("#67EA94"))
                textSize = 15f
                tag = "$prefKey-${entry.key}"
                
                setOnCheckedChangeListener { _, isChecked ->
                    val editor = prefs.edit()
                    val currentSet = (prefs.getStringSet(prefKey, default) ?: default).toMutableSet()
                    
                    if (isChecked) {
                        currentSet.add(entry.key)
                    } else {
                        currentSet.remove(entry.key)
                    }

                    editor.putStringSet(prefKey, currentSet).apply()
                    updateCheckboxState(headCheckBox, map.keys, currentSet)
                    
                    if(isChecked) {
                        handleExclusion(prefKey, entry.key, editor)
                    }
                }
            }
            checkboxRegistry["$prefKey-${entry.key}"] = checkbox
            grid.addView(checkbox)
        }
        container.addView(grid)
    }

    private fun handleExclusion(changedGroupKey: String, changedChar: String, editor: SharedPreferences.Editor) {
        if (changedChar != "т") return

        val (otherGroupKey, otherGroupMap) = if (changedGroupKey == "enabled_group2") {
            Pair("enabled_group3", group3)
        } else {
            Pair("enabled_group2", group2)
        }

        val otherGroupSet = (prefs.getStringSet(otherGroupKey, emptySet()) ?: emptySet()).toMutableSet()
        if (otherGroupSet.remove("т")) {
            editor.putStringSet(otherGroupKey, otherGroupSet).apply()
            checkboxRegistry["$otherGroupKey-т"]?.isChecked = false
            (checkboxRegistry[otherGroupKey] as? CheckBox)?.let{
                 updateCheckboxState(it, otherGroupMap.keys, otherGroupSet)
            }
        }
    }

    private fun updateChildCheckboxes(prefKey: String, newSet: Set<String>) {
        val map = when(prefKey) {
            "enabled_group1" -> group1
            "enabled_group2" -> group2
            else -> group3
        }
        for (key in map.keys) {
            checkboxRegistry["$prefKey-$key"]?.isChecked = newSet.contains(key)
        }
    }

    private fun updateCheckboxState(checkbox: CheckBox, allKeys: Set<String>, enabledKeys: Set<String>) {
        val enabledCount = enabledKeys.intersect(allKeys).size
        when {
            enabledCount == 0 -> { // All disabled
                checkbox.isChecked = false
                checkbox.alpha = 1.0f
            }
            enabledCount == allKeys.size -> { // All enabled
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