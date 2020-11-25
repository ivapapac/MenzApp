package hr.foi.air2003.menzapp.core.livedata

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.getField
import hr.foi.air2003.menzapp.core.model.Post
import hr.foi.air2003.menzapp.core.other.DataOrException

typealias PostOrException = DataOrException<Post, FirebaseFirestoreException>

class PostLiveData (private val documentReference: DocumentReference) : LiveData<PostOrException>(), EventListener<DocumentSnapshot> {
    private var listenerRegistration: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()
        listenerRegistration = documentReference.addSnapshotListener(this)
    }

    override fun onInactive() {
        super.onInactive()
        listenerRegistration?.remove()
    }

    override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        if(value != null && value.exists()){
            val model = Post(
                value.id,
                value.getField<Map<String, String>>("author")!!,
                value.getTimestamp("timestamp")!!,
                value.getString("description")!!,
                value.getField<Int>("numberOfPeople")!!,
                value.getField<List<String>>("userRequests")!!
            )

            setValue(PostOrException(model, error))
        }
        else if(error != null){
            // TODO Handle error
        }
    }
}