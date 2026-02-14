package com.kisara0930.watchllmchat

import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kisara0930.watchllmchat.databinding.ActivityModelManagerBinding

class ModelManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModelManagerBinding
    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var modelAdapter: ModelAdapter
    private val models = mutableListOf<String>()
    private var selectedModel: String = Config.MODEL_NAME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModelManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadModels()
        loadWebSearchSwitchState()
        loadBriefReplySwitchState()

        modelAdapter = ModelAdapter(models, selectedModel) { model ->
            selectedModel = model
            saveSelectedModel()
        }

        binding.modelsRecyclerView.apply {
            adapter = modelAdapter
            layoutManager = LinearLayoutManager(this@ModelManagerActivity)
        }

        binding.addModelButton.setOnClickListener {
            val newModel = binding.newModelEditText.text.toString().trim()
            if (newModel.isNotEmpty() && !models.contains(newModel)) {
                modelAdapter.addModel(newModel)
                binding.newModelEditText.text.clear()
                saveModels()
            }
        }

        binding.webSearchSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveWebSearchSwitchState(isChecked)
        }

        binding.briefReplySwitch.setOnCheckedChangeListener { _, isChecked ->
            saveBriefReplySwitchState(isChecked)
        }

        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 != null && e2.x > e1.x) { // Right swipe
                    finish()
                    return true
                }
                return false
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            gestureDetector.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun loadModels() {
        val sharedPreferences = getSharedPreferences("ModelPrefs", Context.MODE_PRIVATE)
        val savedModels = sharedPreferences.getStringSet("models", setOf()) ?: setOf()
        selectedModel = sharedPreferences.getString("selectedModel", Config.MODEL_NAME)!!

        models.clear()
        val modelSet = mutableSetOf<String>()
        modelSet.addAll(savedModels)
        modelSet.add(Config.MODEL_NAME) // Always ensure default model is in the list
        models.addAll(modelSet)

        if (!models.contains(selectedModel)) {
            selectedModel = Config.MODEL_NAME
            saveSelectedModel()
        }
    }

    private fun saveModels() {
        val sharedPreferences = getSharedPreferences("ModelPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("models", models.toSet())
        editor.apply()
    }

    private fun saveSelectedModel() {
        val sharedPreferences = getSharedPreferences("ModelPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("selectedModel", selectedModel)
        editor.apply()
    }

    private fun loadWebSearchSwitchState() {
        val sharedPreferences = getSharedPreferences("ModelPrefs", Context.MODE_PRIVATE)
        val isWebSearchEnabled = sharedPreferences.getBoolean("webSearch", false)
        binding.webSearchSwitch.isChecked = isWebSearchEnabled
    }

    private fun saveWebSearchSwitchState(isEnabled: Boolean) {
        val sharedPreferences = getSharedPreferences("ModelPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("webSearch", isEnabled)
        editor.apply()
    }

    private fun loadBriefReplySwitchState() {
        val sharedPreferences = getSharedPreferences("ModelPrefs", Context.MODE_PRIVATE)
        val isBriefReplyEnabled = sharedPreferences.getBoolean("briefReply", false)
        binding.briefReplySwitch.isChecked = isBriefReplyEnabled
    }

    private fun saveBriefReplySwitchState(isEnabled: Boolean) {
        val sharedPreferences = getSharedPreferences("ModelPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("briefReply", isEnabled)
        editor.apply()
    }
}