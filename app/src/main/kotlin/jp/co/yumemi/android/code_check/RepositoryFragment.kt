/*
 * Copyright © 2021 YUMEMI Inc. All rights reserved.
 */
package jp.co.yumemi.android.code_check

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import jp.co.yumemi.android.code_check.TopActivity.Companion.lastSearchDate
import jp.co.yumemi.android.code_check.databinding.FragmentRepositoryBinding

class RepositoryFragment : Fragment(R.layout.fragment_repository) {

    private val args: RepositoryFragmentArgs by navArgs()
    private var binding: FragmentRepositoryBinding? = null
    private val _binding: FragmentRepositoryBinding
        get() = binding ?: throw IllegalStateException("View binding is accessed before initialization or after destruction.")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("検索した日時", lastSearchDate.toString())

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
        callback.isEnabled = true

        binding = FragmentRepositoryBinding.bind(view)

        setupActionBar()
        setupMenu()
        
        // varでなくvalで問題ない
        val item = args.item
        val context = requireContext()

        _binding.ownerIconView.load(item.ownerIconUrl);
        _binding.nameView.text = item.name;
        _binding.languageView.text = item.language;
        _binding.starsView.text = "${item.stargazersCount} stars";
        _binding.watchersView.text = "${item.watchersCount} watchers";
        _binding.forksView.text = "${item.forksCount} forks";
        _binding.openIssuesView.text = "${item.openIssuesCount} open issues";
    }

    // 戻るボタンの実装
    private fun setupActionBar() {
        // 強制ダウンキャストの回避
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Repository Details"
            setDisplayHomeAsUpEnabled(true)
        }
        Log.d("TwoFragment", "ActionBar set up with back button")
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        activity?.onBackPressedDispatcher?.onBackPressed()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        // 強制ダウンキャストの回避
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}
