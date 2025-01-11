package com.example.labproject.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.labproject.R
import com.example.labproject.model.ToDoItem

class ToDoAdapter (private val context: Context, val listener: TodoClickListener):
    RecyclerView.Adapter<ToDoAdapter.TodoViewHolder>() {
    private val todoList = ArrayList<ToDoItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoAdapter.TodoViewHolder {
        return TodoViewHolder(
            LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ToDoAdapter.TodoViewHolder, position: Int) {
        val item = todoList[position]
        holder.title.text = item.title
        holder.title.isSelected = true
        holder.note.text = item.note
        holder.date.text = item.date
        holder.date.isSelected = true
        holder.todo_layout.setOnClickListener {
            listener.onItemClicked(todoList[holder.adapterPosition])
        }
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    fun updateList(newList: List<ToDoItem>){
        todoList.clear()
        todoList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class TodoViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val todo_layout = itemView.findViewById<CardView>(R.id.card_layout)
        val title = itemView.findViewById<TextView>(R.id.tv_title)
        val note = itemView.findViewById<TextView>(R.id.tv_note)
        val date = itemView.findViewById<TextView>(R.id.tv_date)
    }

    interface TodoClickListener {
        fun onItemClicked(todo: ToDoItem)
    }

}