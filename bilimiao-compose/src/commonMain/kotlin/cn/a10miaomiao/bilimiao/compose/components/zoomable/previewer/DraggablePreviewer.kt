package cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.DEFAULT_ITEM_SPACE
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.PagerGestureScope
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.PagerZoomablePolicyScope
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.SupportedPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-12-25 10:29
 **/

// 默认下拉关闭缩放阈值
// EN: Default drag-down close scale threshold
const val DEFAULT_SCALE_TO_CLOSE_MIN_VALUE = 0.9F

/**
 * 垂直手势的类型
 *
 */
enum class VerticalDragType {
    // 不开启垂直手势
    // EN: No vertical gesture
    None,

    // 仅开启下拉手势
    // EN: Only drag-down gesture
    Down,

    // 支持上下拉手势
    // EN: Support both up and down drag gestures
    UpAndDown,
    ;
}

/**
 * 拖拉拽状态与控制
 *
 * @property scope 协程作用域
 * @constructor
 *
 * @param defaultAnimationSpec 默认动画窗格
 * @param verticalDragType 开启垂直手势的类型
 * @param scaleToCloseMinValue 下拉关闭的缩小的阈值
 * @param pagerState 预览状态
 * @param itemStateMap 用于获取transformItemState
 * @param getKey 获取当前key
 */
// EN: Drag state and control
// EN: Coroutine scope
// EN: Default animation spec
// EN: Type of vertical gesture enabled
// EN: Scale threshold for close on drag down
// EN: Preview state
// EN: For getting transformItemState
// EN: Get current key
open class DraggablePreviewerState(
    private val scope: CoroutineScope,
    defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC,
    verticalDragType: VerticalDragType = VerticalDragType.None,
    scaleToCloseMinValue: Float = DEFAULT_SCALE_TO_CLOSE_MIN_VALUE,
    pagerState: SupportedPagerState,
    itemStateMap: ItemStateMap,
    getKey: (Int) -> Any,
) : TransformPreviewerState(
    scope, defaultAnimationSpec, pagerState, itemStateMap, getKey
) {

    /**
     * 开启垂直手势的类型
     */
    // EN: Type of vertical gesture enabled
    private var verticalDragType by mutableStateOf(verticalDragType)

    /**
     * 下拉关闭的缩放的阈值，当scale小于这个值，就关闭，否则还原
     */
    // EN: Scale threshold for drag-down close; close when scale is below this value, otherwise restore
    private var scaleToCloseMinValue by mutableStateOf(scaleToCloseMinValue)

    /**
     * 下拉关闭容器状态
     */
    // EN: Drag-down close container state
    val draggableContainerState = DraggableContainerState(
        defaultAnimationSpec = defaultAnimationSpec
    )

    suspend fun verticalDrag(pointerInputScope: PointerInputScope) {
        pointerInputScope.apply {
            // 记录开始时的位置
            // EN: Record start position
            var startOffset by mutableStateOf<Offset?>(null)
            // 标记是否为下拉关闭
            // EN: Mark whether it is drag-down close
            var orientationDown by mutableStateOf<Boolean?>(null)
            // 如果getKay不为空才开始检测手势
            // EN: Only start detecting gesture if getKey is not null
            if (verticalDragType != VerticalDragType.None) detectVerticalDragGestures(
                onDragStart = OnDragStart@{
                    val zoomableState = zoomableViewState.value
                    if (zoomableState != null) {
                        // 只有viewer的缩放率为1时才允许下拉手势
                        // EN: Only allow drag-down gesture when viewer scale is 1
                        if (zoomableState.scale.value == 1F) {
                            startOffset = it
                            // 进入下拉手势时禁用viewer的手势
                            // EN: Disable viewer gesture when entering drag-down gesture
                            zoomableState.allowGestureInput = false
                        }
                    } else {
                        // 需要在预览图层正常显示的时候才允许手势
                        // EN: Only allow gesture when preview layer is normally displayed
                        if (previewerAlpha.value == 1F) {
                            startOffset = it
                        }
                    }
                },
                onDragEnd = OnDragEnd@{
                    // 如果开始位置为空，就退出
                    // EN: Exit if start position is null
                    if (startOffset == null) return@OnDragEnd
                    // 重置开始位置和方向
                    // EN: Reset start position and orientation
                    startOffset = null
                    orientationDown = null
                    // 解除viewer的手势输入限制
                    // EN: Release viewer gesture input restriction
                    val zoomableState = zoomableViewState.value
                    zoomableState?.allowGestureInput = true
                    // 缩放小于阈值，执行关闭动画，大于就恢复原样
                    // EN: Scale below threshold, execute close animation; otherwise restore
                    if (draggableContainerState.scale.value < scaleToCloseMinValue) {
                        scope.launch {
                            val itemState = findTransformItemByIndex(currentPage)
                            if (itemState != null) {
                                dragDownClose(itemState)
                            } else {
                                viewerContainerShrinkDown()
                            }
                        }
                    } else {
                        scope.launch {
                            decorationAlpha.snapTo(1F)
                        }
                        scope.launch {
                            draggableContainerState.reset()
                        }
                    }
                },
                onVerticalDrag = OnVerticalDrag@{ change, dragAmount ->
                    if (startOffset == null) return@OnVerticalDrag
                    if (orientationDown == null) orientationDown = dragAmount > 0
                    if (orientationDown == true || verticalDragType == VerticalDragType.UpAndDown) {
                        val offsetY = change.position.y - startOffset!!.y
                        val offsetX = change.position.x - startOffset!!.x
                        val containerHeight = containerSize.value.height
                        val scale = (containerHeight - offsetY.absoluteValue).div(
                            containerHeight
                        )
                        scope.launch {
                            decorationAlpha.snapTo(scale)
                            draggableContainerState.offsetX.snapTo(offsetX)
                            draggableContainerState.offsetY.snapTo(offsetY)
                            draggableContainerState.scale.snapTo(scale)
                        }
                    } else {
                        // 如果不是向上，就返还输入权，以免页面卡顿
                        // EN: If not dragging up, return input control to prevent page stuttering
                        val zoomableState = zoomableViewState.value
                        zoomableState?.allowGestureInput = true
                    }
                }
            )
        }
    }

    /**
     * 响应下拉关闭
     */
    // EN: Respond to drag-down close
    private suspend fun dragDownClose(itemState: TransformItemState) {
        // 取消开启动画
        // EN: Cancel enter animation
        cancelEnterTransform()
        // 标记动作开始
        // EN: Mark action start
        stateCloseStart()

        draggableContainerState.apply {
            val displaySize =
                if (itemState.intrinsicSize != null && itemState.intrinsicSize!!.isSpecified) {
                    getDisplaySize(itemState.intrinsicSize!!, containerSize.value)
                } else {
                    getDisplaySize(containerSize.value, containerSize.value)
                }

            val centerX = containerSize.value.width.div(2)
            val centerY = containerSize.value.height.div(2)

            val nextSize = displaySize.times(scale.value)
            val nextTargetX = centerX + offsetX.value - nextSize.width.div(2)
            val nextTargetY = centerY + offsetY.value - nextSize.height.div(2)

            displayWidth.snapTo(nextSize.width)
            displayHeight.snapTo(nextSize.height)
            displayOffsetX.snapTo(nextTargetX)
            displayOffsetY.snapTo(nextTargetY)
        }

        // 启动关闭
        // EN: Start close
        exitFromCurrentState(itemState)

        // 恢复原来的状态
        // EN: Restore original state
        draggableContainerState.resetImmediately()

        // 标记动作结束
        // EN: Mark action end
        stateCloseEnd()
    }

    /**
     * viewer容器缩小关闭
     */
    // EN: Viewer container shrink close
    private suspend fun viewerContainerShrinkDown(
        animationSpec: AnimationSpec<Float>? = null
    ) {
        val currentAnimationSpec = animationSpec ?: defaultAnimationSpec
        // 标记动作开始
        // EN: Mark action start
        stateCloseStart()

        coroutineScope {
            listOf(
                // 缩小容器
                // EN: Shrink container
                async {
                    draggableContainerState.scale.animateTo(
                        0F,
                        animationSpec = currentAnimationSpec
                    )
                },
                // 关闭UI
                // EN: Close UI
                async {
                    decorationAlpha.animateTo(0F, animationSpec = currentAnimationSpec)
                }
            ).awaitAll()

            // 关闭动画组件
            // EN: Close animation component
            animateContainerVisibleState = MutableTransitionState(false)

            // 恢复原来的状态
            // EN: Restore original state
            draggableContainerState.resetImmediately()

            // 标记动作结束
            // EN: Mark action end
            stateCloseEnd()
        }
    }
}

/**
 * 变换动画容器的状态
 *
 * @property defaultAnimationSpec 默认动画窗格
 */
// EN: Transition animation container state
// EN: Default animation spec
class DraggableContainerState(
    var defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC
) {
    // 容器的偏移量X
    // EN: Container offset X
    var offsetX = Animatable(0F)

    // 容器的偏移量Y
    // EN: Container offset Y
    var offsetY = Animatable(0F)

    // 容器缩放
    // EN: Container scale
    var scale = Animatable(1F)

    /**
     * 重置回原来的状态
     * @param animationSpec AnimationSpec<Float>
     */
    // EN: Reset to original state
    suspend fun reset(animationSpec: AnimationSpec<Float> = defaultAnimationSpec) {
        coroutineScope {
            listOf(
                async {
                    offsetX.animateTo(0F, animationSpec)
                },
                async {
                    offsetY.animateTo(0F, animationSpec)
                },
                async {
                    scale.animateTo(1F, animationSpec)
                },
            ).awaitAll()
        }
    }

    /**
     * 立刻重置
     */
    // EN: Reset immediately
    suspend fun resetImmediately() {
        offsetX.snapTo(0F)
        offsetY.snapTo(0F)
        scale.snapTo(1F)
    }
}

/**
 * 可拖拽释放的弹出预览组件
 *
 * @param modifier 图层修饰
 * @param state 状态对象
 * @param itemSpacing 图片间的间隔
 * @param beyondViewportPageCount 页面外缓存个数
 * @param enter 调用open时的进入动画
 * @param exit 调用close时的退出动画
 * @param debugMode 调试模式
 * @param detectGesture 检测手势
 * @param previewerLayer 容器的图层修饰
 * @param zoomablePolicy 缩放图层的修饰
 */
// EN: Draggable dismissible popup preview component
// EN: Layer modifier
// EN: State object
// EN: Item spacing
// EN: Beyond viewport cache count
// EN: Enter animation
// EN: Exit animation
// EN: Debug mode
// EN: Gesture detection
// EN: Container layer modifier
// EN: Zoom layer modifier
@Composable
fun DraggablePreviewer(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    state: DraggablePreviewerState,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    beyondViewportPageCount: Int = DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT,
    enter: EnterTransition = DEFAULT_PREVIEWER_ENTER_TRANSITION,
    exit: ExitTransition = DEFAULT_PREVIEWER_EXIT_TRANSITION,
    debugMode: Boolean = false,
    detectGesture: PagerGestureScope = PagerGestureScope(),
    previewerLayer: TransformLayerScope = TransformLayerScope(),
    zoomablePolicy: @Composable PagerZoomablePolicyScope.(page: Int) -> Boolean,
) {
    state.apply {
        TransformPreviewer(
            modifier = modifier,
            state = state,
            itemSpacing = itemSpacing,
            beyondViewportPageCount = beyondViewportPageCount,
            enter = enter,
            exit = exit,
            debugMode = debugMode,
            detectGesture = detectGesture,
            previewerLayer = TransformLayerScope(
                previewerDecoration = { innerBox ->
                    val actionColor = Color.Yellow
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding)
                            .pointerInput(getKey) {
                                verticalDrag(this)
                            }
                            .graphicsLayer {
                                translationX = draggableContainerState.offsetX.value
                                translationY = draggableContainerState.offsetY.value
                                scaleX = draggableContainerState.scale.value
                                scaleY = draggableContainerState.scale.value
                            }
                            .run {
                                if (debugMode) border(width = 2.dp, color = actionColor) else this
                            }
                    ) {
                        previewerLayer.previewerDecoration {
                            innerBox()
                        }
                        if (debugMode) Text(
                            modifier = Modifier.align(Alignment.TopEnd),
                            text = "DraggablePreviewer",
                            color = actionColor,
                        )
                    }
                },
                background = previewerLayer.background,
                foreground = previewerLayer.foreground,
            ),
            zoomablePolicy = zoomablePolicy
        )
    }
}