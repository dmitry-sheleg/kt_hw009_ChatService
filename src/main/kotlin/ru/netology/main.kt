package ru.netology

// Класс пользователя
data class User(
    val id: Int = 0,
    val name: String
)

// Класс сообщения
data class Message(
    val id: Int = 0,
    val sender: User,
    var text: String,
    val timestamp: Long = System.currentTimeMillis(),
    var isRead: Boolean = false
)

// Класс чата
data class Chat(
    val chatId: Int = 0,
    val withUser: User,
    val messages: MutableList<Message> = mutableListOf()
) {
    // Добавляем новое сообщение в чат
    fun addMessage(message: Message) {
        messages.add(message)
    }

    // Удаляем сообщение из чата по его ID
    fun removeMessage(messageId: Int): Boolean {
        return messages.removeIf { it.id == messageId }
    }

    // Получаем последние N сообщений из чата и автоматически помечаем их как прочитанные
    fun getLastMessages(count: Int): List<Message> {
        if (count < 0) {
            throw IllegalArgumentException("Count cannot be negative")
        }
        return messages.takeLast(count).also {
            it.forEach { message -> message.isRead = true }
        }
    }

    // Проверяем, есть ли в чате непрочитанные сообщения
    fun hasUnreadMessages(): Boolean {
        return messages.any { !it.isRead }
    }

    // Возвращаем текст последнего сообщения в чате или «нет сообщений», если сообщений нет
    fun getLastMessageText(): String {
        return messages.lastOrNull()?.text ?: "нет сообщений"
    }
}

// Сервис для управления чатами и сообщениями
class ChatService {
    // Коллекция пользователей системы
    private val users = mapOf(
        1 to User(1, "Dmitry"),
        2 to User(2, "Victor"),
        3 to User(3, "Arthur")
    )

    // Коллекция чатов: ключ — ID пользователя, с которым ведётся переписка
    private val chats = mutableMapOf<Int, Chat>()

    // Получаем количество чатов с непрочитанными сообщениями
    // используем цепочку lambda‑функций без явных циклов
    fun getUnreadChatsCount(): Int {
        return chats.values.count { it.hasUnreadMessages() }
    }

    // Получаем полный список всех чатов пользователя
    fun getChats(): List<Chat> {
        return chats.values.toList()
    }

    // Получаем список последних сообщений из всех чатов
    // Чаты сортируются по времени последнего сообщения (новые сверху)
    // Если в чате нет сообщений, возвращаем «нет сообщений»
    fun getLastMessagesFromAllChats(): List<String> {
        return chats.values
            .sortedByDescending { it.messages.lastOrNull()?.timestamp ?: 0 }
            .map { it.getLastMessageText() }
    }

    // Получаем последние сообщения из конкретного чата
    // После вызова все полученные сообщения автоматически помечаются как прочитанные
    fun getMessagesFromChat(withUserId: Int, count: Int): List<Message> {
        val chat = chats[withUserId] ?: return emptyList()
        return chat.getLastMessages(count)
    }

    // Создаём новое сообщение и добавляем его в соответствующий чат
    // Если чат с указанным пользователем не существует, он создаётся автоматически
    fun createMessage(sender: User, withUserId: Int, text: String): Message {
        if (text.isBlank()) {
            throw IllegalArgumentException("Message text cannot be blank")
        }
        if (!users.containsKey(withUserId)) {
            throw UserNotFoundException("User with ID $withUserId not found")
        }

        val message = Message(sender = sender, text = text)

        val chat = chats.getOrPut(withUserId) {
            Chat(withUser = users[withUserId]!!)
        }
        chat.addMessage(message)
        return message
    }

    // Удаляем сообщение из указанного чата
    fun deleteMessage(withUserId: Int, messageId: Int): Boolean {
        if (!chats.containsKey(withUserId)) {
            throw ChatNotFoundException("Chat with user ID $withUserId not found")
        }
        return chats[withUserId]?.removeMessage(messageId) ?: false
    }

    // Создаём новый чат с указанным пользователем
    fun createChat(withUserId: Int): Chat {
        if (!users.containsKey(withUserId)) {
            throw UserNotFoundException("User with ID $withUserId not found")
        }
        return chats.getOrPut(withUserId) {
            Chat(withUser = users[withUserId]!!)
        }
    }

    // Полностью удаляем чат и всю переписку с указанным пользователем
    fun deleteChat(withUserId: Int): Boolean {
        return if (chats.containsKey(withUserId)) {
            chats.remove(withUserId) != null
        } else {
            throw ChatNotFoundException("Chat with user ID $withUserId not found")
        }
    }

    // Собираем статистику по чатам с использованием цепочки lambda‑вызовов
    fun getChatStatistics(): Map<String, Any> {
        return chats.values.fold(mutableMapOf()) { acc, chat ->
            acc.apply {
                this["totalChats"] = chats.size
                this["chatsWithUnread"] = getUnreadChatsCount()
                this["totalMessages"] = chats.values.sumOf { it.messages.size }
                this["unreadMessages"] = chats.values.sumOf { chat ->
                    chat.messages.count { !it.isRead }
                }
            }
        }
    }
}

class UserNotFoundException(message: String) : Exception(message)
class ChatNotFoundException(message: String) : Exception(message)