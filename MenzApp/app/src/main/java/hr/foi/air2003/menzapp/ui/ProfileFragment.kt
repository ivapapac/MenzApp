package hr.foi.air2003.menzapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Explode
import androidx.transition.TransitionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import hr.foi.air2003.menzapp.R
import hr.foi.air2003.menzapp.SplashScreenActivity
import hr.foi.air2003.menzapp.assistants.DateTimePicker
import hr.foi.air2003.menzapp.core.model.Feedback
import hr.foi.air2003.menzapp.core.model.Post
import hr.foi.air2003.menzapp.core.model.User
import hr.foi.air2003.menzapp.recyclerview.ProfileFeedbackRecyclerViewAdapter
import hr.foi.air2003.menzapp.recyclerview.ProfilePostRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {
    private lateinit var dateTimePicker: DateTimePicker
    private lateinit var viewModel: ProfileViewModel
    private lateinit var adapterPost: ProfilePostRecyclerViewAdapter
    private lateinit var adapterFeedback: ProfileFeedbackRecyclerViewAdapter
    private lateinit var user: User

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if(arguments != null)
            user = Gson().fromJson(arguments!!.getString("currentUser"), User::class.java)

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expandViewListener()
        createRecyclerViews()

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        dateTimePicker = DateTimePicker()

        retrieveUserData(user)

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, SplashScreenActivity::class.java)
            activity?.startActivity(intent)
        }
    }

    private fun createRecyclerViews() {
        adapterPost = ProfilePostRecyclerViewAdapter()
        adapterFeedback = ProfileFeedbackRecyclerViewAdapter()

        rvProfilePosts.hasFixedSize()
        rvProfilePosts.layoutManager = LinearLayoutManager(context)
        rvProfilePosts.itemAnimator = DefaultItemAnimator()
        rvProfilePosts.adapter = adapterPost

        rvProfileFeedbacks.hasFixedSize()
        rvProfileFeedbacks.layoutManager = LinearLayoutManager(context)
        rvProfileFeedbacks.itemAnimator = DefaultItemAnimator()
        rvProfileFeedbacks.adapter = adapterFeedback

        adapterPost.editClick = { post ->
            editPost(post)
        }
    }

    private fun expandViewListener() {
        expandableProfilePosts.visibility = View.GONE
        expandableProfileFeedbacks.visibility = View.GONE

        cvProfileMyPosts.setOnClickListener {
            if (expandableProfilePosts.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(cvProfileMyPosts, Explode())
                expandableProfilePosts.visibility = View.VISIBLE
            } else {
                TransitionManager.beginDelayedTransition(cvProfileMyPosts, Explode())
                expandableProfilePosts.visibility = View.GONE
            }
        }

        cvProfileFeedback.setOnClickListener {
            if (expandableProfileFeedbacks.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(cvProfileFeedback, Explode())
                expandableProfileFeedbacks.visibility = View.VISIBLE
            } else {
                TransitionManager.beginDelayedTransition(cvProfileFeedback, Explode())
                expandableProfileFeedbacks.visibility = View.GONE
            }
        }
    }

    private fun retrieveUserData(user: User) {
        //Populate user info with data from firestore
        createUserLayout(user)

        //Populate posts with data from firestore
        createPostLayout(user.userId)

        //Populate feedbacks with data from firestore
        createFeedbackLayout(user.userId)
    }

    private fun createFeedbackLayout(userId: String) {
        val liveData = viewModel.getFeedbacks(userId)
        liveData.observe(viewLifecycleOwner, {
            val feedbacks: MutableList<Feedback> = mutableListOf()
            val data = it.data
            if(data != null){
                for(d in data){
                    feedbacks.add(d.item)
                }

                adapterFeedback.addItems(feedbacks)
            }
        })
    }

    private fun createPostLayout(userId: String) {
        val liveData = viewModel.getPosts(userId)
        liveData.observe(viewLifecycleOwner, {
            val posts: MutableList<Post> = mutableListOf()
            val data = it.data
            if(data != null){
                for(d in data){
                        posts.add(d.item)
                }

                adapterPost.addItems(posts)
            }
        })
    }

    private fun createUserLayout(user: User) {
        tvProfileFullName.text = user.fullName
        tvProfileAboutMe.text = user.bio

        // TODO Show user profile picture
    }

    private fun editPost(post: Post) {
        val bundle = Bundle()
        val postJson = Gson().toJson(post)
        bundle.putString("post", postJson)
        bundle.putString("user", arguments?.getString("currentUser"))

        val newPostFragment = NewPostFragment()
        newPostFragment.setTargetFragment(this, 1)
        newPostFragment.arguments = bundle
        newPostFragment.show(requireFragmentManager(), "Post")
    }
}