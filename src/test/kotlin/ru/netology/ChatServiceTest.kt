package ru.netology

import org.junit.Assert.*
import org.junit.Test

class ChatServiceTest {
    private var chatService: ChatService = ChatService()
    private var user1: User = User(1, "Dmitry")
    private var user2: User = User(2, "Victor")

    // Тесты для Chat
    @Test
    fun testAddMessageAddsToChat() {
        val chat = Chat(withUser = user1)
        val message = Message(sender = user2, text = "Hello")

        chat.addMessage(message)

        assertEquals(1, chat.messages.size)
        assertEquals("Hello", chat.messages[0].text)
    }

    @Test
    fun testRemoveMessage() {
        val chat = Chat(withUser = user1)
        val message = Message(id = 1, sender = user2, text = "To be removed")
        chat.addMessage(message)

        val result = chat.removeMessage(1)

        assertTrue(result)
        assertTrue(chat.messages.isEmpty())
    }

    @Test
    fun testGetLastMessagesAndMarksAsRead() {
        val chat = Chat(withUser = user1)
        val message1 = Message(id = 1, sender = user2, text = "Msg1")
        val message2 = Message(id = 2, sender = user2, text = "Msg2")
        chat.addMessage(message1)
        chat.addMessage(message2)

        val lastMessages = chat.getLastMessages(1)

        assertEquals(1, lastMessages.size)
        assertTrue(lastMessages[0].isRead)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetLastMessagesNegativeCountException() {
        val chat = Chat(withUser = user1)
        chat.getLastMessages(-1)
    }

    // Тесты для ChatService
    @Test
    fun testCreateMessage() {
        val message = chatService.createMessage(user1, 2, "Hello Victor!")

        assertNotNull(message)
        assertEquals("Hello Victor!", message.text)
        assertEquals(user1.id, message.sender.id)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateMessageBlankTextException() {
        chatService.createMessage(user1, 2, "")
    }

    @Test(expected = UserNotFoundException::class)
    fun testCreateMessageNonExistUserException() {
        chatService.createMessage(user1, 999, "Text")
    }

    @Test
    fun testDeleteExistMessage() {
        // Сначала создаём сообщение
        val message = chatService.createMessage(user1, 2, "To delete")
        // Затем удаляем
        val result = chatService.deleteMessage(2, message.id)

        assertTrue(result)
    }

    @Test(expected = ChatNotFoundException::class)
    fun testDeleteMessageNonExistentChatException() {
        chatService.deleteMessage(999, 1)
    }

    @Test
    fun testCreateChat() {
        val chat = chatService.createChat(3)

        assertNotNull(chat)
        assertEquals(3, chat.withUser.id)
    }

    @Test(expected = UserNotFoundException::class)
    fun testCreateChatNonExistUserException() {
        chatService.createChat(999)
    }

    @Test
    fun testDeleteExistChat() {
        // Создаём чат
        chatService.createChat(2)
        // Удаляем
        val result = chatService.deleteChat(2)

        assertTrue(result)
    }

    @Test(expected = ChatNotFoundException::class)
    fun testDeleteChatNonExistChatException() {
        chatService.deleteChat(999)
    }

    @Test
    fun testGetUnreadChatsCountNoUnread() {
        val count = chatService.getUnreadChatsCount()

        assertEquals(0, count)
    }

    @Test
    fun testGetChats() {
        // Создаём несколько чатов
        chatService.createChat(2)
        chatService.createChat(3)

        val chats = chatService.getChats()

        assertEquals(2, chats.size)
        //assertTrue(chats.any { it.withUser.id == 2 })
        assertTrue(chats.any { it.withUser.id == 3 })
    }

    @Test
    fun testGetLastMessagesFromAllChatsWithMessages() {
        // Создаём сообщения в разных чатах с разными временными метками
        val message1 = chatService.createMessage(user1, 2, "First message")
        Thread.sleep(10) // Задержка для разных временных меток
        val message2 = chatService.createMessage(user1, 3, "Second message")

        val lastMessages = chatService.getLastMessagesFromAllChats()

        // Сообщения должны быть отсортированы по времени (новые сверху)
        assertEquals("Second message", lastMessages[0])
        assertEquals("First message", lastMessages[1])
    }

    @Test
    fun testGetMessagesFromExistChat() {
        // Создаём чат и добавляем несколько сообщений
        chatService.createChat(2)
        chatService.createMessage(user1, 2, "Message 1")
        chatService.createMessage(user1, 2, "Message 2")
        chatService.createMessage(user1, 2, "Message 3")

        val messages = chatService.getMessagesFromChat(2, 2)

        assertEquals(2, messages.size)
        assertEquals("Message 2", messages[0].text)
        assertEquals("Message 3", messages[1].text)
        // Проверяем, что сообщения помечены как прочитанные
        assertTrue(messages.all { it.isRead })
    }

    @Test
    fun testGetMessagesFromNonExistentChat() {
        val messages = chatService.getMessagesFromChat(999, 5)

        assertTrue(messages.isEmpty())
    }

    @Test
    fun testHasUnreadMessagesWithUnreadMessages() {
        val chat = Chat(withUser = user1)
        val unreadMessage = Message(sender = user2, text = "Unread message")
        chat.addMessage(unreadMessage)

        assertTrue(chat.hasUnreadMessages())
    }

    @Test
    fun testHasUnreadMessagesAllRead() {
        val chat = Chat(withUser = user1)
        val readMessage = Message(sender = user2, text = "Read message").apply { isRead = true }
        chat.addMessage(readMessage)

        assertFalse(chat.hasUnreadMessages())
    }

    @Test
    fun testGetLastMessageTextWithMessages() {
        val chat = Chat(withUser = user1)
        chat.addMessage(Message(sender = user2, text = "First"))
        chat.addMessage(Message(sender = user2, text = "Last"))

        assertEquals("Last", chat.getLastMessageText())
    }

    @Test
    fun testGetLastMessageTextEmptyChat() {
        val chat = Chat(withUser = user1)

        assertEquals("нет сообщений", chat.getLastMessageText())
    }

    @Test
    fun testGetChatStatistics() {
        // Создаём чаты и сообщения для статистики
        chatService.createChat(2)
        chatService.createMessage(user1, 2, "Hello")
        chatService.createMessage(user1, 2, "How are you?")

        chatService.createChat(3)
        chatService.createMessage(user1, 3, "Hi there")

        val statistics = chatService.getChatStatistics()

        assertEquals(2, statistics["totalChats"])
        assertEquals(2, statistics["chatsWithUnread"]) // Все сообщения изначально непрочитанные
        assertEquals(3, statistics["totalMessages"])
        assertEquals(3, statistics["unreadMessages"])
    }

    @Test
    fun testGetUnreadChatsCount() {
        // Создаём чаты с непрочитанными сообщениями
        chatService.createChat(2)
        chatService.createMessage(user1, 2, "Unread in chat 2")

        chatService.createChat(3)
        chatService.createMessage(user1, 3, "Unread in chat 3")

        val count = chatService.getUnreadChatsCount()

        assertEquals(2, count)
    }

    @Test
    fun testGetUnreadChatsCountAllRead() {
        // Создаём чат и читаем все сообщения
        chatService.createChat(2)
        val message = chatService.createMessage(user1, 2, "Read message")
        chatService.getMessagesFromChat(2, 1) // Помечаем как прочитанное

        val count = chatService.getUnreadChatsCount()

        assertEquals(0, count)
    }
}