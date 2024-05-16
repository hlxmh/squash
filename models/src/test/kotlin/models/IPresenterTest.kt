package models

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach

class TestSubscriber : ISubscriber {
    var updateCalled = false

    override fun update() {
        updateCalled = true
    }
}

class IPresenterTest : IPresenter() {

    private lateinit var privateSubscribers: MutableList<ISubscriber>

    @BeforeEach
    fun setUp() {
        val privateField = IPresenter::class.java.getDeclaredField("subscribers")
        privateField.isAccessible = true
        privateSubscribers = privateField.get(this) as MutableList<ISubscriber>
    }

    @Test
    fun testNotifySubscribers() {
        val subscriber1 = TestSubscriber()
        val subscriber2 = TestSubscriber()

        subscribe(subscriber1)
        subscribe(subscriber2)

        notifySubscribers()

        assertTrue(subscriber1.updateCalled)
        assertTrue(subscriber2.updateCalled)
    }

    @Test
    fun testSubscribe() {
        val subscriber = TestSubscriber()

        subscribe(subscriber)

        assertTrue(privateSubscribers.contains(subscriber))
    }

    @Test
    fun testUnsubscribe() {
        val subscriber = TestSubscriber()
        subscribe(subscriber)

        unsubscribe(subscriber)

        assertFalse(privateSubscribers.contains(subscriber))
    }

    @Test
    fun `unsubscribe should not affect other subscribers`() {
        val subscriber1 = TestSubscriber()
        val subscriber2 = TestSubscriber()
        subscribe(subscriber1)
        subscribe(subscriber2)

        unsubscribe(subscriber1)

        assertFalse(privateSubscribers.contains(subscriber1))
        assertTrue(privateSubscribers.contains(subscriber2))
    }
}