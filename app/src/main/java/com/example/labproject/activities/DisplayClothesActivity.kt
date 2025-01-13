package com.example.labproject.activities

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.labproject.R
import com.example.labproject.adapter.ClothingItemRVVBListAdapter
import com.example.labproject.adapter.TaskRVVBListAdapter
import com.example.labproject.databinding.ActivityDisplayClothesBinding

import com.example.labproject.model.ClothingItem
import com.example.labproject.model.Task
import com.example.labproject.utils.Util
import com.example.labproject.utils.Util.clearEditText
import com.example.labproject.utils.Util.hideKeyBoard
import com.example.labproject.utils.Util.longToastShow
import com.example.labproject.utils.Util.setupDialog
import com.example.labproject.utils.Util.validateEditText
import com.example.labproject.viewmodel.ClothingItemViewModel

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class DisplayClothesActivity : AppCompatActivity() {

    private val clothingItemBinding: ActivityDisplayClothesBinding by lazy {
        ActivityDisplayClothesBinding.inflate(layoutInflater)
    }

    private val addClothingItemDialog: Dialog by lazy {
        Dialog(this, R.style.DialogCustomTheme).apply {
            setupDialog(R.layout.add_clothing_item_dialog)
        }
    }

    private val updateClothingItemDialog: Dialog by lazy {
        Dialog(this, R.style.DialogCustomTheme).apply {
            setupDialog(R.layout.update_clothing_item_dialog)
        }
    }

    private val loadingDialog: Dialog by lazy {
        Dialog(this, R.style.DialogCustomTheme).apply {
            setupDialog(R.layout.loading_dialog)
        }
    }

    private val clothingItemViewModel: ClothingItemViewModel by lazy {
        ViewModelProvider(this)[ClothingItemViewModel::class.java]
    }

    private val isListMutableLiveData = MutableLiveData<Boolean>().apply {
        postValue(true)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(clothingItemBinding.root)


        // Add task start
        val addCloseImg = addClothingItemDialog.findViewById<ImageView>(R.id.closeImg)
        addCloseImg.setOnClickListener { addClothingItemDialog.dismiss() }

        val addETName = addClothingItemDialog.findViewById<TextInputEditText>(R.id.edClothingItemName)
        val addETNameL = addClothingItemDialog.findViewById<TextInputLayout>(R.id.edClothingItemNameL)

        addETName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(addETName, addETNameL)
            }

        })




        clothingItemBinding.addClothingItemFABtn.setOnClickListener {
            clearEditText(addETName, addETNameL)

            addClothingItemDialog.show()
        }
        val saveClothingItemBtn = addClothingItemDialog.findViewById<Button>(R.id.saveClothingItemBtn)
        saveClothingItemBtn.setOnClickListener {
            if (validateEditText(addETName, addETNameL)

            ) {

                val newClothingItem = ClothingItem(
                    UUID.randomUUID().toString(),
                    addETName.text.toString().trim(),
                    
                )
                hideKeyBoard(it)
                addClothingItemDialog.dismiss()
                clothingItemViewModel.insertClothingItem(newClothingItem)
            }
        }
        // Add task end


        // Update Task Start
        val updateETName = updateClothingItemDialog.findViewById<TextInputEditText>(R.id.edClothingItemName)
        val updateETNameL = updateClothingItemDialog.findViewById<TextInputLayout>(R.id.edClothingItemNameL)

        updateETName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(updateETName, updateETNameL)
            }

        })
/*
        val updateETDesc = updateClothingItemDialog.findViewById<TextInputEditText>(R.id.edTaskDesc)
        val updateETDescL = updateClothingItemDialog.findViewById<TextInputLayout>(R.id.edTaskDescL)

        updateETDesc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(updateETDesc, updateETDescL)
            }
        })
*/
        val updateCloseImg = updateClothingItemDialog.findViewById<ImageView>(R.id.closeImg)
        updateCloseImg.setOnClickListener { updateClothingItemDialog.dismiss() }

        val updateClothingItemBtn = updateClothingItemDialog.findViewById<Button>(R.id.updateClothingItemBtn)

        // Update Task End

        isListMutableLiveData.observe(this){
            if (it){
                clothingItemBinding.clothingItemRV.layoutManager = LinearLayoutManager(
                    this, LinearLayoutManager.VERTICAL,false
                )
                //clothingItemBinding.sortImg.setImageResource(R.drawable.ic_view_module)
            }else{
                clothingItemBinding.clothingItemRV.layoutManager = StaggeredGridLayoutManager(
                    2, LinearLayoutManager.VERTICAL
                )
               // clothingItemBinding.sortImg.setImageResource(R.drawable.ic_view_list)
            }
        }
        /*
                clothingItemBinding.listOrGridImg.setOnClickListener {
                    isListMutableLiveData.postValue(!isListMutableLiveData.value!!)
                }
                */


        val clothingItemRVVBListAdapter = ClothingItemRVVBListAdapter(isListMutableLiveData ) { type, position, clothingItem ->
            if (type == "delete") {
                clothingItemViewModel
                   
                    .deleteClothingItemUsingId(clothingItem.id)

                // Restore Deleted task
                restoreDeletedClothingItem(clothingItem)
            } else if (type == "update") {
                updateETName.setText(clothingItem.name)
                updateClothingItemBtn.setOnClickListener {
                    if (validateEditText(updateETName, updateETNameL)
                    ) {
                        val updateClothingItem = ClothingItem(
                            clothingItem.id,
                            updateETName.text.toString().trim()
                        
                        )
                        hideKeyBoard(it)
                        updateClothingItemDialog.dismiss()
                        clothingItemViewModel
                            .updateClothingItem(updateClothingItem)
//                            
                    }
                }
                updateClothingItemDialog.show()
            }
        }
        clothingItemBinding.clothingItemRV.adapter = clothingItemRVVBListAdapter
        ViewCompat.setNestedScrollingEnabled(clothingItemBinding.clothingItemRV,false)
        clothingItemRVVBListAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
//                mainBinding.taskRV.smoothScrollToPosition(positionStart)
                clothingItemBinding.nestedScrollView.smoothScrollTo(0,positionStart)
            }
        })
        callGetClothingItemsList(clothingItemRVVBListAdapter)
        callSortByLiveData()
        statusCallback()
        callSearch()
    }


    private fun statusCallback() {
        clothingItemViewModel
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


    private fun callSearch() {
        clothingItemBinding.edSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(query: Editable) {
                if (query.toString().isNotEmpty()){
                    clothingItemViewModel.searchClothingList(query.toString())
                }else{
                    callSortByLiveData()
                }
            }
        })

        clothingItemBinding.edSearch.setOnEditorActionListener{ v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                hideKeyBoard(v)
                return@setOnEditorActionListener true
            }
            false
        }

        callSortByDialog()
    }

    private fun callSortByLiveData(){
        clothingItemViewModel.sortByLiveData.observe(this){
            clothingItemViewModel.getClothingItems(it.second,it.first)
        }
    }

    private fun callSortByDialog() {
        var checkedItem = 0   // 2 is default item set
        val items = arrayOf("Name Ascending", "Name Descending",)

        clothingItemBinding.sortImg.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Sort By")
                .setPositiveButton("Ok") { _, _ ->
                    when (checkedItem) {
                        0 -> {
                            clothingItemViewModel.setSortBy(Pair("name",true))
                        }

                        else -> {
                            clothingItemViewModel.setSortBy(Pair("name",false))
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


    private fun restoreDeletedClothingItem(deletedClothingItem : ClothingItem){
        val snackBar = Snackbar.make(
            clothingItemBinding.root, "Deleted '${deletedClothingItem.name}'",
            Snackbar.LENGTH_LONG
        )
        snackBar.setAction("Undo"){
            clothingItemViewModel.insertClothingItem(deletedClothingItem)
        }
        snackBar.show()
    }



    private fun callGetClothingItemsList(clothingItemRecyclerViewAdapter: ClothingItemRVVBListAdapter) {

        CoroutineScope(Dispatchers.Main).launch {
            clothingItemViewModel
                .clothingItemStateFlow
                .collectLatest {
                    Log.d("status", it.status.toString())

                    when (it.status) {
                        Util.Status.LOADING -> {
                            loadingDialog.show()
                        }

                        Util.Status.SUCCESS -> {
                            loadingDialog.dismiss()
                            it.data?.collect { clothingItemList ->
                                clothingItemRecyclerViewAdapter.submitList(clothingItemList)
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