package com.tharunbirla.librecuts.utils

import android.net.Uri
import com.tharunbirla.librecuts.models.EditOperation
import com.tharunbirla.librecuts.models.EditRecipe
import com.tharunbirla.librecuts.models.TextPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.After
import org.junit.Before
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito

class ProjectSerializerTest {
    private lateinit var mockedUri: MockedStatic<Uri>



    @Before

    fun setUp() {

        mockedUri = mockStatic(Uri::class.java)

        mockedUri.`when`<Uri> { Uri.parse(org.mockito.ArgumentMatchers.anyString()) }.thenAnswer { invocation ->

            val arg = invocation.getArgument<String>(0)

            mockUri(arg)

        }

    }



    @After

    fun tearDown() {

        mockedUri.close()

    }

    @Test
    fun `valid project round trips with every operation retained`() {
        val sourceUri = mockUri("content://media/video/42")
        val recipe = EditRecipe(
            projectName = "Weekend edit",
            sourceUri = sourceUri,
            sourceName = "clip.mp4",
            operations = listOf(
                EditOperation.Trim(startMs = 100L, endMs = 9_000L, id = "trim"),
                EditOperation.Crop(aspectRatio = "9:16", id = "crop"),
                EditOperation.AddText(
                    text = "Hello",
                    fontSize = 32,
                    position = TextPosition.CENTER,
                    id = "text"
                ),
                EditOperation.Adjust(index = 0, brightness = 12, id = "adjust")
            )
        )

        val result = ProjectSerializer.read(ProjectSerializer.serialize(recipe))

        assertTrue((result as? ProjectSerializer.ProjectReadResult.Failure)?.message ?: "Not failure", result is ProjectSerializer.ProjectReadResult.Success)
        val restored = (result as ProjectSerializer.ProjectReadResult.Success).recipe
        assertEquals(recipe.operations, restored.operations)
    }

    @Test
    fun `unsupported operation returns explicit failure instead of dropping it`() {
        val result = ProjectSerializer.read(
            """{
                "projectName":"Future edit",
                "sourceUri":"content://media/video/42",
                "sourceName":"clip.mp4",
                "operations":[{"operationType":"FutureEffect","id":"future"}]
            }"""
        )

        println("Result is: $result")

        if (result is ProjectSerializer.ProjectReadResult.Success) println("Recipe: ${result.recipe}")
        assertTrue(result is ProjectSerializer.ProjectReadResult.Failure)
        assertTrue((result as ProjectSerializer.ProjectReadResult.Failure).message.contains("Unsupported"))
    }

    @Test
    fun `malformed project returns explicit failure`() {
        val result = ProjectSerializer.read("{ not valid json")

        println("Result is: $result")

        if (result is ProjectSerializer.ProjectReadResult.Success) println("Recipe: ${result.recipe}")
        assertTrue(result is ProjectSerializer.ProjectReadResult.Failure)
    }

    private fun mockUri(value: String): Uri = Mockito.mock(Uri::class.java).also {
        Mockito.`when`(it.toString()).thenReturn(value)
    }
}
