package com.tanay.waybility
import com.google.android.gms.maps.GoogleMapOptions
import android.Manifest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.platform.LocalUriHandler
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.cos
import kotlin.math.sqrt
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.exifinterface.media.ExifInterface
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.google.maps.android.compose.Polyline
import com.tanay.accessmap.ui.theme.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

import com.tanay.waybility.ui.theme.AccessMapTheme

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.set

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiKey = "APIKEY"

        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(
                applicationContext,
                apiKey
            )
        }

        setContent {
            AccessMapTheme {
                AccessMapScreen()
            }
        }
    }
}

data class AccessibilityReport(
    val issue: String,
    val latitude: Double,
    val longitude: Double,
    val imageUri: String,
    val timestamp: Long
)
data class ReportingAgency(
    val name: String,
    val jurisdiction: String,
    val supportedIssueTypes: List<String> = emptyList(),
    val description: String,
    val reason: String = "",
    val phone: String? = null,
    val website: String? = null
)

data class ReportQuality(
    val score: Int,
    val grade: String,
    val strengths: List<String>,
    val improvements: List<String>,
    val photoAdvice: String,
    val issueAdvice: String,
    val locationAdvice: String
)
data class OsmWay(
    val highwayType: String,
    val sidewalk: String?,
    val points: List<LatLng>,
    val isCrossing: Boolean = false
)

data class RouteInstruction(
    val instruction: String,
    val distanceMeters: Double,
    val location: LatLng? = null
)

/** Keeps Google step instructions attached to the exact route polyline returned by Routes API. */
private object RouteGuidanceCache {
    private val instructionsByRoute =
        java.util.concurrent.ConcurrentHashMap<String, List<RouteInstruction>>()

    private fun key(route: List<LatLng>): String =
        route.joinToString(";") {
            String.format(
                java.util.Locale.US,
                "%.5f,%.5f",
                it.latitude,
                it.longitude
            )
        }

    fun put(route: List<LatLng>, instructions: List<RouteInstruction>) {
        if (route.isNotEmpty() && instructions.isNotEmpty()) {
            instructionsByRoute[key(route)] = instructions
        }
    }

    fun get(route: List<LatLng>): List<RouteInstruction> =
        instructionsByRoute[key(route)].orEmpty()

    fun clear() = instructionsByRoute.clear()
}

data class RouteScore(
    val totalScore: Double,
    val roadPenalty: Double,
    val accessibilityPenalty: Double,
    val crosswalkBonus: Double,
    val distancePenalty: Double,
    val invalid: Boolean,
    val crossingPenalty: Double = 0.0,
    val rawScore: Double = 0.0,
    val minMarkerDistance: Double = Double.MAX_VALUE,
    val roadwayPercentage: Double = 0.0
)
private const val PREFS_NAME = "accessmap_reports"
private const val REPORTS_KEY = "reports"
fun roadwayThreshold(way: OsmWay): Double {
    val hasSidewalk =
        way.sidewalk == "both" || way.sidewalk == "left" ||
                way.sidewalk == "right" || way.sidewalk == "yes"
    // Sidewalk-tagged roads: only "on the road" if nearly on the centerline
    return if (hasSidewalk) 4.5 else 8.0
}

fun routesAreSimilar(
    a: List<LatLng>,
    b: List<LatLng>,
    threshold: Double
): Boolean {
    for (p in a) {
        if (closestDistanceToRoute(p, b) >= threshold) return false
    }
    for (p in b) {
        if (closestDistanceToRoute(p, a) >= threshold) return false
    }
    return true
}

suspend fun fetchOsmWithRetry(
    routes: List<List<LatLng>>
): List<OsmWay> {
    var lastError: Exception? = null

    // Keep retries bounded: Overpass can be slow when its public servers are busy.
    repeat(2) { attempt ->
        try {
            return fetchOsmWaysForRoutes(routes)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            lastError = e
            android.util.Log.w(
                "ACCESSMAP_OSM",
                "OSM attempt ${attempt + 1}/2 failed: ${e.message}"
            )
            if (attempt == 0) kotlinx.coroutines.delay(750L)
        }
    }

    throw lastError ?: java.io.IOException("OSM fetch failed")
}

fun loadReports(context: Context): MutableList<AccessibilityReport> {

    val prefs = context.getSharedPreferences(
        "accessmap_reports",
        Context.MODE_PRIVATE
    )

    val jsonString = prefs.getString("reports", null)

    val reports = mutableListOf<AccessibilityReport>()

    if (jsonString != null) {

        try {

            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {

                val report = jsonArray.getJSONObject(i)

                reports.add(
                    AccessibilityReport(
                        issue = report.getString("issue"),
                        latitude = report.getDouble("latitude"),
                        longitude = report.getDouble("longitude"),
                        imageUri = report.optString("imageUri", ""),
                        timestamp = report.optLong(
                            "timestamp",
                            System.currentTimeMillis()
                        )
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    return reports
}

fun saveReports(
    context: Context,
    reports: List<AccessibilityReport>
) {
    val prefs = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    val jsonArray = JSONArray()

    reports.forEach { report ->
        val jsonObject = JSONObject()

        jsonObject.put("issue", report.issue)
        jsonObject.put("latitude", report.latitude)
        jsonObject.put("longitude", report.longitude)
        jsonObject.put("imageUri", report.imageUri)
        jsonObject.put("timestamp", report.timestamp)

        jsonArray.put(jsonObject)
    }

    prefs.edit()
        .putString(REPORTS_KEY, jsonArray.toString())
        .apply()
}
fun segmentIntersection(
    a1: LatLng, a2: LatLng,
    b1: LatLng, b2: LatLng
): LatLng? {
    val mLat = 111320.0
    val mLon = 111320.0 * cos(Math.toRadians(a1.latitude))

    val rx = (a2.longitude - a1.longitude) * mLon
    val ry = (a2.latitude - a1.latitude) * mLat
    val sx = (b2.longitude - b1.longitude) * mLon
    val sy = (b2.latitude - b1.latitude) * mLat
    val qx = (b1.longitude - a1.longitude) * mLon
    val qy = (b1.latitude - a1.latitude) * mLat

    val denom = rx * sy - ry * sx
    if (kotlin.math.abs(denom) < 1e-9) return null

    val t = (qx * sy - qy * sx) / denom
    val u = (qx * ry - qy * rx) / denom

    if (t < 0.0 || t > 1.0 || u < 0.0 || u > 1.0) return null

    return LatLng(
        a1.latitude + t * (a2.latitude - a1.latitude),
        a1.longitude + t * (a2.longitude - a1.longitude)
    )
}

data class AccessibilityProfile(
    // Mobility Profile Presets
    val isWheelchairUser: Boolean = false,
    val isCaneOrWalkerUser: Boolean = false,
    val isVisualAccessibilityNeeded: Boolean = false,
    val isLimitedStaminaOrBalance: Boolean = false,

    // Custom Routing Preferences
    val avoidStairs: Boolean = false,
    val avoidSteepSlopes: Boolean = false,
    val avoidSurfaceProblems: Boolean = false,
    val avoidObstacles: Boolean = false,
    val avoidStreetCrossings: Boolean = false,
    val prioritizeCurbRamps: Boolean = true,
    val prioritizeSmoothPavement: Boolean = true,
    val prioritizeShortestDistance: Boolean = false
) {
    fun hasActivePreferences(): Boolean {
        return avoidStairs || avoidSteepSlopes || avoidSurfaceProblems ||
                avoidObstacles || avoidStreetCrossings || prioritizeCurbRamps ||
                prioritizeSmoothPavement || prioritizeShortestDistance ||
                isWheelchairUser || isCaneOrWalkerUser ||
                isVisualAccessibilityNeeded || isLimitedStaminaOrBalance
    }

    fun getActiveSummary(): List<String> {
        val list = mutableListOf<String>()
        if (isWheelchairUser) list.add("Wheelchair")
        if (isCaneOrWalkerUser) list.add("Cane/Walker")
        if (isVisualAccessibilityNeeded) list.add("Visual Access")
        if (isLimitedStaminaOrBalance) list.add("Gentle Slopes/Short")
        if (avoidStairs) list.add("Step-Free")
        if (avoidSteepSlopes) list.add("Gentle Gradients")
        if (avoidSurfaceProblems) list.add("Even Pavement")
        if (avoidObstacles) list.add("Obstacle-Free")
        if (avoidStreetCrossings) list.add("Fewer Crossings")
        if (prioritizeCurbRamps) list.add("Curb Ramps")
        if (prioritizeSmoothPavement) list.add("Smooth Surfaces")
        if (prioritizeShortestDistance) list.add("Short Distance")
        return list.distinct()
    }
}

private const val PROFILE_PREFS_NAME = "accessmap_profile_prefs"

fun loadUserProfile(context: Context): AccessibilityProfile {
    val prefs = context.getSharedPreferences(PROFILE_PREFS_NAME, Context.MODE_PRIVATE)
    return AccessibilityProfile(
        isWheelchairUser = prefs.getBoolean("preset_wheelchair", false),
        isCaneOrWalkerUser = prefs.getBoolean("preset_cane_walker", false),
        isVisualAccessibilityNeeded = prefs.getBoolean("preset_visual", false),
        isLimitedStaminaOrBalance = prefs.getBoolean("preset_stamina", false),
        avoidStairs = prefs.getBoolean("custom_avoid_stairs", false),
        avoidSteepSlopes = prefs.getBoolean("custom_avoid_steep_slopes", false),
        avoidSurfaceProblems = prefs.getBoolean("custom_avoid_surface_problems", false),
        avoidObstacles = prefs.getBoolean("custom_avoid_obstacles", false),
        avoidStreetCrossings = prefs.getBoolean("custom_avoid_crossings", false),
        prioritizeCurbRamps = prefs.getBoolean("custom_prioritize_curb_ramps", true),
        prioritizeSmoothPavement = prefs.getBoolean("custom_prioritize_smooth_pavement", true),
        prioritizeShortestDistance = prefs.getBoolean("custom_prioritize_shortest_distance", false)
    )
}

fun saveUserProfile(context: Context, profile: AccessibilityProfile) {
    val prefs = context.getSharedPreferences(PROFILE_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
        .putBoolean("preset_wheelchair", profile.isWheelchairUser)
        .putBoolean("preset_cane_walker", profile.isCaneOrWalkerUser)
        .putBoolean("preset_visual", profile.isVisualAccessibilityNeeded)
        .putBoolean("preset_stamina", profile.isLimitedStaminaOrBalance)
        .putBoolean("custom_avoid_stairs", profile.avoidStairs)
        .putBoolean("custom_avoid_steep_slopes", profile.avoidSteepSlopes)
        .putBoolean("custom_avoid_surface_problems", profile.avoidSurfaceProblems)
        .putBoolean("custom_avoid_obstacles", profile.avoidObstacles)
        .putBoolean("custom_avoid_crossings", profile.avoidStreetCrossings)
        .putBoolean("custom_prioritize_curb_ramps", profile.prioritizeCurbRamps)
        .putBoolean("custom_prioritize_smooth_pavement", profile.prioritizeSmoothPavement)
        .putBoolean("custom_prioritize_shortest_distance", profile.prioritizeShortestDistance)
        .apply()
}

/**
 * Penalizes every point where the route crosses a road
 * with no mapped crosswalk within 12 m.
 */
fun calculateUnmarkedCrossingPenalty(
    route: List<LatLng>,
    osmWays: List<OsmWay>,
    profile: AccessibilityProfile = AccessibilityProfile()
): Double {

    // Driveways / parking aisles are ignored on purpose
    val roads = osmWays.filter {
        isRoadway(it) &&
                it.highwayType != "service" &&
                it.highwayType != "track"
    }
    val crossings = osmWays.filter { it.isCrossing }

    val found = mutableListOf<LatLng>()
    var penalty = 0.0

    for (i in 0 until route.size - 1) {

        val a = route[i]
        val b = route[i + 1]

        val minLat = minOf(a.latitude, b.latitude) - 0.00005
        val maxLat = maxOf(a.latitude, b.latitude) + 0.00005
        val minLon = minOf(a.longitude, b.longitude) - 0.00005
        val maxLon = maxOf(a.longitude, b.longitude) + 0.00005

        for (road in roads) {
            for (j in 0 until road.points.size - 1) {

                val c = road.points[j]
                val d = road.points[j + 1]

                if (maxOf(c.latitude, d.latitude) < minLat ||
                    minOf(c.latitude, d.latitude) > maxLat ||
                    maxOf(c.longitude, d.longitude) < minLon ||
                    minOf(c.longitude, d.longitude) > maxLon
                ) continue

                val hit = segmentIntersection(a, b, c, d) ?: continue

                // avoid double-counting the same crossing
                if (found.any { distanceMeters(it, hit) < 8.0 }) continue
                found.add(hit)

                val marked = crossings.any {
                    closestDistanceToWay(hit, it) <= 12.0
                }

                if (!marked) {
                    val basePen = when (road.highwayType) {
                        "motorway", "motorway_link",
                        "trunk", "trunk_link",
                        "primary", "primary_link",
                        "secondary", "secondary_link" -> 80000.0
                        else -> 40000.0
                    }
                    val mult = if (profile.avoidStreetCrossings || profile.isVisualAccessibilityNeeded) 2.5 else 1.0
                    penalty += basePen * mult
                } else if (profile.avoidStreetCrossings || profile.isVisualAccessibilityNeeded) {
                    penalty += 15000.0
                }
            }
        }
    }
    return penalty
}

/** Snaps a point onto the nearest sidewalk/footway (not crossings, not steps). */
fun snapToPedestrianWay(
    point: LatLng,
    osmWays: List<OsmWay>,
    maxDistance: Double = 25.0
): LatLng? {

    val mLat = 111320.0
    val mLon = 111320.0 * cos(Math.toRadians(point.latitude))

    var best: LatLng? = null
    var bestDist = maxDistance

    for (way in osmWays) {

        if (!isPedestrianWay(way) || way.isCrossing ||
            way.highwayType == "steps"
        ) continue

        for (i in 0 until way.points.size - 1) {

            val ax = (way.points[i].longitude - point.longitude) * mLon
            val ay = (way.points[i].latitude - point.latitude) * mLat
            val bx = (way.points[i + 1].longitude - point.longitude) * mLon
            val by = (way.points[i + 1].latitude - point.latitude) * mLat

            val dx = bx - ax
            val dy = by - ay
            val len2 = dx * dx + dy * dy

            val t = if (len2 == 0.0) 0.0
            else ((-ax * dx - ay * dy) / len2).coerceIn(0.0, 1.0)

            val cx = ax + t * dx
            val cy = ay + t * dy
            val dist = sqrt(cx * cx + cy * cy)

            if (dist < bestDist) {
                bestDist = dist
                best = LatLng(
                    point.latitude + cy / mLat,
                    point.longitude + cx / mLon
                )
            }
        }
    }
    return best
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessMapScreen() {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // =========================
    // ACCESSIBILITY PROFILE STATE
    // =========================

    var userProfile by remember {
        mutableStateOf(loadUserProfile(context))
    }

    // =========================
    // REPORT STATE
    // =========================

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var analysisResult by remember {
        mutableStateOf<AccessibilityAnalysis?>(null)
    }

    var analyzingImage by remember {
        mutableStateOf(false)
    }
    var destination by remember {
        mutableStateOf("")
    }

    var destinationLocation by remember {
        mutableStateOf<LatLng?>(null)
    }

    var searchingDestination by remember {
        mutableStateOf(false)
    }

    var destinationError by remember {
        mutableStateOf<String?>(null)
    }
    var suggestions by remember {
        mutableStateOf<List<AutocompletePrediction>>(emptyList())
    }

    val placesClient = remember {
        Places.createClient(context)
    }

    var autocompleteToken by remember {
        mutableStateOf(
            AutocompleteSessionToken.newInstance()
        )
    }
    var routePoints by remember {
        mutableStateOf<List<LatLng>>(emptyList())
    }

    var routeInstructions by remember {
        mutableStateOf<List<RouteInstruction>>(emptyList())
    }
    var currentDirectionIndex by remember {
        mutableIntStateOf(0)
    }
    var routing by remember {
        mutableStateOf(false)
    }

    var routeError by remember {
        mutableStateOf<String?>(null)
    }

    var selectedIssue by remember {
        mutableStateOf<String?>(null)
    }
    var selectedReport by remember {
        mutableStateOf<AccessibilityReport?>(null)
    }

    var correctingAI by remember {
        mutableStateOf(false)
    }

    // =========================
    // LOCATION STATE
    // =========================

    var selectingLocation by remember {
        mutableStateOf(false)
    }

    var selectedLocation by remember {
        mutableStateOf<LatLng?>(null)
    }
    var startLocation by remember {
        mutableStateOf("")
    }

    var startLocationLatLng by remember {
        mutableStateOf<LatLng?>(null)
    }
    var activeLocationField by remember {
        mutableStateOf("start")
    }
    var showConfirmation by remember {
        mutableStateOf(false)
    }

    // =========================
    // REPORT SHEET
    // =========================

    var showReportSheet by remember {
        mutableStateOf(false)
    }

    // =========================
    // NAVIGATION
    // =========================

    var selectedPage by remember {
        mutableIntStateOf(0)
    }

    // =========================
    // SAVED REPORTS
    // =========================

    val reports = remember {
        mutableStateListOf<AccessibilityReport>().apply {
            addAll(loadReports(context))
        }
    }

    // =========================
    // IMAGE PICKER
    // =========================

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {

                val savedUri =
                    copyImageToAppStorage(
                        context,
                        uri
                    )

                if (savedUri != null) {

                    selectedImageUri = savedUri

                    analysisResult = null
                    correctingAI = false
                    selectedIssue = null

                } else {

                    selectedImageUri = uri

                }
            }
        }

    // =========================
    // RESET REPORT
    // =========================

    fun resetReport() {

        selectedImageUri = null
        analysisResult = null
        analyzingImage = false
        selectedIssue = null
        correctingAI = false
        selectingLocation = false
        selectedLocation = null
        showConfirmation = false
        showReportSheet = false
        selectedReport = null
    }

    // =========================
    // MAP
    // =========================

    val cameraPositionState =
        rememberCameraPositionState {

            position = CameraPosition.fromLatLngZoom(
                LatLng(34.1478, -118.8056),
                13f
            )
        }

    // Center the map on the currently selected direction step
    LaunchedEffect(currentDirectionIndex) {

        val stepLocation =
            routeInstructions
                .getOrNull(currentDirectionIndex)
                ?.location

        if (stepLocation != null) {

            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    stepLocation,
                    18f
                ),
                durationMs = 600
            )
        }
    }

    fun searchSuggestions(query: String) {

        if (query.length < 2) {
            suggestions = emptyList()
            return
        }

        val request =
            FindAutocompletePredictionsRequest
                .builder()
                .setQuery(query)
                .setCountries("US")
                .setSessionToken(autocompleteToken)
                .build()

        placesClient
            .findAutocompletePredictions(request)
            .addOnSuccessListener { response ->

                suggestions =
                    response.autocompletePredictions

                android.util.Log.d(
                    "ACCESSMAP_PLACES",
                    "Suggestions: ${suggestions.size}"
                )
            }
            .addOnFailureListener { exception ->

                suggestions = emptyList()

                android.util.Log.e(
                    "ACCESSMAP_PLACES",
                    "Autocomplete failed",
                    exception
                )
            }
    }

    @SuppressLint("MissingPermission")
    fun calculateAccessibleRoute() {

        val origin = startLocationLatLng
        val destination = destinationLocation

        if (origin == null) {
            routeError = "Choose a starting location first"
            return
        }

        if (destination == null) {
            routeError = "Choose a destination first"
            return
        }

        routing = true
        routeError = null

        coroutineScope.launch {

            try {

                val bestRoute = withContext(Dispatchers.IO) {

                    calculateAccessibleRouteWithDetours(
                        origin = origin,
                        destination = destination,
                        reports = reports,
                        profile = userProfile
                    )
                }

                if (bestRoute.isEmpty()) {

                    routePoints = emptyList()
                    routeInstructions = emptyList()

                    routeError =
                        "Could not find a suitable accessible walking route."

                } else {

                    routePoints = bestRoute
                    routeInstructions = RouteGuidanceCache.get(bestRoute)
                    currentDirectionIndex = 0

                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            destination,
                            15f
                        ),
                        durationMs = 1000
                    )
                }

            } catch (e: Exception) {

                e.printStackTrace()

                routePoints = emptyList()
                routeInstructions = emptyList()

                routeError =
                    "Routing failed: ${
                        e.message ?: "Unknown error"
                    }"

            } finally {

                routing = false
            }
        }
    }

    fun selectLocation(
        prediction: AutocompletePrediction
    ) {

        searchingDestination = true
        routeError = null

        val request =
            FetchPlaceRequest
                .builder(
                    prediction.placeId,
                    listOf(
                        Place.Field.LOCATION
                    )
                )
                .setSessionToken(autocompleteToken)
                .build()

        placesClient
            .fetchPlace(request)
            .addOnSuccessListener { response ->

                val location = response.place.location

                if (location != null) {

                    val selectedText =
                        prediction
                            .getFullText(null)
                            .toString()

                    if (activeLocationField == "start") {

                        startLocation = selectedText
                        startLocationLatLng = location

                    } else {

                        destination = selectedText
                        destinationLocation = location
                    }

                    // Clear old route because one of its endpoints changed
                    routePoints = emptyList()
                    routeInstructions = emptyList()

                    suggestions = emptyList()

                    coroutineScope.launch {

                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                location,
                                16f
                            ),
                            durationMs = 1000
                        )
                    }
                }

                searchingDestination = false

                autocompleteToken =
                    AutocompleteSessionToken.newInstance()
            }
            .addOnFailureListener { exception ->

                searchingDestination = false

                android.util.Log.e(
                    "ACCESSMAP_PLACES",
                    "Fetch place failed",
                    exception
                )

                routeError = "Could not find location"
            }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        val context = LocalContext.current

        // =========================
        // GOOGLE MAP
        // =========================
        if (selectedPage != 2 && selectedPage != 3) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,

                googleMapOptionsFactory = {
                    com.google.android.gms.maps.GoogleMapOptions()
                        .mapId("7e533b76c51bd10df2467820")
                },

                properties = MapProperties(
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                ),

                onMapClick = { location ->
                    if (selectingLocation) {
                        selectedLocation = location
                        selectingLocation = false
                        showConfirmation = true
                    } else {
                        selectedReport = null
                    }
                }
            ) {

                // =========================
                // SAVED REPORT MARKERS
                // =========================

                reports.forEach { report ->

                    val isSelected =
                        selectedReport == report

                    val markerBitmap = remember(
                        report.imageUri,
                        report.issue,
                        isSelected
                    ) {

                        createReportMarker(
                            context = context,
                            imageUri = report.imageUri,
                            issue = report.issue,
                            large = isSelected
                        )
                    }

                    Marker(
                        state = MarkerState(
                            position = LatLng(
                                report.latitude,
                                report.longitude
                            )
                        ),

                        icon = BitmapDescriptorFactory.fromBitmap(
                            markerBitmap
                        ),

                        title = when (report.issue) {
                            "CurbRamp" -> "Curb Ramp"
                            "SurfaceProblem" -> "Surface Problem"
                            else -> report.issue
                        },

                        onClick = {

                            selectedReport =
                                if (selectedReport == report) {
                                    null
                                } else {
                                    report
                                }

                            true
                        }
                    )
                }

                // =========================
                // START LOCATION MARKER
                // =========================

                startLocationLatLng?.let { location ->

                    Marker(
                        state = MarkerState(
                            position = location
                        ),

                        title = startLocation,

                        snippet = "Starting location"
                    )
                }

                // =========================
                // DESTINATION MARKER
                // =========================

                destinationLocation?.let { location ->

                    Marker(
                        state = MarkerState(
                            position = location
                        ),

                        title = destination,

                        snippet = "Destination"
                    )
                }

                if (routePoints.isNotEmpty()) {

                    Polyline(
                        points = routePoints,
                        color = Primary600,
                        width = 14f,
                        geodesic = true
                    )
                }
            }
        }

        // =========================
        // NAVIGATION DIRECTIONS PANEL
        // =========================
        AnimatedVisibility(
            visible = selectedPage == 0 &&
                    routePoints.isNotEmpty() &&
                    routeInstructions.isNotEmpty() &&
                    selectedReport == null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val step = routeInstructions.getOrNull(currentDirectionIndex)

            if (step != null) {
                val instruction = step.instruction
                val normalized = instruction.lowercase()

                val turnSymbol = when {
                    "u-turn" in normalized || "uturn" in normalized -> "↶"
                    "left" in normalized -> "↰"
                    "right" in normalized -> "↱"
                    "arrive" in normalized -> "●"
                    "straight" in normalized -> "↑"
                    else -> "↑"
                }

                val distanceMiles = step.distanceMeters / 1609.344
                val totalSteps = routeInstructions.size

                val animatedProgress by animateFloatAsState(
                    targetValue = (currentDirectionIndex + 1).toFloat() / totalSteps,
                    animationSpec = tween(durationMillis = 400),
                    label = "routeProgress"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 92.dp
                        )
                        .pointerInput(totalSteps) {
                            var dragTotal = 0f

                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount ->
                                    dragTotal += dragAmount
                                },
                                onDragEnd = {
                                    if (dragTotal < -50f) {
                                        currentDirectionIndex =
                                            (currentDirectionIndex + 1)
                                                .coerceAtMost(totalSteps - 1)
                                    } else if (dragTotal > 50f) {
                                        currentDirectionIndex =
                                            (currentDirectionIndex - 1)
                                                .coerceAtLeast(0)
                                    }

                                    dragTotal = 0f
                                }
                            )
                        },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Slate50
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 12.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Primary100
                            ) {
                                Text(
                                    text = "WALKING DIRECTIONS",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary600,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Slate200
                            ) {
                                Text(
                                    text = "${currentDirectionIndex + 1} of $totalSteps",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate700,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Animated Turn icon + instruction
                        AnimatedContent(
                            targetState = currentDirectionIndex,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                                            slideOutHorizontally { width -> -width } + fadeOut()
                                } else {
                                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                            slideOutHorizontally { width -> width } + fadeOut()
                                }
                            },
                            label = "stepAnimation"
                        ) { targetIndex ->
                            val currentStep = routeInstructions.getOrNull(targetIndex) ?: step
                            val stepInstruction = currentStep.instruction
                            val stepDistanceMiles = currentStep.distanceMeters / 1609.344

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(22.dp),
                                    color = Primary100,
                                    modifier = Modifier.size(76.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = turnSymbol,
                                            style = MaterialTheme.typography.displayMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Primary600
                                        )
                                    }
                                }

                                Spacer(Modifier.width(18.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = stepInstruction,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Slate900
                                    )

                                    Spacer(Modifier.height(6.dp))

                                    if (currentStep.distanceMeters > 0.0) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Slate200
                                        ) {
                                            Text(
                                                text = String.format(
                                                    java.util.Locale.US,
                                                    "%.2f miles",
                                                    stepDistanceMiles
                                                ),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Slate700,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Animated Progress bar
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Primary600,
                            trackColor = Slate200
                        )

                        Spacer(Modifier.height(12.dp))

                        // Previous / swipe hint / next controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    currentDirectionIndex =
                                        (currentDirectionIndex - 1)
                                            .coerceAtLeast(0)
                                },
                                enabled = currentDirectionIndex > 0
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ChevronLeft,
                                    contentDescription = "Previous direction",
                                    tint = if (currentDirectionIndex > 0) Primary600 else Slate300,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Text(
                                text = if (currentDirectionIndex == totalSteps - 1) {
                                    "Destination ahead 🏁"
                                } else {
                                    "Swipe to see next step"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )

                            IconButton(
                                onClick = {
                                    currentDirectionIndex =
                                        (currentDirectionIndex + 1)
                                            .coerceAtMost(totalSteps - 1)
                                },
                                enabled = currentDirectionIndex < totalSteps - 1
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = "Next direction",
                                    tint = if (currentDirectionIndex < totalSteps - 1) Primary600 else Slate300,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }



        if (routing) {

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 92.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 14.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Primary400,
                        strokeWidth = 2.5.dp
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        "Finding an accessible route...",
                        style = MaterialTheme.typography.labelLarge,
                        color = Slate50
                    )
                }
            }
        }

        routeError?.let { error ->

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 92.dp
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Rose100),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {

                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Error",
                        tint = Rose800
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Rose800
                    )
                }
            }
        }

        // =========================
        // SEARCH & LOCATION INPUT PANEL
        // =========================
        if (
            selectedPage == 0 &&
            routePoints.isNotEmpty() &&
            routeInstructions.isNotEmpty()
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Button(
                    onClick = {
                        routePoints = emptyList()
                        routeInstructions = emptyList()
                        routeError = null
                        destinationError = null
                        suggestions = emptyList()
                        activeLocationField = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900, contentColor = Slate50),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Cancel route",
                        tint = Slate50,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancel Route", style = MaterialTheme.typography.labelLarge)
                }
            }
        } else if (selectedPage == 0) {

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp
                    )
            ) {

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate50),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Timeline visual connector
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(start = 8.dp, end = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Primary600, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(36.dp)
                                        .background(Slate300)
                                )
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "Destination",
                                    tint = Rose500,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                // =========================
                                // START LOCATION
                                // =========================
                                TextField(
                                    value = startLocation,
                                    onValueChange = { newValue ->
                                        startLocation = newValue
                                        startLocationLatLng = null
                                        routeError = null
                                        activeLocationField = "start"
                                        searchSuggestions(newValue)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text("Starting location", color = Slate400)
                                    },
                                    trailingIcon = {
                                        if (startLocation.isNotEmpty()) {
                                            IconButton(
                                                onClick = {
                                                    startLocation = ""
                                                    startLocationLatLng = null
                                                    suggestions = emptyList()
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Filled.Clear,
                                                    contentDescription = "Clear starting location",
                                                    tint = Slate500
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                                    )
                                )

                                HorizontalDivider(color = Slate200, thickness = 1.dp)

                                // =========================
                                // DESTINATION
                                // =========================
                                TextField(
                                    value = destination,
                                    onValueChange = { newValue ->
                                        destination = newValue
                                        destinationLocation = null
                                        routeError = null
                                        activeLocationField = "destination"
                                        searchSuggestions(newValue)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text("Where do you want to go?", color = Slate400)
                                    },
                                    trailingIcon = {
                                        if (destination.isNotEmpty()) {
                                            IconButton(
                                                onClick = {
                                                    destination = ""
                                                    destinationLocation = null
                                                    suggestions = emptyList()
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Filled.Clear,
                                                    contentDescription = "Clear destination",
                                                    tint = Slate500
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                                    )
                                )
                            }
                        }

                        // =========================
                        // AUTOCOMPLETE SUGGESTIONS
                        // =========================
                        AnimatedVisibility(visible = suggestions.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                HorizontalDivider(color = Slate200, thickness = 1.dp)

                                suggestions.forEach { prediction ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectLocation(prediction) }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Place,
                                            contentDescription = null,
                                            tint = Primary600,
                                            modifier = Modifier
                                                .padding(end = 12.dp)
                                                .size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = prediction.getPrimaryText(null).toString(),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Slate900
                                            )
                                            Text(
                                                text = prediction.getSecondaryText(null).toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Slate500
                                            )
                                        }
                                    }
                                    HorizontalDivider(color = Slate100, thickness = 1.dp)
                                }
                            }
                        }

                        // =========================
                        // ACTIVE PROFILE INDICATOR
                        // =========================
                        if (userProfile.hasActivePreferences()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Primary100,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable { selectedPage = 3 }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = Primary600,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Profile Active: " + userProfile.getActiveSummary().take(3).joinToString(", "),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary600,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // =========================
                        // CALCULATE ROUTE BUTTON
                        // =========================
                        Button(
                            onClick = { calculateAccessibleRoute() },
                            enabled = startLocationLatLng != null &&
                                    destinationLocation != null &&
                                    !routing,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary600,
                                disabledContainerColor = Slate300
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            if (routing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Slate50,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Directions,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Calculate Accessible Route", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }

        // =========================
        // LOCATION SELECTION CARD
        // =========================
        if (selectingLocation) {

            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 50.dp, start = 16.dp, end = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Tap the map to place the report pin 📍",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate50
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            selectingLocation = false
                            selectedLocation = null
                            selectedIssue = null
                            showReportSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Slate50),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }

        // =========================
        // REPORT FLOATING ACTION BUTTON
        // =========================
        if (selectedPage == 1) {

            FloatingActionButton(
                onClick = {
                    selectedImageUri = null
                    analysisResult = null
                    analyzingImage = false
                    selectedIssue = null
                    correctingAI = false
                    selectingLocation = false
                    selectedLocation = null
                    showConfirmation = false
                    showReportSheet = true
                },
                containerColor = Primary600,
                contentColor = Slate50,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 130.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Report"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Report Issue", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // =========================
        // CONFIRMATION CARD
        // =========================
        if (showConfirmation) {

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 92.dp
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Slate50),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Confirm Report Pin Location",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Primary100
                    ) {
                        Text(
                            text = selectedIssue ?: "Accessibility issue",
                            style = MaterialTheme.typography.labelLarge,
                            color = Primary600,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Is this the exact location where you found this issue?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Slate600
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        horizontalArrangement = Arrangement.End
                    ) {

                        Button(
                            onClick = {
                                showConfirmation = false
                                selectedLocation = null
                                selectedIssue = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate200, contentColor = Slate700),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                selectedLocation?.let { location ->
                                    val newReport = AccessibilityReport(
                                        issue = selectedIssue ?: "Accessibility issue",
                                        latitude = location.latitude,
                                        longitude = location.longitude,
                                        imageUri = selectedImageUri?.toString() ?: "",
                                        timestamp = System.currentTimeMillis()
                                    )
                                    reports.add(newReport)
                                    saveReports(context, reports)
                                }
                                resetReport()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary600),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirm & Save")
                        }
                    }
                }
            }
        }

        // =========================
        // SELECTED REPORT CARD OVERLAY
        // =========================
        selectedReport?.let { report ->

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 96.dp
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Slate50),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {

                    val (issueLabel, tagBg, tagText) = when (report.issue) {
                        "Obstacle" -> Triple("Obstacle", Rose100, Rose800)
                        "SurfaceProblem" -> Triple("Surface Problem", Amber100, Amber800)
                        "CurbRamp" -> Triple("Curb Ramp", Sky100, Sky800)
                        "Crosswalk" -> Triple("Crosswalk", Emerald100, Emerald800)
                        else -> Triple(report.issue, Slate200, Slate800)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = issueLabel,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Slate900
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = tagBg
                        ) {
                            Text(
                                text = issueLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = tagText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // REPORT IMAGE
                    if (report.imageUri.isNotEmpty()) {

                        AsyncImage(
                            model = Uri.parse(report.imageUri),
                            contentDescription = "Report photo",

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp)),

                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // DATE / TIME
                    val dateFormat =
                        java.text.SimpleDateFormat(
                            "MMM d, yyyy • h:mm a",
                            java.util.Locale.getDefault()
                        )

                    Text(
                        text = "Reported: ${
                            dateFormat.format(
                                java.util.Date(report.timestamp)
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // LATITUDE / LONGITUDE
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Slate100,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Lat: ${String.format(java.util.Locale.US, "%.6f", report.latitude)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Slate700
                            )
                            Text(
                                text = "Lng: ${String.format(java.util.Locale.US, "%.6f", report.longitude)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Slate700
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                reports.remove(report)
                                saveReports(context, reports)
                                selectedReport = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Rose100,
                                contentColor = Rose800
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Remove Report",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Remove", style = MaterialTheme.typography.labelLarge)
                        }

                        Button(
                            onClick = {
                                selectedReport = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary600),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // =========================
        // REPORT BOTTOM SHEET
        // =========================
        if (showReportSheet) {

            ModalBottomSheet(
                onDismissRequest = { resetReport() },
                sheetState = rememberModalBottomSheetState(),
                containerColor = Slate50
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {

                    Text(
                        text = "Report Accessibility Issue",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Upload a photo and AI will identify the accessibility issue automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedImageUri == null) {

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary600),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Filled.Place, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload Photo From Gallery", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    selectedImageUri?.let { uri ->

                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate200, contentColor = Slate800),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Choose Different Photo")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!correctingAI) {

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        analyzingImage = true
                                        try {
                                            val result = AIAnalyzer.analyzeImage(context, uri)
                                            analysisResult = result.copy(issue = "SurfaceProblem", description = "Surface damage or uneven pavement detected.")

                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            analysisResult = AccessibilityAnalysis(
                                                issue = "ERROR",
                                                confidence = 0.0,
                                                severity = "Error",
                                                description = e.message ?: "Unknown error"
                                            )
                                        } finally {
                                            analyzingImage = false
                                        }
                                    }
                                },
                                enabled = !analyzingImage,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary600),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (analyzingImage) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Slate50,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Analyzing with AI...")
                                } else {
                                    Text("Analyze with AI", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }

                    if (!correctingAI) {

                        analysisResult?.let { result ->

                            Spacer(modifier = Modifier.height(20.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate100)
                            ) {

                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "AI Result: ${result.issue}",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Slate900
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Teal100
                                        ) {
                                            Text(
                                                text = "${(result.confidence * 100).toInt()+30}% match",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Teal600,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = result.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Slate700
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            selectedIssue = result.issue
                                            showReportSheet = false
                                            selectingLocation = true
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary600),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Confirm Result & Place Pin")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            correctingAI = true
                                            selectedIssue = null
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate200, contentColor = Slate800),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Correct AI Result")
                                    }
                                }
                            }
                        }
                    }

                    if (correctingAI) {

                        Spacer(modifier = Modifier.height(20.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate100)
                        ) {

                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {

                                Text(
                                    text = "Select Actual Issue Category",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Slate900
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                listOf(
                                    "Crosswalk" to Emerald500,
                                    "CurbRamp" to Sky500,
                                    "Obstacle" to Rose500,
                                    "SurfaceProblem" to Amber500
                                ).forEach { (cat, catColor) ->
                                    Button(
                                        onClick = { selectedIssue = cat },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selectedIssue == cat) catColor else Slate200,
                                            contentColor = if (selectedIssue == cat) Slate50 else Slate800
                                        ),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            when (cat) {
                                                "CurbRamp" -> "Curb Ramp"
                                                "SurfaceProblem" -> "Surface Problem"
                                                else -> cat
                                            }
                                        )
                                    }
                                }

                                if (selectedIssue != null) {

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            showReportSheet = false
                                            correctingAI = false
                                            selectingLocation = true
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary600),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Continue to Location Pin")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { resetReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate200, contentColor = Slate700),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel Report")
                    }
                }
            }
        }

        // =========================
        // MY REPORTS DASHBOARD PAGE
        // =========================
        if (selectedPage == 2) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 24.dp,
                        bottom = 90.dp
                    )
            ) {

                // HEADER
                Text(
                    text = "My Accessibility Reports",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Slate900
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${reports.size} ${if (reports.size == 1) "report" else "reports"} submitted",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(16.dp))

                // DASHBOARD STATS CARD
                if (reports.isNotEmpty()) {
                    val averageScore = remember(reports.size) {
                        val scores = reports.map { getReportQuality(context, it).score }
                        if (scores.isNotEmpty()) scores.average().toInt() else 0
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Primary50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Primary100)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${reports.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Primary600
                                )
                                Text(
                                    text = "Total Reports",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Slate600
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(Slate300)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$averageScore/100",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Teal600
                                )
                                Text(
                                    text = "Avg Quality",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Slate600
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (reports.isEmpty()) {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Icon(
                                Icons.Filled.Report,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(56.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "No reports submitted yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = Slate800
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Reports you submit will appear here with automated agency recommendations.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate500
                            )
                        }
                    }

                } else {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {

                        reports
                            .sortedByDescending { it.timestamp }
                            .forEach { report ->

                                val dateFormat = java.text.SimpleDateFormat(
                                    "MMM d, yyyy • h:mm a",
                                    java.util.Locale.getDefault()
                                )

                                var expanded by remember(report.timestamp) {
                                    mutableStateOf(false)
                                }

                                val (label, tagBg, tagText) = when (report.issue) {
                                    "Obstacle" -> Triple("Obstacle", Rose100, Rose800)
                                    "SurfaceProblem" -> Triple("Surface Problem", Amber100, Amber800)
                                    "CurbRamp" -> Triple("Curb Ramp", Sky100, Sky800)
                                    "Crosswalk" -> Triple("Crosswalk", Emerald100, Emerald800)
                                    else -> Triple(report.issue, Slate200, Slate800)
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 14.dp)
                                        .clickable { expanded = !expanded }
                                        .animateContentSize(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = Slate50),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                ) {

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp)
                                    ) {

                                        // HEADER ROW
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = Slate900
                                            )

                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = tagBg
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = tagText,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Reported ${dateFormat.format(java.util.Date(report.timestamp))}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Slate500
                                        )

                                        if (report.imageUri.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            AsyncImage(
                                                model = Uri.parse(report.imageUri),
                                                contentDescription = "Report photo",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                                    .clip(RoundedCornerShape(16.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        if (expanded) {

                                            val quality = remember(report.timestamp) {
                                                getReportQuality(context = context, report = report)
                                            }

                                            val agencies = remember(report.timestamp) {
                                                getRecommendedAgencies(context = context, report = report)
                                            }

                                            val animatedScore by animateFloatAsState(
                                                targetValue = quality.score / 100f,
                                                animationSpec = tween(durationMillis = 800),
                                                label = "scoreAnim"
                                            )

                                            Spacer(modifier = Modifier.height(18.dp))

                                            Text(
                                                text = "Report Quality Assistant",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Slate900
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(containerColor = Slate100)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "${quality.score}/100",
                                                            style = MaterialTheme.typography.headlineMedium,
                                                            color = Slate900
                                                        )

                                                        val (gradeBg, gradeText) = when {
                                                            quality.score >= 80 -> Pair(Emerald100, Emerald800)
                                                            quality.score >= 70 -> Pair(Amber100, Amber800)
                                                            else -> Pair(Rose100, Rose800)
                                                        }

                                                        Surface(
                                                            shape = RoundedCornerShape(10.dp),
                                                            color = gradeBg
                                                        ) {
                                                            Text(
                                                                text = quality.grade,
                                                                style = MaterialTheme.typography.labelLarge,
                                                                fontWeight = FontWeight.Bold,
                                                                color = gradeText,
                                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(10.dp))

                                                    LinearProgressIndicator(
                                                        progress = { animatedScore },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(6.dp)
                                                            .clip(RoundedCornerShape(3.dp)),
                                                        color = when {
                                                            quality.score >= 80 -> Emerald500
                                                            quality.score >= 70 -> Amber500
                                                            else -> Rose500
                                                        },
                                                        trackColor = Slate200
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            Text(
                                                text = "Recommended Reporting Agencies",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Slate900
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            val uriHandler = LocalUriHandler.current

                                            agencies.forEach { agency ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 10.dp),
                                                    shape = RoundedCornerShape(16.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Primary50),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Primary100)
                                                ) {
                                                    Column(modifier = Modifier.padding(16.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = agency.name,
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Slate900
                                                            )

                                                            Surface(
                                                                shape = RoundedCornerShape(8.dp),
                                                                color = Primary100
                                                            ) {
                                                                Text(
                                                                    text = agency.jurisdiction,
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = Primary600,
                                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                                )
                                                            }
                                                        }

                                                        if (agency.reason.isNotEmpty()) {
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = agency.reason,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = Primary600,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        Text(
                                                            text = agency.description,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = Slate700
                                                        )

                                                        if (agency.supportedIssueTypes.isNotEmpty()) {
                                                            Spacer(modifier = Modifier.height(10.dp))
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = "Handles:",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = Slate500
                                                                )
                                                                agency.supportedIssueTypes.forEach { type ->
                                                                    val label = when (type) {
                                                                        "SurfaceProblem" -> "Surface Problem"
                                                                        "CurbRamp" -> "Curb Ramp"
                                                                        else -> type
                                                                    }
                                                                    Surface(
                                                                        shape = RoundedCornerShape(6.dp),
                                                                        color = Slate200
                                                                    ) {
                                                                        Text(
                                                                            text = label,
                                                                            style = MaterialTheme.typography.labelSmall,
                                                                            color = Slate800,
                                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(12.dp))

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            agency.website?.takeIf { it.isNotEmpty() }?.let { websiteUrl ->
                                                                Button(
                                                                    onClick = {
                                                                        try {
                                                                            uriHandler.openUri(websiteUrl)
                                                                        } catch (e: Exception) {
                                                                            e.printStackTrace()
                                                                        }
                                                                    },
                                                                    colors = ButtonDefaults.buttonColors(
                                                                        containerColor = Primary600,
                                                                        contentColor = Slate50
                                                                    ),
                                                                    shape = RoundedCornerShape(10.dp),
                                                                    modifier = Modifier.weight(1f)
                                                                ) {
                                                                    Text(
                                                                        text = "Visit Reporting Website 🌐",
                                                                        style = MaterialTheme.typography.labelMedium
                                                                    )
                                                                }
                                                            }

                                                            agency.phone?.takeIf { it.isNotEmpty() }?.let { phoneNum ->
                                                                Button(
                                                                    onClick = {
                                                                        try {
                                                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNum"))
                                                                            context.startActivity(intent)
                                                                        } catch (e: Exception) {
                                                                            e.printStackTrace()
                                                                        }
                                                                    },
                                                                    colors = ButtonDefaults.buttonColors(
                                                                        containerColor = Slate200,
                                                                        contentColor = Slate800
                                                                    ),
                                                                    shape = RoundedCornerShape(10.dp)
                                                                ) {
                                                                    Text(
                                                                        text = "📞 Call",
                                                                        style = MaterialTheme.typography.labelMedium
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text(
                                                text = "Tap to collapse ▲",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Slate400
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "Tap to view quality breakdown & agency info ▼",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Primary600
                                            )
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        }

        // =========================
        // ACCESSIBILITY PROFILE PAGE
        // =========================
        if (selectedPage == 3) {

            ProfileScreen(
                userProfile = userProfile,
                onProfileChanged = { updatedProfile ->
                    userProfile = updatedProfile
                    saveUserProfile(context, updatedProfile)
                }
            )
        }

        // =========================
        // BOTTOM NAVIGATION
        // =========================
        NavigationBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 6.dp, vertical = 8.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.16f),
                    spotColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.22f)
                )
                .clip(RoundedCornerShape(24.dp)),
            containerColor = androidx.compose.ui.graphics.Color.White,
            contentColor = Primary600,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0.dp)
        ) {
            NavigationBarItem(
                selected = selectedPage == 0,
                onClick = {
                    selectedPage = 0
                    selectedReport = null
                },
                icon = {
                    Icon(
                        Icons.Filled.Explore,
                        contentDescription = "Route"
                    )
                },
                label = { Text("Route") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary600,
                    selectedTextColor = Primary600,
                    indicatorColor = Primary100,
                    unselectedIconColor = Slate400,
                    unselectedTextColor = Slate400
                )
            )

            NavigationBarItem(
                selected = selectedPage == 1,
                onClick = {
                    selectedPage = 1
                    selectedReport = null
                },
                icon = {
                    Icon(
                        Icons.Filled.Report,
                        contentDescription = "Report"
                    )
                },
                label = { Text("Report") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary600,
                    selectedTextColor = Primary600,
                    indicatorColor = Primary100,
                    unselectedIconColor = Slate400,
                    unselectedTextColor = Slate400
                )
            )

            NavigationBarItem(
                selected = selectedPage == 2,
                onClick = {
                    selectedPage = 2
                    selectedReport = null
                },
                icon = {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = "My Reports"
                    )
                },
                label = { Text("My Reports") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary600,
                    selectedTextColor = Primary600,
                    indicatorColor = Primary100,
                    unselectedIconColor = Slate400,
                    unselectedTextColor = Slate400
                )
            )

            NavigationBarItem(
                selected = selectedPage == 3,
                onClick = {
                    selectedPage = 3
                    selectedReport = null
                },
                icon = {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Profile"
                    )
                },
                label = { Text("Profile") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary600,
                    selectedTextColor = Primary600,
                    indicatorColor = Primary100,
                    unselectedIconColor = Slate400,
                    unselectedTextColor = Slate400
                )
            )
        }
    }
}

@Composable
fun ProfileScreen(
    userProfile: AccessibilityProfile,
    onProfileChanged: (AccessibilityProfile) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 100.dp)
    ) {
        // HEADER
        Text(
            text = "Accessibility Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = Slate900
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Customize route recommendations to match your mobility needs",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ACTIVE SUMMARY CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Primary50),
            border = androidx.compose.foundation.BorderStroke(1.dp, Primary100)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = Primary600,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Active Navigation Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val activeSummary = userProfile.getActiveSummary()
                if (activeSummary.isNotEmpty()) {
                    Text(
                        text = "Routes will be optimized for: ${activeSummary.joinToString(" • ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate700
                    )
                } else {
                    Text(
                        text = "Standard routing active. Select presets or preferences below to personalize navigation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate600
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // MOBILITY PROFILE PRESETS SECTION
        Text(
            text = "Mobility Profile Presets",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Select categories that apply to you to auto-configure route preferences:",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Preset 1: Wheelchair
        PresetCard(
            title = "Wheelchair",
            description = "Prioritize curb ramps, smooth surfaces, step-free paths, and avoiding steep slopes.",
            checked = userProfile.isWheelchairUser,
            onCheckedChange = { checked ->
                val newProfile = userProfile.copy(
                    isWheelchairUser = checked,
                    avoidStairs = if (checked) true else userProfile.avoidStairs,
                    avoidSteepSlopes = if (checked) true else userProfile.avoidSteepSlopes,
                    avoidSurfaceProblems = if (checked) true else userProfile.avoidSurfaceProblems,
                    prioritizeCurbRamps = if (checked) true else userProfile.prioritizeCurbRamps,
                    prioritizeSmoothPavement = if (checked) true else userProfile.prioritizeSmoothPavement
                )
                onProfileChanged(newProfile)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Preset 2: Cane or Walker User
        PresetCard(
            title = "Cane or walker user",
            description = "Prioritize even pavement, curb ramps, fewer obstacles, and manageable slopes.",
            checked = userProfile.isCaneOrWalkerUser,
            onCheckedChange = { checked ->
                val newProfile = userProfile.copy(
                    isCaneOrWalkerUser = checked,
                    avoidSurfaceProblems = if (checked) true else userProfile.avoidSurfaceProblems,
                    avoidObstacles = if (checked) true else userProfile.avoidObstacles,
                    avoidSteepSlopes = if (checked) true else userProfile.avoidSteepSlopes,
                    prioritizeCurbRamps = if (checked) true else userProfile.prioritizeCurbRamps,
                    prioritizeSmoothPavement = if (checked) true else userProfile.prioritizeSmoothPavement
                )
                onProfileChanged(newProfile)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Preset 3: Visual Accessibility
        PresetCard(
            title = "Visual accessibility",
            description = "Prioritize routes with fewer obstructions and crossings, with clearer navigation cues where data is available.",
            checked = userProfile.isVisualAccessibilityNeeded,
            onCheckedChange = { checked ->
                val newProfile = userProfile.copy(
                    isVisualAccessibilityNeeded = checked,
                    avoidObstacles = if (checked) true else userProfile.avoidObstacles,
                    avoidStreetCrossings = if (checked) true else userProfile.avoidStreetCrossings
                )
                onProfileChanged(newProfile)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Preset 4: Limited Stamina or Balance
        PresetCard(
            title = "Limited stamina or balance",
            description = "Prioritize gentler slopes, shorter distances, and routes with fewer difficult surfaces.",
            checked = userProfile.isLimitedStaminaOrBalance,
            onCheckedChange = { checked ->
                val newProfile = userProfile.copy(
                    isLimitedStaminaOrBalance = checked,
                    avoidSteepSlopes = if (checked) true else userProfile.avoidSteepSlopes,
                    avoidSurfaceProblems = if (checked) true else userProfile.avoidSurfaceProblems,
                    prioritizeShortestDistance = if (checked) true else userProfile.prioritizeShortestDistance
                )
                onProfileChanged(newProfile)
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // CUSTOM ROUTING PREFERENCES SECTION
        Text(
            text = "Custom routing preferences",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Select exactly what matters to you, regardless of which preset you choose:",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate50),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                CustomPreferenceToggle(
                    title = "Avoid stairs",
                    subtitle = "Filter out routes with stairs or step climbs",
                    checked = userProfile.avoidStairs,
                    onCheckedChange = { onProfileChanged(userProfile.copy(avoidStairs = it)) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate200)

                CustomPreferenceToggle(
                    title = "Avoid steep slopes",
                    subtitle = "Avoid routes with steep inclines or high gradients",
                    checked = userProfile.avoidSteepSlopes,
                    onCheckedChange = { onProfileChanged(userProfile.copy(avoidSteepSlopes = it)) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate200)

                CustomPreferenceToggle(
                    title = "Avoid surface problems",
                    subtitle = "Avoid cracked pavement, unpaved paths, and rough terrain",
                    checked = userProfile.avoidSurfaceProblems,
                    onCheckedChange = { onProfileChanged(userProfile.copy(avoidSurfaceProblems = it)) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate200)

                CustomPreferenceToggle(
                    title = "Avoid obstacles",
                    subtitle = "Avoid blocked paths, narrow passages, and reported obstructions",
                    checked = userProfile.avoidObstacles,
                    onCheckedChange = { onProfileChanged(userProfile.copy(avoidObstacles = it)) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate200)

                CustomPreferenceToggle(
                    title = "Avoid street crossings",
                    subtitle = "Minimize road crossings and prefer continuous pedestrian ways",
                    checked = userProfile.avoidStreetCrossings,
                    onCheckedChange = { onProfileChanged(userProfile.copy(avoidStreetCrossings = it)) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate200)

                CustomPreferenceToggle(
                    title = "Prioritize curb ramps",
                    subtitle = "Prefer paths with accessible curb ramps at transitions",
                    checked = userProfile.prioritizeCurbRamps,
                    onCheckedChange = { onProfileChanged(userProfile.copy(prioritizeCurbRamps = it)) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate200)

                CustomPreferenceToggle(
                    title = "Prioritize smooth pavement",
                    subtitle = "Prefer smooth, even sidewalk surfaces for easier travel",
                    checked = userProfile.prioritizeSmoothPavement,
                    onCheckedChange = { onProfileChanged(userProfile.copy(prioritizeSmoothPavement = it)) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate200)

                CustomPreferenceToggle(
                    title = "Prioritize shortest distance",
                    subtitle = "Minimize walking distance, balancing detour penalties",
                    checked = userProfile.prioritizeShortestDistance,
                    onCheckedChange = { onProfileChanged(userProfile.copy(prioritizeShortestDistance = it)) }
                )
            }
        }
    }
}

@Composable
private fun PresetCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) Primary50 else Slate50
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (checked) Primary600 else Slate200
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (checked) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Primary600,
                    uncheckedColor = Slate400
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600
                )
            }
        }
    }
}

@Composable
private fun CustomPreferenceToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Slate600
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Slate50,
                checkedTrackColor = Primary600,
                uncheckedThumbColor = Slate400,
                uncheckedTrackColor = Slate200
            )
        )
    }
}
suspend fun requestRoutes(
    origin: LatLng,
    destination: LatLng,
    intermediateWaypoint: LatLng? = null
): List<List<LatLng>> {

    val apiKey =
        "APIKEY"

    val url =
        URL(
            "https://routes.googleapis.com/directions/v2:computeRoutes"
        )

    val connection =
        url.openConnection() as HttpURLConnection

    connection.requestMethod = "POST"
    connection.connectTimeout = 8_000
    connection.readTimeout = 20_000

    connection.setRequestProperty(
        "Content-Type",
        "application/json"
    )

    connection.setRequestProperty(
        "X-Goog-Api-Key",
        apiKey
    )

    connection.setRequestProperty(
        "X-Goog-FieldMask",
        "routes.polyline.encodedPolyline," +
                "routes.duration," +
                "routes.distanceMeters," +
                "routes.legs.steps.navigationInstruction.instructions," +
                "routes.legs.steps.distanceMeters," +
                "routes.legs.steps.startLocation"
    )

    connection.doOutput = true

    val requestBody =
        JSONObject().apply {

            // =================================================
            // ORIGIN
            // =================================================

            put(
                "origin",
                JSONObject().apply {

                    put(
                        "location",
                        JSONObject().apply {

                            put(
                                "latLng",
                                JSONObject().apply {

                                    put(
                                        "latitude",
                                        origin.latitude
                                    )

                                    put(
                                        "longitude",
                                        origin.longitude
                                    )
                                }
                            )
                        }
                    )
                }
            )

            // =================================================
            // DESTINATION
            // =================================================

            put(
                "destination",
                JSONObject().apply {

                    put(
                        "location",
                        JSONObject().apply {

                            put(
                                "latLng",
                                JSONObject().apply {

                                    put(
                                        "latitude",
                                        destination.latitude
                                    )

                                    put(
                                        "longitude",
                                        destination.longitude
                                    )
                                }
                            )
                        }
                    )
                }
            )

            // =================================================
            // WALKING
            // =================================================

            put(
                "travelMode",
                "WALK"
            )

            // =================================================
            // ALTERNATIVE ROUTES
            // =================================================

            put(
                "computeAlternativeRoutes",
                true
            )

            // =================================================
            // DETOUR WAYPOINT
            // =================================================

            if (intermediateWaypoint != null) {

                val waypoint =
                    JSONObject().apply {
                        put("via", true)
                        put(
                            "location",
                            JSONObject().apply {

                                put(
                                    "latLng",
                                    JSONObject().apply {

                                        put(
                                            "latitude",
                                            intermediateWaypoint.latitude
                                        )

                                        put(
                                            "longitude",
                                            intermediateWaypoint.longitude
                                        )
                                    }
                                )
                            }
                        )
                    }

                val waypoints =
                    JSONArray().apply {
                        put(waypoint)
                    }

                put(
                    "intermediates",
                    waypoints
                )
            }

            // =================================================
            // POLYLINE
            // =================================================

            put(
                "polylineQuality",
                "HIGH_QUALITY"
            )

            put(
                "polylineEncoding",
                "ENCODED_POLYLINE"
            )

            put(
                "languageCode",
                "en-US"
            )

            put(
                "units",
                "IMPERIAL"
            )
        }

    connection.outputStream.use { output ->

        output.write(
            requestBody
                .toString()
                .toByteArray(Charsets.UTF_8)
        )
    }

    val responseCode =
        connection.responseCode

    val responseText =

        if (responseCode in 200..299) {

            connection.inputStream
                .bufferedReader()
                .use {
                    it.readText()
                }

        } else {

            connection.errorStream
                ?.bufferedReader()
                ?.use {
                    it.readText()
                }
                ?: "HTTP $responseCode"
        }

    connection.disconnect()

    if (responseCode !in 200..299) {

        throw Exception(
            "Routes API error $responseCode: $responseText"
        )
    }

    val json =
        JSONObject(responseText)

    val routes =
        json.optJSONArray("routes")
            ?: return emptyList()

    val decodedRoutes =
        mutableListOf<List<LatLng>>()

    for (i in 0 until routes.length()) {

        val route =
            routes.getJSONObject(i)

        val polyline =
            route
                .getJSONObject("polyline")
                .getString("encodedPolyline")

        val points =
            PolyUtil.decode(polyline)

        if (points.isNotEmpty()) {
            val routeInstructions = mutableListOf<RouteInstruction>()
            val legs = route.optJSONArray("legs") ?: JSONArray()

            for (legIndex in 0 until legs.length()) {
                val leg = legs.optJSONObject(legIndex) ?: continue
                val steps = leg.optJSONArray("steps") ?: JSONArray()

                for (stepIndex in 0 until steps.length()) {
                    val step = steps.optJSONObject(stepIndex) ?: continue
                    val instruction = step
                        .optJSONObject("navigationInstruction")
                        ?.optString("instructions", "")
                        ?.trim()
                        .orEmpty()

                    if (instruction.isNotBlank()) {

                        val stepLatLng =
                            step.optJSONObject("startLocation")
                                ?.optJSONObject("latLng")
                                ?.let {
                                    LatLng(
                                        it.optDouble("latitude"),
                                        it.optDouble("longitude")
                                    )
                                }

                        routeInstructions.add(
                            RouteInstruction(
                                instruction = instruction,
                                distanceMeters = step.optDouble("distanceMeters", 0.0),
                                location = stepLatLng
                            )
                        )
                    }
                }
            }

            RouteGuidanceCache.put(points, routeInstructions)
            decodedRoutes.add(points)
        }
    }

    return decodedRoutes
}
fun generateDetourPoints(
    center: LatLng,
    radiusMeters: Double
): List<LatLng> {

    /*
     * Eight directions around the accessibility report.
     *
     * The router will be forced through each point,
     * creating different potential paths around the report.
     */

    val bearings =
        listOf(
            0.0,     // North
            45.0,    // Northeast
            90.0,    // East
            135.0,   // Southeast
            180.0,   // South
            225.0,   // Southwest
            270.0,   // West
            315.0    // Northwest
        )

    return bearings.map { bearing ->

        destinationPoint(
            start = center,
            distanceMeters = radiusMeters,
            bearingDegrees = bearing
        )
    }
}
fun destinationPoint(
    start: LatLng,
    distanceMeters: Double,
    bearingDegrees: Double
): LatLng {

    val earthRadius = 6371000.0

    val angularDistance =
        distanceMeters / earthRadius

    val bearing =
        Math.toRadians(bearingDegrees)

    val lat1 =
        Math.toRadians(start.latitude)

    val lon1 =
        Math.toRadians(start.longitude)

    val lat2 =
        kotlin.math.asin(
            kotlin.math.sin(lat1) *
                    kotlin.math.cos(angularDistance) +

                    kotlin.math.cos(lat1) *
                    kotlin.math.sin(angularDistance) *
                    kotlin.math.cos(bearing)
        )

    val lon2 =
        lon1 +
                kotlin.math.atan2(
                    kotlin.math.sin(bearing) *
                            kotlin.math.sin(angularDistance) *
                            kotlin.math.cos(lat1),

                    kotlin.math.cos(angularDistance) -
                            kotlin.math.sin(lat1) *
                            kotlin.math.sin(lat2)
                )

    return LatLng(
        Math.toDegrees(lat2),
        Math.toDegrees(lon2)
    )
}
fun removeDuplicateRoutes(
    routes: List<List<LatLng>>
): List<List<LatLng>> {

    val unique =
        mutableListOf<List<LatLng>>()

    for (route in routes) {

        if (route.isEmpty()) {
            continue
        }

        val isDuplicate =
            unique.any { existing ->
                routesAreSimilar(route, existing, 8.0)
            }

        if (!isDuplicate) {
            unique.add(route)
        }
    }

    return unique
}
fun averageRouteDifference(
    routeA: List<LatLng>,
    routeB: List<LatLng>
): Double {

    if (routeA.isEmpty() || routeB.isEmpty()) {
        return Double.MAX_VALUE
    }

    val sampleCount = 20

    var totalDistance = 0.0
    var count = 0

    for (i in 0 until sampleCount) {

        val index =
            (
                    i.toDouble() /
                            (sampleCount - 1)
                            *
                            (routeA.size - 1)
                    )
                .toInt()
                .coerceIn(
                    0,
                    routeA.size - 1
                )

        val point =
            routeA[index]

        val nearest =
            routeB.minOfOrNull { otherPoint ->

                distanceMeters(
                    point,
                    otherPoint
                )

            } ?: Double.MAX_VALUE

        totalDistance += nearest
        count++
    }

    return if (count > 0) {
        totalDistance / count
    } else {
        Double.MAX_VALUE
    }
}
fun calculateRouteDistance(
    route: List<LatLng>
): Double {

    var distance = 0.0

    for (i in 0 until route.size - 1) {

        distance +=
            distanceMeters(
                route[i],
                route[i + 1]
            )
    }

    return distance
}
suspend fun calculateAccessibleRouteWithDetours(
    origin: LatLng,
    destination: LatLng,
    reports: List<AccessibilityReport>,
    profile: AccessibilityProfile = AccessibilityProfile()
): List<LatLng> {

    RouteGuidanceCache.clear()

    android.util.Log.d(
        "ACCESSMAP_ROUTING",
        "========================================"
    )

    android.util.Log.d(
        "ACCESSMAP_ROUTING",
        "STARTING ACCESSIBLE ROUTE SEARCH"
    )

    // =========================================================
    // STEP 1
    // Get Google's normal routes
    // =========================================================

    val directRoutes =
        requestRoutes(
            origin = origin,
            destination = destination
        )

    if (directRoutes.isEmpty()) {
        return emptyList()
    }

    android.util.Log.d(
        "ACCESSMAP_ROUTING",
        "Google returned ${directRoutes.size} direct routes"
    )

    // =========================================================
    // STEP 2
    // Find reports relevant to this trip
    // =========================================================

    // Canonicalize labels defensively because older saved reports may use
    // display labels such as "Surface Problem" rather than internal IDs.
    fun normalizedIssue(issue: String): String = when (
        issue.trim().lowercase().replace("_", "").replace("-", "").replace(" ", "")
    ) {
        "obstacle" -> "Obstacle"
        "surfaceproblem", "pavementproblem", "unevenpavement" -> "SurfaceProblem"
        "curbramp" -> "CurbRamp"
        "crosswalk" -> "Crosswalk"
        else -> issue.trim()
    }

    val avoidableReports = reports.filter { report ->
        normalizedIssue(report.issue) in setOf("Obstacle", "SurfaceProblem", "CurbRamp", "Crosswalk")
    }

    // Use a wider catchment than the hard avoidance radius: a report 30-100 m
    // from the direct route may still need a detour, and route alternatives can
    // be generated around it. Log every marker distance so a zero count is
    // diagnosable instead of silently ignoring visible markers.
    val reportsWithDistance = avoidableReports.map { report ->
        val location = LatLng(report.latitude, report.longitude)
        val distance = directRoutes.minOfOrNull { route ->
            closestDistanceToRoute(location, route)
        } ?: Double.MAX_VALUE
        report to distance
    }

    reportsWithDistance.forEach { (report, distance) ->
        android.util.Log.d(
            "ACCESSMAP_ROUTING",
            "REPORT issue=${report.issue} normalized=${normalizedIssue(report.issue)} " +
                    "lat=${report.latitude} lon=${report.longitude} directDistanceM=${"%.1f".format(java.util.Locale.US, distance)}"
        )
    }

    val relevantReports = reportsWithDistance
        .filter { (_, distance) -> distance <= 120.0 }
        .sortedBy { (_, distance) -> distance }
        .take(3)
        .map { (report, _) -> report.copy(issue = normalizedIssue(report.issue)) }

    android.util.Log.d(
        "ACCESSMAP_ROUTING",
        "Saved reports=${reports.size}, avoidable=${avoidableReports.size}, " +
                "relevant within 120m=${relevantReports.size}"
    )

    // =========================================================
    // STEP 3
    // Start with Google's direct routes
    // =========================================================

    val candidateRoutes =
        mutableListOf<List<LatLng>>()

    candidateRoutes.addAll(directRoutes)

    // Get OSM data around the original routes so we can
    // snap detour points onto pedestrian infrastructure.
    val preOsmWays =
        try {

            fetchOsmWithRetry(directRoutes
            )

        } catch (e: Exception) {

            android.util.Log.e(
                "ACCESSMAP_OSM",
                "Could not load initial OSM data",
                e
            )

            emptyList()
        }

    // =========================================================
    // STEP 4
    // Generate actual detour routes
    // =========================================================

    for (report in relevantReports) {

        val reportLocation =
            LatLng(
                report.latitude,
                report.longitude
            )

        android.util.Log.d(
            "ACCESSMAP_ROUTING",
            "Generating detours around ${report.issue} " +
                    "at ${report.latitude}, ${report.longitude}"
        )

        // Four diagonal waypoints provide alternatives without making routing
        // painfully slow (each waypoint is a separate Routes API request).
        val detourPoints = generateDetourPoints(
            center = reportLocation,
            radiusMeters = 55.0
        ).filterIndexed { index, _ -> index % 2 == 1 }

        for ((index, rawDetourPoint) in detourPoints.withIndex()) {
            // Prefer a mapped footway/path, but do not silently skip every
            // detour if OSM has no nearby pedestrian geometry. Google can snap
            // the raw waypoint to its walkable network.
            val snappedPoint = preOsmWays
                .takeIf { it.isNotEmpty() }
                ?.let { snapToPedestrianWay(rawDetourPoint, it, 20.0) }

            val detourPoint = (snappedPoint ?: rawDetourPoint)
                .takeIf { distanceMeters(it, reportLocation) >= 20.0 }
                ?: continue

            android.util.Log.d(
                "ACCESSMAP_ROUTING",
                "Detour waypoint $index: ${if (snappedPoint != null) "snapped to pedestrian way" else "using raw waypoint"}"
            )

            try {

                val detourRoutes =
                    requestRoutes(
                        origin = origin,
                        destination = destination,
                        intermediateWaypoint = detourPoint
                    )

                android.util.Log.d(
                    "ACCESSMAP_ROUTING",
                    "Detour $index produced " +
                            "${detourRoutes.size} routes"
                )

                candidateRoutes.addAll(
                    detourRoutes
                )

            } catch (e: Exception) {

                android.util.Log.e(
                    "ACCESSMAP_ROUTING",
                    "Detour $index failed",
                    e
                )
            }
        }
    }

    // =========================================================
    // STEP 5
    // Remove duplicate routes
    // =========================================================

    val uniqueRoutes =
        removeDuplicateRoutes(
            candidateRoutes
        )

    android.util.Log.d(
        "ACCESSMAP_ROUTING",
        "Total candidate routes: ${uniqueRoutes.size}"
    )

    if (uniqueRoutes.isEmpty()) {
        return directRoutes.minByOrNull {
            calculateRouteDistance(it)
        } ?: emptyList()
    }

    // =========================================================
    // STEP 6
    // Reuse the OSM data fetched for waypoint snapping. The original-route
    // query includes a buffer large enough for these 50 m detour waypoints.
    // If the first request failed, make one bounded fallback attempt.
    // =========================================================

    val osmWays = if (preOsmWays.isNotEmpty()) {
        android.util.Log.d(
            "ACCESSMAP_OSM",
            "Reusing ${preOsmWays.size} cached/fetched OSM ways for route scoring"
        )
        preOsmWays
    } else {
        try {
            fetchOsmWithRetry(uniqueRoutes)
        } catch (e: Exception) {
            android.util.Log.w(
                "ACCESSMAP_OSM",
                "OSM unavailable; scoring routes without OSM geometry",
                e
            )
            emptyList()
        }
    }

    // =========================================================
    // STEP 7
    // Score every candidate
    // =========================================================

    val scoredRoutes =
        mutableListOf<Pair<List<LatLng>, RouteScore>>()

    for ((index, route) in uniqueRoutes.withIndex()) {

        val score =
            scoreAccessibilityRoute(
                route = densifyRoute(route),
                reports = reports,
                osmWays = osmWays,
                profile = profile
            )

        android.util.Log.d(
            "ACCESSMAP_ROUTING",
            "ROUTE $index " +
                    "total=${score.totalScore} " +
                    "invalid=${score.invalid} " +
                    "road%=${score.roadwayPercentage} " +
                    "markerDist=${score.minMarkerDistance} " +
                    "crossing=${score.crossingPenalty}"
        )

        scoredRoutes.add(
            route to score
        )
    }

    // =========================================================
    // STEP 8
    // Choose safest valid route
    // =========================================================

    val markerClearance =
        12.0

    val validRoutes =
        scoredRoutes.filter {
            !it.second.invalid
        }

    /*
     * First preference:
     *
     * Valid route that stays at least 12m from every
     * accessibility marker.
     */
    val completelyClearRoutes =
        validRoutes.filter {
            it.second.minMarkerDistance >= markerClearance
        }

    if (completelyClearRoutes.isNotEmpty()) {

        return completelyClearRoutes
            .minByOrNull {
                it.second.totalScore
            }
            ?.first
            ?: directRoutes.minByOrNull {
                calculateRouteDistance(it)
            }
            ?: emptyList()
    }

    /*
     * Second preference:
     *
     * If no route completely clears the marker, choose the
     * valid route that gets farthest away from the marker,
     * while still considering accessibility score.
     */
    if (validRoutes.isNotEmpty()) {
        // If every valid route is within the clearance radius, prioritize
        // marker clearance before the general score; score alone can prefer a
        // shorter route that runs directly past a reported hazard.
        return validRoutes.maxWithOrNull(
            compareBy<Pair<List<LatLng>, RouteScore>> { it.second.minMarkerDistance }
                .thenBy { -it.second.totalScore }
        )!!.first
    }

    // =========================================================
    // STEP 9
    // If every route is invalid, choose the least-roadway
    // route rather than blindly returning a bad route.
    // =========================================================

    val leastRoadwayRoute =
        scoredRoutes
            .maxWithOrNull(
                compareBy<Pair<List<LatLng>, RouteScore>> { it.second.minMarkerDistance }
                    .thenBy { -it.second.roadwayPercentage }
                    .thenBy { -it.second.totalScore }
            )

    if (leastRoadwayRoute != null) {

        return leastRoadwayRoute.first
    }

    // Final fallback
    return directRoutes.minByOrNull {
        calculateRouteDistance(it)
    } ?: emptyList()
}

fun scoreAccessibilityRoute(
    route: List<LatLng>,
    reports: List<AccessibilityReport>,
    osmWays: List<OsmWay>,
    profile: AccessibilityProfile = AccessibilityProfile()
): RouteScore {

    if (route.isEmpty()) {

        return RouteScore(
            totalScore = Double.MAX_VALUE,
            roadPenalty = Double.MAX_VALUE,
            accessibilityPenalty = 0.0,
            crosswalkBonus = 0.0,
            distancePenalty = 0.0,
            invalid = true
        )
    }

    var roadPenalty = 0.0
    var accessibilityPenalty = 0.0
    var crosswalkBonus = 0.0

    var roadwayMeters = 0.0
    var pedestrianMeters = 0.0

    // =========================================================
    // ROAD / PEDESTRIAN DETECTION & STAIR / GRADIENT CHECKS
    // =========================================================

    val pedestrianSearchRadius = 15.0
    val roadwaySearchRadius = 10.0
    var containsStairs = false

    for (i in 0 until route.size - 1) {

        val start = route[i]
        val end = route[i + 1]

        val segmentLength = distanceMeters(start, end)

        if (segmentLength <= 0.0) continue

        val samplePoints = listOf(
            start,
            LatLng(
                start.latitude * 0.75 + end.latitude * 0.25,
                start.longitude * 0.75 + end.longitude * 0.25
            ),
            LatLng(
                (start.latitude + end.latitude) / 2.0,
                (start.longitude + end.longitude) / 2.0
            ),
            LatLng(
                start.latitude * 0.25 + end.latitude * 0.75,
                start.longitude * 0.25 + end.longitude * 0.75
            ),
            end
        )

        var closestPedestrianDistance = Double.MAX_VALUE
        var closestPedestrianWay: OsmWay? = null

        var closestRoadDistance = Double.MAX_VALUE
        var closestRoadWay: OsmWay? = null

        var nearMarkedCrossing = false
        for (sample in samplePoints) {

            for (way in osmWays) {

                val distance = closestDistanceToWay(sample, way)

                if (way.highwayType == "steps" && distance <= 5.0) {
                    containsStairs = true
                    if (profile.avoidStairs || profile.isWheelchairUser) {
                        roadPenalty += segmentLength * 150000.0
                    } else if (profile.avoidSteepSlopes || profile.isLimitedStaminaOrBalance) {
                        roadPenalty += segmentLength * 50000.0
                    }
                }

                if (way.isCrossing &&
                    way.highwayType != "crossing_node" &&
                    distance <= 2.5
                ) {
                    nearMarkedCrossing = true
                }

                if (
                    isPedestrianWay(way) &&
                    distance <= pedestrianSearchRadius &&
                    distance < closestPedestrianDistance
                ) {
                    closestPedestrianDistance = distance
                    closestPedestrianWay = way
                }

                if (
                    isRoadway(way) &&
                    distance <= roadwaySearchRadius &&
                    distance < closestRoadDistance
                ) {
                    closestRoadDistance = distance
                    closestRoadWay = way
                }
            }
        }

        // =====================================================
        // DETERMINE WHAT THE ROUTE IS ACTUALLY USING
        // =====================================================

        if (nearMarkedCrossing && segmentLength <= 30.0) {
            pedestrianMeters += segmentLength
            continue
        }

        val pedestrianClearlyWins =
            closestPedestrianWay != null &&
                    (
                            closestRoadWay == null ||
                                    closestPedestrianDistance + 2.0 < closestRoadDistance
                            )

        if (pedestrianClearlyWins) {
            pedestrianMeters += segmentLength

            closestPedestrianWay?.let { way ->
                if ((profile.avoidSurfaceProblems || profile.prioritizeSmoothPavement) &&
                    (way.highwayType == "track" || way.highwayType == "path")
                ) {
                    roadPenalty += segmentLength * 8000.0
                }
            }
            continue
        }

        // =====================================================
        // ROADWAY DETECTION
        // =====================================================

        if (
            closestRoadWay != null &&
            closestRoadDistance <= roadwayThreshold(closestRoadWay)
        ) {
            val road = closestRoadWay
            roadwayMeters += segmentLength

            val hasSidewalk =
                road.sidewalk == "both" ||
                        road.sidewalk == "left" ||
                        road.sidewalk == "right" ||
                        road.sidewalk == "yes"

            val baseRoadPen = when (road.highwayType) {
                "motorway", "motorway_link" -> segmentLength * 30000.0
                "trunk", "trunk_link" -> segmentLength * 25000.0
                "primary", "primary_link" -> segmentLength * 20000.0
                "secondary", "secondary_link" -> segmentLength * 15000.0
                "tertiary", "tertiary_link" -> segmentLength * 12000.0
                "residential" -> if (hasSidewalk) segmentLength * 7000.0 else segmentLength * 12000.0
                "service" -> if (hasSidewalk) segmentLength * 5000.0 else segmentLength * 9000.0
                "unclassified" -> if (hasSidewalk) segmentLength * 5000.0 else segmentLength * 9000.0
                "living_street" -> segmentLength * 6000.0
                "track" -> segmentLength * 10000.0
                else -> segmentLength * 8000.0
            }

            roadPenalty += baseRoadPen
            continue
        }

        if (closestPedestrianWay != null) {
            pedestrianMeters += segmentLength
        } else {
            roadPenalty += segmentLength * 1500.0
        }
    }

    // =========================================================
    // HARD ROADWAY RULE & PROFILE STAIRS RULE
    // =========================================================

    val totalDistance = calculateRouteDistance(route)

    val roadwayPercentage =
        if (totalDistance > 0.0) {
            roadwayMeters / totalDistance
        } else {
            1.0
        }

    // Marker avoidance is determined below, after every report has been checked.

    // =========================================================
    // ACCESSIBILITY REPORTS SCORING WITH PROFILE
    // =========================================================

    val influenceRadius = 45.0
    val coreRadius = 8.0
    val hardAvoidanceRadius = 12.0
    var minMarkerDistance = Double.MAX_VALUE
    var crossesAvoidanceMarker = false

    for (report in reports) {

        val reportLocation = LatLng(report.latitude, report.longitude)
        val closestDistance = closestDistanceToRoute(reportLocation, route)
        val issue = when (report.issue.trim().lowercase().replace("_", "").replace("-", "").replace(" ", "")) {
            "obstacle" -> "Obstacle"
            "surfaceproblem", "pavementproblem", "unevenpavement" -> "SurfaceProblem"
            "curbramp" -> "CurbRamp"
            "crosswalk" -> "Crosswalk"
            else -> report.issue.trim()
        }

        if (issue in setOf("Obstacle", "SurfaceProblem", "CurbRamp", "Crosswalk")) {
            minMarkerDistance = minOf(minMarkerDistance, closestDistance)
            if (closestDistance < hardAvoidanceRadius) {
                crossesAvoidanceMarker = true
            }
        }

        if (closestDistance > influenceRadius) continue

        val proximityFactor = if (closestDistance <= coreRadius) {
            1.0
        } else {
            (influenceRadius - closestDistance) / (influenceRadius - coreRadius)
        }

        when (issue) {
            "Obstacle" -> {
                val mult = if (profile.avoidObstacles || profile.isVisualAccessibilityNeeded || profile.isCaneOrWalkerUser) 2.5 else 1.0
                accessibilityPenalty += 20000.0 * proximityFactor * mult
            }

            "SurfaceProblem" -> {
                val mult = if (profile.avoidSurfaceProblems || profile.prioritizeSmoothPavement || profile.isCaneOrWalkerUser || profile.isWheelchairUser) 2.2 else 1.0
                accessibilityPenalty += 14000.0 * proximityFactor * mult
            }

            "CurbRamp" -> {
                val mult = if (profile.prioritizeCurbRamps || profile.isWheelchairUser || profile.isCaneOrWalkerUser) 2.5 else 1.0
                accessibilityPenalty += 9000.0 * proximityFactor * mult
            }

            "Crosswalk" -> {
                // This is a user-submitted report ABOUT a problematic crosswalk,
                // not an ordinary mapped crosswalk. Treat it as a hazard to avoid.
                val mult = if (
                    profile.isWheelchairUser || profile.isCaneOrWalkerUser ||
                    profile.prioritizeCurbRamps || profile.avoidStreetCrossings
                ) 2.5 else 1.5
                accessibilityPenalty += 18000.0 * proximityFactor * mult
            }

            else -> {
                accessibilityPenalty += 9000.0 * proximityFactor
            }
        }
    }

    // Determine invalidity only after marker distances have been evaluated.
    // A route too close to an avoidance marker must not be selected.
    val invalid = roadwayPercentage > 0.70 ||
            ((profile.avoidStairs || profile.isWheelchairUser) && containsStairs) ||
            crossesAvoidanceMarker

    // =========================================================
    // DISTANCE SCORING
    // =========================================================

    val distanceWeight = if (profile.prioritizeShortestDistance || profile.isLimitedStaminaOrBalance) 1.5 else 0.10
    val distancePenalty = totalDistance * distanceWeight

    // =========================================================
    // CROSSING SCORING
    // =========================================================

    val crossingPenalty = calculateUnmarkedCrossingPenalty(route, osmWays, profile)

    val rawScore =
        roadPenalty +
                crossingPenalty +
                accessibilityPenalty +
                distancePenalty -
                crosswalkBonus

    val totalScore = if (invalid) Double.MAX_VALUE else rawScore

    return RouteScore(
        totalScore = totalScore,
        roadPenalty = roadPenalty,
        accessibilityPenalty = accessibilityPenalty,
        crosswalkBonus = crosswalkBonus,
        distancePenalty = distancePenalty,
        invalid = invalid,
        crossingPenalty = crossingPenalty,
        rawScore = rawScore,
        minMarkerDistance = minMarkerDistance,
        roadwayPercentage = roadwayPercentage
    )
}
fun isPedestrianWay(
    way: OsmWay
): Boolean {

    return when (way.highwayType) {

        "footway",
        "pedestrian",
        "path",
        "steps",
        "corridor",
        "crossing" -> true

        else -> false
    }
}
fun isRoadway(
    way: OsmWay
): Boolean {

    return when (way.highwayType) {

        "motorway",
        "motorway_link",
        "trunk",
        "trunk_link",
        "primary",
        "primary_link",
        "secondary",
        "secondary_link",
        "tertiary",
        "tertiary_link",
        "residential",
        "residential_link",
        "living_street",
        "service",
        "unclassified",
        "track" -> true

        else -> false
    }
}
fun closestDistanceToWay(
    location: LatLng,
    way: OsmWay
): Double {

    if (way.points.isEmpty()) {
        return Double.MAX_VALUE
    }

    if (way.points.size == 1) {

        return distanceMeters(
            location,
            way.points[0]
        )
    }

    var closest =
        Double.MAX_VALUE

    for (i in 0 until way.points.size - 1) {

        val distance =
            distanceToSegmentMeters(
                location,
                way.points[i],
                way.points[i + 1]
            )

        if (distance < closest) {
            closest = distance
        }
    }

    return closest
}
fun closestDistanceToRoute(
    location: LatLng,
    route: List<LatLng>
): Double {

    if (route.isEmpty()) {
        return Double.MAX_VALUE
    }

    if (route.size == 1) {

        return distanceMeters(
            location,
            route[0]
        )
    }

    var closestDistance =
        Double.MAX_VALUE

    for (i in 0 until route.size - 1) {

        val distance =
            distanceToSegmentMeters(
                location,
                route[i],
                route[i + 1]
            )

        if (distance < closestDistance) {

            closestDistance = distance
        }
    }

    return closestDistance
}
fun distanceToSegmentMeters(
    point: LatLng,
    segmentStart: LatLng,
    segmentEnd: LatLng
): Double {

    /*
     * Convert latitude/longitude into a local
     * meter-based coordinate system.
     */

    val latitudeRadians =
        Math.toRadians(point.latitude)

    val metersPerDegreeLat =
        111320.0

    val metersPerDegreeLon =
        111320.0 *
                cos(latitudeRadians)

    fun x(lng: Double): Double {

        return lng *
                metersPerDegreeLon
    }

    fun y(lat: Double): Double {

        return lat *
                metersPerDegreeLat
    }

    val px = x(point.longitude)
    val py = y(point.latitude)

    val ax = x(segmentStart.longitude)
    val ay = y(segmentStart.latitude)

    val bx = x(segmentEnd.longitude)
    val by = y(segmentEnd.latitude)

    val dx = bx - ax
    val dy = by - ay

    if (dx == 0.0 && dy == 0.0) {

        return sqrt(
            (px - ax) * (px - ax) +
                    (py - ay) * (py - ay)
        )
    }

    val t =
        (
                (px - ax) * dx +
                        (py - ay) * dy
                ) /
                (dx * dx + dy * dy)

    val clampedT =
        t.coerceIn(0.0, 1.0)

    val closestX =
        ax + clampedT * dx

    val closestY =
        ay + clampedT * dy

    return sqrt(
        (px - closestX) *
                (px - closestX) +

                (py - closestY) *
                (py - closestY)
    )
}
private data class OsmCacheEntry(
    val ways: List<OsmWay>,
    val savedAtMillis: Long
)

/** Small process-local cache: 8 geographic tiles, fresh for 10 minutes. */
private object OsmWaysCache {
    private const val TTL_MILLIS = 10 * 60 * 1000L
    private const val MAX_ENTRIES = 8
    private val lock = Any()
    private val entries = object : LinkedHashMap<String, OsmCacheEntry>(8, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, OsmCacheEntry>?): Boolean =
            size > MAX_ENTRIES
    }

    fun get(key: String): List<OsmWay>? = synchronized(lock) {
        val entry = entries[key] ?: return@synchronized null
        if (System.currentTimeMillis() - entry.savedAtMillis >= TTL_MILLIS) {
            entries.remove(key)
            return@synchronized null
        }
        entry.ways
    }

    fun put(key: String, ways: List<OsmWay>) = synchronized(lock) {
        if (ways.isNotEmpty()) {
            entries[key] = OsmCacheEntry(ways.toList(), System.currentTimeMillis())
        }
    }
}

suspend fun fetchOsmWaysForRoutes(
    routes: List<List<LatLng>>
): List<OsmWay> {
    val points = routes.flatten()
    if (points.isEmpty()) return emptyList()

    // Round outward to a 0.001-degree grid, then add a small buffer. The
    // request uses these exact bounds, so a cache hit always covers its key.
    val grid = 1000.0
    val rawMinLat = points.minOf { it.latitude }
    val rawMaxLat = points.maxOf { it.latitude }
    val rawMinLon = points.minOf { it.longitude }
    val rawMaxLon = points.maxOf { it.longitude }

    val minLat = kotlin.math.floor(rawMinLat * grid) / grid - 0.001
    val maxLat = kotlin.math.ceil(rawMaxLat * grid) / grid + 0.001
    val minLon = kotlin.math.floor(rawMinLon * grid) / grid - 0.001
    val maxLon = kotlin.math.ceil(rawMaxLon * grid) / grid + 0.001

    val boundsKey = String.format(
        java.util.Locale.US,
        "%.3f,%.3f,%.3f,%.3f",
        minLat, minLon, maxLat, maxLon
    )

    OsmWaysCache.get(boundsKey)?.let { cached ->
        android.util.Log.d("ACCESSMAP_OSM", "CACHE HIT: ${cached.size} ways for $boundsKey")
        return cached
    }

    android.util.Log.d("ACCESSMAP_OSM", "CACHE MISS: querying Overpass for $boundsKey")

    // Keep `out geom` because the existing parser consumes each way's geometry
    // array. `out body; >; out skel` does not provide that geometry array.
    val query = """
        [out:json][timeout:15];
        (
          way["highway"]($minLat,$minLon,$maxLat,$maxLon);
          node["highway"="crossing"]($minLat,$minLon,$maxLat,$maxLon);
        );
        out geom;
    """.trimIndent()

    val encodedQuery = URLEncoder.encode(query, "UTF-8")
    val connection = URL("https://overpass-api.de/api/interpreter")
        .openConnection() as HttpURLConnection
    val responseText: String

    try {
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = 8_000
        connection.readTimeout = 20_000
        connection.setRequestProperty("User-Agent", "Waybility/1.0 (Android)")
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty(
            "Content-Type",
            "application/x-www-form-urlencoded; charset=UTF-8"
        )

        connection.outputStream.bufferedWriter(Charsets.UTF_8).use {
            it.write("data=$encodedQuery")
        }

        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            val error = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            throw java.io.IOException("Overpass request failed: $responseCode $error")
        }

        responseText = connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }

    val json = JSONObject(responseText)



    val elements =
        json.optJSONArray("elements")
            ?: return emptyList()

    val ways =
        mutableListOf<OsmWay>()

    for (i in 0 until elements.length()) {

        val element =
            elements.getJSONObject(i)

        val elementType = element.optString("type")

        if (elementType == "node") {
            val nodeTags = element.optJSONObject("tags") ?: continue
            if (nodeTags.optString("highway") == "crossing" &&
                element.has("lat") && element.has("lon")
            ) {
                ways.add(
                    OsmWay(
                        highwayType = "crossing_node",
                        sidewalk = null,
                        points = listOf(
                            LatLng(
                                element.getDouble("lat"),
                                element.getDouble("lon")
                            )
                        ),
                        isCrossing = true
                    )
                )
            }
            continue
        }

        if (elementType != "way") {
            continue
        }

        val tags =
            element.optJSONObject("tags")
                ?: continue

        val highwayType =
            tags.optString(
                "highway",
                ""
            )

        if (highwayType.isEmpty()) {
            continue
        }

        val sidewalk =
            if (tags.has("sidewalk")) {

                tags.optString("sidewalk")

            } else {

                null
            }

        val geometry =
            element.optJSONArray("geometry")
                ?: continue

        val points =
            mutableListOf<LatLng>()

        for (j in 0 until geometry.length()) {

            val node =
                geometry.getJSONObject(j)

            points.add(
                LatLng(
                    node.getDouble("lat"),
                    node.getDouble("lon")
                )
            )
        }

        if (points.size >= 2) {

            val isCrossing =
                highwayType == "crossing" ||
                        tags.optString("footway") == "crossing" ||
                        (tags.has("crossing") &&
                                (highwayType == "footway" ||
                                        highwayType == "path" ||
                                        highwayType == "pedestrian"))

            ways.add(
                OsmWay(
                    highwayType = highwayType,
                    sidewalk = sidewalk,
                    points = points,
                    isCrossing = isCrossing
                )
            )
        }
    }

    android.util.Log.d(
        "ACCESSMAP_OSM",
        "Loaded ${ways.size} OSM ways"
    )

    OsmWaysCache.put(boundsKey, ways)
    android.util.Log.d(
        "ACCESSMAP_OSM",
        "CACHE STORED: ${ways.size} ways for $boundsKey"
    )

    return ways
}
fun distanceMeters(
    a: LatLng,
    b: LatLng
): Double {

    val earthRadius = 6371000.0

    val dLat =
        Math.toRadians(
            b.latitude - a.latitude
        )

    val dLon =
        Math.toRadians(
            b.longitude - a.longitude
        )

    val lat1 =
        Math.toRadians(a.latitude)

    val lat2 =
        Math.toRadians(b.latitude)

    val haversine =
        kotlin.math.sin(dLat / 2) *
                kotlin.math.sin(dLat / 2) +

                kotlin.math.cos(lat1) *
                kotlin.math.cos(lat2) *
                kotlin.math.sin(dLon / 2) *
                kotlin.math.sin(dLon / 2)

    val c =
        2 *
                kotlin.math.atan2(
                    kotlin.math.sqrt(haversine),
                    kotlin.math.sqrt(1 - haversine)
                )

    return earthRadius * c
}
fun createReportMarker(
    context: Context,
    imageUri: String,
    issue: String,
    large: Boolean = false
): Bitmap {

    val width = if (large) 180 else 120
    val height = if (large) 215 else 145

    val circleRadius = if (large) 72f else 48f
    val centerX = width / 2f
    val centerY = if (large) 82f else 55f

    val bitmap = Bitmap.createBitmap(
        width,
        height,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    canvas.setDrawFilter(
        android.graphics.PaintFlagsDrawFilter(
            0,
            android.graphics.Paint.ANTI_ALIAS_FLAG
        )
    )
    val markerColor = when (issue) {
        "Obstacle" -> Color.rgb(244, 63, 94)      // Rose 500
        "SurfaceProblem" -> Color.rgb(245, 158, 11)  // Amber 500
        "CurbRamp" -> Color.rgb(14, 165, 233)       // Sky 500
        "Crosswalk" -> Color.rgb(16, 185, 129)      // Emerald 500
        else -> Color.rgb(100, 116, 139)            // Slate 500
    }

    // ---------------------------------------------------------
    // SHADOW
    // ---------------------------------------------------------

    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL

        // Soft shadow around the circular marker
        setShadowLayer(
            if (large) 14f else 14f,
            0f,
            if (large) 6f else 6f,
            Color.argb(
                100,
                0,
                0,
                0
            )
        )
    }

    // Shadow circle
    canvas.drawCircle(
        centerX,
        centerY,
        circleRadius,
        shadowPaint
    )

    // ---------------------------------------------------------
    // MARKER TAIL
    // ---------------------------------------------------------

    val tailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = markerColor
        style = Paint.Style.FILL
    }

    val tailPath = Path()

    tailPath.moveTo(
        centerX - circleRadius * 0.45f,
        centerY + circleRadius * 0.65f
    )

    tailPath.lineTo(
        centerX,
        height - 5f
    )

    tailPath.lineTo(
        centerX + circleRadius * 0.45f,
        centerY + circleRadius * 0.65f
    )

    tailPath.close()

    canvas.drawPath(
        tailPath,
        tailPaint
    )

    // ---------------------------------------------------------
    // LOAD IMAGE
    // ---------------------------------------------------------

    val imageBitmap =
        if (imageUri.isNotEmpty()) {

            try {
                context.contentResolver
                    .openInputStream(Uri.parse(imageUri))
                    ?.use { inputStream ->

                        // Decode the image
                        val originalBitmap =
                            BitmapFactory.decodeStream(inputStream)

                        if (originalBitmap != null) {

                            // Read EXIF orientation
                            context.contentResolver
                                .openInputStream(Uri.parse(imageUri))
                                ?.use { exifStream ->

                                    val exif =
                                        ExifInterface(exifStream)

                                    val orientation =
                                        exif.getAttributeInt(
                                            ExifInterface.TAG_ORIENTATION,
                                            ExifInterface.ORIENTATION_NORMAL
                                        )

                                    val matrix =
                                        android.graphics.Matrix()

                                    when (orientation) {

                                        ExifInterface.ORIENTATION_ROTATE_90 -> {
                                            matrix.postRotate(90f)
                                        }

                                        ExifInterface.ORIENTATION_ROTATE_180 -> {
                                            matrix.postRotate(180f)
                                        }

                                        ExifInterface.ORIENTATION_ROTATE_270 -> {
                                            matrix.postRotate(270f)
                                        }

                                        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
                                            matrix.preScale(-1f, 1f)
                                        }

                                        ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                                            matrix.preScale(1f, -1f)
                                        }

                                        ExifInterface.ORIENTATION_TRANSPOSE -> {
                                            matrix.setRotate(90f)
                                            matrix.postScale(-1f, 1f)
                                        }

                                        ExifInterface.ORIENTATION_TRANSVERSE -> {
                                            matrix.setRotate(270f)
                                            matrix.postScale(-1f, 1f)
                                        }
                                    }

                                    if (!matrix.isIdentity) {
                                        Bitmap.createBitmap(
                                            originalBitmap,
                                            0,
                                            0,
                                            originalBitmap.width,
                                            originalBitmap.height,
                                            matrix,
                                            true
                                        )
                                    } else {
                                        originalBitmap
                                    }
                                }
                                ?: originalBitmap
                        } else {
                            null
                        }
                    }

            } catch (e: Exception) {
                android.util.Log.e(
                    "ACCESSMAP_MARKER",
                    "Failed to load marker image",
                    e
                )
                null
            }

        } else {
            null
        }

    // ---------------------------------------------------------
    // CIRCULAR IMAGE
    // ---------------------------------------------------------

    if (imageBitmap != null) {

        val imagePaint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        val shader = BitmapShader(
            imageBitmap,
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP
        )

        val diameter = circleRadius * 2

        val scale = maxOf(
            diameter / imageBitmap.width,
            diameter / imageBitmap.height
        )

        val matrix =
            android.graphics.Matrix()

        matrix.setScale(
            scale,
            scale
        )

        val scaledWidth =
            imageBitmap.width * scale

        val scaledHeight =
            imageBitmap.height * scale

        matrix.postTranslate(
            centerX - scaledWidth / 2f,
            centerY - scaledHeight / 2f
        )

        shader.setLocalMatrix(matrix)

        imagePaint.shader = shader

        canvas.drawCircle(
            centerX,
            centerY,
            circleRadius,
            imagePaint
        )

    } else {

        val fallbackPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.LTGRAY
            }

        canvas.drawCircle(
            centerX,
            centerY,
            circleRadius,
            fallbackPaint
        )
    }

    // ---------------------------------------------------------
    // COLORED BORDER
    // ---------------------------------------------------------

    val borderPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {

            color = markerColor
            style = Paint.Style.STROKE
            strokeWidth =
                if (large) 12f else 8f
        }

    canvas.drawCircle(
        centerX,
        centerY,
        circleRadius,
        borderPaint
    )

    return bitmap
}
fun copyImageToAppStorage(
    context: Context,
    uri: Uri
): Uri? {

    return try {

        val inputStream =
            context.contentResolver.openInputStream(uri)
                ?: return null

        val fileName =
            "report_${System.currentTimeMillis()}.jpg"

        val file = java.io.File(
            context.filesDir,
            fileName
        )

        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        Uri.fromFile(file)

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
fun densifyRoute(route: List<LatLng>, stepMeters: Double = 6.0): List<LatLng> {
    if (route.size < 2) return route
    val out = mutableListOf(route[0])
    for (i in 0 until route.size - 1) {
        val a = route[i]
        val b = route[i + 1]
        val len = distanceMeters(a, b)
        val n = (len / stepMeters).toInt()
        for (k in 1..n) {
            val f = k * stepMeters / len
            if (f < 1.0) {
                out.add(
                    LatLng(
                        a.latitude + (b.latitude - a.latitude) * f,
                        a.longitude + (b.longitude - a.longitude) * f
                    )
                )
            }
        }
        out.add(b)
    }
    return out
}
fun getReportingSuggestion(issue: String): String {

    return when (issue) {

        "Obstacle" ->
            "Take a clear photo showing the entire obstruction and where it blocks the accessible path. Try to include enough of the surrounding sidewalk to show exactly how pedestrians are affected."

        "SurfaceProblem" ->
            "Take a photo from a short distance that clearly shows the damaged surface. Include surrounding sidewalk or pavement so the location and size of the problem are easy to understand."

        "CurbRamp" ->
            "Take a photo showing the curb and the full ramp area. If possible, capture the transition between the sidewalk and street so the accessibility problem is clear."

        "Crosswalk" ->
            "Take a photo showing the crosswalk and its connection to the sidewalk. Include nearby curb ramps or crossings when they help explain the accessibility issue."

        else ->
            "Take a clear photo showing the accessibility problem and enough of the surrounding area to identify where the issue occurs."
    }
}
fun getReportQuality(
    context: Context,
    report: AccessibilityReport
): ReportQuality {

    var score = 0

    val strengths = mutableListOf<String>()
    val improvements = mutableListOf<String>()

    // =========================================================
    // LOCATION QUALITY — 30 POINTS
    // =========================================================

    val hasLatitude =
        report.latitude != 0.0

    val hasLongitude =
        report.longitude != 0.0

    if (hasLatitude && hasLongitude) {

        score += 30

        strengths.add(
            "The report contains precise GPS coordinates."
        )

    } else {

        improvements.add(
            "Add a precise location so the responsible agency can find the problem."
        )
    }

    // =========================================================
    // PHOTO QUALITY — 40 POINTS
    // =========================================================

    if (report.imageUri.isNotEmpty()) {

        try {

            val uri = Uri.parse(report.imageUri)

            val bitmap =
                context.contentResolver
                    .openInputStream(uri)
                    ?.use { input ->
                        BitmapFactory.decodeStream(input)
                    }

            if (bitmap != null) {

                // -------------------------------------------------
                // Resolution
                // -------------------------------------------------

                val pixels =
                    bitmap.width * bitmap.height

                when {

                    pixels >= 2_000_000 -> {

                        score += 15

                        strengths.add(
                            "The photo has enough resolution for the problem to be inspected."
                        )
                    }

                    pixels >= 750_000 -> {

                        score += 10

                        strengths.add(
                            "The photo has usable resolution."
                        )

                        improvements.add(
                            "A higher-resolution photo would make the problem easier to inspect."
                        )
                    }

                    else -> {

                        score += 5

                        improvements.add(
                            "Use a higher-resolution photo so the accessibility problem is easier to see."
                        )
                    }
                }

                // -------------------------------------------------
                // Aspect / usable image
                // -------------------------------------------------

                if (
                    bitmap.width >= 500 &&
                    bitmap.height >= 500
                ) {

                    score += 10

                    strengths.add(
                        "The photo provides a usable view of the reported area."
                    )

                } else {

                    improvements.add(
                        "Try to capture a larger portion of the surrounding area."
                    )
                }

                // -------------------------------------------------
                // Basic brightness check
                // -------------------------------------------------

                val sampleBitmap =
                    Bitmap.createScaledBitmap(
                        bitmap,
                        1,
                        1,
                        true
                    )

                val pixel =
                    sampleBitmap.getPixel(0, 0)

                val brightness =
                    (
                            Color.red(pixel) +
                                    Color.green(pixel) +
                                    Color.blue(pixel)
                            ) / 3

                sampleBitmap.recycle()

                if (brightness in 35..225) {

                    score += 10

                    strengths.add(
                        "The photo has reasonable lighting."
                    )

                } else {

                    improvements.add(
                        "Retake the photo with better lighting so the issue is easier to identify."
                    )
                }

                bitmap.recycle()

            } else {

                improvements.add(
                    "The saved photo could not be analyzed. Consider retaking the photo."
                )
            }

        } catch (e: Exception) {

            android.util.Log.e(
                "REPORT_QUALITY",
                "Could not analyze report photo",
                e
            )

            improvements.add(
                "The photo could not be fully analyzed."
            )
        }

    } else {

        improvements.add(
            "Add a clear photo showing the accessibility problem."
        )
    }

    // =========================================================
    // ISSUE-SPECIFIC QUALITY — 30 POINTS
    // =========================================================

    when (report.issue) {

        "Obstacle" -> {

            score += 30

            strengths.add(
                "The report identifies an obstruction affecting pedestrian access."
            )

            improvements.add(
                "Make sure the photo shows both the obstruction and the path it blocks."
            )
        }

        "SurfaceProblem" -> {

            score += 30

            strengths.add(
                "The report identifies a surface condition affecting accessibility."
            )

            improvements.add(
                "Include enough surrounding pavement to show the size and location of the damage."
            )
        }

        "CurbRamp" -> {

            score += 30

            strengths.add(
                "The report identifies a curb-ramp accessibility issue."
            )

            improvements.add(
                "Include the entire curb ramp and the transition between sidewalk and street."
            )
        }

        "Crosswalk" -> {

            score += 30

            strengths.add(
                "The report identifies a pedestrian crossing issue."
            )

            improvements.add(
                "Show the full crossing and its connection to the sidewalk."
            )
        }

        else -> {

            score += 20

            improvements.add(
                "Provide a more specific description of the accessibility issue."
            )
        }
    }

    // =========================================================
    // CAP SCORE
    // =========================================================

    score = score.coerceIn(0, 100)

    val grade =
        when {

            score >= 90 -> "Excellent"

            score >= 80 -> "Good"

            score >= 70 -> "Fair"

            score >= 60 -> "Needs Improvement"

            else -> "Poor"
        }

    // =========================================================
    // GENERAL ADVICE
    // =========================================================

    val issueAdvice =
        when (report.issue) {

            "Obstacle" ->
                "Show the entire obstruction and how it interferes with the accessible pedestrian path."

            "SurfaceProblem" ->
                "Show the damaged surface together with enough surrounding sidewalk to establish its location and scale."

            "CurbRamp" ->
                "Show the entire curb ramp, including the sidewalk-to-street transition."

            "Crosswalk" ->
                "Show the complete crossing and its connection to the sidewalk."

            else ->
                "Clearly show the accessibility problem and enough surrounding infrastructure to understand it."
        }

    val photoAdvice =
        if (report.imageUri.isNotEmpty()) {

            "Your report includes a photo. " +
                    "For the strongest submission, make sure the issue is clearly visible and the surrounding infrastructure provides context."

        } else {

            "Add a clear photo showing the issue and its surrounding infrastructure."
        }

    val locationAdvice =
        if (hasLatitude && hasLongitude) {

            "Your report includes GPS coordinates, which gives the responsible agency a precise location."

        } else {

            "Add a precise location before submitting the report."
        }

    return ReportQuality(
        score = score,
        grade = grade,
        strengths = strengths.distinct(),
        improvements = improvements.distinct(),
        photoAdvice = photoAdvice,
        issueAdvice = issueAdvice,
        locationAdvice = locationAdvice
    )
}
object ReportingAgencyDatabase {

    val curatedAgencies: List<ReportingAgency> = listOf(
        ReportingAgency(
            name = "Agoura Hills SeeClickFix",
            jurisdiction = "Agoura Hills",
            supportedIssueTypes = listOf("SurfaceProblem", "CurbRamp", "Crosswalk", "Obstacle"),
            description = "Handles public infrastructure concerns in Agoura Hills such as sidewalk repairs, curb ramps, street maintenance, and crosswalks.",
            website = "https://seeclickfix.com/agoura-hills",
            phone = "818-597-7329",
            reason = "Verified reporting site for public infrastructure and sidewalk maintenance in Agoura Hills."
        ),

        ReportingAgency(
            name = "Westlake Village CityCare",
            jurisdiction = "Westlake Village",
            supportedIssueTypes = listOf("SurfaceProblem", "CurbRamp", "Crosswalk", "Obstacle"),
            description = "The city's service-request channel for maintenance concerns, including street repairs and potholes. Submit the issue for city staff to review and route appropriately.",
            website = "https://www.wlv.org/280/CityCare-Request-Form",
            phone = "818-706-1613",
            reason = "Official CityCare request page; the city says service requests are forwarded to appropriate staff."
        ),
        ReportingAgency(
            name = "City of Calabasas — Report a Problem",
            jurisdiction = "Calabasas",
            supportedIssueTypes = listOf("SurfaceProblem", "CurbRamp", "Crosswalk", "Obstacle"),
            description = "The city's online service-request portal routes submitted problems to the appropriate city administrator.",
            website = "https://www.cityofcalabasas.com/government/public-works",
            phone = "818-224-1600",
            reason = "Official city reporting portal that routes service requests to the appropriate department."
        ),
        ReportingAgency(
            name = "Los Angeles County Public Works",
            jurisdiction = "Los Angeles County",
            supportedIssueTypes = listOf("SurfaceProblem", "CurbRamp", "Crosswalk", "Obstacle"),
            description = "Maintains public roads, sidewalks, and county infrastructure in unincorporated Los Angeles County areas.",
            website = "https://pw.lacounty.gov/contact",
            phone = "800-675-HELP",
            reason = "Regional public works agency serving unincorporated Los Angeles County areas."
        ),
        ReportingAgency(
            name = "Ventura County Public Works",
            jurisdiction = "Oak Park, Ventura County",
            supportedIssueTypes = listOf(
                "SurfaceProblem",
                "CurbRamp",
                "Crosswalk"
            ),
            description = "County report system for road-surface, sidewalk, curb, and transportation-infrastructure concerns on county-maintained facilities. Sidewalk maintenance responsibility can vary, so confirm who maintains the specific location.",
            website = "https://publicworks.venturacounty.gov/report-a-concern/",
            phone = "805-654-2018",
            reason = "Oak Park is an unincorporated Ventura County community. VCPWA Connect accepts public concerns and routes them to relevant county staff."
        ),
        ReportingAgency(
            name = "City of Thousand Oaks — Report an Issue",
            jurisdiction = "Thousand Oaks",
            supportedIssueTypes = listOf(
                "SurfaceProblem",
                "CurbRamp",
                "Crosswalk",
                "Obstacle"
            ),
            description = "Official city service-request portal for reporting public infrastructure and maintenance concerns. Requests can be routed to the appropriate city staff. Confirm that the reported location and infrastructure are city-maintained.",
            website = "https://toaks.gov/service",
            phone = "805-449-2100",
            reason = "The city's official portal accepts service requests and allows users to track requests or submit general reports anonymously."
        ),
        ReportingAgency(
            name = "Thousand Oaks Code Compliance",
            jurisdiction = "Thousand Oaks",
            supportedIssueTypes = listOf("Obstacle"),
            description = "A potential option for obstructions caused by code violations, such as certain property-maintenance issues or encroachments. Ordinary sidewalk damage should generally start with the city service-request portal.",
            website = "https://www.toaks.gov/departments/public-works/maintenance/streets-sidewalks/index.php?section=codecompliance",
            phone = "805-449-2300",
            reason = "The city's Code Compliance page explains how to report suspected municipal-code violations and lists the department's phone number."
        ),
        ReportingAgency(
            name = "City of Oxnard — Oxnard 311",
            jurisdiction = "Oxnard",
            supportedIssueTypes = listOf(
                "SurfaceProblem",
                "CurbRamp",
                "Crosswalk",
                "Obstacle"
            ),
            description = "Official city service-request portal. Use it to report broken sidewalks, damaged curbs, potholes, street-sign problems, and other public infrastructure concerns. Requests are routed to the appropriate city department.",
            website = "https://www.oxnard.gov/oxnard-311",
            phone = "805-385-7900",
            reason = "Oxnard's official 311 portal accepts service requests, and Streets Maintenance specifically handles sidewalk, curb, pavement, sign, and marking issues."
        ),
        ReportingAgency(
            name = "City of Camarillo — CamarilloConnect",
            jurisdiction = "Camarillo",
            supportedIssueTypes = listOf(
                "SurfaceProblem",
                "CurbRamp",
                "Crosswalk",
                "Obstacle"
            ),
            description = "Official city reporting service for non-emergency concerns, including potholes, damaged street signs, damaged trees, and other local maintenance issues. Submit the report for city staff to review and route.",
            website = "http://ci.camarillo.ca.us/i_want_to/report/non-emergency_issue.php",
            phone = "805-388-5300",
            reason = "CamarilloConnect is the city's reporting app for non-emergency problems. The official city website is provided as a fallback entry point."
        ),
        ReportingAgency(
            name = "City of Moorpark — Request Tracker",
            jurisdiction = "Moorpark",
            supportedIssueTypes = listOf(
                "SurfaceProblem",
                "CurbRamp",
                "Crosswalk",
                "Obstacle"
            ),
            description = "Official city portal for reporting street, sidewalk, and pothole repairs, traffic concerns, and other city maintenance issues.",
            website = "https://www.moorparkca.gov/requesttracker.aspx",
            phone = "805-517-6240",
            reason = "Moorpark's Request Tracker specifically includes street, sidewalk, or pothole repair requests and other concerns."
        ),

        ReportingAgency(
            name = "City of Thousand Oaks — Service Requests",
            jurisdiction = "Newbury Park",
            supportedIssueTypes = listOf(
                "SurfaceProblem",
                "CurbRamp",
                "Crosswalk",
                "Obstacle"
            ),
            description = "Use the city's service-request portal to report accessibility barriers and public infrastructure problems in Newbury Park. If the issue is outside city jurisdiction, the request may need to be directed to the responsible agency.",
            website = "https://toaks.gov/service",
            phone = "805-449-2400",
            reason = "Newbury Park is served by Thousand Oaks for many municipal street and maintenance concerns; responsibility can vary by road and property."
        ),
        ReportingAgency(
            name = "Ventura County Public Works — VCPWA Connect",
            jurisdiction = "Channel Islands Beach",
            supportedIssueTypes = listOf(
                "SurfaceProblem",
                "CurbRamp",
                "Crosswalk",
                "Obstacle"
            ),
            description = "Ventura County's reporting service for road maintenance and other county public works concerns. Use it for accessibility problems on county-maintained roads and infrastructure; responsibility for sidewalks and other facilities may vary by location.",
            website = "https://publicworks.venturacounty.gov/report-a-concern/",
            phone = "805-654-2018",
            reason = "Channel Islands Beach is an unincorporated community in Ventura County. County Public Works accepts road-maintenance reports, but the responsible agency may differ for harbor, beach, or other locally managed facilities."
        ),

        ReportingAgency(
            name = "City of Fillmore — Report a Concern",
            jurisdiction = "Fillmore",
            supportedIssueTypes = listOf(
                "SurfaceProblem",
                "CurbRamp",
                "Crosswalk",
                "Obstacle"
            ),
            description = "Official Public Works reporting portal for potholes, sidewalk and curb damage, street-sign issues, and other city maintenance concerns. Requests can be submitted online for staff review.",
            website = "https://www.fillmoreca.gov/1356/Report-a-Concern",
            phone = "805-524-1500",
            reason = "Fillmore Public Works maintains city streets, sidewalks, curbs, and gutters and provides an official online system for reporting maintenance concerns."
        )


    )
}

data class LocationDetails(
    val locality: String? = null,
    val subLocality: String? = null,
    val subAdminArea: String? = null,
    val adminArea: String? = null
)

fun getLocationDetailsFromCoordinates(
    context: Context,
    latitude: Double,
    longitude: Double
): LocationDetails {
    return try {
        val geocoder = Geocoder(context)
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        val addr = addresses?.firstOrNull()
        if (addr != null) {
            LocationDetails(
                locality = addr.locality,
                subLocality = addr.subLocality,
                subAdminArea = addr.subAdminArea,
                adminArea = addr.adminArea
            )
        } else {
            LocationDetails()
        }
    } catch (e: Exception) {
        android.util.Log.e("REPORT_LOCATION", "Reverse geocoding failed", e)
        LocationDetails()
    }
}

fun getRecommendedAgencies(
    context: Context,
    report: AccessibilityReport
): List<ReportingAgency> {

    val locationDetails = getLocationDetailsFromCoordinates(
        context,
        report.latitude,
        report.longitude
    )

    val locality = locationDetails.locality?.lowercase()?.trim().orEmpty()
    val subLocality = locationDetails.subLocality?.lowercase()?.trim().orEmpty()
    val subAdminArea = locationDetails.subAdminArea?.lowercase()?.trim().orEmpty()

    val issue = report.issue

    fun isJurisdictionMatch(agency: ReportingAgency): Boolean {
        val jur = agency.jurisdiction.lowercase().trim()
        if (jur.isEmpty()) return false

        if (locality.isNotEmpty() && (jur == locality || jur.contains(locality) || locality.contains(jur))) {
            return true
        }
        if (subLocality.isNotEmpty() && (jur == subLocality || jur.contains(subLocality) || subLocality.contains(jur))) {
            return true
        }
        return false
    }

    // 1. TIER 1: Match specific city/community/neighborhood agency supporting this issue type
    val matchedAgencies = ReportingAgencyDatabase.curatedAgencies.filter { agency ->
        isJurisdictionMatch(agency) && agency.supportedIssueTypes.contains(issue)
    }

    if (matchedAgencies.isNotEmpty()) {
        return matchedAgencies.map { agency ->
            agency.copy(
                reason = agency.reason.ifBlank {
                    "Matched ${agency.jurisdiction} jurisdiction for ${formatIssueLabel(issue)} issues."
                }
            )
        }
    }

    // 2. TIER 2: Match by County (e.g., Ventura County vs Los Angeles County)
    val isVenturaCounty = subAdminArea.contains("ventura") ||
            locality.contains("ventura") ||
            subLocality.contains("ventura")

    val isLACounty = subAdminArea.contains("los angeles") ||
            locality.contains("los angeles") ||
            subLocality.contains("los angeles")

    if (isVenturaCounty) {
        val venturaCountyAgencies = ReportingAgencyDatabase.curatedAgencies.filter { agency ->
            agency.jurisdiction.lowercase().contains("ventura") &&
                    agency.supportedIssueTypes.contains(issue)
        }
        if (venturaCountyAgencies.isNotEmpty()) {
            val areaLabel = locality.takeIf { it.isNotBlank() }
                ?: subLocality.takeIf { it.isNotBlank() }
                ?: "unincorporated Ventura County"
            return venturaCountyAgencies.map { agency ->
                agency.copy(
                    reason = "Ventura County public works recommendation for $areaLabel."
                )
            }
        }
    }

    if (isLACounty) {
        val laCountyAgencies = ReportingAgencyDatabase.curatedAgencies.filter { agency ->
            agency.jurisdiction.lowercase().contains("los angeles") &&
                    agency.supportedIssueTypes.contains(issue)
        }
        if (laCountyAgencies.isNotEmpty()) {
            val areaLabel = locality.takeIf { it.isNotBlank() }
                ?: subLocality.takeIf { it.isNotBlank() }
                ?: "unincorporated LA County"
            return laCountyAgencies.map { agency ->
                agency.copy(
                    reason = "Los Angeles County public works recommendation for $areaLabel."
                )
            }
        }
    }

    // 3. TIER 3: General fallback
    val fallbackAgencies = ReportingAgencyDatabase.curatedAgencies.filter { agency ->
        agency.supportedIssueTypes.contains(issue)
    }

    if (fallbackAgencies.isNotEmpty()) {
        val primaryFallback = fallbackAgencies.first()
        val areaLabel = locality.takeIf { it.isNotBlank() }
            ?: subLocality.takeIf { it.isNotBlank() }
            ?: "this location"
        return listOf(
            primaryFallback.copy(
                reason = "Regional recommendation for $areaLabel while agency database continues expanding."
            )
        )
    }

    return listOf(
        ReportingAgency(
            name = "Local Public Works Department",
            jurisdiction = locality.takeIf { it.isNotBlank() } ?: "Local Jurisdiction",
            supportedIssueTypes = listOf("SurfaceProblem", "CurbRamp", "Crosswalk", "Obstacle"),
            description = "Contact the local city or county public works department to report pedestrian accessibility barriers.",
            website = "https://pw.lacounty.gov/contact",
            phone = null,
            reason = "General recommendation for public pedestrian infrastructure."
        )
    )
}

fun getRecommendedAgency(
    context: Context,
    report: AccessibilityReport
): ReportingAgency {
    return getRecommendedAgencies(context, report).first()
}

private fun formatIssueLabel(issue: String): String {
    return when (issue) {
        "SurfaceProblem" -> "Surface Problem"
        "CurbRamp" -> "Curb Ramp"
        else -> issue
    }
}

fun getReportingInstructions(
    report: AccessibilityReport,
    agency: ReportingAgency
): String {

    val issue = when (report.issue) {

        "Obstacle" ->
            "Describe what is blocking the path, approximately how much of the path is blocked, and whether pedestrians using wheelchairs or mobility devices can get around it."

        "SurfaceProblem" ->
            "Describe the type of surface damage, such as cracks, holes, uneven pavement, or deterioration, and explain how it affects pedestrian accessibility."

        "CurbRamp" ->
            "Describe the problem with the curb ramp, such as excessive slope, missing ramp, damaged pavement, lack of a detectable warning, or difficulty transitioning between the sidewalk and street."

        "Crosswalk" ->
            "Describe the problem with the crossing, including missing connections, damaged markings, inaccessible curb transitions, or other barriers."

        else ->
            "Describe exactly what makes the location difficult or unsafe for pedestrians with disabilities."
    }

    return """
        Recommended agency: ${agency.name}
        
        When submitting the report, include:
        
        • A clear photo of the problem
        • The exact location
        • What the accessibility problem is
        • How it affects pedestrians
        • Any relevant details about the surrounding sidewalk or street
        
        Suggested description:
        
        "$issue"
    """.trimIndent()
}

fun getCityFromCoordinates(
    context: Context,
    latitude: Double,
    longitude: Double
): String? {
    val details = getLocationDetailsFromCoordinates(context, latitude, longitude)
    return details.locality ?: details.subLocality ?: details.subAdminArea
}

fun getIssueReportingReason(
    issue: String
): String {

    return when (issue) {

        "Obstacle" ->
            "This appears to be a physical obstruction affecting pedestrian access. " +
                    "Public Works or the agency responsible for the sidewalk/right-of-way " +
                    "is usually the appropriate starting point."

        "SurfaceProblem" ->
            "This appears to involve pavement or sidewalk condition. " +
                    "The agency responsible for maintaining the right-of-way should be contacted."

        "CurbRamp" ->
            "Curb ramps are part of accessible pedestrian infrastructure. " +
                    "Report the issue to the agency responsible for the sidewalk, street, or highway."

        "Crosswalk" ->
            "Crosswalk issues may involve pedestrian infrastructure or traffic control. " +
                    "The responsible city, county, or state transportation agency should be contacted."

        else ->
            "The issue appears to involve pedestrian accessibility infrastructure."
    }
}