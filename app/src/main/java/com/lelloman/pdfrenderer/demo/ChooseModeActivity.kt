package com.lelloman.pdfrenderer.demo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class ChooseModeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_mode)
    }

    fun openPaged(view: View) {
        startActivity(Intent(this, PagedActivity::class.java))
    }

    fun openScrolled(view: View) {
        startActivity(Intent(this, ScrolledActivity::class.java))
    }
}