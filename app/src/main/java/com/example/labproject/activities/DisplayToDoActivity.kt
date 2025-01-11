package com.example.labproject.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.labproject.adapter.ToDoAdapter
import com.example.labproject.database.ToDoDataBase
import com.example.labproject.databinding.ActivityDisplayToDoBinding
import com.example.labproject.model.ToDoItem
import com.example.labproject.viewmodel.ToDoViewModel

@Suppress("DEPRECATION")
class DisplayToDoActivity : AppCompatActivity(), ToDoAdapter.TodoClickListener {
    private lateinit var binding: ActivityDisplayToDoBinding
    private lateinit var database: ToDoDataBase
    lateinit var viewModel: ToDoViewModel
    lateinit var adapter: ToDoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisplayToDoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(ToDoViewModel::class.java)

        viewModel.allTodo.observe(this) { list ->
            list?.let {
                adapter.updateList(list)
            }
        }

        database = ToDoDataBase.getDatabase(this)
    }

    private fun initUI() {
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = ToDoAdapter(this, this)
        binding.recyclerView.adapter = adapter

        val getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val todo = result.data?.getSerializableExtra("todo") as? ToDoItem
                    if (todo != null) {
                        viewModel.insertTodo(todo)
                    }
                }
            }

        binding.fabAddTodo.setOnClickListener {
            val intent = Intent(this, AddToDoActivity::class.java)
            getContent.launch(intent)
        }

    }

    private val updateOrDeleteTodo =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val todo = result.data?.getSerializableExtra("todo") as ToDoItem
                val isDelete = result.data?.getBooleanExtra("delete_todo", false) as Boolean
                if (todo != null && !isDelete) {
                    viewModel.updateTodo(todo)
                }else if(todo != null && isDelete){
                    viewModel.deleteTodo(todo)
                }
            }
        }



    override fun onItemClicked(todo: ToDoItem) {
        val intent = Intent(this@DisplayToDoActivity, AddToDoActivity::class.java)
        intent.putExtra("current_todo", todo)
        updateOrDeleteTodo.launch(intent)
    }
}