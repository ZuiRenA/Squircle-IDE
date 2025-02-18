/*
 * Copyright 2022 Squircle IDE contributors.
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

package com.blacksquircle.ui.feature.themes.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.SearchView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.utils.MDUtil.getStringArray
import com.blacksquircle.ui.core.ui.delegate.viewBinding
import com.blacksquircle.ui.core.ui.extensions.checkStorageAccess
import com.blacksquircle.ui.core.ui.extensions.debounce
import com.blacksquircle.ui.core.ui.extensions.navigate
import com.blacksquircle.ui.core.ui.extensions.showToast
import com.blacksquircle.ui.core.ui.viewstate.ViewEvent
import com.blacksquircle.ui.feature.themes.R
import com.blacksquircle.ui.feature.themes.data.utils.GridSpacingItemDecoration
import com.blacksquircle.ui.feature.themes.data.utils.readAssetFileText
import com.blacksquircle.ui.feature.themes.databinding.FragmentThemesBinding
import com.blacksquircle.ui.feature.themes.domain.model.ThemeModel
import com.blacksquircle.ui.feature.themes.ui.adapters.ThemeAdapter
import com.blacksquircle.ui.feature.themes.ui.navigation.ThemesScreen
import com.blacksquircle.ui.feature.themes.ui.viewmodel.ThemesViewModel
import com.blacksquircle.ui.feature.themes.ui.viewstate.ThemesViewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ThemesFragment : Fragment(R.layout.fragment_themes) {

    private val viewModel by activityViewModels<ThemesViewModel>()
    private val binding by viewBinding(FragmentThemesBinding::bind)
    private val navController by lazy { findNavController() }

    private lateinit var adapter: ThemeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()

        val gridLayoutManager = binding.recyclerView.layoutManager as GridLayoutManager
        GridSpacingItemDecoration(8, gridLayoutManager.spanCount).let {
            binding.recyclerView.addItemDecoration(it)
        }
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = ThemeAdapter(object : ThemeAdapter.Actions {
            override fun selectTheme(themeModel: ThemeModel) = viewModel.selectTheme(themeModel)
            override fun exportTheme(themeModel: ThemeModel) {
                activity?.checkStorageAccess(
                    onSuccess = { viewModel.exportTheme(themeModel) },
                    onFailure = { context?.showToast(R.string.message_access_required) }
                )
            }
            override fun editTheme(themeModel: ThemeModel) {
                navController.navigate(ThemesScreen.Update(themeModel.uuid))
            }
            override fun removeTheme(themeModel: ThemeModel) = viewModel.removeTheme(themeModel)
            override fun showInfo(themeModel: ThemeModel) {
                context?.showToast(text = themeModel.description)
            }
        }).also {
            adapter = it
        }

        binding.actionAdd.setOnClickListener {
            navController.navigate(ThemesScreen.Create)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_themes, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        val state = viewModel.themesState.value
        if (state.query.isNotEmpty()) {
            searchItem?.expandActionView()
            searchView?.setQuery(state.query, false)
        }

        searchView?.debounce(viewLifecycleOwner.lifecycleScope) {
            viewModel.fetchThemes(it)
        }

        val spinnerItem = menu.findItem(R.id.spinner)
        val spinnerView = spinnerItem?.actionView as? AppCompatSpinner

        spinnerView?.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.language_names,
            android.R.layout.simple_spinner_dropdown_item
        )
        spinnerView?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val path = requireContext().getStringArray(R.array.language_paths)[position]
                val extension = requireContext().getStringArray(R.array.language_extensions)[position]
                adapter.codeSnippet = requireContext().readAssetFileText(path) to extension
            }
        }
    }

    private fun observeViewModel() {
        viewModel.themesState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is ThemesViewState.Empty -> {
                        binding.loadingBar.isVisible = false
                        binding.emptyView.isVisible = true
                        binding.recyclerView.isInvisible = true
                    }
                    is ThemesViewState.Data -> {
                        binding.loadingBar.isVisible = false
                        binding.emptyView.isVisible = false
                        binding.recyclerView.isInvisible = false
                        adapter.submitList(state.themes)
                    }
                    ThemesViewState.Loading -> {
                        binding.loadingBar.isVisible = true
                        binding.emptyView.isVisible = false
                        binding.recyclerView.isInvisible = true
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.viewEvent.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { event ->
                when (event) {
                    is ViewEvent.Toast -> context?.showToast(text = event.message)
                    else -> Unit
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}