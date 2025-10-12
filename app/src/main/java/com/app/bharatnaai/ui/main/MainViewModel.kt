package com.app.bharatnaai.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.bharatnaai.data.model.CustomerDetails
import com.app.bharatnaai.data.repository.ApiResult
import com.app.bharatnaai.data.repository.CustomerDetailsRepo
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val customerDetailsRepo = CustomerDetailsRepo(application.applicationContext)

    private val _customerDetailsState = MutableLiveData<ApiResult<CustomerDetails>>()
    val customerDetailsState: LiveData<ApiResult<CustomerDetails>> = _customerDetailsState

    fun fetchCustomerDetails() {
        viewModelScope.launch {
            _customerDetailsState.value = ApiResult.Loading(true)
            val result = customerDetailsRepo.getCustomerDetails()
            _customerDetailsState.value = result
        }
    }
}
