package com.example.data

import android.content.Context
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class ProjectRepository(private val context: Context) {

    private val projectsDir: File
        get() = File(context.filesDir, "projects").apply { if (!exists()) mkdirs() }

    private val sessionFile: File
        get() = File(context.filesDir, "last_session.json")

    suspend fun getAllProjects(): List<DesignProject> = withContext(Dispatchers.IO) {
        val files = projectsDir.listFiles { _, name -> name.endsWith(".json") } ?: return@withContext emptyList()
        val list = mutableListOf<DesignProject>()
        for (file in files) {
            try {
                val json = file.readText()
                val project = jsonToProject(JSONObject(json))
                list.add(project)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        list.sortedByDescending { it.updatedAt }
    }

    suspend fun getProject(id: String): DesignProject? = withContext(Dispatchers.IO) {
        val file = File(projectsDir, "$id.json")
        if (!file.exists()) return@withContext null
        try {
            val json = file.readText()
            jsonToProject(JSONObject(json))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveProject(project: DesignProject) = withContext(Dispatchers.IO) {
        val updated = project.copy(updatedAt = System.currentTimeMillis())
        val json = projectToJson(updated)
        val file = File(projectsDir, "${project.id}.json")
        file.writeText(json.toString(2))
        saveLastSessionId(project.id)
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        val file = File(projectsDir, "$id.json")
        if (file.exists()) {
            file.delete()
        }
        if (getLastSessionId() == id) {
            clearLastSession()
        }
    }

    suspend fun duplicateProject(id: String): DesignProject? = withContext(Dispatchers.IO) {
        val original = getProject(id) ?: return@withContext null
        val newId = UUID.randomUUID().toString()
        val copy = original.copy(
            id = newId,
            name = "${original.name} (نسخة)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            elements = original.elements.map { it.copy(id = UUID.randomUUID().toString()) }
        )
        saveProject(copy)
        copy
    }

    suspend fun renameProject(id: String, newName: String): DesignProject? = withContext(Dispatchers.IO) {
        val project = getProject(id) ?: return@withContext null
        val updated = project.copy(name = newName, updatedAt = System.currentTimeMillis())
        saveProject(updated)
        updated
    }

    suspend fun saveLastSessionId(projectId: String) = withContext(Dispatchers.IO) {
        try {
            sessionFile.writeText(JSONObject().put("projectId", projectId).toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getLastSessionId(): String? = withContext(Dispatchers.IO) {
        if (!sessionFile.exists()) return@withContext null
        try {
            val json = JSONObject(sessionFile.readText())
            json.optString("projectId", null)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun clearLastSession() = withContext(Dispatchers.IO) {
        if (sessionFile.exists()) {
            sessionFile.delete()
        }
    }

    // JSON Mappers
    private fun projectToJson(project: DesignProject): JSONObject {
        val json = JSONObject()
        json.put("id", project.id)
        json.put("name", project.name)
        json.put("width", project.width)
        json.put("height", project.height)
        json.put("presetName", project.presetName)
        json.put("deviceFrame", project.deviceFrame.name)
        json.put("createdAt", project.createdAt)
        json.put("updatedAt", project.updatedAt)
        project.previewThumbnailPath?.let { json.put("previewThumbnailPath", it) }

        // Background
        val bgJson = JSONObject()
        bgJson.put("type", project.background.type.name)
        bgJson.put("colorStart", project.background.colorStart)
        bgJson.put("colorEnd", project.background.colorEnd)
        bgJson.put("gradientAngle", project.background.gradientAngle.toDouble())
        project.background.imageUri?.let { bgJson.put("imageUri", it) }
        json.put("background", bgJson)

        // Elements
        val elementsArray = JSONArray()
        for (element in project.elements) {
            val elJson = JSONObject()
            elJson.put("id", element.id)
            elJson.put("type", element.type.name)
            elJson.put("name", element.name)
            elJson.put("x", element.x.toDouble())
            elJson.put("y", element.y.toDouble())
            elJson.put("width", element.width.toDouble())
            elJson.put("height", element.height.toDouble())
            elJson.put("zIndex", element.zIndex)
            elJson.put("isVisible", element.isVisible)
            elJson.put("isLocked", element.isLocked)
            elJson.put("variant", element.variant)
            elJson.put("backgroundColor", element.backgroundColor)
            element.backgroundColorSecondary?.let { elJson.put("backgroundColorSecondary", it) }
            elJson.put("textColor", element.textColor)
            elJson.put("borderColor", element.borderColor)
            elJson.put("borderWidth", element.borderWidth.toDouble())
            elJson.put("borderRadius", element.borderRadius.toDouble())
            elJson.put("opacity", element.opacity.toDouble())
            elJson.put("elevation", element.elevation.toDouble())
            elJson.put("shadowColor", element.shadowColor)
            elJson.put("text", element.text)
            elJson.put("fontSize", element.fontSize.toDouble())
            elJson.put("fontWeight", element.fontWeight)
            elJson.put("textAlign", element.textAlign)
            elJson.put("letterSpacing", element.letterSpacing.toDouble())
            element.imageUri?.let { elJson.put("imageUri", it) }
            element.iconName?.let { elJson.put("iconName", it) }
            elJson.put("scaleType", element.scaleType)
            elJson.put("isChecked", element.isChecked)
            elJson.put("progress", element.progress.toDouble())
            elJson.put("placeholder", element.placeholder)
            elJson.put("badgeCount", element.badgeCount)

            val itemsArray = JSONArray()
            for (item in element.items) {
                itemsArray.put(item)
            }
            elJson.put("items", itemsArray)

            elementsArray.put(elJson)
        }
        json.put("elements", elementsArray)

        return json
    }

    private fun jsonToProject(json: JSONObject): DesignProject {
        val id = json.getString("id")
        val name = json.getString("name")
        val width = json.optInt("width", 390)
        val height = json.optInt("height", 844)
        val presetName = json.optString("presetName", "iPhone 12 / 13 / 14")
        val deviceFrame = try {
            DeviceFrameStyle.valueOf(json.optString("deviceFrame", DeviceFrameStyle.ANDROID_MODERN.name))
        } catch (_: Exception) {
            DeviceFrameStyle.ANDROID_MODERN
        }
        val createdAt = json.optLong("createdAt", System.currentTimeMillis())
        val updatedAt = json.optLong("updatedAt", System.currentTimeMillis())
        val previewThumbnailPath = if (json.has("previewThumbnailPath")) json.getString("previewThumbnailPath") else null

        // Background
        val bgJson = json.optJSONObject("background")
        val background = if (bgJson != null) {
            val bgType = try {
                BackgroundType.valueOf(bgJson.optString("type", BackgroundType.SOLID.name))
            } catch (_: Exception) {
                BackgroundType.SOLID
            }
            CanvasBackground(
                type = bgType,
                colorStart = bgJson.optLong("colorStart", 0xFFFFFFFF),
                colorEnd = bgJson.optLong("colorEnd", 0xFFF1F5F9),
                gradientAngle = bgJson.optDouble("gradientAngle", 0.0).toFloat(),
                imageUri = if (bgJson.has("imageUri")) bgJson.getString("imageUri") else null
            )
        } else {
            CanvasBackground()
        }

        // Elements
        val elementsArray = json.optJSONArray("elements") ?: JSONArray()
        val elements = mutableListOf<CanvasElement>()
        for (i in 0 until elementsArray.length()) {
            val elJson = elementsArray.getJSONObject(i)
            val typeStr = elJson.optString("type", ElementType.SHAPE_RECTANGLE.name)
            val type = try {
                ElementType.valueOf(typeStr)
            } catch (_: Exception) {
                ElementType.SHAPE_RECTANGLE
            }

            val itemsList = mutableListOf<String>()
            val itemsArray = elJson.optJSONArray("items")
            if (itemsArray != null) {
                for (j in 0 until itemsArray.length()) {
                    itemsList.add(itemsArray.getString(j))
                }
            }

            elements.add(
                CanvasElement(
                    id = elJson.optString("id", UUID.randomUUID().toString()),
                    type = type,
                    name = elJson.optString("name", type.displayName),
                    x = elJson.optDouble("x", 0.0).toFloat(),
                    y = elJson.optDouble("y", 0.0).toFloat(),
                    width = elJson.optDouble("width", 100.0).toFloat(),
                    height = elJson.optDouble("height", 100.0).toFloat(),
                    zIndex = elJson.optInt("zIndex", i),
                    isVisible = elJson.optBoolean("isVisible", true),
                    isLocked = elJson.optBoolean("isLocked", false),
                    variant = elJson.optString("variant", "Filled"),
                    backgroundColor = elJson.optLong("backgroundColor", 0xFF4F46E5),
                    backgroundColorSecondary = if (elJson.has("backgroundColorSecondary")) elJson.getLong("backgroundColorSecondary") else null,
                    textColor = elJson.optLong("textColor", 0xFFFFFFFF),
                    borderColor = elJson.optLong("borderColor", 0xFFE2E8F0),
                    borderWidth = elJson.optDouble("borderWidth", 0.0).toFloat(),
                    borderRadius = elJson.optDouble("borderRadius", 8.0).toFloat(),
                    opacity = elJson.optDouble("opacity", 1.0).toFloat(),
                    elevation = elJson.optDouble("elevation", 0.0).toFloat(),
                    shadowColor = elJson.optLong("shadowColor", 0x40000000),
                    text = elJson.optString("text", ""),
                    fontSize = elJson.optDouble("fontSize", 16.0).toFloat(),
                    fontWeight = elJson.optString("fontWeight", "Normal"),
                    textAlign = elJson.optString("textAlign", "Center"),
                    letterSpacing = elJson.optDouble("letterSpacing", 0.0).toFloat(),
                    imageUri = if (elJson.has("imageUri")) elJson.getString("imageUri") else null,
                    iconName = if (elJson.has("iconName")) elJson.getString("iconName") else null,
                    scaleType = elJson.optString("scaleType", "Crop"),
                    isChecked = elJson.optBoolean("isChecked", false),
                    progress = elJson.optDouble("progress", 0.5).toFloat(),
                    placeholder = elJson.optString("placeholder", "اكتب هنا..."),
                    badgeCount = elJson.optString("badgeCount", "3"),
                    items = itemsList
                )
            )
        }

        return DesignProject(
            id = id,
            name = name,
            width = width,
            height = height,
            presetName = presetName,
            deviceFrame = deviceFrame,
            background = background,
            elements = elements,
            createdAt = createdAt,
            updatedAt = updatedAt,
            previewThumbnailPath = previewThumbnailPath
        )
    }
}
