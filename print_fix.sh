#!/bin/bash
sed -i '/assertTrue(result is ProjectSerializer.ProjectReadResult.Failure)/i \
        println("Result is: $result")\n\
        if (result is ProjectSerializer.ProjectReadResult.Success) println("Recipe: ${result.recipe}")' app/src/test/java/com/tharunbirla/librecuts/utils/ProjectSerializerTest.kt
