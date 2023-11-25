package com.myapp.videocall.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.myapp.videocall.R
import com.myapp.videocall.databinding.ActivityCoinShopBinding

class CoinShopActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCoinShopBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    binding = DataBindingUtil.setContentView(this,R.layout.activity_coin_shop)
    //       setContentView(R.layout.activity_coin_shop)


    }
}