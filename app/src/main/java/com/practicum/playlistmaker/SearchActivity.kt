package com.practicum.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.appbar.MaterialToolbar

class SearchActivity : AppCompatActivity() {

    private var countValue: String = TEXT_DEF

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

//        if (savedInstanceState != null) {
//            countValue = savedInstanceState.getString(TEXT_SEARCH, TEXT_DEF)
//        }

        val backButton = findViewById<MaterialToolbar>(R.id.toolbarSearch)
        val clearButton = findViewById<ImageView>(R.id.clearIcon)
        val inputEditText = findViewById<EditText>(R.id.inputEditText)

        inputEditText.addTextChangedListener {
            countValue = it.toString()
            clearButton.visibility = if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        clearButton.setOnClickListener {
            inputEditText.setText("")
            clearButton.visibility = View.GONE
        }

        backButton.setNavigationOnClickListener {
            finish()
        }

//        val simpleTextWatcher = object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                // empty
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                // empty
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                // empty
//            }
//        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TEXT_SEARCH, countValue)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Вторым параметром мы передаём значение по умолчанию
        countValue = savedInstanceState.getString(TEXT_SEARCH, TEXT_DEF)
    }

    companion object {
        const val TEXT_SEARCH = "TEXT_SEARCH"
        const val TEXT_DEF = ""
    }
}