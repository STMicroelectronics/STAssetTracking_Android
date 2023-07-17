package com.st.assetTracking.atrBle1.sensorTileBox.communication

import com.st.BlueSTSDK.Node
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private class WaitStateListener(private val finalState: Node.State,
                                private val continuation: CancellableContinuation<Unit>) :
        Node.NodeStateListener {
    override fun onStateChange(node: Node, newState: Node.State, prevState: Node.State) {
        if (newState == finalState) {
            node.removeNodeStateListener(this)
            continuation.resume(Unit)
        }
    }

}


internal suspend fun Node.waitStatus(status: Node.State) {
    if (state == status)
        return
    //else wait
    suspendCancellableCoroutine<Unit> { cancellableContinuation ->
        val nodeListener = WaitStateListener(status, cancellableContinuation)
        addNodeStateListener(nodeListener)
        cancellableContinuation.invokeOnCancellation {
            removeNodeStateListener(nodeListener)
        }
    }
}