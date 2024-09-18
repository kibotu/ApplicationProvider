package com.github.florent37.application.provider

import android.app.Activity
import android.app.Application
import android.os.Bundle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue


interface ActivityCreatedListener {
    fun onActivityCreated(activity: Activity)
}

interface ActivityResumedListener {
    fun onActivityResumed(activity: Activity)
}

interface ActivityPausedListener {
    fun onActivityPaused(activity: Activity)
}

interface ActivityDestroyedListener {
    fun onActivityDestroyed(activity: Activity)
}

interface ActivityStoppedListener {
    fun onActivityStopped(activity: Activity)
}

interface ActivityStartedListener {
    fun onActivityStarted(activity: Activity)
}

enum class ActivityState {
    CREATE,
    START,
    RESUME,
    PAUSE,
    STOP,
    DESTROY
}

class ActivityAndState(activity: Activity, val state: ActivityState) {
    /**
     *the address
     */
    val name: String = (activity as Any).toString()
    val activity: WeakReference<Activity> = WeakReference(activity)
}

object ActivityProvider {

    private val activityCreatedListeners = ConcurrentLinkedQueue<ActivityCreatedListener>()

    private val activityResumedListeners = ConcurrentLinkedQueue<ActivityResumedListener>()

    private val activityPausedListeners = ConcurrentLinkedQueue<ActivityPausedListener>()

    private val activityStoppedListeners = ConcurrentLinkedQueue<ActivityStoppedListener>()

    private val activityStartedListeners = ConcurrentLinkedQueue<ActivityStartedListener>()

    private val activityDestroyedListeners = ConcurrentLinkedQueue<ActivityDestroyedListener>()

    @JvmStatic
    fun addCreatedListener(listener: ActivityCreatedListener) {
        activityCreatedListeners.add(listener)
    }

    @JvmStatic
    fun removeCreatedListener(listener: ActivityCreatedListener) {
        activityCreatedListeners.remove(listener)
    }

    @JvmStatic
    fun addResumedListener(listener: ActivityResumedListener) {
        activityResumedListeners.add(listener)
    }

    @JvmStatic
    fun removeResumedListener(listener: ActivityResumedListener) {
        activityResumedListeners.remove(listener)
    }

    @JvmStatic
    fun addPausedListener(listener: ActivityPausedListener) {
        activityPausedListeners.add(listener)
    }

    @JvmStatic
    fun removePausedListener(listener: ActivityPausedListener) {
        activityPausedListeners.remove(listener)
    }

    @JvmStatic
    fun addDestroyedListener(listener: ActivityDestroyedListener) {
        activityDestroyedListeners.add(listener)
    }

    @JvmStatic
    fun removeDestroyedListener(listener: ActivityDestroyedListener) {
        activityDestroyedListeners.remove(listener)
    }

    @JvmStatic
    fun addStoppedListener(listener: ActivityStoppedListener) {
        activityStoppedListeners.add(listener)
    }

    @JvmStatic
    fun removeStoppedListener(listener: ActivityStoppedListener) {
        activityStoppedListeners.remove(listener)
    }

    @JvmStatic
    fun addStartedListener(listener: ActivityStartedListener) {
        activityStartedListeners.add(listener)
    }

    @JvmStatic
    fun removeStartedListener(listener: ActivityStartedListener) {
        activityStartedListeners.remove(listener)
    }

    internal fun pingResumedListeners(activity: Activity) {
        _activitiesState.tryEmit(ActivityAndState(activity, ActivityState.RESUME))
        offerIfDiffer(activity)
        activityResumedListeners.forEach {
            it.onActivityResumed(activity)
        }
    }

    internal fun pingPausedListeners(activity: Activity) {
        _activitiesState.tryEmit(ActivityAndState(activity, ActivityState.PAUSE))
        activityPausedListeners.forEach {
            it.onActivityPaused(activity)
        }
    }

    internal fun pingCreatedListeners(activity: Activity) {
        offerIfDiffer(activity)
        _activitiesState.tryEmit(ActivityAndState(activity, ActivityState.CREATE))
        activityCreatedListeners.forEach {
            it.onActivityCreated(activity)
        }
    }

    internal fun pingDestroyedListeners(activity: Activity) {
        _activitiesState.tryEmit(ActivityAndState(activity, ActivityState.DESTROY))
        activityDestroyedListeners.forEach {
            it.onActivityDestroyed(activity)
        }
    }

    internal fun pingStartedListeners(activity: Activity) {
        _activitiesState.tryEmit(ActivityAndState(activity, ActivityState.START))
        activityStartedListeners.forEach {
            it.onActivityStarted(activity)
        }
    }

    internal fun pingStoppedListeners(activity: Activity) {
        _activitiesState.tryEmit(ActivityAndState(activity, ActivityState.STOP))
        activityStoppedListeners.forEach {
            it.onActivityStopped(activity)
        }
    }

    private fun offerIfDiffer(newActivity: Activity) {
        val current = currentActivity
        if (current == null || current != newActivity) {
            _currentActivity.tryEmit(WeakReference(newActivity))
        }
    }

    internal val _currentActivity = MutableStateFlow<WeakReference<Activity?>>(WeakReference(null))

    val listenCurrentActivity: Flow<Activity> = _currentActivity.mapNotNull { it.get() }
    suspend fun activity(): Activity = listenCurrentActivity.first()

    @JvmStatic
    val currentActivity: Activity?
        get() = _currentActivity.value.get()


    internal val _activitiesState = MutableStateFlow<ActivityAndState?>(null)
    val listenActivitiesState: Flow<ActivityAndState?> = _activitiesState

    fun listenCreated(): Flow<Activity> = callbackFlow {
        val listener =
            object : ActivityCreatedListener { // implementation of some callback interface
                override fun onActivityCreated(activity: Activity) {
                    trySend(activity)
                }
            }
        addCreatedListener(listener)
        // Suspend until either onCompleted or external cancellation are invoked
        awaitClose { removeCreatedListener(listener) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun listenStarted(): Flow<Activity> = callbackFlow {
        val listener =
            object : ActivityStartedListener { // implementation of some callback interface
                override fun onActivityStarted(activity: Activity) {
                    trySend(activity)
                }
            }
        addStartedListener(listener)
        // Suspend until either onCompleted or external cancellation are invoked
        awaitClose { removeStartedListener(listener) }
    }

    fun listenResumed(): Flow<Activity> = callbackFlow {
        val listener =
            object : ActivityResumedListener { // implementation of some callback interface
                override fun onActivityResumed(activity: Activity) {
                    trySend(activity)
                }
            }
        addResumedListener(listener)
        // Suspend until either onCompleted or external cancellation are invoked
        awaitClose { removeResumedListener(listener) }
    }

    fun listenDestroyed(): Flow<Activity> = callbackFlow {
        val listener =
            object : ActivityDestroyedListener { // implementation of some callback interface
                override fun onActivityDestroyed(activity: Activity) {
                    trySend(activity)
                }
            }
        addDestroyedListener(listener)
        // Suspend until either onCompleted or external cancellation are invoked
        awaitClose { removeDestroyedListener(listener) }
    }

    fun listenStopped(): Flow<Activity> = callbackFlow {
        val listener =
            object : ActivityStoppedListener { // implementation of some callback interface
                override fun onActivityStopped(activity: Activity) {
                    trySend(activity)
                }
            }
        addStoppedListener(listener)
        // Suspend until either onCompleted or external cancellation are invoked
        awaitClose { removeStoppedListener(listener) }
    }

    fun listenPaused(): Flow<Activity> = callbackFlow {
        val listener =
            object : ActivityPausedListener { // implementation of some callback interface
                override fun onActivityPaused(activity: Activity) {
                    trySend(activity)
                }
            }
        addPausedListener(listener)
        // Suspend until either onCompleted or external cancellation are invoked
        awaitClose { removePausedListener(listener) }
    }


    fun listenActivityChanged() = listenActivitiesState
        .filter { it?.state == ActivityState.RESUME }
        .distinctUntilChangedBy { it?.name }
}

class LastActivityProvider : EmptyProvider() {
    override fun onCreate(): Boolean {
        ApplicationProvider.listen { application ->
            application.registerActivityLifecycleCallbacks(object :
                Application.ActivityLifecycleCallbacks {

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    ActivityProvider.pingCreatedListeners(activity)
                }

                override fun onActivityResumed(activity: Activity) {
                    ActivityProvider.pingResumedListeners(activity)
                }

                override fun onActivityPaused(activity: Activity) {
                    ActivityProvider.pingPausedListeners(activity)
                }

                override fun onActivityDestroyed(activity: Activity) {
                    ActivityProvider.pingDestroyedListeners(activity)
                }


                override fun onActivityStarted(activity: Activity) {
                    ActivityProvider.pingStartedListeners(activity)
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                }

                override fun onActivityStopped(activity: Activity) {
                    ActivityProvider.pingStoppedListeners(activity)
                }

            })
        }
        return true
    }
}