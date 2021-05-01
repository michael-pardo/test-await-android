package com.mistpaag.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.Statement

@ExperimentalCoroutinesApi
class ViewModelTest{
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @get:Rule
    val testCoroutineRule = CoroutinesTestRule()

    lateinit var viewModel: ViewModel

    @RelaxedMockK
    lateinit var observable: Observer<SearchStates>

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true, relaxed = true)
        viewModel = ViewModel(testCoroutineRule.testDispatcher).apply { state.observeForever(observable) }
    }

    @Test
    fun `get todoText` () = testCoroutineRule.testDispatcher.runBlockingTest {
        var slor = slot<SearchStates>()
        var mutableList = mutableListOf<SearchStates>()
        every { observable.onChanged(capture(slor)) } answers { mutableList.add(slor.captured) }
        viewModel.getMethodsByAsync()
        advanceTimeBy(
            1000000000000000000
        )
        val result = viewModel.state.value
        assertEquals(
            SearchStates.Loading, mutableList[0]
        )
        assertEquals(
            SearchStates.AwaitText(3), mutableList[1]
        )
        assertEquals(2, mutableList.size)
        result?.let {
            assertEquals(it,SearchStates.AwaitText(3))
        }
    }
}

@ExperimentalCoroutinesApi
class TestCoroutineRule : TestRule {

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private val testCoroutineScope = TestCoroutineScope(testCoroutineDispatcher)

    override fun apply(base: Statement, description: Description?) = object : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
            Dispatchers.setMain(testCoroutineDispatcher)

            base.evaluate()

            Dispatchers.resetMain()
            testCoroutineScope.cleanupTestCoroutines()
        }
    }

    fun runBlockingTest(block: suspend TestCoroutineScope.() -> Unit) =
        testCoroutineScope.runBlockingTest { block() }

}

@ExperimentalCoroutinesApi
class CoroutinesTestRule(

) : TestWatcher() {
    val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}