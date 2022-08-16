# AndroidCoroutinesRoom

## Signup

<img width="558" alt="スクリーンショット 2022-08-16 21 33 57" src="https://user-images.githubusercontent.com/47273077/184880648-c4915828-56b5-4a9a-bf3e-e22a5a540c28.png">

### Fragment

```kt
class SignupFragment : Fragment() {

    private lateinit var viewModel: SignupViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signupBtn.setOnClickListener { onSignup(it) }
        gotoLoginBtn.setOnClickListener { onGotoLogin(it) }

        viewModel = ViewModelProviders.of(this).get(SignupViewModel::class.java)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.signupComplete.observe(this, Observer { isComplete ->
            Toast.makeText(activity, "Signup complete", Toast.LENGTH_SHORT).show()
            val action = SignupFragmentDirections.actionGoToMain()
            Navigation.findNavController(signupUsername).navigate(action)
        })

        viewModel.error.observe(this, Observer { error ->
            Toast.makeText(activity, "Error: $error", Toast.LENGTH_SHORT).show()
        })
    }

    private fun onSignup(v: View){
        val username = signupUsername.text.toString()
        val password = signupPassword.text.toString()
        val info = otherInfo.text.toString()

        if (username.isNullOrEmpty() || password.isNullOrEmpty() || info.isNullOrEmpty()) {
            Toast.makeText(activity, "Please fill all fields", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.signup(username, password, info)
        }
    }

    private fun onGotoLogin(v: View) {
        val action = SignupFragmentDirections.actionGoToLogin()
        Navigation.findNavController(v).navigate(action)
    }
}
```

### ViewModel

```kt
class SignupViewModel(application: Application) : AndroidViewModel(application) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val db by lazy { UserDatabase(getApplication()).userDao() }

    val signupComplete = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    fun signup(username: String, password: String, info: String) {
        coroutineScope.launch {
            val user = db.getUser(username)
            if (user != null) {
                withContext(Dispatchers.Main) {
                    error.value = "User already exists"
                }
            } else {
                val user = User(username, password.hashCode(), info)
                val userId = db.insertUser(user)
                user.id = userId
                LoginState.login(user)
                withContext(Dispatchers.Main) {
                    signupComplete.value = true
                }
            }
        }
    }

}
```

--------

## Main

<img width="558" alt="スクリーンショット 2022-08-16 21 34 35" src="https://user-images.githubusercontent.com/47273077/184880758-fb36008a-29a9-45c6-abac-cde83a17342b.png">

### Fragment

```kt
class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameTV.text = LoginState.user?.username
        signoutBtn.setOnClickListener { onSignout() }
        deleteUserBtn.setOnClickListener { onDelete() }

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        observeViewModel()
    }

    fun observeViewModel() {
        viewModel.signout.observe(this, Observer {
            Toast.makeText(activity, "Signed out", Toast.LENGTH_SHORT).show()
            goToSignupScreen()
        })
        viewModel.userDeleted.observe(this, Observer {
            Toast.makeText(activity, "User deleted", Toast.LENGTH_SHORT).show()
            goToSignupScreen()
        })
    }

    private fun goToSignupScreen() {
        val action = MainFragmentDirections.actionGoToSignup()
        Navigation.findNavController(usernameTV).navigate(action)
    }

    private fun onSignout() {
       viewModel.onSignout()
    }

    private fun onDelete() {
       activity?.let {
           AlertDialog.Builder(it)
               .setTitle("Delete user")
               .setMessage("Are you sure you want to delete this user")
               .setPositiveButton("Yes") {_, p1 -> viewModel.onDeleteUser() }
               .setNegativeButton("Cancel", null)
               .create()
               .show()
       }
    }

}
```

## ViewModel

```kt
class SignupViewModel(application: Application) : AndroidViewModel(application) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val db by lazy { UserDatabase(getApplication()).userDao() }

    val signupComplete = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    fun signup(username: String, password: String, info: String) {
        coroutineScope.launch {
            val user = db.getUser(username)
            if (user != null) {
                withContext(Dispatchers.Main) {
                    error.value = "User already exists"
                }
            } else {
                val user = User(username, password.hashCode(), info)
                val userId = db.insertUser(user)
                user.id = userId
                LoginState.login(user)
                withContext(Dispatchers.Main) {
                    signupComplete.value = true
                }
            }
        }
    }

}
```

--------
## Login

<img width="558" alt="スクリーンショット 2022-08-16 21 35 10" src="https://user-images.githubusercontent.com/47273077/184880867-37756651-468b-4f3c-95fd-0bc45c60add2.png">

### Fragment

```kt
class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginBtn.setOnClickListener { onLogin(it) }
        gotoSignupBtn.setOnClickListener { onGotoSignup(it) }

        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.loginComplete.observe(this, Observer { isComplete ->
            Toast.makeText(activity, "Login Completed", Toast.LENGTH_SHORT).show()
            val action = LoginFragmentDirections.actionGoToMain()
            Navigation.findNavController(loginUsername).navigate(action)
        })

        viewModel.error.observe(this, Observer { error ->
            Toast.makeText(activity, error, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onLogin(v: View) {
        val username = loginUsername.text.toString()
        val password = loginPassword.text.toString()

        if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
            Toast.makeText(activity, "Please fill all fields", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.login(username, password)
        }
    }

    private fun onGotoSignup(v: View){
        val action = SignupFragmentDirections.actionGoToMain()
        Navigation.findNavController(v).navigate(action)
    }
}
```

### ViewModel

```kt
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
```

--------


## DB

### Entity

```kt
@Entity
data class User(
    val username: String,

    @ColumnInfo(name = "password_hash")
    val passwordHash: Int,

    val info: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
```

### Dao

```kt
@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM user WHERE username = :username")
    suspend fun getUser(username: String): User

    @Query("DELETE FROM user WHERE id= :id")
    suspend fun deleteUser(id: Long)
}
```

### Database

```kt
@Database(entities = [User::class], version = 1)
abstract class UserDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var instance: UserDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            UserDatabase::class.java,
            "userdatabase"
        ).build()
    }
}
```
