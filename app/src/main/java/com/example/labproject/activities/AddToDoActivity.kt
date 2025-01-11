package com.example.labproject.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.labproject.databinding.ActivityAddToDoBinding
import com.example.labproject.model.ToDoItem
import java.text.SimpleDateFormat
import java.util.Date

class AddToDoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddToDoBinding
    private lateinit var todo: ToDoItem
    private lateinit var oldTodo: ToDoItem
    var isUpdate = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddToDoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            oldTodo = intent.getSerializableExtra("current_todo") as ToDoItem
            binding.etTitle.setText(oldTodo.title)
            binding.etNote.setText(oldTodo.note)
            isUpdate = true
        }catch (e: Exception){
            e.printStackTrace()
        }
        if(isUpdate){
            binding.imgDelete.visibility = View.VISIBLE
        }else{
            binding.imgDelete.visibility = View.INVISIBLE
        }

        binding.imgCheck.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val todoDescription = binding.etNote.text.toString()

            if(title.isNotEmpty() && todoDescription.isNotEmpty()){
                val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a")

                if(isUpdate){
                    todo = ToDoItem(oldTodo.id, title, todoDescription, formatter.format(Date()))
                }else{
                    todo = ToDoItem(0, title, todoDescription, formatter.format(Date()))
                }
                var intent = Intent()
                intent.putExtra("todo", todo)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }else{
                Toast.makeText(this@AddToDoActivity, "please enter some data", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
        }

        binding.imgDelete.setOnClickListener {
            var intent = Intent()
            intent.putExtra("todo", oldTodo)
            intent.putExtra("delete_todo", true)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        binding.imgBackArrow.setOnClickListener {
            onBackPressed()
        }
    }
}