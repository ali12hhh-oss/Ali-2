package com.tharunbirla.librecuts.utils

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.tharunbirla.librecuts.models.EditOperation
import com.tharunbirla.librecuts.models.EditRecipe
import java.lang.reflect.Type

object ProjectSerializer {

    /** Bumped whenever serialized fields change meaning; absent version = legacy v1. */
    const val SCHEMA_VERSION = 2

    /**
     * The outcome of reading a saved project.
     *
     * Importers should prefer [read] so a corrupt or newer project can be reported to
     * the user without accidentally opening a project with missing edits.  The older
     * [deserialize] overloads deliberately remain available for existing callers.
     */
    sealed class ProjectReadResult {
        data class Success(val recipe: EditRecipe) : ProjectReadResult()

        data class Failure(
            val message: String,
            val cause: Throwable? = null
        ) : ProjectReadResult()
    }

    /** Thrown by the source-compatible deserialize overloads when [read] fails. */
    class ProjectReadException(message: String, cause: Throwable? = null) :
        IllegalArgumentException(message, cause)

    private val uriSerializer = object : JsonSerializer<Uri>, JsonDeserializer<Uri> {
        override fun serialize(src: Uri, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.toString())
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Uri {
            return Uri.parse(json.asString)
        }
    }

    private val baseGson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(Uri::class.java, uriSerializer)
            .create()
    }

    private val editOperationSerializer = object : JsonSerializer<EditOperation>, JsonDeserializer<EditOperation> {
        override fun serialize(src: EditOperation, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val element = baseGson.toJsonTree(src)
            if (element is JsonObject) {
                element.addProperty("type", src.javaClass.simpleName)
                element.addProperty("operationType", src.javaClass.simpleName)
            }
            return element
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): EditOperation {
            val jsonObject = json.asJsonObject
            
            var opTypeStr = jsonObject.get("operationType")?.asString
            if (opTypeStr == null) {
                val typeVal = jsonObject.get("type")?.asString
                val validClasses = setOf("Trim", "SpeedMain", "ReverseMain", "MirrorMain", "Crop", "AddText", "Merge", "MaskMain", "MuteAudio", "Transition", "MuteClip", "ColorFilter", "AddBackgroundAudio", "AddImageOverlay", "AddSubtitles", "Adjust", "CanvasBackground")
                
                if (typeVal != null && validClasses.contains(typeVal)) {
                    opTypeStr = typeVal
                } else {
                    if (jsonObject.has("durationMs") && jsonObject.has("index") && !jsonObject.has("items")) {
                        opTypeStr = "Transition"
                    } else if (jsonObject.has("colorHex") && jsonObject.has("blurRadius")) {
                        opTypeStr = "CanvasBackground"
                    } else {
                        opTypeStr = typeVal
                    }
                }
            }
            
            val type = opTypeStr ?: throw JsonParseException("Missing type property in EditOperation")
            
            if (type == "Transition") {
                val t = jsonObject.get("type")?.asString
                if (t != null && t != "Transition") {
                     jsonObject.addProperty("transitionType", t)
                } else if (!jsonObject.has("transitionType")) {
                     jsonObject.addProperty("transitionType", "fade")
                }
            } else if (type == "CanvasBackground") {
                val t = jsonObject.get("type")?.asString
                if (t != null && t != "CanvasBackground") {
                     jsonObject.addProperty("backgroundType", t)
                } else if (!jsonObject.has("backgroundType")) {
                     jsonObject.addProperty("backgroundType", "COLOR")
                }
            }
            
            val clazz = when (type) {
                "Trim" -> EditOperation.Trim::class.java
                "SpeedMain" -> EditOperation.SpeedMain::class.java
                "ReverseMain" -> EditOperation.ReverseMain::class.java
                "MirrorMain" -> EditOperation.MirrorMain::class.java
                "Crop" -> EditOperation.Crop::class.java
                "AddText" -> EditOperation.AddText::class.java
                "Merge" -> EditOperation.Merge::class.java
                "MaskMain" -> EditOperation.MaskMain::class.java
                "MuteAudio" -> EditOperation.MuteAudio::class.java
                "Transition" -> EditOperation.Transition::class.java
                "MuteClip" -> EditOperation.MuteClip::class.java
                "ColorFilter" -> EditOperation.ColorFilter::class.java
                "AddBackgroundAudio" -> EditOperation.AddBackgroundAudio::class.java
                "AddImageOverlay" -> EditOperation.AddImageOverlay::class.java
                "AddSubtitles" -> EditOperation.AddSubtitles::class.java
                "Adjust" -> EditOperation.Adjust::class.java
                "CanvasBackground" -> EditOperation.CanvasBackground::class.java
                else -> null
            }

            if (clazz == null) {
                throw JsonParseException("Unsupported EditOperation type: $type")
            }
            
            return try {
                baseGson.fromJson(jsonObject, clazz)
            } catch (e: Exception) {
                throw JsonParseException("Invalid $type EditOperation: ${e.message}", e)
            }
        }
    }

    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Uri::class.java, uriSerializer)
        .registerTypeHierarchyAdapter(EditOperation::class.java, editOperationSerializer)
        .create()
        
    fun serialize(recipe: EditRecipe): String {
        val json = gson.toJsonTree(recipe)
        if (json.isJsonObject) {
            json.asJsonObject.addProperty("schemaVersion", SCHEMA_VERSION)
        }
        return gson.toJson(json)
    }

    /**
     * Safely reads a project without discarding unknown or invalid operations.
     * A failure is returned instead of silently changing the user's edit recipe.
     */
    fun read(json: String): ProjectReadResult = readJsonElement {
        JsonParser.parseString(json)
    }

    /** Reader variant of [read], useful for Storage Access Framework imports. */
    fun read(reader: java.io.Reader): ProjectReadResult = readJsonElement {
        JsonParser.parseReader(reader)
    }

    fun deserialize(json: String): EditRecipe {
        return read(json).getOrThrow()
    }

    fun deserialize(reader: java.io.Reader): EditRecipe {
        return read(reader).getOrThrow()
    }

    private fun readJsonElement(parse: () -> JsonElement): ProjectReadResult {
        return try {
            val json = parse()
            validateRecipeJson(json)
            patchLegacyDefaults(json.asJsonObject)
            val recipe = gson.fromJson(json, EditRecipe::class.java)
                ?: throw JsonParseException("Project content is empty")
            validateOperations(recipe)
            ProjectReadResult.Success(recipe)
        } catch (e: Exception) {
            ProjectReadResult.Failure(
                message = e.message ?: "Project content could not be read",
                cause = e
            )
        }
    }

    /**
     * Gson bypasses Kotlin constructors, so fields absent from older JSON deserialize to JVM
     * defaults (0/false) instead of the declared Kotlin defaults. Re-inject the known schema
     * defaults into the JSON before binding so legacy projects load with their intended values.
     */
    private fun patchLegacyDefaults(root: JsonObject) {
        val operations = root.getAsJsonArray("operations") ?: return
        fun ensureFloat(obj: JsonObject, field: String, value: Float) {
            if (!obj.has(field) || obj.get(field).isJsonNull) obj.addProperty(field, value)
        }
        fun ensureBool(obj: JsonObject, field: String, value: Boolean) {
            if (!obj.has(field) || obj.get(field).isJsonNull) obj.addProperty(field, value)
        }
        fun ensureLong(obj: JsonObject, field: String, value: Long) {
            if (!obj.has(field) || obj.get(field).isJsonNull) obj.addProperty(field, value)
        }
        operations.forEach { raw ->
            val op = raw as? JsonObject ?: return@forEach
            when (op.get("operationType")?.asString ?: op.get("type")?.asString) {
                "AddText" -> ensureFloat(op, "opacity", 1.0f)
                "AddImageOverlay" -> {
                    ensureBool(op, "isLooping", true)
                    ensureFloat(op, "opacity", 1.0f)
                }
                "Transition" -> ensureLong(op, "durationMs", 1000L)
            }
            // MaskConfig nests inside several operation shapes; defaults are 0.5-centred.
            listOf("maskConfig", "mask").forEach { key ->
                val mask = op.get(key) as? JsonObject
                if (mask != null) {
                    ensureFloat(mask, "relativeX", 0.5f)
                    ensureFloat(mask, "relativeY", 0.5f)
                }
            }
            (op.get("items") as? com.google.gson.JsonArray)?.forEach { rawItem ->
                val item = rawItem as? JsonObject ?: return@forEach
                val mask = item.get("maskConfig") as? JsonObject
                if (mask != null) {
                    ensureFloat(mask, "relativeX", 0.5f)
                    ensureFloat(mask, "relativeY", 0.5f)
                }
            }
        }
    }

    /** Fail explicitly rather than loading a structurally impossible project. */
    private fun validateOperations(recipe: EditRecipe) {
        recipe.operations.forEach { op ->
            when (op) {
                is EditOperation.Trim -> require(op.endMs >= op.startMs && op.startMs >= 0) {
                    "Trim range invalid: ${op.startMs}..${op.endMs}"
                }
                is EditOperation.AddBackgroundAudio -> require(op.audioUri.toString().isNotBlank()) {
                    "Audio operation has no source"
                }
                else -> Unit
            }
        }
    }

    private fun validateRecipeJson(json: JsonElement) {
        if (!json.isJsonObject) throw JsonParseException("Project root must be a JSON object")
        val root = json.asJsonObject
        requireString(root, "projectName")
        requireString(root, "sourceUri")
        requireString(root, "sourceName")
        val operations = root.get("operations")
            ?: throw JsonParseException("Project is missing operations")
        if (!operations.isJsonArray) throw JsonParseException("Project operations must be an array")
    }

    private fun requireString(root: JsonObject, field: String) {
        val value = root.get(field)
        if (value == null || value.isJsonNull || !value.isJsonPrimitive || !value.asJsonPrimitive.isString || value.asString.isBlank()) {
            throw JsonParseException("Project is missing a valid $field")
        }
    }

    private fun ProjectReadResult.getOrThrow(): EditRecipe = when (this) {
        is ProjectReadResult.Success -> recipe
        is ProjectReadResult.Failure -> throw ProjectReadException(message, cause)
    }
}
