/*
 * Copyright © 2021 YUMEMI Inc. All rights reserved.
 */
package jp.co.yumemi.android.code_check

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import jp.co.yumemi.android.code_check.databinding.FragmentSearchBinding
// テスト1
// テスト2
class SearchFragment: Fragment(R.layout.fragment_search) {

    // 必要なときに初めて初期化する方式
    private var binding: FragmentSearchBinding? = null
    private val _binding: FragmentSearchBinding
        get() = binding ?: throw IllegalStateException("View binding is accessed before initialization or after destruction.")

    // by lazyを用いた初期化
    private val _viewModel: SearchViewModel by lazy { SearchViewModel() }
    private val _adapter: CustomAdapter by lazy {
        CustomAdapter(object : CustomAdapter.OnItemClickListener {
            override fun itemClick(item: Items) {
                gotoRepositoryFragment(item)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ここでbindingを初期化
        binding = FragmentSearchBinding.bind(view)

        val _layoutManager = LinearLayoutManager(context!!)
        val _dividerItemDecoration =
            DividerItemDecoration(context!!, _layoutManager.orientation)
        val _adapter = CustomAdapter(object : CustomAdapter.OnItemClickListener {
            override fun itemClick(item: Items) {
                gotoRepositoryFragment(item)
            }
        })

        _binding.searchInputText
            .setOnEditorActionListener{ editText, action, _ ->
                if (action== EditorInfo.IME_ACTION_SEARCH){
                    editText.text.toString().let {
                        _viewModel.searchResults(it).apply{
                            _adapter.submitList(this)
                        }
                    }
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }

        _binding.recyclerView.also {
            it.layoutManager = _layoutManager
            it.addItemDecoration(_dividerItemDecoration)
            it.adapter = _adapter
        }
    }

    fun gotoRepositoryFragment(item: Items) {
        val _action = OneFragmentDirections
            .actionRepositoriesFragmentToRepositoryFragment(item = item)
        findNavController().navigate(_action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // メモリを開放することでメモリリークを防ぐ。
        binding = null
    }
}

val DIFF_UTIL = object: DiffUtil.ItemCallback<Items>() {
    override fun areItemsTheSame(oldItem: Items, newItem: Items): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Items, newItem: Items): Boolean {
        return oldItem == newItem
    }
}
// test
class CustomAdapter(
    private val itemClickListener: OnItemClickListener,
) : ListAdapter<item, CustomAdapter.ViewHolder>(DIFF_UTIL) {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view)

    interface OnItemClickListener {
    	fun itemClick(item: Items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    	val _view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item, parent, false)
    	return ViewHolder(_view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    	val _item = getItem(position)
        (holder.itemView.findViewById<View>(R.id.repositoryNameView) as TextView).text =
            _item.name

    	holder.itemView.setOnClickListener {
     		itemClickListener.itemClick(_item)
    	}
    }
}
