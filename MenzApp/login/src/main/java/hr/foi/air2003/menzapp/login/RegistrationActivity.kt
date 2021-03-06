package hr.foi.air2003.menzapp.login

import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import hr.foi.air2003.menzapp.core.model.User
import kotlinx.android.synthetic.main.registration_main.*

class RegistrationActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_main)

        auth = FirebaseAuth.getInstance()
        viewModel = LoginViewModel()

        btnCreateAccount.setOnClickListener {
            checkRegistrationInput()
        }

        txtSignIn.setOnClickListener {
            finish()
        }
    }

    private fun checkRegistrationInput() {
        if (txtEmail.text.toString().isEmpty()) {
            txtEmail.error = "Molimo unesite email"
            txtEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(txtEmail.text.toString()).matches()) {
            txtEmail.error = "Molimo unesite ispravan email"
            txtEmail.requestFocus()
            return
        }

        if (txtPassword.text.toString().isEmpty() || txtPassword.text.length < 6) {
            txtPassword.error = "Molimo unesite lozinku najmanje duljine 6 znakova"
            txtPassword.requestFocus()
            return
        }

        createAccount()
    }

    private fun createAccount() {
        auth.createUserWithEmailAndPassword(txtEmail.text.toString(), txtPassword.text.toString())
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if(user != null) {
                        viewModel.createUser(getUserInfo(user.uid))
                        user.sendEmailVerification().addOnSuccessListener {
                            updateUI(true)
                        }
                    }
                }
                .addOnFailureListener {
                    updateUI(false)
                }
    }

    private fun getUserInfo(uid: String): User {
        return User(
                userId = uid,
                fullName = txtFullName.text.toString(),
                email = txtEmail.text.toString()
        )
    }

    private fun updateUI(success: Boolean) {
        val builder = AlertDialog.Builder(this)

        if(success) {
            builder.setTitle("Uspjeh")
            builder.setMessage("Uspje??no ste se registrirali. Molimo potvrdite va??u email adresu!")
        }else{
            builder.setTitle("Gre??ka")
            builder.setMessage("Registracija nije uspjela!")
        }

        builder.setPositiveButton("U redu") { dialog, which ->
            dialog.dismiss()
            finish()
        }

        val dialog = builder.create()
        dialog.show()
    }
}