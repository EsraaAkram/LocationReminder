package com.udacity.project4.views.frags

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.views.activities.auth.AuthAct
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.RemindersListFragBinding
import com.udacity.project4.views.adapters.RemindersListAdapter
import com.udacity.project4.viewModels.RemindersListViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import com.firebase.ui.auth.AuthUI
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemindersListFrag : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private var binding: RemindersListFragBinding?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.reminders_list_frag,
            container,
            false)
        binding?.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))


        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.lifecycleOwner = this

        setAllViewModelObservers()

        setupRecyclerView()

        itemsClicks()

    }

    private fun setAllViewModelObservers() {
        //authenticationState OBSERVER WILL HANDLE SIGN OUT:
//        _viewModel.authState.observe(viewLifecycleOwner) { authenticationState ->
//            when (authenticationState) {
//                AuthState.AUTHENTICATED -> {
//
//
//                }else -> {//LOG OUT PRESSED
//                    startActivity(Intent(requireActivity(), AuthAct::class.java))
//                    requireActivity().finish()
//
//
//                }
//            }
//        }

    }

    private fun itemsClicks() {

        binding?.refreshLayout?.setOnRefreshListener {
            _viewModel.loadReminders()
        }

        binding?.addReminderFAB?.setOnClickListener {
            navigateToAddReminder()
        }

    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                RemindersListFragDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {selectedReminder->


        }

        binding?.reminderssRecyclerView?.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {

                _viewModel.deleteAllReminders()// I THINK IT'S BETTER BECAUSE IT DOES NOT MAKE ANY SENSE TO ME TO NOTIFY USER IF HE IS LOGGED OUT
                AuthUI.getInstance().signOut(requireContext())

                startActivity(Intent(requireActivity(), AuthAct::class.java))
                requireActivity().finish()


            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.main_menu, menu)
    }

}
