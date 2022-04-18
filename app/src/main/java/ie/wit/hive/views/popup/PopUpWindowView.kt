package ie.wit.hive.views.popup

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import ie.wit.hive.R
import ie.wit.hive.databinding.ActivityPopUpWindowBinding
import ie.wit.hive.models.Comments
import ie.wit.hive.models.HiveModel
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat


class PopUpWindowView : AppCompatActivity() {

    private lateinit var binding: ActivityPopUpWindowBinding
    private lateinit var presenter: PopUpWindowPresenter
    lateinit var hive : HiveModel

    private var popupText = ""
    lateinit var comments:java.util.ArrayList<String>
    private var newComment: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPopUpWindowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setSupportActionBar(binding.toolbarAdd)

        presenter = PopUpWindowPresenter(this)

        // ...

        newComment = binding.multiAutoCompleteTextView.text.toString()

        val addCommentButton: Button = findViewById(R.id.popup_window_button_add)
        addCommentButton.setOnClickListener {
            runBlocking { addComment(binding.multiAutoCompleteTextView.text.toString()) }
        }


        // Set the data
        binding.popupWindowTitle.text = "Hive ${presenter.hive.tag} Comments"
        binding.popupWindowText.text = popupText
        binding.popupWindowButton.text = "BACK"

binding.popupWindowBackground.setBackgroundColor(Color.TRANSPARENT)



        // Fade animation for the background of Popup Window
        val alpha = 100 //between 0-255
        val alphaColor = ColorUtils.setAlphaComponent(Color.parseColor("#000000"), alpha)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), Color.TRANSPARENT, alphaColor)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            binding.popupWindowBackground.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()

        // Fade animation for the Popup Window
        binding.popupWindowViewWithBorder.alpha = 0f
        binding.popupWindowViewWithBorder.animate().alpha(1f).setDuration(500).setInterpolator(
            DecelerateInterpolator()
        ).start()

        // Close the Popup Window when you press the button
        binding.popupWindowButton.setOnClickListener {
            onBackPressed()
        }

        binding.multiAutoCompleteTextView .setOnKeyListener { _, keyCode, event ->

            when {

                //Check if it is the Enter-Key,      Check if the Enter Key was pressed down
                ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) -> {


                    //perform an action here e.g. a send message button click
                    runBlocking { addComment(binding.multiAutoCompleteTextView.text.toString()) }
                    //onBackPressed()

                    //return true
                    return@setOnKeyListener true
                }
                else -> false
            }

        }

            populateComments()






    }


    override fun onBackPressed() {
        // Fade animation for the background of Popup Window when you press the back button
        val alpha = 100 // between 0-255
        val alphaColor = ColorUtils.setAlphaComponent(Color.parseColor("#000000"), alpha)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), alphaColor, Color.TRANSPARENT)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            binding.popupWindowBackground.setBackgroundColor(
                animator.animatedValue as Int
            )
        }

        // Fade animation for the Popup Window when you press the back button
        binding.popupWindowViewWithBorder.animate().alpha(0f).setDuration(500).setInterpolator(
            DecelerateInterpolator()
        ).start()

        // After animation finish, close the Activity
        colorAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                finish()
                overridePendingTransition(0, 0)
            }
        })
        colorAnimation.start()
    }
    private fun populateComments(){
        presenter.comments.forEach {
            log(it)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun log(comment:Comments) {
        val formattedMessage = SimpleDateFormat("dd/MM/YY HH:mm:ss").format(comment.dateLogged .toLong()*1000)+"\n"+comment.comment
        runOnUiThread {
            var currentLogText = binding.popupWindowText .text
            binding.popupWindowText.text.ifEmpty {
                currentLogText = "Comment History:"
            }
            binding.popupWindowText.text = "$currentLogText\n\n$formattedMessage"
            binding.logScrollView.post { binding.logScrollView.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private suspend fun addComment(comment: String){
        presenter.addComment(comment)
    }



}