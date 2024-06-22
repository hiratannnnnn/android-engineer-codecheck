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
import android.view.KeyEvent

import kotlinx.coroutines.*
import android.util.Log
import androidx.lifecycle.lifecycleScope

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

        // モジュール化し、とりわけAPIからfetchする部分はViewModelの方に移植
        setupRecyclerView()
        setupObservers()
        setupSearchInput()
    }

    private fun setupRecyclerView() {
        // contextはここで初めて取得する
        val layoutManager = LinearLayoutManager(requireContext())
        val dividerItemDecoration = DividerItemDecoration(requireContext())
        _binding.recyclerView.apply {
            this.layoutManager = layoutManager
            this.addItemDecoration(dividerItemDecoration)
            this.adapter = _adapter
            // ↑の部分で、adapter変数の_がないとバグが発生したので、
            // 全体を通して_をつけた変数名で統一するのが望ましいと考えた。
        }
    }

    private fun setupObservers() {
        _viewModel.searchResults.observe(viewLifecyclerOwner) { results ->
            _adapter.submitList(results)
        }
    }

    private fun setupSearchInput() {
        _binidng.searchInputText.setOnEditorActionListener { editText, action, keyEvent ->
            when (action) {
                EditorInfo.IME_ACTION_SEARCH,
                EditorInfo.IME_ACTION_DONE,
                EditorInfo.IME_ACTION_NEXT -> {
                    handleSearchAction(editText.text.toString())
                    true
                }
                else -> {
                    if (keyEvent != null && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                        handleSearchAction(editText.text.toString())
                        true
                    }
                    false
                }
            }
        }
    }

    private fun handleSearchAction(query: String) {
        viewLifecycleOwner.lifeyclerScope.launch {
            _viewModel.searchResults(requireContext(), query)
        }
    }

    fun gotoRepositoryFragment(item: Items) {
        // localな変数は_は不要とAndroid Studioに言われたので_を取った。
        val action = OneFragmentDirections
            .actionRepositoriesFragmentToRepositoryFragment(item = item)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // メモリを開放することでメモリリークを防ぐ。
        binding = null
    }
}

class CustomAdapter(
    private val itemClickListener: OnItemClickListener,
) : ListAdapter<item, CustomAdapter.ViewHolder>(DIFF_UTIL) {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view)

    interface OnItemClickListener {
    	fun itemClick(item: Items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    	// 上と同様の理由で_を取った。
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item, parent, false)
    	return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    	val _item = getItem(position)
        (holder.itemView.findViewById<View>(R.id.repositoryNameView) as TextView).text =
            _item.name

    	holder.itemView.setOnClickListener {
     		itemClickListener.itemClick(_item)
    	}
    }
    // DIFF_UTILをCustomAdapter class内にまとめておく
    companion object {
        val DIFF_UTIL = object: DiffUtil.ItemCallback<Items>() {
            override fun areItemsTheSame(oldItem: Items, newItem: Items): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: Items, newItem: Items): Boolean {
                return oldItem == newItem
            }
        }
    }
}
