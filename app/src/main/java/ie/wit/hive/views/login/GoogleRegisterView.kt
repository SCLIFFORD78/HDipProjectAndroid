package ie.wit.hive.views.login

import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import ie.wit.hive.R
import ie.wit.hive.databinding.ActivityGoogleRegisterBinding
import ie.wit.hive.databinding.ActivityRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class GoogleRegisterView : AppCompatActivity() {
    lateinit var presenter: GoogleRegisterPresenter
    private lateinit var binding: ActivityGoogleRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        presenter = GoogleRegisterPresenter(this)
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarAdd)

        binding.progressBar.visibility = View.GONE
        binding.signUp.setOnClickListener {
            val userName = binding.registerUsername.text.toString()
            val firstName = binding.registerFirstname.text.toString()
            val secondName = binding.registerSecondname.text.toString()
            if (userName == "" || firstName == "" || secondName == "") {
                showSnackBar("please provide all details")
            } else {
                runBlocking { presenter.doSignUp(userName, firstName, secondName) }
            }
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_cancel, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.back -> {
                runBlocking { presenter.doLogout() }
            }


        }
        return super.onOptionsItemSelected(item)
    }


    fun showSnackBar(message: CharSequence) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .show()
    }

    fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }
}