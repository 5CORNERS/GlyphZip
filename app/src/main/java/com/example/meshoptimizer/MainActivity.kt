package com.example.meshoptimizer

import android.app.Dialog
import android.content.*
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import java.nio.charset.Charset
import android.content.ClipboardManager

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var prefs: SharedPreferences
    private lateinit var inputArea: EditText
    private lateinit var outputArea: TextView
    private lateinit var inputCounter: TextView
    private lateinit var outputCounter: TextView
    private lateinit var adviceContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        prefs = getSharedPreferences("GlyphZipPrefs", MODE_PRIVATE)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.parseColor("#121212"))
        }

        // --- Header ---
        val header = RelativeLayout(this).apply{

            val leftSection = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                id = View.generateViewId()
            }

            val titleContainer = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            titleContainer.addView(TextView(this@MainActivity).apply {
                text = "GlyphZip"
                textSize = 28f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
            })

            titleContainer.addView(ImageView(this@MainActivity).apply {
                setImageResource(R.drawable.ic_logo)
                layoutParams = LinearLayout.LayoutParams(90, 90).apply{
                    leftMargin = 30
                }
            })

            leftSection.addView(titleContainer)

            val linkContainer = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 0, 0, 0)
            }

            linkContainer.addView(TextView(this@MainActivity).apply {
                text = getString(R.string.from_creators_of)
                setTextColor(Color.WHITE)
                textSize = 13f
            })

            linkContainer.addView(TextView(this@MainActivity).apply {
                text = "Le-francais.ru"
                setTextColor(Color.parseColor("#42A5F5"))
                paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                textSize = 13f
                setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://le-francais.ru"))) }
            })

            leftSection.addView(linkContainer)

            addView(leftSection)

            val settingsBtn = ImageButton(this@MainActivity).apply {
                setImageResource(R.drawable.ic_settings_gear_24)
                setBackgroundColor(Color.TRANSPARENT)
                scaleType = ImageView.ScaleType.FIT_CENTER
                setPadding(15, -10, 15, 15)
                setOnClickListener { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }
            }

            addView(settingsBtn, RelativeLayout.LayoutParams(130, 130).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.ALIGN_TOP, leftSection.id)
                topMargin = 0
            })
        }
        root.addView(header)

        // --- Paste/Clear buttons ---
        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 40, 0, 20)
        }

        fun createModernBtn(txt: String, iconRes: Int, weight: Float, bgColor: String, strokeColor: String, onClick: () -> Unit): FrameLayout {
            val container = FrameLayout(this).apply {
                background = GradientDrawable().apply {
                    setColor(Color.parseColor(bgColor))
                    cornerRadius = 20f
                    setStroke(2, Color.parseColor(strokeColor))
                }
                layoutParams = LinearLayout.LayoutParams(0, 110, weight).apply {
                    marginEnd = if (weight > 0) 20 else 0
                }
                setOnClickListener { onClick() }
            }

            val content = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(-2, -2, Gravity.CENTER)
            }

            content.addView(ImageView(this).apply { setImageResource(iconRes) })
            content.addView(TextView(this).apply {
                text = txt
                isAllCaps = false
                setTextColor(Color.WHITE)
                textSize = 16f
                setPadding(12, 0, 0, 0)
            })
            container.addView(content)
            return container
        }

        btnRow.addView(createModernBtn(getString(R.string.paste_button), R.drawable.ic_content_paste_24, 1.3f, "#128293", "#444444") {
            val cb = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            inputArea.setText(cb.primaryClip?.getItemAt(0)?.text ?: "")
            updateUI()
        })
        btnRow.addView(createModernBtn(getString(R.string.clear_button), R.drawable.ic_clear_all_24, 0.7f, "#3A3A3A", "#555555") {
            inputArea.setText("")
            updateUI()
        })
        root.addView(btnRow)

        // --- Input/Output fields ---
        val inputBgDefault = GradientDrawable().apply {
            setColor(Color.parseColor("#1A1A1A"))
            setStroke(2, Color.parseColor("#333333"))
            cornerRadius = 25f
        }
        val inputBgFocused = GradientDrawable().apply {
            setColor(Color.parseColor("#1A1A1A"))
            setStroke(4, Color.parseColor("#80D8FF")) // Bright and thick stroke
            cornerRadius = 25f
        }
        val outputBg = GradientDrawable().apply {
            setColor(Color.parseColor("#161616")) // Slightly different background, no stroke
            cornerRadius = 25f
        }


        inputArea = EditText(this).apply {
            hint = getString(R.string.input_area_hint)
            setHintTextColor(Color.DKGRAY)
            setTextColor(Color.WHITE)
            background = inputBgDefault
            setPadding(35, 35, 35, 35)
            gravity = Gravity.TOP
            minLines = 5
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setOnFocusChangeListener { _, hasFocus ->
                background = if (hasFocus) inputBgFocused else inputBgDefault
            }
        }
        root.addView(inputArea)

        inputCounter = TextView(this).apply {
            gravity = Gravity.RIGHT
            setPadding(0, 10, 20, 30)
            setTextColor(Color.GRAY)
        }
        root.addView(inputCounter)

        outputArea = TextView(this).apply {
            background = outputBg
            setPadding(35, 35, 35, 35)
            minLines = 5
            textSize = 17f
            setTextColor(Color.WHITE)
        }
        root.addView(outputArea)

        outputCounter = TextView(this).apply {
            gravity = Gravity.RIGHT
            setPadding(0, 10, 20, 30)
        }
        root.addView(outputCounter)

        adviceContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(adviceContainer)

        // --- Copy ---
        val copyBtnContainer = FrameLayout(this).apply {
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#67EA94"))
                cornerRadius = 25f
            }

            layoutParams = LinearLayout.LayoutParams(-1, 120).apply { topMargin = 20 }
            setOnClickListener {
                val cb = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                cb.setPrimaryClip(ClipData.newPlainText("zip", outputArea.text))
                Toast.makeText(this@MainActivity, getString(R.string.copied_toast), Toast.LENGTH_SHORT).show()
            }
        }

        val copyBtnContent = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(-2, -2, Gravity.CENTER)
        }

        copyBtnContent.addView(ImageView(this).apply {
            setImageResource(R.drawable.ic_content_copy_24)
            imageTintList = ColorStateList.valueOf(Color.BLACK)
        })
        copyBtnContent.addView(TextView(this).apply {
            text = getString(R.string.copy_button)
            isAllCaps = false
            textSize = 18f
            setTextColor(Color.BLACK)
            setPadding(20, 0, 0, 0)
        })
        copyBtnContainer.addView(copyBtnContent)
        root.addView(copyBtnContainer)
        
        inputArea.setText(viewModel.inputText)

        inputArea.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.inputText = s.toString()
                updateUI()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        setContentView(ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#121212"))
            fitsSystemWindows = true
            addView(root)
        })
        showWelcomeDialog()
    }

    private fun showWelcomeDialog() {
        val version = "1.1"
        val doNotShowAgainPref = "show_welcome_dialog_$version"

        if (prefs.getBoolean(doNotShowAgainPref, false)) {
            return
        }

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_welcome)

        val message = dialog.findViewById<TextView>(R.id.dialog_message)
        val checkBox = dialog.findViewById<AppCompatCheckBox>(R.id.dialog_checkbox)
        val closeButton = dialog.findViewById<Button>(R.id.dialog_close_button)

        val htmlText = """
            <b>Привет! Это GlyphZip $version.</b><br><br>
            Буквы кириллицы в Unicode занимают два байта, а латиницы — всего один. В сетях с жестким лимитом (Meshtastic, MeshCore, SMS) это вдвое сокращает длину вашего сообщения.<br><br>
            GlyphZip помогает сократить этот разрыв, и вот как:<br><br>
            <b>Омоглифы.</b> Мы заменяем русские буквы на идентичные латинские (например, «о», «а», «с»). Внешний вид букв не меняется, а размер текста в байтах сокращается.<br><br>
            <b>Визуальные замены.</b> Для максимального сжатия приложение может использовать похожие символы или знаки из других наборов. Если текст не укладывается в лимиты, приложение предложит включить более агрессивную замену букв.<br><br>
            Экономьте место, передавайте больше! :)<br><br>
            Команда <b>Le-francais.ru</b>
        """.trimIndent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            message.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            message.text = Html.fromHtml(htmlText)
        }

        closeButton.setOnClickListener {
            if (checkBox.isChecked) {
                prefs.edit().putBoolean(doNotShowAgainPref, true).apply()
            }
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#2A2A2A")))
        dialog.show()
    }

    override fun onResume() { super.onResume(); updateUI() }

    private fun updateUI() {
        val input = inputArea.text.toString()
        val limit = prefs.getInt("byte_limit", 200)
        val g1 = prefs.getStringSet("enabled_group1", Replacements.group1.keys) ?: Replacements.group1.keys
        val g2 = prefs.getStringSet("enabled_group2", emptySet()) ?: emptySet()
        val g3 = prefs.getStringSet("enabled_group3", emptySet()) ?: emptySet()

        Replacements.group1 + Replacements.group2 + Replacements.group3

        val inputBytes = input.toByteArray(Charset.forName("UTF-8")).size
        val spannable = SpannableStringBuilder()
        var outBytes = 0

        for (char in input) {
            val s = char.toString()
            val repl = when {
                g2.contains(s) -> Replacements.group2[s]
                g3.contains(s) -> Replacements.group3[s]
                g1.contains(s) -> Replacements.group1[s]
                else -> null
            }
            val target = repl ?: s
            val bSize = target.toByteArray(Charset.forName("UTF-8")).size
            val start = spannable.length
            spannable.append(target)
            outBytes += bSize

            if (outBytes > limit) {
                spannable.setSpan(ForegroundColorSpan(Color.RED), start, spannable.length, 33)
            } else if (repl != null) {
                spannable.setSpan(ForegroundColorSpan(Color.parseColor("#81C784")), start, spannable.length, 33)
            }
        }

        outputArea.text = spannable
        inputCounter.text = "$inputBytes ${getPluralBytes(inputBytes)}"
        outputCounter.text = "$outBytes / $limit ${getPluralBytes(outBytes)}"
        outputCounter.setTextColor(if (outBytes > limit) Color.RED else Color.GRAY)

        adviceContainer.removeAllViews()
        if (outBytes > limit) {
            val enabledChars = g1 + g2 + g3
            val g2Missing = Replacements.group2.keys.filter { !enabledChars.contains(it) }
            if (g2Missing.isNotEmpty()) {
                val gain = g2Missing.sumOf { char -> input.count { it.toString() == char } }
                if (gain > 0) createAdviceBtn(getString(R.string.enable_cursive_advice), gain, "enabled_group2", g2Missing.toSet())
            } else {
                // Order: б, З, У, к, г, ч, т, ш, Ш, ь thanks to LinkedMap
                for (char in Replacements.group3.keys) {
                    if (!enabledChars.contains(char)) {
                        val gain = input.count { it.toString() == char }
                        if (gain > 0) {
                            createAdviceBtn(getString(R.string.replace_advice, char.lowercase(), Replacements.group3[char]), gain, "enabled_group3", setOf(char))
                            break
                        }
                    }
                }
            }
        }
    }

    private fun createAdviceBtn(label: String, gain: Int, prefKey: String, chars: Set<String>) {
        adviceContainer.addView(Button(this).apply {
            text = "$label (+ $gain ${getPluralBytes(gain)})"
            isAllCaps = false
            textSize = 16f
            setTextColor(Color.parseColor("#FF8A80"))
            background = GradientDrawable().apply {
                setColor(Color.TRANSPARENT)
                setStroke(3, Color.parseColor("#FF8A80"))
                cornerRadius = 20f
            }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = 10; bottomMargin = 10 }
            setOnClickListener {
                val editor = prefs.edit()
                val current = (prefs.getStringSet(prefKey, emptySet()) ?: emptySet()).toMutableSet()
                current.addAll(chars)
                handleExclusion(prefKey, editor, current)
                editor.putStringSet(prefKey, current).apply()
                updateUI()
            }
        })
    }
    private fun handleExclusion(currentGroupKey: String, editor: SharedPreferences.Editor, currentGroupSet: MutableSet<String>) {
        val isCursiveT = currentGroupKey == "enabled_group2" && currentGroupSet.contains("т")
        val isAggressiveT = currentGroupKey == "enabled_group3" && currentGroupSet.contains("т")

        if (isCursiveT) {
            val aggressiveSet = (prefs.getStringSet("enabled_group3", emptySet()) ?: emptySet()).toMutableSet()
            if (aggressiveSet.remove("т")) {
                editor.putStringSet("enabled_group3", aggressiveSet)
            }
        } else if (isAggressiveT) {
            val cursiveSet = (prefs.getStringSet("enabled_group2", emptySet()) ?: emptySet()).toMutableSet()
            if (cursiveSet.remove("т")) {
                editor.putStringSet("enabled_group2", cursiveSet)
            }
        }
    }

    private fun getPluralBytes(number: Int): String {
        val lastTwoDigits = number % 100
        return when {
            lastTwoDigits in 11..19 -> getString(R.string.bytes_plural)
            else -> {
                when (number % 10) {
                    1 -> getString(R.string.bytes_singular)
                    in 2..4 -> getString(R.string.bytes_paucal)
                    else -> getString(R.string.bytes_plural)
                }
            }
        }
    }
}