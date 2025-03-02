package com.example.labproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.labproject.databinding.ViewClothingGridLayoutBinding
import com.example.labproject.databinding.ViewClothingListLayoutBinding
import com.example.labproject.model.ClothingItem
import java.text.SimpleDateFormat
import java.util.Locale


class ClothingItemRVVBListAdapter(
    private val isList: MutableLiveData<Boolean>,
    private val deleteUpdateCallback: (type: String, position: Int, clothingItem: ClothingItem) -> Unit,
) :
    ListAdapter<ClothingItem, RecyclerView.ViewHolder>(DiffCallback()) {



    class ListClothingItemViewHolder(private val viewClothingListLayoutBinding: ViewClothingListLayoutBinding) :
        RecyclerView.ViewHolder(viewClothingListLayoutBinding.root) {

        fun bind(
            clothingItem: ClothingItem,
            deleteUpdateCallback: (type: String, position: Int, clothingItem: ClothingItem) -> Unit,
        ) {
            viewClothingListLayoutBinding.nameTxt.text = clothingItem.name

            

            viewClothingListLayoutBinding.deleteImg.setOnClickListener {
                if (adapterPosition != -1) {
                    deleteUpdateCallback("delete", adapterPosition, clothingItem)
                }
            }
            viewClothingListLayoutBinding.editImg.setOnClickListener {
                if (adapterPosition != -1) {
                    deleteUpdateCallback("update", adapterPosition, clothingItem)
                }
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        return if (viewType == 1)
        {  ListClothingItemViewHolder(
            ViewClothingListLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
         )
        }
        else
        {  // Display_List
            ListClothingItemViewHolder(
                ViewClothingListLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val clothingItem = getItem(position)

        if (isList.value!!){
            (holder as ListClothingItemViewHolder).bind(clothingItem,deleteUpdateCallback)
        }else{
            (holder as ListClothingItemViewHolder).bind(clothingItem,deleteUpdateCallback)
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (isList.value!!){
            0 // Display List
        }else{
            1 // Display Grid
        }
    }



    class DiffCallback : DiffUtil.ItemCallback<ClothingItem>() {
        override fun areItemsTheSame(oldItem: ClothingItem, newItem: ClothingItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ClothingItem, newItem: ClothingItem): Boolean {
            return oldItem == newItem
        }

    }

}