# ShoreLine: Course Syllabus Coverage Matrix

This document tracks every syllabus concept required by the university curriculum, mapped to its exact code file, stage of implementation, and status.

---

## Section A: Android Studio, Gradle, Manifest, Lifecycle, Intents

| Syllabus Concept | Specific Requirement | Stage | File / Resource Path | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Project Structure** | `app/`, `manifests/`, `java/`, `res/`, Gradle scripts | Stage 0 | Project root & subfolders | **COMPLETED** |
| **Gradle Configuration** | Module vs Project `build.gradle`, compileSdk, minSdk, targetSdk, Groovy DSL | Stage 0 | [`build.gradle`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/build.gradle), [`app/build.gradle`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/build.gradle) | **COMPLETED** |
| **ViewBinding** | `buildFeatures { viewBinding true }`, null-safe UI binding without `findViewById` | Stage 0 | [`app/build.gradle`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/build.gradle), [`SplashActivity.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/splash/SplashActivity.java) | **COMPLETED** |
| **AndroidManifest** | `<application>`, `<activity>`, launcher intent-filter, `exported` security attribute | Stage 0 | [`AndroidManifest.xml`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/AndroidManifest.xml) | **COMPLETED** |
| **Permissions Architecture** | Normal permissions vs Dangerous (runtime flow), Android 12/13/14+ attributes | Stage 0 | [`AndroidManifest.xml`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/AndroidManifest.xml) | **COMPLETED** |
| **Package Visibility** | `<queries>` element for explicit dialer, SMS, and geolocation package resolution | Stage 0 | [`AndroidManifest.xml`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/AndroidManifest.xml) | **COMPLETED** |
| **Activity Lifecycle** | All 7 callbacks (`onCreate` ... `onDestroy`), `BaseActivity` logging | Stage 0 | [`BaseActivity.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/base/BaseActivity.java) | **COMPLETED** |
| **Lifecycle Lab Screen** | Live diagnostic screen showing real-time lifecycle transitions | Stage 0 | [`LifecycleLabActivity.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/lifecycle/LifecycleLabActivity.java) | **COMPLETED** |
| **State Preservation** | `onSaveInstanceState`, `onRestoreInstanceState`, screen rotation handling | Stage 0 | [`LifecycleLabActivity.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/lifecycle/LifecycleLabActivity.java) | **COMPLETED** |
| **Event Listeners** | Lambda vs `View.OnClickListener` vs `android:onClick` in XML (with viva comparison) | Stage 0 | [`LifecycleLabActivity.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/lifecycle/LifecycleLabActivity.java) | **COMPLETED** |
| **Advanced Listeners** | `TextWatcher`, `OnLongClickListener`, `CompoundButton.OnCheckedChangeListener` | Stage 0 | [`LifecycleLabActivity.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/lifecycle/LifecycleLabActivity.java) | **COMPLETED** |
| **Intents (Explicit)** | Multi-activity flow (Splash -> Login -> Register -> Home) with flags & extras | Stage 1 | [`SplashActivity.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/splash/SplashActivity.java), [`LoginActivity.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/auth/LoginActivity.java) | **COMPLETED** |
| **Intents (Implicit)** | `ACTION_DIAL`, `ACTION_SENDTO`, `ACTION_VIEW`, `ACTION_SEND` | Stage 6 | `app/src/main/java/.../util/IntentHelper.java` | Stage 6 |
| **XML Layouts & Themes** | `ConstraintLayout`, `ScrollView`, `CardView`, `dimens.xml`, `colors.xml`, `themes.xml` | Stage 0/1 | `res/values/`, `res/layout/` | **COMPLETED** |
| **Localization** | Bangla-first + English UI (`values/strings.xml` and `values-bn/strings.xml`) | Stage 0/1 | [`strings.xml`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/res/values/strings.xml), [`values-bn/strings.xml`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/res/values-bn/strings.xml) | **COMPLETED** |

---

## Section B: UI, RecyclerView, Custom Views, UX

| Syllabus Concept | Specific Requirement | Stage | File / Resource Path | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Accessibility Targets** | Minimum 48dp touch targets on all buttons and interactive elements | Stage 0/1 | [`dimens.xml`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/res/values/dimens.xml), [`activity_login.xml`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/res/layout/activity_login.xml) | **COMPLETED** |
| **Vector Drawables** | Custom XML vector drawables (resolution-independent icons) | Stage 0 | [`ic_shoreline_logo.xml`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/res/drawable/ic_shoreline_logo.xml) | **COMPLETED** |
| **Custom View 1** | `CapacityGaugeView` (subclass View, custom `attrs.xml`, `onDraw`, `onMeasure`) | Stage 2 | [`CapacityGaugeView.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/view/CapacityGaugeView.java) | **COMPLETED** |
| **Custom View 2** | `NearbyRadarView` / Route-risk meter drawn with Canvas/Paint | Stage 6 | `app/src/main/java/.../ui/view/NearbyRadarView.java` | Stage 6 |
| **RecyclerView 1** | Vessel search list (custom ViewHolder, ItemDecoration, DiffUtil) | Stage 2 | [`VesselAdapter.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/vessel/VesselAdapter.java) | **COMPLETED** |
| **RecyclerView 2** | Overload reports list (card layout, empty states) | Stage 3 | [`ReportAdapter.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/report/ReportAdapter.java) | **COMPLETED** |
| **RecyclerView 3** | River route steps (multi-view-type adapter: ghat step vs weather warning row) | Stage 4 | [`RouteStepAdapter.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/route/RouteStepAdapter.java) | **COMPLETED** |
| **RecyclerView 4** | SOS Inbox / Admin incident logs (swipe-to-action / long-press menu) | Stage 7 | `app/src/main/java/.../ui/admin/SosInboxAdapter.java` | Stage 7 |
| **Layout Managers** | `LinearLayoutManager` & `GridLayoutManager` (dashboard tiles) | Stage 2/7 | [`VesselCheckActivity.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/vessel/VesselCheckActivity.java) | **COMPLETED (Stage 2)** |
| **Reusable Components** | Shared toolbar via `<include>`, reusable card layouts | Stage 2 | [`layout_toolbar.xml`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/res/layout/layout_toolbar.xml) | **COMPLETED** |
| **Waypoint Weather List** | RecyclerView rendering hourly weather metrics for all intermediate route nodes | Stage 4 Ext | [`RouteNodeWeatherAdapter.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/route/RouteNodeWeatherAdapter.java), [`item_route_node_weather.xml`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/res/layout/item_route_node_weather.xml) | **COMPLETED** |
| **Reusable Weather Card** | Custom ViewBinding layout for origin, destination, and current location | Stage 4 Ext | [`layout_terminal_weather_card.xml`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/res/layout/layout_terminal_weather_card.xml) | **COMPLETED** |


---

## Section C: Services, Broadcast Receivers, Content Providers

| Syllabus Concept | Specific Requirement | Stage | File / Resource Path | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Foreground Service** | Journey GPS tracking, notification channel, `FOREGROUND_SERVICE_LOCATION` | Stage 5 | `app/src/main/java/.../service/JourneyTrackingService.java` | Stage 5 |
| **Bound Service** | Binder IPC interface communicating live journey stats to Activity | Stage 5 | `app/src/main/java/.../service/JourneyTrackingService.java` | Stage 5 |
| **WorkManager** | Guaranteed background offline queue sync with network constraints | Stage 3 | [`ReportUploadWorker.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/worker/ReportUploadWorker.java) | **COMPLETED** |
| **Boot BroadcastReceiver** | `RECEIVE_BOOT_COMPLETED` receiver to restore tracking / offline alerts | Stage 8 | `app/src/main/java/.../receiver/BootReceiver.java` | Stage 8 |
| **Connectivity Receiver** | Dynamic NetworkCallback / BroadcastReceiver for immediate auto-sync | Stage 3/8 | `app/src/main/java/.../receiver/ConnectivityReceiver.java` | Stage 3/8 |
| **Custom Broadcast** | In-app package-restricted broadcast for SOS detection updates | Stage 6 | `app/src/main/java/.../receiver/SosAlertReceiver.java` | Stage 6 |
| **Battery Broadcast** | `ACTION_BATTERY_LOW` warning during active river journey | Stage 8 | `app/src/main/java/.../receiver/BatteryStateReceiver.java` | Stage 8 |
| **Custom ContentProvider** | Exposes saved river routes & emergency contacts (`query`, `insert`, etc.) | Stage 8 | `app/src/main/java/.../provider/ShoreLineContentProvider.java` | Stage 8 |
| **ContentProvider Consumer**| Consumer activity demonstrating `ContentResolver` queries | Stage 8 | `app/src/main/java/.../ui/provider/ProviderTestActivity.java` | Stage 8 |

---

## Section D: Networking, Data Handling, Advanced APIs, Firebase

| Syllabus Concept | Specific Requirement | Stage | File / Resource Path | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Room Local Database** | Entities, DAOs, `@TypeConverters`, singleton, Room vs SQLite comparison | Stage 1 | [`ShoreLineDatabase.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/data/local/ShoreLineDatabase.java), `data/local/` | **COMPLETED** |
| **Offline Queue Pattern** | Pending sync flag (`isSynced = false`) in Room entity for offline reports | Stage 1 | [`OverloadReportEntity.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/data/local/entity/OverloadReportEntity.java) | **COMPLETED** |
| **Session Management** | `SharedPreferences` session cache for active user ID, email, role, and kin contact | Stage 1 | [`SessionManager.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/util/SessionManager.java) | **COMPLETED** |
| **Database Migrations** | Schema versioning and migration strategy | Stage 1 | [`ShoreLineDatabase.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/data/local/ShoreLineDatabase.java) | **COMPLETED** |
| **Firebase Auth & Roles** | Dual-layer authentication: Firebase Auth + Firestore role mapping + Room offline fallback | Stage 1 | [`AuthRepository.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/data/remote/AuthRepository.java) | **COMPLETED** |
| **Cloud Firestore** | Offline caching enabled, collections (`users`, `vessels`, `reports`, `sos`, `alerts`) | Stage 1 | [`FirestoreManager.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/data/remote/FirestoreManager.java) | **COMPLETED** |
| **Firebase Realtime DB** | High-frequency live passenger position updates for family tracker | Stage 5 | `app/src/main/java/.../data/remote/LiveTrackingManager.java` | Stage 5 |
| **FCM Topic Alerts** | Storm signal push notifications via topic subscriptions | Stage 7 | `app/src/main/java/.../service/ShoreLineMessagingService.java` | Stage 7 |
| **HTTP Networking** | Plain `HttpURLConnection` + `ExecutorService` + `Handler` (Open-Meteo) | Stage 4 | [`WeatherApiClient.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/network/WeatherApiClient.java) | **COMPLETED** |
| **JSON Manual Parsing** | `org.json.JSONObject` manual parsing into Java model objects | Stage 4 | [`WeatherParser.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/network/WeatherParser.java) | **COMPLETED** |
| **XML Manual Parsing** | `XmlPullParser` parsing river ghats & routes from `res/xml` seed dataset | Stage 4 | [`GhatXmlParser.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/util/GhatXmlParser.java) | **COMPLETED** |
| **Graph Algorithms** | Dijkstra pathfinding on river network with weather edge-weights | Stage 4 | [`RiverGraphRouter.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/graph/RiverGraphRouter.java) | **COMPLETED** |
| **OpenStreetMap (osmdroid)**| Offline-capable interactive river map with polylines and custom markers | Stage 4 | [`RiverMapActivity.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/map/RiverMapActivity.java) | **COMPLETED** |
| **Nearby Connections** | P2P_CLUSTER offline SOS mesh relay (hop count, dedupe, forward) | Stage 6 | `app/src/main/java/.../nearby/NearbySosRelayManager.java` | Stage 6 |
| **Barcode / QR Scanner** | ZXing QR code scanner for launch registration lookup | Stage 2 | [`VesselCheckActivity.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/vessel/VesselCheckActivity.java) | **COMPLETED** |
| **Fused Location Provider** | High-accuracy GPS tracking with runtime permissions & fallback | Stage 3/5 | [`LocationHelper.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/util/LocationHelper.java) | **COMPLETED (Stage 3)** |
| **Multi-Coordinate Batch REST** | Single-request Open-Meteo multi-location batching with 1:1 index mapping | Stage 4 Ext | [`WeatherRepository.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/data/repository/WeatherRepository.java), [`WeatherParser.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/network/WeatherParser.java) | **COMPLETED** |
| **Room Forecast Caching** | Composite key `(nodeId, forecastTime)`, 45-min cache freshness window & offline fallback | Stage 4 Ext | [`NodeWeatherEntity.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/data/local/entity/NodeWeatherEntity.java), [`NodeWeatherDao.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/data/local/dao/NodeWeatherDao.java) | **COMPLETED** |
| **Weather Risk Multiplier & Dijkstra** | Heuristic edge weighting: `baseDistance * Math.max(fromMult, toMult)` & route comparison | Stage 4 Ext | [`RiskScorer.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/util/RiskScorer.java), [`RiverGraphRouter.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/graph/RiverGraphRouter.java) | **COMPLETED** |
| **Material Time & Date Picker** | Departure-time hourly forecast selection & estimated destination arrival calculation | Stage 4 Ext | [`SafeRouteActivity.java`](file:///c:/Users/Abrar%20Jawad/OneDrive/Desktop/AndroidApk/app/src/main/java/com/shoreline/app/ui/route/SafeRouteActivity.java) | **COMPLETED** |

