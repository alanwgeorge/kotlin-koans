package i_introduction._7_Nullable_Types

import util.JavaCode

class JavaCode7 : JavaCode() {
    fun sendMessageToClient(client: Client?, message: String?, mailer: Mailer) {
        val email = client?.personalInfo?.email

        if (email != null && message != null) {
            mailer.sendMessage(email, message)
        }
    }
}
