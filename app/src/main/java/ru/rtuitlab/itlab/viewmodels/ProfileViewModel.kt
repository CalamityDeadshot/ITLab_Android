package ru.rtuitlab.itlab.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rtuitlab.itlab.api.Resource
import ru.rtuitlab.itlab.api.users.models.UserModel
import ru.rtuitlab.itlab.persistence.AuthStateStorage
import ru.rtuitlab.itlab.repositories.UserRepository
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
		private val userRepo: UserRepository,
		private val authStateStorage: AuthStateStorage
) : ViewModel() {

	private val _userCredentialsFlow = MutableStateFlow<Resource<UserModel>>(Resource.Loading)
	val userCredentialsFlow = _userCredentialsFlow.asStateFlow()

	init {
		loadUserCredentials()
	}

	private fun loadUserCredentials() {
		viewModelScope.launch {
			val response = withContext(Dispatchers.IO) {
				userRepo.getUser(authStateStorage.userIdFlow.first())
			}
			_userCredentialsFlow.emit(response)
		}
	}
}