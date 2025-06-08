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
import com.example.labproject.databinding.ActivityTaskBinding
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale

class TaskActivity : AppCompatActivity() {
    private val taskBinding: ActivityTaskBinding by lazy {
        ActivityTaskBinding.inflate(layoutInflater)
    }

    private val loadingDialog: Dialog by lazy {
        Dialog(this, R.style.DialogCustomTheme).apply {
            setupDialog(R.layout.loading_dialog)
        }
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


    private val taskViewModel: TaskViewModel by lazy {
        ViewModelProvider(this)[TaskViewModel::class.java]
    }

    private val isListMutableLiveData = MutableLiveData<Boolean>().apply {
        postValue(true)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val timePicker: TimePicker = addTaskDialog.findViewById(R.id.starttime)
        val timePicker2: TimePicker = addTaskDialog.findViewById(R.id.endtime)

        // Initialize calendars for start and end times
        val startCalendar = Calendar.getInstance()
        val endCalendar = Calendar.getInstance()
        var startMoment = startCalendar.time
        var endMoment = endCalendar.time

        // Get weather data from intent
        val tempValue = intent.getIntExtra("Temperature", 0)
        val weatherDescription = intent.getStringExtra("WeatherDescription") ?: ""
        val windSpeed = intent.getFloatExtra("WindSpeed", 0f)
        val rainChance = intent.getIntExtra("RainChance", 0)

        // Set up time picker listeners
        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            startCalendar.set(Calendar.MINUTE, minute)
            startMoment = startCalendar.time
            android.util.Log.d("TaskActivity", "Start time set to: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(startMoment)}")
        }

        timePicker2.setOnTimeChangedListener { _, hourOfDay, minute ->
            endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            endCalendar.set(Calendar.MINUTE, minute)
            endMoment = endCalendar.time
            android.util.Log.d("TaskActivity", "End time set to: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(endMoment)}")
        }

        setContentView(taskBinding.root)

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

        taskBinding.addTaskFABtn.setOnClickListener {
            clearEditText(addETTitle, addETTitleL)
            clearEditText(addETDesc, addETDescL)

            // Reset time pickers to current time
            val now = Calendar.getInstance()
            startCalendar.time = now.time
            endCalendar.time = now.time
            startMoment = startCalendar.time
            endMoment = endCalendar.time

            timePicker.hour = now.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = now.get(Calendar.MINUTE)
            timePicker2.hour = now.get(Calendar.HOUR_OF_DAY)
            timePicker2.minute = now.get(Calendar.MINUTE)

            addTaskDialog.show()
        }

        val saveTaskBtn = addTaskDialog.findViewById<Button>(R.id.saveTaskBtn)
        saveTaskBtn.setOnClickListener {
            if (validateEditText(addETTitle, addETTitleL)
                && validateEditText(addETDesc, addETDescL)
            ) {
                android.util.Log.d("TaskActivity", "Creating new task with title: ${addETTitle.text.toString().trim()}")
                android.util.Log.d("TaskActivity", "Creating new task with start: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(startMoment)} and end: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(endMoment)}")

                val newTask = Task(
                    UUID.randomUUID().toString(),
                    addETTitle.text.toString().trim(),
                    addETDesc.text.toString().trim(),
                    startMoment,
                    endMoment
                )
                android.util.Log.d("TaskActivity", "Created task object: $newTask")
                hideKeyBoard(it)
                addTaskDialog.dismiss()
                taskViewModel.insertTask(newTask)
            }
        }

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



        isListMutableLiveData.observe(this) {
            if (it) {
                taskBinding.taskRV.layoutManager = LinearLayoutManager(
                    this, LinearLayoutManager.VERTICAL, false
                )
                taskBinding.listOrGridImg.setImageResource(R.drawable.ic_view_module)
            } else {
                taskBinding.taskRV.layoutManager = StaggeredGridLayoutManager(
                    2, LinearLayoutManager.VERTICAL
                )
                taskBinding.listOrGridImg.setImageResource(R.drawable.ic_view_list)
            }
        }

        taskBinding.listOrGridImg.setOnClickListener {
            isListMutableLiveData.postValue(!isListMutableLiveData.value!!)
        }

        val taskRVVBListAdapter =
            TaskRVVBListAdapter(isListMutableLiveData) { type, position, task ->
                if (type == "delete") {
                    taskViewModel
                        .deleteTaskUsingId(task.id)
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
                                startMoment,
                                endMoment
                            )
                            hideKeyBoard(it)
                            updateTaskDialog.dismiss()
                            taskViewModel
                                .updateTask(updateTask)
                        }
                    }
                    updateTaskDialog.show()
                }
            }
        taskBinding.taskRV.adapter = taskRVVBListAdapter
        ViewCompat.setNestedScrollingEnabled(taskBinding.taskRV, false)
        taskRVVBListAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                taskBinding.nestedScrollView.smoothScrollTo(0, positionStart)
            }
        })






        callGetTaskList(taskRVVBListAdapter)
        callSortByLiveData()
        statusCallback()
        callSearch()

        taskBinding.nextFABtn.setOnClickListener {
            // Collect all tasks for today
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    android.util.Log.d("TaskActivity", "Current date: $today")

                    val taskFlow = taskViewModel.taskStateFlow.first()
                    android.util.Log.d("TaskActivity", "Task flow status: ${taskFlow.status}")

                    val allTasks = taskFlow.data?.first()
                    android.util.Log.d("TaskActivity", "All tasks: ${allTasks?.map { it.title }}")

                    val tasks = allTasks?.filter { task ->
                        val taskDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(task.starttime)
                        android.util.Log.d("TaskActivity", "Comparing task date: $taskDate with today: $today")
                        taskDate == today
                    } ?: emptyList()

                    // Create ArrayList of task names
                    val taskNames = ArrayList<String>()
                    tasks.forEach { task ->
                        taskNames.add(task.title)
                        android.util.Log.d("TaskActivity", "Adding task to list: ${task.title}")
                    }

                    // Log the task names we're passing
                    android.util.Log.d("TaskActivity", "Final task names being passed: $taskNames")

                    // Start DisplayClothesActivity with all data
                    val intent = Intent(this@TaskActivity, DisplayClothesActivity::class.java)
                        .putExtra("Temperature", tempValue)
                        .putExtra("WeatherDescription", weatherDescription)
                        .putExtra("WindSpeed", windSpeed)
                        .putExtra("RainChance", intent.getIntExtra("RainChance", 0))
                        .putStringArrayListExtra("TaskNames", taskNames)
                    startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("TaskActivity", "Error collecting tasks", e)
                    Toast.makeText(this@TaskActivity, "Error collecting tasks: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
        taskBinding.edSearch.addTextChangedListener(object : TextWatcher {
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

        taskBinding.edSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyBoard(v)
                return@setOnEditorActionListener true
            }
            false
        }

        callSortByDialog()
    }

    private fun callSortByLiveData() {
        taskViewModel.sortByLiveData.observe(this) { sortPair ->
            taskViewModel.getTaskList(sortPair.second, sortPair.first)
        }
    }

    private fun callSortByDialog() {
        var checkedItem = 0   // 2 is default item set
        val items =
            arrayOf("Title Ascending", "Title Descending", "Date Ascending", "Date Descending")

        taskBinding.sortImg.setOnClickListener {
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
            taskBinding.root, "Deleted '${deletedTask.title}'",
            Snackbar.LENGTH_LONG
        )
        snackBar.setAction("Undo") {
            taskViewModel.insertTask(deletedTask)
        }
        snackBar.show()
    }


    private fun callGetTaskList(taskRVVBListAdapter: TaskRVVBListAdapter) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                taskViewModel.taskStateFlow.collectLatest {
                    when (it.status) {
                        Util.Status.LOADING -> {
                            loadingDialog.show()
                        }

                        Util.Status.SUCCESS -> {
                            loadingDialog.dismiss()
                            it.data?.collect { taskList ->
                                taskRVVBListAdapter.submitList(taskList)
                            }
                        }

                        Util.Status.ERROR -> {
                            loadingDialog.dismiss()
                            it.message?.let { it1 -> longToastShow(it1) }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskActivity", "Error collecting tasks", e)
                loadingDialog.dismiss()
                Toast.makeText(this@TaskActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}










