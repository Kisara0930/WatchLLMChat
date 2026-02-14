package com.kisara0930.watchllmchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ModelAdapter(
    private val models: MutableList<String>,
    private var selectedModel: String,
    private val onModelSelected: (String) -> Unit
) : RecyclerView.Adapter<ModelAdapter.ModelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_model, parent, false)
        return ModelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        val model = models[position]
        holder.modelNameTextView.text = model
        holder.radioButton.isChecked = model == selectedModel

        holder.itemView.setOnClickListener {
            val previousSelected = selectedModel
            selectedModel = model
            onModelSelected(model)
            notifyItemChanged(models.indexOf(previousSelected))
            notifyItemChanged(position)
        }

        holder.radioButton.setOnClickListener {
            val previousSelected = selectedModel
            selectedModel = model
            onModelSelected(model)
            notifyItemChanged(models.indexOf(previousSelected))
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = models.size

    fun addModel(model: String) {
        models.add(model)
        notifyItemInserted(models.size - 1)
    }

    class ModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radioButton: RadioButton = itemView.findViewById(R.id.radioButton)
        val modelNameTextView: TextView = itemView.findViewById(R.id.modelNameTextView)
    }
}