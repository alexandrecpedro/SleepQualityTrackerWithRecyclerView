/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** variables for Header and SleepNight item types **/
private val ITEM_VIEW_TYPE_HEADER = 0
private val ITEM_VIEW_TYPE_ITEM = 1

/**
 * An adapter that provides a list of [SleepNight] to a RecyclerView
 *
 * Setting [data] will cause the displayed list to update
 */
// Update the declaration of SleepNightAdapter to support any type of view holder
class SleepNightAdapter(val clickListener: SleepNightListener) : ListAdapter<SleepNightAdapter.DataItem,
        RecyclerView.ViewHolder>(SleepNightDiffCallback()) {

    /** Convert a List<SleepNight> to a List<DataItem> **/
    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addHeaderAndSubmitList(list: List<SleepNight>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.SleepNightItem(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    /**
     * Part of the RecyclerView adapter, called when RecyclerView needs a new [ViewHolder]
     *
     * A ViewHolder holds a view for the [RecyclerView] as well as providing additional information
     * to the RecyclerView such as where on the screen it was last drawn during scrolling
     **/
    // TODO Override onCreateViewHolder()’s return type (TextItemViewHolder) to
    //  RecyclerView.ViewHolder, and update it to return the correct ITEM_VIEW_TYPE
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    class TextViewHolder(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.header, parent, false)
                return TextViewHolder(view)
            }
        }
    }

    // override getItemViewType and return the correct item view type
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.SleepNightItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    /**
     * Part of the RecyclerView adapter, called when RecyclerView needs to show an item
     *
     * The ViewHolder passed may be recycled, so make sure that this sets any properties that
     * may have been set previously
     **/
    // Unwrap the DataItem to pass a SleepNight to the ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val nightItem = getItem(position) as DataItem.SleepNightItem
                holder.bind(nightItem.sleepNight, clickListener)
            }
        }
    }

    // Refactor and rename the ViewHolder class’s constructor parameter to take
    // a ListItemSleepNightBinding parameter called binding.root
    class ViewHolder private constructor(val binding: ListItemSleepNightBinding)
        : RecyclerView.ViewHolder(binding.root){

        // Encapsulating the logic for binding the ViewHolder into a bind() method
        // Replace the code with a single binding to the SleepNight item
        // Pass the click listener to bind() and add it to the binding
        fun bind(item: SleepNight, clickListener: SleepNightListener) {
            binding.sleep = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                // Get the LayoutInflater from parent.context
                val layoutInflater = LayoutInflater.from(parent.context)
                // Use ListItemSleepNightBinding.inflate to create a binding object
                val binding = ListItemSleepNightBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    /**
     * Callback for calculating the diff between two non-null items in a list.
     *
     * Used by ListAdapter to calculate the minimum number of changes between and old list and a new
     * list that's been passed to `submitList`.
     */
    // Update your DiffCallback to handle DataItem instead of SleepNight
    class SleepNightDiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }
    }

    /** create a new class called SleepNightListener **/
    class SleepNightListener(val clickListener: (sleepId: Long) -> Unit) {
        fun onClick(night: SleepNight) = clickListener(night.nightId)
    }

    /** Add a sealed class called DataItem **/
    sealed class DataItem {
        data class SleepNightItem(val sleepNight: SleepNight): DataItem() {
            override val id = sleepNight.nightId
        }

        object Header: DataItem() {
            override val id = Long.MIN_VALUE
        }

        abstract val id: Long
    }

}
