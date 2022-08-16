package com.devtides.coroutinesroom.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.devtides.coroutinesroom.model.LoginState
import com.devtides.coroutinesroom.model.UserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val db by lazy { UserDatabase(getApplication()).userDao() }

    val loginComplete = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    fun login(username: String, password: String) {
        coroutineScope.launch {
            var user = db.getUser(username)

            withContext(Dispatchers.Main) {
                if (user == null) {
                    error.value = "User not found"
                } else if (user.passwordHash != password.hashCode()) {
                    error.value = "Password is incorrect"
                } else {
                    LoginState.login(user)
                    loginComplete.value = true
                }
            }
        }
    }
}