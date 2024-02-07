package com.sc.lesa.mediashar

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.sc.lesa.mediashar.databinding.TutorialActivityBinding


class TutorialActivity : AppCompatActivity() {
    private lateinit var binding: TutorialActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.tutorial_activity)
        binding.re.setOnClickListener {
            finish()
        }
        val actionBar = supportActionBar
        actionBar?.let {
            it.hide()
        }

        var fragment1 = OneFragment()
        var fragment2 = TwoFragment()
        var fragment3 = ThreeFragment()

        var fragments = ArrayList<Fragment>()
        fragments.add(fragment1)
        fragments.add(fragment2)
        fragments.add(fragment3)

        var adapter = PageAdapter(this, fragments)
        binding.viewPager2.adapter = adapter
        binding.dotsIndicator.setViewPager2(binding.viewPager2)
    }
}
