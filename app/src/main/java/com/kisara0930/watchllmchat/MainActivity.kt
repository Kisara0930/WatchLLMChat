package com.kisara0930.watchllmchat

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kisara0930.watchllmchat.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var messageAdapter: MessageAdapter
    private val viewModel: MainViewModel by viewModels()
    private lateinit var gestureDetector: GestureDetectorCompat

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageAdapter = MessageAdapter()
        binding.recyclerView.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        binding.sendButton.setOnClickListener {
            val text = binding.editText.text.toString()
            if (text.isNotEmpty()) {
                viewModel.sendMessage(text)
                binding.editText.text.clear()
            }
        }

        lifecycleScope.launch {
            viewModel.messages.collectLatest { messages ->
                messageAdapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    binding.placeholderTextView.visibility = View.GONE
                    binding.recyclerView.scrollToPosition(messages.size - 1)
                } else {
                    binding.placeholderTextView.visibility = View.VISIBLE
                }
            }
        }

        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                if (binding.inputLayout.visibility == View.VISIBLE) {
                    binding.inputLayout.visibility = View.GONE
                } else {
                    binding.inputLayout.visibility = View.VISIBLE
                }
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 != null) {
                    val diffX = e2.x - e1.x
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX < 0) { // Left swipe
                            val intent = Intent(this@MainActivity, ModelManagerActivity::class.java)
                            startActivity(intent)
                            return true
                        }
                    }
                }
                return false
            }
        })

        binding.recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(e)
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })

        binding.inputLayout.setOnTouchListener { _, _ -> true } // Consume touches on inputLayout to prevent hiding
    }
}