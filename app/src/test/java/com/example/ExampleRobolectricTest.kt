package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.GameEngine2048
import com.example.model.BoardSize
import com.example.model.Direction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("2048 Online", appName)
    }

    @Test
    fun `test 2048 game engine initialization and moves`() {
        val engine = GameEngine2048(BoardSize.SIZE_4X4)
        assertEquals(2, engine.tiles.size)
        assertEquals(0, engine.score)

        // Perform multiple consecutive moves
        val result1 = engine.move(Direction.DOWN)
        assertNotNull(result1)

        val result2 = engine.move(Direction.RIGHT)
        assertNotNull(result2)

        val result3 = engine.move(Direction.UP)
        assertNotNull(result3)

        val result4 = engine.move(Direction.LEFT)
        assertNotNull(result4)

        assertTrue(engine.tiles.size >= 2)
    }

    @Test
    fun `test sliding without merging detects move`() {
        val engine = GameEngine2048(BoardSize.SIZE_4X4)
        // Reset and manually test moves
        var successMoves = 0
        val directions = listOf(Direction.LEFT, Direction.DOWN, Direction.RIGHT, Direction.UP)
        for (dir in directions) {
            val res = engine.move(dir)
            if (res.moved) {
                successMoves++
            }
        }
        assertTrue("At least two different directions must move the tiles", successMoves >= 2)
    }
}
