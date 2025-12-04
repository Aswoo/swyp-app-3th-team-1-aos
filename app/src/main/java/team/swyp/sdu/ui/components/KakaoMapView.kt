package team.swyp.sdu.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.opengl.GLException
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.platform.LocalContext
import com.kakao.vectormap.graphics.gl.GLSurfaceView
import java.io.File
import java.io.FileOutputStream
import java.nio.IntBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.kakao.vectormap.route.RouteLineManager
import com.kakao.vectormap.route.RouteLineLayer
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineStyle
import com.kakao.vectormap.route.RouteLineStyles
import com.kakao.vectormap.route.RouteLineStylesSet
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.opengles.GL10
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.camera.CameraPosition
import com.kakao.vectormap.GestureType
import team.swyp.sdu.data.model.LocationPoint
import team.swyp.sdu.presentation.viewmodel.CameraSettings
import team.swyp.sdu.presentation.viewmodel.KakaoMapViewModel
import team.swyp.sdu.presentation.viewmodel.KakaoMapUiState
import team.swyp.sdu.presentation.viewmodel.MapRenderState
import timber.log.Timber

/**
 * 상수 정의
 */
private object MapSnapshotConstants {
    const val TILE_LOADING_DELAY_MS = 500L // fallback용 타일 로딩 대기 시간
    const val FALLBACK_DELAY_MS = 100L
    const val ROUTE_LINE_WIDTH = 16f
    const val ROUTE_LINE_COLOR = "#4285F4"
    const val RENDER_FRAMES_TO_WAIT = 5 // GPU 렌더링 완료를 위해 대기할 프레임 수 (타일 로딩 보장)
    const val TILE_LOADING_EXTRA_DELAY_MS = 300L // 마지막 프레임 후 추가 타일 로딩 대기 시간
}

/**
 * KakaoMap을 Compose에서 사용하기 위한 컴포저블
 * UI 렌더링과 MapView 제어만 담당하며, 비즈니스 로직은 ViewModel에서 처리합니다.
 *
 * @param locations 경로를 표시할 위치 좌표 리스트
 * @param modifier Modifier
 * @param viewModel KakaoMapViewModel (옵션, 없으면 자동 생성)
 * @param onSnapshotCaptured drawPath 완료 후 스냅샷이 생성되면 호출되는 콜백 (옵션)
 * @param showMapView MapView를 화면에 표시할지 여부 (false면 스냅샷만 표시)
 */
@Composable
fun KakaoMapView(
    locations: List<LocationPoint>,
    modifier: Modifier = Modifier,
    viewModel: KakaoMapViewModel = hiltViewModel(),
    onSnapshotCaptured: ((Bitmap?) -> Unit)? = null,
    showMapView: Boolean = false,
) {
    val context = LocalContext.current

    // ViewModel 상태 구독
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snapshotState by viewModel.snapshotState.collectAsStateWithLifecycle()
    val renderState by viewModel.renderState.collectAsStateWithLifecycle()

    // MapView 관련 상태 (UI 제어용)
    var kakaoMapInstance by remember {
        mutableStateOf<KakaoMap?>(null)
    }
    var mapViewRef by remember {
        mutableStateOf<MapView?>(null)
    }
    var mapStarted by remember {
        mutableStateOf(false)
    }

    // ViewModel에 locations 전달
    LaunchedEffect(locations) {
        viewModel.setLocations(locations)
    }

    // 스냅샷 상태 변경 시 콜백 호출
    LaunchedEffect(snapshotState) {
        snapshotState?.let { bitmap ->
            onSnapshotCaptured?.invoke(bitmap)
        }
    }

    // 렌더링 상태에 따라 작업 수행
    LaunchedEffect(renderState) {
        val kakaoMap = kakaoMapInstance
        val mapView = mapViewRef
        if (kakaoMap == null || mapView == null) return@LaunchedEffect

        when (renderState) {
            is MapRenderState.DrawingPath -> {
                val currentUiState = uiState
                if (currentUiState is KakaoMapUiState.Ready && currentUiState.shouldDrawPath) {
                    Timber.d("경로 그리기 시작")
                    drawPath(kakaoMap, currentUiState.locations, viewModel, mapView)
                } else {
                    // 경로가 없으면 바로 Ready 상태로
                    viewModel.onPathDrawComplete()
                }
            }
            is MapRenderState.Ready -> {
                Timber.d("경로 그리기 완료 - 스냅샷 생성 시작")
                captureSnapshot(mapView, viewModel, context)
            }
            else -> {}
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // MapView는 렌더링을 위해 필요하지만 보이지 않게 설정
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    layoutParams =
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                    // showMapView가 false면 INVISIBLE로 설정 (렌더링은 되지만 보이지 않음)
                    visibility = if (showMapView) View.VISIBLE else View.INVISIBLE
                    mapViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                // visibility 업데이트
                mapView.visibility = if (showMapView) View.VISIBLE else View.INVISIBLE
                
                if (!mapStarted) {
                    mapStarted = true
                    initializeMapView(mapView, viewModel, uiState, context) { kakaoMap ->
                        kakaoMapInstance = kakaoMap
                    }
                }
            },
        )
        
        // 스냅샷이 생성되면 Image로 표시
        snapshotState?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "지도 스냅샷",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
        }
    }

    // UI 상태 변경 시 지도 업데이트
    LaunchedEffect(uiState, kakaoMapInstance, mapViewRef) {
        val map = kakaoMapInstance
        val mapView = mapViewRef
        if (map != null && mapView != null && mapStarted) {
            updateMapFromState(map, mapView, uiState, viewModel, context)
        }
    }

}

/**
 * MapView 초기화 및 리스너 설정
 */
private fun initializeMapView(
    mapView: MapView,
    viewModel: KakaoMapViewModel,
    uiState: KakaoMapUiState,
    context: Context,
    onMapReady: (KakaoMap) -> Unit,
) {
    mapView.start(
        object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Timber.d("KakaoMap destroyed")
            }

            override fun onMapError(error: Exception) {
                Timber.e(error, "KakaoMap error")
            }
        },
        object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                Timber.d("KakaoMap ready")
                setupCameraListener(kakaoMap, viewModel)
                onMapReady(kakaoMap)
                updateMapFromState(kakaoMap, mapView, uiState, viewModel, context)
            }
        },
    )
}

/**
 * 카메라 이동 완료 리스너 설정
 */
private fun setupCameraListener(
    kakaoMap: KakaoMap,
    viewModel: KakaoMapViewModel,
) {
    kakaoMap.setOnCameraMoveEndListener(null)
    kakaoMap.setOnCameraMoveEndListener { _: KakaoMap, _: CameraPosition, gestureType: GestureType ->
        if (gestureType == GestureType.Unknown &&
            viewModel.renderState.value == MapRenderState.MovingCamera
        ) {
            Timber.d("카메라 이동 완료 (프로그래밍 방식)")
            viewModel.onCameraMoveComplete()
        }
    }
}

/**
 * ViewModel 상태에 따라 지도 업데이트
 */
private fun updateMapFromState(
    kakaoMap: KakaoMap,
    mapView: MapView,
    uiState: KakaoMapUiState,
    viewModel: KakaoMapViewModel,
    context: Context,
) {
    when (uiState) {
        is KakaoMapUiState.Ready -> {
            // 이미 카메라 이동 중이거나 경로 그리기 중이면 중복 호출 방지
            val currentRenderState = viewModel.renderState.value
            if (currentRenderState != MapRenderState.Idle && 
                currentRenderState != MapRenderState.Ready) {
                Timber.d("이미 렌더링 진행 중: $currentRenderState - 업데이트 스킵")
                return
            }
            
            try {
                // 카메라 이동 시작
                viewModel.startCameraMove()
                moveCameraToPath(kakaoMap, uiState.cameraSettings)
            } catch (e: Exception) {
                Timber.e(e, "지도 업데이트 실패")
            }
        }

        is KakaoMapUiState.Error -> {
            Timber.e("지도 오류: ${uiState.message}")
        }

        is KakaoMapUiState.Initial -> {
            // 초기 상태 - 아무 작업도 하지 않음
        }
    }
}

/**
 * 스냅샷 캡처 (PixelCopy 우선, 없으면 fallback)
 */
private fun captureSnapshot(
    mapView: MapView,
    viewModel: KakaoMapViewModel,
    context: Context,
) {
    try {
        if (mapView.width == 0 || mapView.height == 0) {
            Timber.w("MapView 크기가 0입니다: ${mapView.width}x${mapView.height}")
            viewModel.setSnapshot(null)
            return
        }

        mapView.visibility = View.VISIBLE
        val glSurfaceView = findGLSurfaceView(mapView)

        when {
            glSurfaceView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                Timber.d("PixelCopy 방식으로 스냅샷 캡처 시작")
                captureUsingPixelCopy(glSurfaceView, mapView, viewModel, context)
            }
            glSurfaceView != null -> {
                Timber.d("OpenGL 방식으로 스냅샷 캡처 시작 (Android < 8.0)")
                captureFromGLSurface(glSurfaceView, mapView, viewModel, context)
            }
            else -> {
                Timber.w("GLSurfaceView를 찾을 수 없음 - View 방식 사용")
                captureFromView(mapView, viewModel, context)
            }
        }
    } catch (e: Exception) {
        Timber.e(e, "스냅샷 생성 준비 실패: ${e.message}")
        viewModel.setSnapshot(null)
    }
}

/**
 * PixelCopy API를 사용한 스냅샷 캡처 (Android 8.0+)
 */
private fun captureUsingPixelCopy(
    glSurfaceView: GLSurfaceView,
    mapView: MapView,
    viewModel: KakaoMapViewModel,
    context: Context,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        Timber.e("PixelCopy는 Android 8.0 이상에서만 사용 가능")
        captureFromGLSurface(glSurfaceView, mapView, viewModel, context)
        return
    }

    try {
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        waitForFramesToRender(glSurfaceView, "PixelCopy") {
            performPixelCopy(glSurfaceView, viewModel, context)
        }
    } catch (e: Exception) {
        Timber.e(e, "PixelCopy 준비 실패: ${e.message}")
        viewModel.setSnapshot(null)
    }
}

/**
 * 실제 PixelCopy 수행
 */
private fun performPixelCopy(
    glSurfaceView: GLSurfaceView,
    viewModel: KakaoMapViewModel,
    context: Context,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    
    try {
        val bitmap = Bitmap.createBitmap(
            glSurfaceView.width,
            glSurfaceView.height,
            Bitmap.Config.ARGB_8888
        )
        
        PixelCopy.request(
            glSurfaceView,
            bitmap,
            { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    Timber.d("PixelCopy 스냅샷 생성 완료: ${bitmap.width}x${bitmap.height}")
                    val savedPath = saveSnapshotToFile(context, bitmap, 0)
                    Timber.d("PixelCopy 스냅샷 파일 저장: $savedPath")
                    viewModel.setSnapshot(bitmap)
                } else {
                    Timber.e("PixelCopy 실패: $copyResult")
                    // PixelCopy 실패 시 OpenGL 방식으로 재시도
                    Timber.d("OpenGL 방식으로 재시도")
                    captureFromGLSurface(glSurfaceView, null, viewModel, context)
                }
            },
            Handler(Looper.getMainLooper())
        )
    } catch (e: Exception) {
        Timber.e(e, "PixelCopy 실행 실패: ${e.message}")
        viewModel.setSnapshot(null)
    }
}

/**
 * MapView에서 GLSurfaceView 찾기
 */
private fun findGLSurfaceView(view: android.view.View): GLSurfaceView? {
    if (view is GLSurfaceView) {
        return view
    }
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            val child = view.getChildAt(i)
            val glSurfaceView = findGLSurfaceView(child)
            if (glSurfaceView != null) {
                return glSurfaceView
            }
        }
    }
    return null
}

/**
 * OpenGL 기반 스냅샷 캡처 (fallback용 또는 Android < 8.0)
 */
private fun captureFromGLSurface(
    glSurfaceView: GLSurfaceView,
    mapView: MapView?,
    viewModel: KakaoMapViewModel,
    context: Context,
) {
    glSurfaceView.queueEvent {
        val bitmap = try {
            val egl = EGLContext.getEGL() as EGL10
            val gl = egl.eglGetCurrentContext()?.gl as? GL10
            
            if (gl == null) {
                Timber.e("OpenGL 컨텍스트를 가져올 수 없음")
                null
            } else {
                // GPU 작업이 모두 완료될 때까지 대기
                gl.glFinish()
                
                createBitmapFromGLSurface(
                    0,
                    0,
                    glSurfaceView.width,
                    glSurfaceView.height,
                    gl,
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "OpenGL 스냅샷 생성 실패: ${e.message}")
            null
        }
        
        Handler(Looper.getMainLooper()).post {
            if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
                val savedPath = saveSnapshotToFile(context, bitmap, 0)
                Timber.d("OpenGL 스냅샷 파일 저장: $savedPath, 크기: ${bitmap.width}x${bitmap.height}")
                viewModel.setSnapshot(bitmap)
            } else {
                Timber.e("비트맵 생성 실패 또는 크기가 0")
                viewModel.setSnapshot(null)
            }
        }
    }
}

/**
 * OpenGL 프레임버퍼에서 비트맵 생성
 */
private fun createBitmapFromGLSurface(
    x: Int,
    y: Int,
    w: Int,
    h: Int,
    gl: GL10,
): Bitmap? {
    return try {
        val bitmapBuffer = IntArray(w * h)
        val bitmapSource = IntArray(w * h)
        val intBuffer = IntBuffer.wrap(bitmapBuffer)
        intBuffer.position(0)
        
        gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer)
        
        var offset1: Int
        var offset2: Int
        for (i in 0 until h) {
            offset1 = i * w
            offset2 = (h - i - 1) * w
            for (j in 0 until w) {
                val texturePixel = bitmapBuffer[offset1 + j]
                val blue = (texturePixel shr 16) and 0xff
                val red = (texturePixel shl 16) and 0x00ff0000
                val greenMask = 0xff00ff00L.toInt()
                val pixel = (texturePixel and greenMask) or red or blue
                bitmapSource[offset2 + j] = pixel
            }
        }
        
        Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888)
    } catch (e: GLException) {
        Timber.e(e, "OpenGL 비트맵 생성 실패")
        null
    } catch (e: OutOfMemoryError) {
        Timber.e(e, "메모리 부족")
        null
    }
}

/**
 * View 기반 스냅샷 캡처 (최종 fallback)
 */
private fun captureFromView(
    mapView: MapView,
    viewModel: KakaoMapViewModel,
    context: Context,
) {
    mapView.postDelayed({
        try {
            val bitmap = Bitmap.createBitmap(
                mapView.width,
                mapView.height,
                Bitmap.Config.ARGB_8888,
            )
            val canvas = Canvas(bitmap)
            mapView.draw(canvas)

            if (bitmap.width > 0 && bitmap.height > 0) {
                val savedPath = saveSnapshotToFile(context, bitmap, 0)
                Timber.d("View 스냅샷 파일 저장: $savedPath")
                viewModel.setSnapshot(bitmap)
            } else {
                Timber.w("스냅샷 크기가 0: width=${bitmap.width}, height=${bitmap.height}")
                viewModel.setSnapshot(null)
            }
        } catch (e: Exception) {
            Timber.e(e, "View 스냅샷 생성 실패: ${e.message}")
            viewModel.setSnapshot(null)
        }
    }, MapSnapshotConstants.FALLBACK_DELAY_MS)
}

/**
 * 스냅샷을 파일로 저장 (디버깅용)
 */
private fun saveSnapshotToFile(
    context: Context,
    bitmap: Bitmap,
    retryCount: Int,
): String? {
    return try {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "map_snapshot_${timestamp}_retry${retryCount}.png"
        
        val fileDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        val file = File(fileDir, fileName)
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        
        val absolutePath = file.absolutePath
        Timber.d("스냅샷 파일 저장 완료: $absolutePath")
        absolutePath
    } catch (e: Exception) {
        Timber.e(e, "스냅샷 파일 저장 실패: ${e.message}")
        null
    }
}

/**
 * 경로 그리기
 */
private fun drawPath(
    kakaoMap: KakaoMap,
    locations: List<LocationPoint>,
    viewModel: KakaoMapViewModel,
    mapView: MapView,
) {
    if (locations.isEmpty()) {
        Timber.d("경로 포인트가 없습니다")
        viewModel.onPathDrawComplete()
        return
    }

    if (locations.size < 2) {
        Timber.d("경로 포인트가 1개뿐입니다: ${locations.size}개 - 마커만 표시")
        viewModel.onPathDrawComplete()
        return
    }

    try {
        val routeLineManager = kakaoMap.routeLineManager
            ?: run {
                Timber.e("RouteLineManager를 가져올 수 없습니다")
                viewModel.onPathDrawComplete()
                return
            }

        val routeLineOptions = createRouteLineOptions(locations)
        routeLineManager.layer.addRouteLine(routeLineOptions) { _, _ ->
            Timber.d("RouteLine 생성 완료 - GPU 렌더링 대기 중")
            waitForRouteLineRender(mapView, viewModel)
        }
        Timber.d("RouteLine 추가 요청 완료 (콜백 등록됨)")

    } catch (e: Exception) {
        Timber.e(e, "RouteLine 그리기 실패: ${e.message}")
        e.printStackTrace()
        viewModel.onPathDrawComplete()
    }
}

/**
 * 여러 프레임 렌더링 대기 후 콜백 실행 (공통 로직)
 */
private fun waitForFramesToRender(
    glSurfaceView: GLSurfaceView,
    logPrefix: String,
    onComplete: () -> Unit,
) {
    var framesRendered = 0

    fun renderNextFrame() {
        glSurfaceView.requestRender()
        glSurfaceView.queueEvent {
            Handler(Looper.getMainLooper()).post {
                framesRendered++
                Timber.d("$logPrefix 프레임 렌더링 완료: $framesRendered/${MapSnapshotConstants.RENDER_FRAMES_TO_WAIT}")

                if (framesRendered < MapSnapshotConstants.RENDER_FRAMES_TO_WAIT) {
                    renderNextFrame()
                } else {
                    Timber.d("$logPrefix 모든 프레임 렌더링 완료 - 타일 로딩 추가 대기 중 (${MapSnapshotConstants.TILE_LOADING_EXTRA_DELAY_MS}ms)")
                    Handler(Looper.getMainLooper()).postDelayed({
                        onComplete()
                    }, MapSnapshotConstants.TILE_LOADING_EXTRA_DELAY_MS)
                }
            }
        }
    }

    renderNextFrame()
}

/**
 * RouteLine 렌더링 완료 대기
 */
private fun waitForRouteLineRender(
    mapView: MapView,
    viewModel: KakaoMapViewModel,
) {
    val glSurfaceView = findGLSurfaceView(mapView)
    if (glSurfaceView != null) {
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        waitForFramesToRender(glSurfaceView, "RouteLine") {
            viewModel.onPathDrawComplete()
        }
    } else {
        Timber.w("GLSurfaceView를 찾을 수 없음 - fallback 사용")
        mapView.postDelayed({
            Timber.d("RouteLine 렌더링 완료 (fallback)")
            viewModel.onPathDrawComplete()
        }, MapSnapshotConstants.TILE_LOADING_DELAY_MS)
    }
}

/**
 * RouteLine 옵션 생성
 */
private fun createRouteLineOptions(locations: List<LocationPoint>): RouteLineOptions {
    val latLngList = locations.map { location ->
        LatLng.from(location.latitude, location.longitude)
    }

    val routeLineStyle = RouteLineStyle.from(
        MapSnapshotConstants.ROUTE_LINE_WIDTH,
        Color.parseColor(MapSnapshotConstants.ROUTE_LINE_COLOR),
    )

    val routeLineStyles = RouteLineStyles.from(routeLineStyle)
    val routeLineStylesSet = RouteLineStylesSet.from(routeLineStyles)
    val routeLineSegment = RouteLineSegment.from(latLngList)
        .setStyles(routeLineStylesSet.getStyles(0))

    return RouteLineOptions.from(routeLineSegment)
        .setStylesSet(routeLineStylesSet)
}

/**
 * 카메라 이동
 */
private fun moveCameraToPath(
    kakaoMap: KakaoMap,
    cameraSettings: CameraSettings,
) {
    try {
        val centerPosition = LatLng.from(cameraSettings.centerLat, cameraSettings.centerLon)
        val cameraUpdate =
            CameraUpdateFactory.newCenterPosition(centerPosition, cameraSettings.zoomLevel)
        
        kakaoMap.moveCamera(cameraUpdate)
        Timber.d("카메라 이동 요청: 중심 (${cameraSettings.centerLat}, ${cameraSettings.centerLon}), 줌 레벨: ${cameraSettings.zoomLevel}")
    } catch (e: Exception) {
        Timber.e(e, "카메라 이동 실패: ${e.message}")
    }
}