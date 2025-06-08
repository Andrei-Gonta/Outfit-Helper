package com.example.labproject.activities

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.labproject.R
import com.example.labproject.adapter.TaskRVVBListAdapter
import com.example.labproject.databinding.ActivityMainBinding

import com.example.labproject.databinding.ActivityTaskSimpleBinding
import com.example.labproject.model.Task
import com.example.labproject.utils.Util
import com.example.labproject.utils.Util.clearEditText
import com.example.labproject.utils.Util.hideKeyBoard
import com.example.labproject.utils.Util.longToastShow
import com.example.labproject.utils.Util.setupDialog
import com.example.labproject.utils.Util.validateEditText
import com.example.labproject.viewmodel.TaskViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID

class TaskSimpleActivity : AppCompatActivity() {
    private val taskSimpleBinding: ActivityTaskSimpleBinding by lazy {
        ActivityTaskSimpleBinding.inflate(layoutInflater)
    }



    private val addTaskDialog: Dialog by lazy {
        Dialog(this, R.style.DialogCustomTheme).apply {
            setupDialog(R.layout.add_task_dialog)
        }
    }

    private val updateTaskDialog: Dialog by lazy {
        Dialog(this, R.style.DialogCustomTheme).apply {
            setupDialog(R.layout.update_task_dialog)
        }
    }

    private val loadingDialog: Dialog by lazy {
        Dialog(this, R.style.DialogCustomTheme).apply {
            setupDialog(R.layout.loading_dialog)
        }
    }

    private val taskViewModel: TaskViewModel by lazy {
        ViewModelProvider(this)[TaskViewModel::class.java]
    }

    private val isListMutableLiveData = MutableLiveData<Boolean>().apply {
        postValue(true)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val timePicker: TimePicker = addTaskDialog.findViewById(R.id.starttime)
        val calendar = Calendar.getInstance()
        var myDate = calendar.time

        val temp_value = intent.getIntExtra("Temperature", 0)
        val weatherDescription = intent.getStringExtra("WeatherDescription") ?: ""
        val windSpeed = intent.getFloatExtra("WindSpeed", 0f)
        val humidity = intent.getIntExtra("Humidity", 0)

        //timePicker.setIs24HourView(true)
        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            // Now you can use myDate as needed
            myDate = calendar.time
        }
        setContentView(taskSimpleBinding.root)


        // Add task start
        val addCloseImg = addTaskDialog.findViewById<ImageView>(R.id.closeImg)
        addCloseImg.setOnClickListener { addTaskDialog.dismiss() }

        val addETTitle = addTaskDialog.findViewById<TextInputEditText>(R.id.edTaskTitle)
        val addETTitleL = addTaskDialog.findViewById<TextInputLayout>(R.id.edTaskTitleL)

        addETTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(addETTitle, addETTitleL)
            }

        })

        val addETDesc = addTaskDialog.findViewById<TextInputEditText>(R.id.edTaskDesc)
        val addETDescL = addTaskDialog.findViewById<TextInputLayout>(R.id.edTaskDescL)

        addETDesc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(addETDesc, addETDescL)
            }
        })

        //-----------------------------------


        taskSimpleBinding.nextFABtn.setOnClickListener {
            val intent = Intent(this, ResultActivity::class.java)
                .putExtra("Temperature", temp_value)
                .putExtra("WeatherDescription", weatherDescription)
                .putExtra("WindSpeed", windSpeed)
                .putExtra("Humidity", humidity)
            startActivity(intent)
        }

        val saveTaskBtn = addTaskDialog.findViewById<Button>(R.id.saveTaskBtn)
        saveTaskBtn.setOnClickListener {
            if (validateEditText(addETTitle, addETTitleL)
                && validateEditText(addETDesc, addETDescL)
            ) {

                val newTask = Task(
                    UUID.randomUUID().toString(),
                    addETTitle.text.toString().trim(),
                    addETDesc.text.toString().trim(),
                    myDate,
                    Date()
                )
                hideKeyBoard(it)
                addTaskDialog.dismiss()
                taskViewModel.insertTask(newTask)
            }
        }
        // Add task end


        // Update Task Start
        val updateETTitle = updateTaskDialog.findViewById<TextInputEditText>(R.id.edTaskTitle)
        val updateETTitleL = updateTaskDialog.findViewById<TextInputLayout>(R.id.edTaskTitleL)

        updateETTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(updateETTitle, updateETTitleL)
            }
        })

        val updateETDesc = updateTaskDialog.findViewById<TextInputEditText>(R.id.edTaskDesc)
        val updateETDescL = updateTaskDialog.findViewById<TextInputLayout>(R.id.edTaskDescL)

        updateETDesc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(updateETDesc, updateETDescL)
            }
        })

        val updateCloseImg = updateTaskDialog.findViewById<ImageView>(R.id.closeImg)
        updateCloseImg.setOnClickListener { updateTaskDialog.dismiss() }

        val updateTaskBtn = updateTaskDialog.findViewById<Button>(R.id.updateTaskBtn)

        // Update Task End

        isListMutableLiveData.observe(this) {
            if (it) {
                taskSimpleBinding.taskRV.layoutManager = LinearLayoutManager(
                    this, LinearLayoutManager.VERTICAL, false
                )
                taskSimpleBinding.listOrGridImg.setImageResource(R.drawable.ic_view_module)
            } else {
                taskSimpleBinding.taskRV.layoutManager = StaggeredGridLayoutManager(
                    2, LinearLayoutManager.VERTICAL
                )
                taskSimpleBinding.listOrGridImg.setImageResource(R.drawable.ic_view_list)
            }
        }

        taskSimpleBinding.listOrGridImg.setOnClickListener {
            isListMutableLiveData.postValue(!isListMutableLiveData.value!!)
        }

        val taskRVVBListAdapter =
            TaskRVVBListAdapter(isListMutableLiveData) { type, position, task ->
                if (type == "delete") {
                    taskViewModel
                        // Deleted Task
//                .deleteTask(task)
                        .deleteTaskUsingId(task.id)

                    // Restore Deleted task
                    restoreDeletedTask(task)
                } else if (type == "update") {
                    updateETTitle.setText(task.title)
                    updateETDesc.setText(task.description)
                    updateTaskBtn.setOnClickListener {
                        if (validateEditText(updateETTitle, updateETTitleL)
                            && validateEditText(updateETDesc, updateETDescL)
                        ) {
                            val updateTask = Task(
                                task.id,
                                updateETTitle.text.toString().trim(),
                                updateETDesc.text.toString().trim(),
//                           here i Date updated
                                myDate,
                                Date()
                            )
                            hideKeyBoard(it)
                            updateTaskDialog.dismiss()
                            taskViewModel
                                .updateTask(updateTask)
//                            .updateTaskPaticularField(
//                                task.id,
//                                updateETTitle.text.toString().trim(),
//                                updateETDesc.text.toString().trim()
//                            )
                        }
                    }
                    updateTaskDialog.show()
                }
            }
        taskSimpleBinding.taskRV.adapter = taskRVVBListAdapter
        ViewCompat.setNestedScrollingEnabled(taskSimpleBinding.taskRV, false)
        taskRVVBListAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
//                mainBinding.taskRV.smoothScrollToPosition(positionStart)
                taskSimpleBinding.nestedScrollView.smoothScrollTo(0, positionStart)
            }
        })






        callGetTaskList(taskRVVBListAdapter)
        callSortByLiveData()
        statusCallback()
        callSearch()
    }


    private fun statusCallback() {
        taskViewModel
            .statusLiveData
            .observe(this) {
                when (it.status) {
                    Util.Status.LOADING -> {
                        loadingDialog.show()
                    }

                    Util.Status.SUCCESS -> {
                        loadingDialog.dismiss()
                        when (it.data as Util.StatusResult) {
                            Util.StatusResult.Added -> {
                                Log.d("StatusResult", "Added")
                            }

                            Util.StatusResult.Deleted -> {
                                Log.d("StatusResult", "Deleted")

                            }

                            Util.StatusResult.Updated -> {
                                Log.d("StatusResult", "Updated")

                            }
                        }
                        it.message?.let { it1 -> longToastShow(it1) }
                    }

                    Util.Status.ERROR -> {
                        loadingDialog.dismiss()
                        it.message?.let { it1 -> longToastShow(it1) }
                    }
                }
            }
    }


    //--------------------------------


    private fun callSearch() {
        taskSimpleBinding.edSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(query: Editable) {
                if (query.toString().isNotEmpty()) {
                    taskViewModel.searchTaskList(query.toString())
                } else {
                    callSortByLiveData()
                }
            }
        })

        taskSimpleBinding.edSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyBoard(v)
                return@setOnEditorActionListener true
            }
            false
        }

        callSortByDialog()
    }

    private fun callSortByLiveData() {
        taskViewModel.sortByLiveData.observe(this) {
            taskViewModel.getTaskList(it.second, it.first)
        }
    }

    private fun callSortByDialog() {
        var checkedItem = 0   // 2 is default item set
        val items =
            arrayOf("Title Ascending", "Title Descending", "Date Ascending", "Date Descending")

        taskSimpleBinding.sortImg.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Sort By")
                .setPositiveButton("Ok") { _, _ ->
                    when (checkedItem) {
                        0 -> {
                            taskViewModel.setSortBy(Pair("title", true))
                        }

                        1 -> {
                            taskViewModel.setSortBy(Pair("title", false))
                        }

                        2 -> {
                            taskViewModel.setSortBy(Pair("starttime", true))
                        }

                        else -> {
                            taskViewModel.setSortBy(Pair("start_time", false))
                        }
                    }
                }
                .setSingleChoiceItems(items, checkedItem) { _, selectedItemIndex ->
                    checkedItem = selectedItemIndex
                }
                .setCancelable(false)
                .show()
        }
    }


    private fun restoreDeletedTask(deletedTask: Task) {
        val snackBar = Snackbar.make(
            taskSimpleBinding.root, "Deleted '${deletedTask.title}'",
            Snackbar.LENGTH_LONG
        )
        snackBar.setAction("Undo") {
            taskViewModel.insertTask(deletedTask)
        }
        snackBar.show()
    }


    private fun callGetTaskList(taskRecyclerViewAdapter: TaskRVVBListAdapter) {

        CoroutineScope(Dispatchers.Main).launch {
            taskViewModel
                .taskStateFlow
                .collectLatest {
                    Log.d("status", it.status.toString())

                    when (it.status) {
                        Util.Status.LOADING -> {
                            loadingDialog.show()
                        }

                        Util.Status.SUCCESS -> {
                            loadingDialog.dismiss()
                            it.data?.collect { taskList ->
                                taskRecyclerViewAdapter.submitList(taskList)
                            }
                        }

                        Util.Status.ERROR -> {
                            loadingDialog.dismiss()
                            it.message?.let { it1 -> longToastShow(it1) }
                        }
                    }

                }
        }
    }

}










