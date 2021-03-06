package hr.foi.air2003.menzapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import hr.foi.air2003.menzapp.R
import hr.foi.air2003.menzapp.activities.MainActivity
import hr.foi.air2003.menzapp.assistants.SharedViewModel
import hr.foi.air2003.menzapp.core.model.*
import hr.foi.air2003.menzapp.recyclerview.NotificationRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_dialog_notifications.*
import kotlinx.android.synthetic.main.fragment_dialog_notifications.btnNotifications

class NotificationFragment : Fragment() {

    private lateinit var adapterNotification: NotificationRecyclerViewAdapter
    private lateinit var user: User
    private val viewModel = SharedViewModel()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        user = (activity as MainActivity).getCurrentUser()
        return inflater.inflate(R.layout.fragment_dialog_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createRecyclerViews()

        createNotificationLayout(user.userId)

        btnNotifications.setOnClickListener {
            val profileFragment = ProfileFragment()
            profileFragment.setTargetFragment(this, 1)
            (activity as MainActivity).setCurrentFragment(profileFragment)
        }
    }

    private fun createRecyclerViews() {
        adapterNotification = NotificationRecyclerViewAdapter()

        rvNotifications.hasFixedSize()
        rvNotifications.layoutManager = LinearLayoutManager(context)
        rvNotifications.itemAnimator = DefaultItemAnimator()
        rvNotifications.adapter = adapterNotification

        adapterNotification.confirmClick = { notification ->
            addUserToChat(notification)
        }

        adapterNotification.deleteClick = { notification ->
            deleteRequest(notification)
            rvNotifications.adapter?.notifyDataSetChanged()
        }
    }

    private fun createNotificationLayout(userId: String) {
        val liveData = viewModel.getAllNotifications(userId)
        liveData.observe(viewLifecycleOwner, {
            val data = it.data
            if(data != null){
                val notifications = data.sortedByDescending { notification -> notification.timestamp }
                adapterNotification.addItems(notifications)
            }
        })
    }

    private fun createChat(notification: Notification) {
        val chat = Chat(
                lastMessage = "Initial message",
                participantsId = listOf(notification.authorId, user.userId),
                postId = notification.postId
        )

        viewModel.createChat(chat)
    }

    private fun addUserToChat(notification: Notification) {
        val liveData = viewModel.getChatByPostId(notification.postId)
        liveData.observe(viewLifecycleOwner, {
            val data = it.data
            if (!data.isNullOrEmpty()) {
                for (d in data) {
                    val users = d.participantsId as MutableList
                    users.add(notification.authorId)
                    d.participantsId = users
                    viewModel.updateChat(d)
                }
            } else {
                createChat(notification)
            }

            confirmRequest(notification)
            (activity as MainActivity).setCurrentFragment(ChatFragment())
        })
    }

    private fun confirmRequest(notification: Notification) {
        notification.request = false
        viewModel.updateNotification(notification)

        val newNotification = Notification(
                authorId = user.userId,
                content = "Prijava na zahtjev je prihva??ena",
                request = false,
                postId = notification.postId,
                recipientsId = listOf(notification.authorId)
        )

        viewModel.createNotification(newNotification)
    }

    private fun deleteRequest(notification: Notification) {
        notification.request = false
        viewModel.updateNotification(notification)
        rvNotifications.adapter?.notifyDataSetChanged()

        val liveData = viewModel.getPost(notification.postId)
        liveData.observe(viewLifecycleOwner, {
            val post = it.data
            if (post != null) {
                for(map in post.userRequests){
                    if(map.containsValue(notification.authorId)){
                        val requests: MutableList<Map<String,Any>> = mutableListOf()
                        requests.addAll(0, post.userRequests)
                        requests.remove(map)
                        post.userRequests = requests
                    }
                }

                viewModel.updatePost(post)
            }
        })
    }
}