#!/usr/bin/env python3
"""Fix ALL remaining (see above) fallback translations in the danmaku-engine module."""

import re
import os
import glob

CHINESE_RE = re.compile(r'[\u4e00-\u9fff\u3000-\u303f\uff00-\uffef]+')
BASE = "/home/glown/Documents/hobby/bilimiao2"

# Merge all known translations from the original script
TRANSLATIONS = {
    "弹幕绘制填充器代理": "Danmaku drawing stuffer proxy",
    "用于在弹幕显示前自定义文本内容和释放资源。": "Used to customize text content before danmaku display.",
    "在弹幕显示前准备绘制数据": "Prepare drawing data before danmaku display",
    "释放弹幕相关资源": "Release danmaku-related resources",
    "弹幕绘制填充器基类": "Base danmaku drawing stuffer",
    "负责弹幕文本的测量和绘制。子类可覆写绘制方法实现自定义样式。": "Measures and draws danmaku text. Subclasses can override drawing methods.",
    "通过 [CacheStufferProxy] 可在绘制前修改弹幕内容。": "Danmaku content can be modified before drawing via [CacheStufferProxy].",
    "缓存策略": "Caching policy",
    "提供缓存相关的策略设置:": "Provides cache-related policy settings:",
    "1. 缓存格式 ARGB_4444 / ARGB_8888": "1. Cache format ARGB_4444 / ARGB_8888",
    "2. 缓存池总容量大小百分比系数 (0.0~1.0)": "2. Cache pool capacity factor (0.0~1.0)",
    "3. 过期缓存回收频率": "3. Expired cache reclamation frequency",
    "4. 缓存回收条件内存占比阈值": "4. Memory usage threshold for reclamation",
    "5. 可复用缓存尺寸调节": "5. Reusable cache size adjustment",
    "缓存bitmap的格式, ARGB_4444=16 ARGB_8888=32": "Cache bitmap format: ARGB_4444=16 ARGB_8888=32",
    "回收周期": "Recycle period",
    "内存占用大小超过总容量一定比例值(forceRecyleThreshold值)的缓存,": "Cache exceeding forceRecyleThreshold ratio of total capacity",
    "在回收时进行主动回收,忽略CACHE_PERIOD_NOT_RECYCLE": "will be actively reclaimed, ignoring CACHE_PERIOD_NOT_RECYCLE",
    "可复用缓存偏移像素": "Reusable cache offset (pixels)",
    "最大严格可复用查找次数": "Max strict reusable search count",
    "最大可复用查找次数": "Max reusable search count",
    "弹幕上下文，持有弹幕显示和过滤的所有配置": "Danmaku context: holds all display and filter config",
    "弹幕配置标签枚举": "Danmaku config tag enum",
    "配置变更回调接口": "Config change callback interface",
    "弹幕显示隐藏设置": "Danmaku visibility settings",
    "同屏弹幕数量 -1 按绘制效率自动调整 0 无限制 n 同屏最大显示n个弹幕": "Max on-screen: -1 auto, 0 unlimited, n max n",
    "默认滚动速度系数": "Default scroll speed factor",
    "弹幕显示器（通过依赖注入提供，不自行创建）": "Danmaku displayer (provided via DI, not created directly)",
    "弹幕工厂，负责创建各种类型的弹幕实例并管理弹幕时长参数": "Danmaku factory: creates danmaku instances and manages duration params",
    "B站旧播放器宽度": "Bili old player width",
    "B站播放器宽度": "Bili player width",
    "B站旧播放器高度": "Bili old player height",
    "B站播放器高度": "Bili player height",
    "B站原始分辨率下弹幕存活时间": "Danmaku duration at Bili original resolution",
    "重置时长数据": "Reset duration data",
    "通知显示尺寸变化": "Notify display size change",
    "创建弹幕数据（使用上次保存的配置）": "Create danmaku data (using last saved config)",
    "创建弹幕数据": "Create danmaku data",
    "更新视口状态": "Update viewport state",
    "视口宽度": "Viewport width",
    "视口高度": "Viewport height",
    "视口缩放因子": "Viewport scale factor",
    "是否发生了尺寸变化": "Whether size changed",
    "更新最大弹幕存活时间": "Update max danmaku duration",
    "更新滚动弹幕时长因子": "Update scrolling danmaku duration factor",
    "初始化特殊弹幕的位移数据": "Init special danmaku translation data",
    "初始化特殊弹幕的路径数据": "Init special danmaku path data",
    "初始化特殊弹幕的透明度数据": "Init special danmaku alpha data",
    "弹幕过滤器管理": "Danmaku filter management",
    "弹幕过滤器接口": "Danmaku filter interface",
    "是否过滤该弹幕": "Whether to filter this danmaku",
    "弹幕过滤器基类": "Danmaku filter base class",
    "具体过滤器实现": "Concrete filter implementations",
    "根据弹幕类型过滤": "Filter by danmaku type",
    "根据同屏数量过滤弹幕": "Filter by on-screen count",
    "根据绘制耗时过滤弹幕": "Filter by drawing time",
    "根据文本颜色白名单过滤": "Filter by text color whitelist",
    "根据用户标识黑名单过滤": "Filter by user ID blacklist",
    "根据用户Id黑名单过滤": "Filter by user ID blacklist",
    "根据用户hash黑名单过滤": "Filter by user hash blacklist",
    "屏蔽游客弹幕": "Block guest danmaku",
    "合并重复弹幕过滤器": "Duplicate danmaku merge filter",
    "最大行数过滤器": "Max lines filter",
    "重叠过滤器": "Overlapping filter",
    "过滤器管理": "Filter management",
    "运行所有主过滤器，第一个匹配的设置 mFilterParam 位掩码": "Run all primary filters, first match sets mFilterParam bitmask",
    "运行所有次级过滤器": "Run all secondary filters",
    "弹幕基类": "Danmaku base class",
    "弹幕集合实现": "Danmaku collection implementation",
    "弹幕集合接口": "Danmaku collection interface",
    "弹幕显示器接口": "Danmaku displayer interface",
    "清除文本高度缓存": "Clear text height cache",
    "左到右滚动弹幕": "Left-to-right scrolling danmaku",
    "右到左滚动弹幕": "Right-to-left scrolling danmaku",
    "特殊弹幕（支持位移动画、透明度动画、路径动画）": "Special danmaku (supports translation, alpha, path animations)",
    "弹幕解析器基类": "Base danmaku parser",
    "提供链式调用的加载、配置、解析流程。子类需实现 [parse] 方法完成实际解析逻辑。": "Chain-call loading, config, parsing. Subclasses implement [parse] for actual logic.",
    "弹幕数据源接口": "Danmaku data source interface",
    "获取数据": "Get data",
    "释放资源": "Release resources",
    "弹幕位图抽象，对应 Android Bitmap": "Danmaku bitmap abstraction (Android Bitmap)",
    "位图工厂": "Bitmap factory",
    "弹幕绘图画布抽象，对应 Android Canvas": "Danmaku canvas abstraction (Android Canvas)",
    "弹幕画笔抽象，对应 Android TextPaint": "Danmaku paint abstraction (Android TextPaint)",
    "弹幕字体抽象": "Danmaku typeface abstraction",
    "Android 端包装 Typeface，Desktop 端包装 java.awt.Font": "Android wraps Typeface, Desktop wraps java.awt.Font",
    "平台时钟抽象": "Platform clock abstraction",
    "获取系统启动至今的毫秒数（不含休眠）": "Get ms since boot (excluding sleep)",
    "休眠指定毫秒": "Sleep for specified ms",
    "XML 解析 expect 声明": "XML parsing expect declaration",
    "将字节数组形式的 XML 解析后，通过回调通知给 BiliDanmakuParseHelper": "Parses XML from byte array, notifies BiliDanmakuParseHelper via callbacks",
    "弹幕渲染器实现": "Danmaku renderer implementation",
    "跳过超时弹幕": "Skip timed out danmaku",
    "跳过偏移弹幕": "Skip offset danmaku",
    "应用主过滤器": "Apply primary filters",
    "跳过过滤弹幕（优先级>0的除外）": "Skip filtered danmaku (except priority > 0)",
    "跳过未到时间的弹幕，请求缓存构建": "Skip premature danmaku, request cache build",
    "同屏弹幕密度只对滚动弹幕有效": "On-screen density only applies to scrolling danmaku",
    "跳过底部超出弹幕": "Skip danmaku exceeding bottom",
    "弹幕位置管理器，负责为弹幕分配显示位置避免碰撞": "Danmaku position manager: assigns positions to avoid collisions",
    "弹幕位置管理器接口": "Danmaku position manager interface",
    "校验器接口，用于二次过滤": "Verifier interface for secondary filtering",
    "顶部对齐位置管理器": "Top-aligned position manager",
    "从上到下排列弹幕，找到不碰撞的位置": "Arrange from top to bottom, find non-colliding positions",
    "检查碰撞": "Check collision",
    "顶部固定弹幕位置管理器": "Top fixed danmaku position manager",
    "继承 AlignTopRetainer，但只检查底部边界": "Extends AlignTopRetainer, only checks bottom boundary",
    "底部对齐位置管理器": "Bottom-aligned position manager",
    "从下到上排列弹幕": "Arrange from bottom to top",
    "弹幕渲染器接口": "Danmaku renderer interface",
    "弹幕首次显示监听器": "First show listener",
    "渲染区域": "Rendering area",
    "渲染状态": "Rendering state",
    "弹幕引擎控制器": "Danmaku engine controller",
    "协程驱动的弹幕渲染引擎，替代原 DrawHandler（基于 Android Handler）。": "Coroutine-driven engine, replaces original DrawHandler (Android Handler).",
    "负责管理渲染循环、计时器同步、播放状态机和弹幕绘制任务的生命周期。": "Manages render loop, timer sync, playback state, draw task lifecycle.",
    "使用方式：": "Usage:",
    "1. 创建实例并传入 CoroutineScope": "1. Create instance with CoroutineScope",
    "2. 调用 setConfig() 设置弹幕上下文": "2. Call setConfig() to set context",
    "3. 调用 setParser() 设置弹幕解析器": "3. Call setParser() to set parser",
    "4. 调用 prepare() 准备弹幕数据": "4. Call prepare() to prepare data",
    "5. 在 onPrepared 回调后调用 start() 开始播放": "5. Call start() after onPrepared",
    "6. 在 UI 层的绘制回调中调用 draw(canvas) 进行渲染": "6. Call draw(canvas) in UI draw callback",
    "协程作用域，用于启动渲染循环": "Coroutine scope for rendering loop",
    "弹幕是否初始可见": "Whether initially visible",
    "弹幕引擎回调接口": "Engine callback interface",
    "弹幕数据准备完成": "Data preparation complete",
    "计时器更新，可用于同步外部播放器时间": "Timer update, can sync external player time",
    "弹幕显示在屏幕上": "Danmaku shown on screen",
    "所有弹幕绘制完成": "All danmaku drawing finished",
    "引擎是否处于停止状态": "Whether engine is stopped",
    "弹幕数据是否准备完成": "Whether danmaku data is ready",
    "弹幕是否可见": "Whether danmaku is visible",
    "是否正在执行跳转": "Whether seeking",
    "跳转目标时间": "Seek target time",
    "是否正在同步计时器": "Whether syncing timer",
    "是否处于等待渲染状态（空闲休眠）": "Whether in idle sleep state",
    "启用空闲休眠（无弹幕时暂停渲染循环）": "Enable idle sleep (pause when no danmaku)",
    "启用非阻塞模式（由外部驱动渲染，不自行循环）": "Enable non-block mode (externally driven)",
    "外部播放器位置（毫秒），由 UI 线程写入，渲染线程读取": "External player position (ms), UI thread writes, render thread reads",
    "上次同步的播放器位置，用于检测位置是否真正变化": "Last synced position, detects actual change",
    "上次播放器位置对应的 wall clock，用于插值估算实时位置": "Wall clock at last position, interpolates real-time position",
    "准备弹幕数据": "Prepare danmaku data",
    "加载并解析弹幕，完成后触发 Callback.prepared() 回调。": "Load and parse, triggers Callback.prepared() on completion.",
    "如果解析器或上下文未设置，会延迟重试。": "Retries if parser or context not set.",
    "延迟重试": "Deferred retry",
    "开始播放弹幕": "Start playing",
    "恢复播放": "Resume",
    "暂停播放": "Pause",
    "跳转到指定时间": "Seek to time",
    "目标时间（毫秒）": "Target time (ms)",
    "退出引擎，释放所有资源": "Exit engine, release resources",
    "是否处于停止状态": "Whether stopped",
    "添加单条弹幕": "Add single danmaku",
    "使弹幕失效并触发重绘": "Invalidate danmaku and redraw",
    "显示弹幕": "Show",
    "恢复播放的时间位置，null 表示从当前时间继续": "Resume position, null = continue from current",
    "隐藏弹幕": "Hide",
    "是否同时退出绘制任务": "Whether to quit draw task",
    "移除所有弹幕": "Remove all",
    "移除所有直播弹幕": "Remove all live",
    "强制重新渲染": "Force re-render",
    "清除屏幕上的弹幕": "Clear on-screen",
    "获取当前可见弹幕": "Get visible danmaku",
    "通知显示器尺寸变更": "Notify displayer size change",
    "绘制弹幕到画布": "Draw to canvas",
    "此方法应在 UI 线程的绘制回调中调用（如 Compose 的 DrawScope 或 Canvas）。": "Call in UI thread draw callback (Compose DrawScope or Canvas).",
    "同步计时器并绘制弹幕（外部渲染循环专用）": "Sync timer and draw (external render loop)",
    "当播放器位置真正变化时（~1秒一次），直接对齐引擎计时器，避免 seekTo 清除弹幕。": "When position changes (~1s), align timer directly, avoid seekTo clearing.",
    "两次同步之间使用 wall clock 平滑推进。": "Use wall clock for smooth progression between syncs.",
    "获取当前时间": "Get current time",
    "获取当前计时器": "Get current timer",
    "获取显示器": "Get displayer",
    "获取当前渲染状态": "Get rendering state",
    "获取弹幕可见性": "Get visibility",
    "通知外部执行绘制": "Notify external draw",
    "绘制耗时（毫秒）": "Draw time (ms)",
    "实际绘制由外部调用 draw() 完成": "Actual drawing via external draw() call",
    "这里仅计算一次渲染循环的开销": "Only calculates one render loop overhead",
    "延迟唤醒": "Deferred wakeup",
    "引擎是否停止": "Engine stopped",
    "弹幕绘制任务": "Danmaku draw task",
    "核心渲染循环的实现。负责：": "Core render loop implementation. Responsible for:",
    "- 管理弹幕数据列表和屏幕显示窗口": "- Managing danmaku data list and screen window",
    "- 协调解析器加载弹幕数据": "- Coordinating parser to load data",
    "- 通过渲染器绘制可见弹幕": "- Drawing visible danmaku via renderer",
    "- 处理播放状态变更、跳转、同步等操作": "- Handling play state changes, seeking, sync",
    "当前屏幕显示窗口的弹幕列表": "Current screen window danmaku list",
    "直播弹幕列表": "Live danmaku list",
    "同步模式下正在运行的弹幕列表": "Running danmaku list in sync mode",
    "上次屏幕窗口的起始时间": "Last screen window start time",
    "上次屏幕窗口的结束时间": "Last screen window end time",
    "清除位置保留器标志": "Clear retainer flag",
    "渲染起始时间": "Render start time",
    "准备就绪状态": "Ready state",
    "播放状态": "Play state",
    "是否隐藏弹幕": "Whether hidden",
    "最后一条弹幕": "Last danmaku",
    "是否请求渲染（即使隐藏也触发一次绘制）": "Whether render requested (draws even if hidden)",
    "配置变更回调": "Config change callback",
    "初始化渲染器回调": "Init renderer callbacks",
    "注册重复合并过滤器": "Register duplicate merge filter",
    "初始化计时器": "Init timer",
    "弹幕被移除时的回调，子类可覆写（如 CacheManagingDrawTask）": "Callback on removal, subclasses can override (e.g. CacheManagingDrawTask)",
    "子类可覆写": "Subclasses can override",
    "移除超时的直播弹幕": "Remove timed out live danmaku",
    "最大处理时间（毫秒），避免阻塞过久": "Max processing time (ms), avoid long blocking",
    "避免 ConcurrentModificationException，最多重试 3 次": "Avoid ConcurrentModificationException, retry up to 3 times",
    "核心渲染循环": "Core render loop",
    "1. 清除位置保留器（如有标志）": "1. Clear retainer (if flagged)",
    "2. 计算当前时间窗口": "2. Calculate time window",
    "3. 获取屏幕窗口内的弹幕": "3. Get danmaku in window",
    "4. 先绘制同步模式下的运行弹幕": "4. Draw running danmaku in sync mode",
    "5. 绘制屏幕窗口内的弹幕": "5. Draw window danmaku",
    "6. 返回渲染状态": "6. Return rendering state",
    "渲染状态，无弹幕数据时返回 null": "Rendering state, null if no data",
    "隐藏状态且未请求渲染时直接返回": "Return if hidden and no render requested",
    "计算可见时间窗口": "Calculate visible time window",
    "获取或复用屏幕窗口弹幕列表": "Get or reuse screen window list",
    "先绘制同步模式下的运行弹幕": "Draw running in sync mode first",
    "绘制屏幕窗口内的弹幕": "Draw window danmaku",
    "处理弹幕配置变更": "Handle config change",
    "处理具体的配置变更逻辑": "Handle specific config change logic",
    "其他配置变更不做特殊处理": "Other config changes: no special handling",
    "开始渲染追踪": "Begin render tracing",
    "结束渲染追踪": "End render tracing",
    "弹幕绘制任务接口": "Danmaku draw task interface",
    "定义弹幕渲染任务的生命周期和绘制操作。": "Defines render task lifecycle and draw operations.",
    "负责管理弹幕数据的加载、过滤、布局和绘制。": "Manages loading, filtering, layout, and drawing.",
    "播放状态：播放中": "Play state: playing",
    "播放状态：暂停": "Play state: paused",
    "清除当前屏幕上的弹幕": "Clear current on-screen",
    "当前时间（毫秒）": "Current time (ms)",
    "获取指定时间点的可见弹幕": "Get visible at time",
    "时间点（毫秒）": "Time (ms)",
    "可见弹幕集合": "Visible collection",
    "执行弹幕绘制": "Execute draw",
    "重置绘制任务状态": "Reset draw task state",
    "启动绘制任务": "Start draw task",
    "退出绘制任务，释放资源": "Quit draw task, release resources",
    "准备绘制任务，加载弹幕数据": "Prepare draw task, load data",
    "播放状态变更通知": "Play state change notification",
    "请求清除渲染状态": "Request clear rendering state",
    "请求清除弹幕位置保留器": "Request clear retainer",
    "请求同步弹幕时间偏移": "Request sync time offset",
    "用于播放器跳转后保持已在屏幕上的弹幕的相对位置。": "Maintains relative positions of on-screen danmaku after seek.",
    "设置弹幕解析器": "Set parser",
    "使指定弹幕失效，触发重绘": "Invalidate danmaku, trigger redraw",
    "是否需要重新测量": "Whether remeasure needed",
    "请求隐藏弹幕（不清除数据）": "Request hide (no data clear)",
    "请求渲染（即使处于隐藏状态也触发一次绘制）": "Request render (draws even if hidden)",
    "绘制任务监听器": "Draw task listener",
    "弹幕添加到列表": "Added to list",
    "被添加的弹幕": "The added danmaku",
    "弹幕首次显示在屏幕上": "First shown on screen",
    "被显示的弹幕": "The shown danmaku",
    "弹幕配置变更": "Config changed",
    "所有弹幕绘制完成（最后一条弹幕已超时）": "All finished (last timed out)",
    "检测两个弹幕是否会碰撞": "Check if two danmaku collide",
    "允许不同类型弹幕的碰撞": "Allow different types to collide",
    "不同类型不碰撞": "Different types don't collide",
    "Desktop 端基于 AWT Graphics2D 的画布实现": "Desktop canvas based on AWT Graphics2D",
    "STROKE 模式：通过 TextLayout 获取文字轮廓路径，再用 g2d.draw() 描边": "STROKE: gets outline via TextLayout, strokes with g2d.draw()",
    "Desktop 端弹幕显示器实现": "Desktop displayer implementation",
    "将弹幕渲染到 BufferedImage，供 Compose 绘制。": "Renders to BufferedImage for Compose.",
    "实际 UI 缩放密度（Compose density），绘制阶段用于逻辑像素→物理像素缩放。": "Actual UI density (Compose density), used for logical to physical pixel scaling.",
    "桌面端此值反映系统 UI 缩放（100% = 1.0，150% = 1.5，200% = 2.0），": "Desktop reflects UI scale (100%=1.0, 150%=1.5, 200%=2.0),",
    "而非安卓的真实物理屏幕密度（通常 1.5~3.0）。": "not Android physical density (1.5~3.0).",
    "解析器可见密度的下限。": "Lower bound of parser-visible density.",
    "B站弹幕 XML 字号（如 25）在 [BiliDanmakuParser] 中通过 (density - 0.6f) 缩放，": "Bili XML font size (e.g. 25) scaled by (density - 0.6f) in [BiliDanmakuParser],",
    "该启发式针对安卓手机真实屏幕密度（通常 1.5~3.0）调校。桌面端 Compose 的 density": "Heuristic tuned for Android density (1.5~3.0). Desktop density",
    "直接反映系统 UI 缩放，100% 时为 1.0，代入后 (1.0 - 0.6) = 0.4 会让基础字号被压到 40%，": "reflects UI scale (1.0 at 100%), (1.0-0.6)=0.4 base font at 40%,",
    "叠加绘制阶段再次乘以 density，最终像素尺寸过小。": "compounded by density in drawing, final size too small.",
    "此处令 [density] getter 返回 max(_density, _minParserDensity)，保证 (density - 0.6)": "[density] getter returns max(_density, _minParserDensity), ensures (density-0.6)",
    "不低于 0.9（等效 240dpi 安卓设备的基础字号）；density ≥ 1.5（150% 缩放及以上）不受影响，": ">= 0.9 (equivalent to 240dpi base font); density >= 1.5 (150%+) unaffected,",
    "保持原有渲染效果。绘制阶段仍使用原始 [_density]，DPI 缩放线性生效。": "Keeps rendering. Drawing uses original [_density], DPI scaling applies linearly.",
    "双缓冲：front 供 UI 读取，back 供渲染线程写入": "Double buffer: front for UI, back for render thread",
    "同步锁：保护快照引用交换": "Sync lock: protects snapshot swap",
    "延迟创建标记：setSize() 只记录尺寸，由渲染线程在下一帧执行 createSurface()": "Deferred creation: setSize() records size, render thread calls createSurface()",
    "三缓冲：彻底隔离渲染线程与 UI 线程": "Triple buffer: isolates render and UI threads",
    "_writeBuffer: 渲染线程写入（UI 线程不读取）": "_writeBuffer: render writes (UI doesn't read)",
    "_snapshotBuffer: UI 线程读取（渲染线程不写入）": "_snapshotBuffer: UI reads (render doesn't write)",
    "_spareBuffer: 空闲 buffer，下一帧成为 writeBuffer（被复用，零分配）": "_spareBuffer: free buffer, becomes writeBuffer next frame (reused, zero alloc)",
    "解析器与部分引擎逻辑可见的密度。返回原始 [_density] 与 [_minParserDensity] 的较大者，": "Density visible to parser/engine. Returns max([_density], [_minParserDensity]),",
    "避免低 DPI 下 (density - 0.6) 字号缩放过小（见 [_minParserDensity] 说明）。": "Avoids font being too small at low DPI (see [_minParserDensity]).",
    "将当前 back buffer 的已绘制内容复制到快照": "Copy back buffer content to snapshot",
    "由渲染线程在每帧绘制完成后、swapBuffers 之前调用": "Called by render after frame draw, before swapBuffers",
    "真正的三缓冲：": "True triple buffering:",
    "1. 渲染线程写入 _writeBuffer（UI 线程不读取）": "1. Render writes _writeBuffer (UI doesn't read)",
    "2. 原子交换 _writeBuffer ↔ _snapshotBuffer": "2. Atomic swap _writeBuffer / _snapshotBuffer",
    "3. 旧 snapshot 成为下一帧的 _spareBuffer（被复用，零分配）": "3. Old snapshot becomes _spareBuffer (reused, zero alloc)",
    "渲染线程写入 writeBuffer": "Render writes writeBuffer",
    "原子交换：writeBuffer 成为 snapshot，旧 snapshot 成为 spare": "Atomic swap: writeBuffer->snapshot, old snapshot->spare",
    "spare 成为下一帧的 writeBuffer": "spare becomes next frame's writeBuffer",
    "获取渲染图像的像素快照引用": "Get pixel snapshot reference",
    "三缓冲保证安全：返回的 IntArray 在下一帧 snapshotPixels() 写入 _writeBuffer 时不会被修改，": "Triple buffer: returned IntArray won't be modified when writeBuffer is written",
    "因为 _writeBuffer 是独立的 buffer（spare 或新分配的）。": "because _writeBuffer is independent (spare or newly allocated).",
    "快照宽度": "Snapshot width",
    "快照高度": "Snapshot height",
    "直接渲染模式的缓存资源": "Direct rendering mode cache",
    "在主线程直接渲染弹幕（带缓存，尺寸不变时复用 BufferedImage 和 Graphics2D）": "Direct render on main thread (cached, reuse BufferedImage/G2D when unchanged)",
    "尺寸变化时重建缓存": "Rebuild cache on size change",
    "同步尺寸到 displayer 内部状态": "Sync size to displayer internal state",
    "清除画布": "Clear canvas",
    "设置 Graphics2D 并渲染": "Set G2D and render",
    "获取最近一次渲染的像素数据": "Get most recent pixel data",
    "设置当前帧的 Graphics2D 和 image（直接渲染模式使用）": "Set current G2D and image (direct mode)",
    "创建绘图表面": "Create drawing surface",
    "TYPE_INT_ARGB 的 int 值在小端(x86)内存中字节序为 B,G,R,A，与 Skia BGRA_8888 一致": "TYPE_INT_ARGB on little-endian: B,G,R,A order, matches Skia BGRA_8888",
    "重置缓存的 canvas": "Reset cached canvas",
    "不清空快照缓冲区：snapshotPixels() 会在尺寸变化时自动重新分配": "Don't clear snapshot: snapshotPixels() auto-allocates on size change",
    "避免 _snapshotBuffer = null 导致 UI 线程读到 null 像素显示空白帧": "Prevent _snapshotBuffer=null causing blank frame",
    "应用延迟的尺寸变更（由渲染线程在每帧开始时调用）": "Apply deferred size change (called by render at frame start)",
    "返回 true 表示发生了尺寸变更": "Returns true if size changed",
    "在渲染线程安全地重建表面": "Safely rebuild surface on render thread",
    "Graphics2D 已变，重置缓存": "G2D changed, reset cache",
    "清除画布（back buffer）": "Clear canvas (back buffer)",
    "特殊弹幕坐标缩放": "Special danmaku coordinate scaling",
    "透明弹幕跳过": "Transparent danmaku skip",
    "设置画笔": "Set paint",
    "透明度处理": "Alpha handling",
    "无缓存，直接绘制文本": "No cache, draw text directly",
    "交换前后缓冲区": "Swap buffers",
    "Desktop 端无需特殊回收": "No special recycle needed on Desktop",
    "由 cacheStuffer 管理": "Managed by cacheStuffer",
    "Desktop 端基于 AWT Font 的画笔实现": "Desktop paint based on AWT Font",
    # BiliDanmakuParser specific additions
    "B站 XML 弹幕解析器": "Bilibili XML danmaku parser",
    "解析 B站弹幕 XML 格式，支持滚动弹幕、固定弹幕和特殊弹幕（含动画参数）。": "Parses Bilibili XML format: scrolling, fixed, and special danmaku (with animation).",
    "特殊弹幕的文本为 JSON 数组格式，包含位移、透明度、旋转、路径等动画参数。": "Special danmaku text is JSON arrays with translation, alpha, rotation, path params.",
    "XML 格式示例:": "XML format example:",
    "p 属性格式: 时间(秒),类型,字号,颜色,时间戳,弹幕池id,用户hash,弹幕id": "p attr: time(s),type,size,color,timestamp,poolId,userHash,danmakuId",
    "0:时间(弹幕出现时间, 秒)": "0: Time (danmaku appear time, seconds)",
    "1:类型(1从右至左|6从左至右|5顶端固定|4底端固定|7特殊弹幕)": "1: Type (1 R2L|6 L2R|5 top|4 bottom|7 special)",
    "2:字号": "2: Font size",
    "3:颜色": "3: Color",
    "4:时间戳": "4: Timestamp",
    "5:弹幕池id": "5: Danmaku pool ID",
    "6:用户hash": "6: User hash",
    "7:弹幕id": "7: Danmaku ID",
    "秒转毫秒": "Seconds to ms",
    "弹幕类型": "Danmaku type",
    "字体大小": "Font size",
    "处理累积的文本内容": "Handle accumulated text",
    "解析特殊弹幕的 JSON 参数": "Parse special danmaku JSON params",
    "弹幕有效（有文本且有时长）则添加到结果集": "Add to result if valid (has text and duration)",
    "累积文本内容，XML 解析器可能分多次回调": "Accumulate text, XML parser may call back multiple times",
    "更新工厂视口状态": "Update factory viewport state",
    "解析特殊弹幕的 JSON 数组参数": "Parse special danmaku JSON array params",
    "beginX/beginY: 起始坐标（0.0~1.0 为百分比，>1 为像素）": "beginX/beginY: start coords (0.0~1.0 = percentage, >1 = pixels)",
    "alphaRange: 透明度范围，如 \"0-1\" 表示从完全透明到完全不透明": "alphaRange: alpha range, e.g. \"0-1\" = transparent to opaque",
    "duration: 动画持续时间（秒）": "duration: animation duration (seconds)",
    "text: 显示文本": "text: display text",
    "rotateZ/rotateY: 旋转角度": "rotateZ/rotateY: rotation angle",
    "endX/endY: 结束坐标": "endX/endY: end coordinates",
    "translationDuration: 位移动画时长（毫秒）": "translationDuration: translation duration (ms)",
    "delay: 位移开始延迟（毫秒）": "delay: translation start delay (ms)",
    "noStroke: \"true\" 表示无描边": "noStroke: \"true\" means no stroke",
    "easing: \"0\" 为 Quadratic.easeOut，其他为 Linear.easeIn": "easing: \"0\"=Quadratic.easeOut, other=Linear.easeIn",
    "pathData: SVG 路径数据，如 \"M0,0L100,100L200,0\"": "pathData: SVG path, e.g. \"M0,0L100,100L200,0\"",
    "设置显示文本": "Set display text",
    "判断颜色是否为深色（HSV 明度 < 0.1）": "Check if color is dark (HSV value < 0.1)",
    "用于决定弹幕文字的阴影/描边颜色：": "Determines shadow/stroke color:",
    "深色文字使用白色阴影，浅色文字使用黑色阴影。": "Dark text uses white shadow, light text uses black shadow.",
    "移除开头的 \"M\" 命令符": "Remove leading \"M\" command",
    # Additional phrases
    "状态": "State",
    "region 状态": "region State",
    "Region": "Region",
    "缓存池总容量大小百分比系数 (0.0~1.0), 超过0.5的话有OOM风险": "Pool capacity factor (0.0~1.0), >0.5 risks OOM",
    "默认": "Default",
    "不回收": "Do not recycle",
    "填充器": "Stuffer",
    "填充器代理": "Stuffer proxy",
    "同步器": "Synchronizer",
    "设置缓存策略": "Set caching policy",
    "缓存策略": "Caching policy",
    "数据类型": "Data type",
    "底部固定弹幕": "Bottom fixed danmaku",
    "顶部固定弹幕": "Top fixed danmaku",
    "占位/空弹幕，用于比较器和占位": "Placeholder/empty danmaku for comparator",
    "弹幕类型": "Danmaku type",
    "弹幕实例": "Danmaku instance",
    "弹幕对象": "Danmaku object",
    "显示器": "Displayer",
    "计时器": "Timer",
    "数据源": "Data source",
    "监听器": "Listener",
    "起始时间": "Start time",
    "目标时间": "Target time",
    "时间偏移量": "Time offset",
    "测量": "Measure",
    "准备绘制": "Prepare drawing",
    "布局": "Layout",
    "绘制": "Draw",
    "描述": "Description",
    "文本": "Text",
    "颜色": "Color",
    "字号": "Font size",
    "上边距": "Top margin",
    "下边距": "Bottom margin",
    "外边距": "Margin",
    "内边距(像素)": "Padding (pixels)",
    "字体大小": "Font size",
    "是否可见": "Whether visible",
    "宽度": "Width",
    "高度": "Height",
    "字体": "Font (index 12, not yet handled)",
    "TODO: 字体 textArr[12]": "TODO: font textArr[12]",
    "占位宽度": "Placeholder width",
    "占位高度": "Placeholder height",
    "存活时间(毫秒)": "Duration (ms)",
    "索引/编号": "Index/number",
    "偏移时间": "Offset time",
    "弹幕发布者id, 0表示游客": "Publisher ID, 0 = guest",
    "弹幕发布者hash": "Publisher hash",
    "是否游客": "Whether guest",
    "弹幕优先级, 0为低优先级, >0为高优先级不会被过滤器过滤": "Priority: 0=low, >0=high (not filtered)",
    "下划线颜色, 0表示无下划线": "Underline color, 0 = no underline",
    "框的颜色, 0表示无框": "Border color, 0 = no border",
    "阴影/描边颜色": "Shadow/stroke color",
    "标记是否首次显示": "Flag for first display",
    "重置位 visible": "Reset: visible",
    "重置位 measure": "Reset: measure",
    "重置位 offset time": "Reset: offset time",
    "重置位 prepare": "Reset: prepare",
    "绘制用缓存": "Drawing cache",
    "是否是直播弹幕": "Is live danmaku",
    "临时, 是否在同线程创建缓存": "Temp: create cache in same thread",
    "库内部使用的临时引用": "Internal temp reference",
    "外部自定义数据引用": "External custom data ref",
    "显示时间(毫秒)": "Display time (ms)",
    "Z轴角度": "Z rotation",
    "Y轴角度": "Y rotation",
    "颜色值数组": "Color values array",
    "用户hash数组": "User hash array",
    "用户id数组": "User ID array",
    "缩放比例": "Scale factor",
    "间距像素": "Margin px",
    "速度系数": "Speed factor",
    "最大数量": "Max count",
    "无限制": "Unlimited",
    "自动调整": "Auto adjust",
    "是否粗体": "Whether bold",
    "是否启用": "Whether enabled",
    "起始X坐标": "Start X",
    "起始Y坐标": "Start Y",
    "结束X坐标": "End X",
    "结束Y坐标": "End Y",
    "起始透明度": "Start alpha",
    "结束透明度": "End alpha",
    "透明度动画时长": "Alpha animation duration",
    "位移动画时长": "Translation duration",
    "位移动画开始延迟": "Translation delay",
    "路径点数组": "Path point array",
    "绘制超过20ms就跳过，默认保持接近50fps": "Skip if >20ms draw, maintain ~50fps",
    "默认背景色（透明）": "Default bg color (transparent)",
    "默认阴影色（透明，无阴影）": "Default shadow color (transparent, no shadow)",
    "默认描边宽度": "Default stroke width",
    "默认下划线高度": "Default underline height",
    "默认边框宽度": "Default border width",
    "绘制代理": "Drawing proxy",
    "绘制弹幕文本": "Draw danmaku text",
    "绘制缓存的弹幕位图": "Draw cached bitmap",
    "是否成功绘制缓存": "Whether cache draw succeeded",
    "holder 应实现 draw 方法，由平台特定实现提供": "holder must implement draw(), provided by platform",
    "准备弹幕绘制数据": "Prepare drawing data",
    "在弹幕显示前调用，可用于自定义文本内容。": "Called before display, can customize text.",
    "默认实现委托给 [CacheStufferProxy]。": "Default delegates to [CacheStufferProxy].",
    "清除缓存": "Clear cache",
    "清除指定弹幕的缓存": "Clear cache for danmaku",
    "默认无操作，子类可覆写": "No-op default, subclasses can override",
    "释放弹幕资源": "Release danmaku resources",
    "纯文本弹幕绘制填充器": "Plain text stuffer",
    "支持纯文本显示，处理文字描边、阴影、下划线和边框。": "Plain text with stroke, shadow, underline, border.",
    "对应原始 Android 版本的 SimpleTextCacheStuffer。": "Corresponds to Android SimpleTextCacheStuffer.",
    "文本高度缓存，避免重复计算": "Text height cache, avoid recomputation",
    "获取缓存的文本行高": "Get cached line height",
    "相同字号的文本行高相同，使用缓存避免重复计算。": "Same font size = same line height, use cache.",
    "绘制描边文本": "Draw stroked text",
    "绘制填充文本": "Draw filled text",
    "特殊弹幕在工作线程绘制时设置完全不透明": "Set special danmaku fully opaque when drawing in worker",
    "绘制弹幕背景": "Draw bg",
    "默认无背景，子类可覆写添加背景绘制。": "No bg by default, subclasses can override.",
    "默认无背景": "No bg by default",
    "边框偏移": "Border offset",
    "配置画笔参数": "Configure paint params",
    "单行文本": "Single line",
    "多行文本": "Multi-line",
    "无多行拆分的文本": "No multi-line split",
    "绘制下划线": "Draw underline",
    "绘制边框": "Draw border",
    "绘制单行弹幕文本（描边 + 填充）": "Draw single line (stroke + fill)",
    "绘制描边/阴影层": "Draw stroke/shadow layer",
    "绘制填充层": "Draw fill layer",
    "弹幕上下文": "Danmaku context",
    "设置弹幕上下文": "Set context",
    "设置绘制代理": "Set drawing proxy",
    "测量弹幕文本的宽高": "Measure text width/height",
    "测量结果会写入 danmaku.paintWidth 和 danmaku.paintHeight。": "Results written to danmaku.paintWidth/Height.",
    "解析器监听器": "Parser listener",
    "弹幕添加回调": "Add callback",
    "弹幕数据变更回调": "Data change callback",
    "弹幕解析完成回调": "Parse complete callback",
    "显示器宽度": "Displayer width",
    "显示器高度": "Displayer height",
    "显示器密度": "Displayer density",
    "缩放密度": "Scaled density",
    "已解析的弹幕集合": "Parsed collection",
    "弹幕上下文": "Context",
    "设置显示器，同时更新视口状态": "Set displayer, update viewport",
    "获取显示器": "Get displayer",
    "设置监听器": "Set listener",
    "计算视口缩放因子，影响滚动弹幕的速度": "Calculate viewport scale, affects scroll speed",
    "加载数据源": "Load data source",
    "设置计时器": "Set timer",
    "获取计时器": "Get timer",
    "获取弹幕集合": "Get collection",
    "首次调用时执行解析，之后返回缓存结果。": "Parses on first call, returns cached result.",
    "解析完成后会释放数据源并更新工厂的最大弹幕时长。": "Releases source after parse, updates factory max duration.",
    "释放数据源": "Release source",
    "执行解析，由子类实现": "Parse, implemented by subclass",
    "解析后的弹幕集合": "Parsed collection",
    "设置弹幕上下文配置": "Set context config",
    "从右往左滚动": "R2L scroll",
    "底端固定": "Bottom fixed",
    "顶端固定": "Top fixed",
    "从左往右滚动": "L2R scroll",
    "特殊弹幕": "Special danmaku",
    "弹幕中等文字大小": "Medium text size",
    "最小弹幕存活时间": "Min duration",
    "高密度下最大弹幕存活时间": "Max duration at high density",
    "当前显示区域宽度": "Current width",
    "当前显示区域高度": "Current height",
    "实际弹幕存活时间": "Actual duration",
    "最大弹幕存活时间": "Max duration",
    "滚动弹幕最大存活时长": "Max scroll duration",
    "固定弹幕最大存活时长": "Max fixed duration",
    "特殊弹幕最大存活时长": "Max special duration",
    "paint alpha: 0-255": "paint alpha: 0-255",
    "默认字体": "Default typeface",
    "设置字体": "Set typeface",
    "设置弹幕透明度": "Set alpha",
    "透明度比例 (0.0~1.0)": "Alpha ratio (0.0~1.0)",
    "设置弹幕文字缩放": "Set text scale",
    "设置弹幕间距": "Set margin",
    "设置顶部间距": "Set top margin",
    "设置是否显示顶部弹幕": "Show top danmaku?",
    "设置是否显示底部弹幕": "Show bottom danmaku?",
    "设置是否显示左右滚动弹幕": "Show L2R scroll?",
    "设置是否显示右左滚动弹幕": "Show R2L scroll?",
    "设置是否显示特殊弹幕": "Show special?",
    "设置同屏弹幕密度 -1自动 0无限制": "Set density: -1 auto, 0 unlimited",
    "设置描边样式": "Set stroke style",
    "设置是否粗体显示,对某些字体无效": "Set bold (may not work on some fonts)",
    "设置色彩过滤弹幕白名单": "Set color filter whitelist",
    "设置屏蔽弹幕用户hash": "Set blocked user hash",
    "添加屏蔽用户hash": "Add blocked user hash",
    "设置屏蔽弹幕用户id, 0 表示游客弹幕": "Set blocked user ID, 0 = guest",
    "用户id数组": "User ID array",
    "添加屏蔽用户": "Add blocked user",
    "设置是否屏蔽游客弹幕": "Block guest?",
    "true屏蔽，false不屏蔽": "true=block, false=unblock",
    "设置弹幕滚动速度系数,只对滚动弹幕有效": "Set scroll speed (scrolling only)",
    "设置是否启用合并重复弹幕": "Enable duplicate merge?",
    "设置弹幕底部对齐": "Set bottom alignment",
    "设置最大显示行数": "Set max display lines",
    "设置null取消行数限制": "null = no line limit",
    "设置防弹幕重叠": "Set anti-overlap",
    "设置null恢复默认设置,默认为允许重叠": "null = default (allow overlap)",
    "true|false 是否重叠": "true|false overlap?",
    "使用 preventOverlapping 替代": "Use preventOverlapping instead",
    "设置缓存绘制填充器": "Set cache stuffer",
    "设置弹幕同步器": "Set synchronizer",
    "0 默认 Choreographer驱动DrawHandler线程刷新": "0: Choreographer-driven DrawHandler",
    "1 \"DFM Update\"单独线程刷新": "1: DFM Update separate thread",
    "2 DrawHandler线程自驱动刷新": "2: DrawHandler self-driven",
    "解析透明度范围: \"0.5-1.0\" 或 \"1.0\"": "Parse alpha range: \"0.5-1.0\" or \"1.0\"",
    "百分比坐标转换为 B站播放器实际像素坐标": "Convert % coords to pixel coords",
    "是否有描边（去除阴影）": "Has stroke (remove shadow)",
    "缓动函数: \"0\" = Quadratic.easeOut, 其他 = Linear.easeIn": "Easing: \"0\"=Quadratic.easeOut, other=Linear.easeIn",
    "路径数据: SVG 格式，如 \"M0,0L100,100L200,0\"": "Path: SVG format, e.g. \"M0,0L100,100L200,0\"",
    "解码 XML 实体字符": "Decode XML entities",
    "判断是否为百分比数字": "Check if percentage number",
    "B站特殊弹幕中，包含小数点的数字视为百分比（0.0~1.0），": "In Bili special, numbers with decimals are percentages (0.0~1.0),",
    "需要乘以播放器宽高转换为实际像素坐标。": "multiply by player width/height for pixel coords.",
    "安全解析浮点数": "Safe parse float",
    "安全解析整数": "Safe parse integer",
    "安全长整型解析": "Safe parse long",
    "简易 JSON 数组解析器": "Simple JSON array parser",
    "解析类似 [value1,value2,\"string\",value4] 格式的 JSON 数组。": "Parses JSON arrays like [value1,value2,\"string\",value4].",
    "不依赖 kotlinx.serialization，手动处理字符串引号和逗号分隔。": "No kotlinx.serialization, manual quote/separator handling.",
    "支持的元素类型:": "Supported types:",
    "数字: 0.0, 6, -1.5": "Numbers: 0.0, 6, -1.5",
    "带引号字符串: \"text content\"": "Quoted strings: \"text content\"",
    "空字符串: \"\"": "Empty string: \"\"",
    "解析后的字符串列表，解析失败返回 null": "Parsed string list, null on failure",
    "检查是否为转义引号（JSON 中 \\\" 表示字面引号）": "Check for escaped quotes (\\\" in JSON)",
    "当前时间": "Current time",
    "画布": "Canvas",
    "是否同时退出绘制任务": "Quit draw task too?",
    "是否同时清除屏幕上的弹幕": "Clear on-screen too?",
    "弹幕计时器": "Danmaku timer",
    "弹幕上下文配置": "Context config",
    "任务监听器": "Task listener",
    "全局弹幕列表（按时间排序）": "Global list (sorted by time)",
    "弹幕解析器": "Parser",
    "弹幕渲染器": "Renderer",
    "弹幕被移除时的回调，子类可覆写（如 CacheManagingDrawTask）": "Callback on removal, subclass can override",
    "开始追踪": "Begin tracing",
    "弹幕数据源接口": "DataSource interface",
    # Remaining missing phrases
    "不同步": "Not syncing",
    "region 依赖": "region Dependencies",
    "region 计时": "region Timing",
    "region 协程": "region Coroutines",
    "region 配置": "region Configuration",
    "region 生命周期": "region Lifecycle",
    "region 弹幕操作": "region Danmaku Operations",
    "region 渲染": "region Rendering",
    "确定弹幕位置": "Determine danmaku position",
    "通过解析器加载弹幕数据": "Load danmaku data via parser",
    "获取当前屏幕上的运行弹幕": "Get current on-screen running danmaku",
    "设置每条弹幕的时间偏移": "Set time offset for each danmaku",
    "缓存的 canvas，避免每条弹幕创建一个 DesktopCanvas": "Cached canvas, avoids creating DesktopCanvas per danmaku",
    "避免浮点精度抖动导致每帧重建：同时检查 _width/_height 和 _pendingWidth/_pendingHeight": "Avoid per-frame rebuild from float jitter: check both _width/_height and _pendingWidth/_pendingHeight",
    "渲染耗时记录": "Render time records",
    "数量": "Count",
    "wall clock 平滑推进": "Wall clock smooth progression",
    "插值 = 上次同步位置 + 距上次同步的 wall clock 间隔": "Interpolation = last sync position + wall clock interval since last sync",
    "region 空闲等待": "region Idle Wait",
    "region 渲染统计": "region Render Stats",
    "region 辅助": "region Helpers",
    "延迟触发重绘": "Deferred trigger redraw",
}

def has_chinese(text):
    return bool(CHINESE_RE.search(text))

def get_indent(line):
    return line[:len(line) - len(line.lstrip())]

def extract_chinese(text):
    """Extract Chinese text from a comment line."""
    # Remove comment markers
    cleaned = text.strip()
    cleaned = re.sub(r'^//\s*', '', cleaned)
    cleaned = re.sub(r'^\s*\*\s*', '', cleaned)
    cleaned = cleaned.replace('/**', '').replace('*/', '').strip()
    return cleaned

def translate_cn_line(text):
    """Translate Chinese text using dictionary with partial matching."""
    text = text.strip()
    if not text:
        return ""
    if text in TRANSLATIONS:
        return TRANSLATIONS[text]
    # Try partial match (longest key first)
    for cn, en in sorted(TRANSLATIONS.items(), key=lambda x: -len(x[0])):
        if cn and cn in text:
            return en
    return ""

def find_chinese_source(lines, idx):
    """Look backward from idx to find the last line with Chinese content."""
    # Search up to 10 lines back
    for lookback in range(1, min(20, idx + 1)):
        check = idx - lookback
        line = lines[check].strip()
        if has_chinese(line) and "EN:" not in line:
            return check, line
    return None, ""

# Collect all files
all_files = []
for root, dirs, files in os.walk(os.path.join(BASE, "danmaku-engine/src")):
    for f in files:
        if f.endswith(".kt"):
            all_files.append(os.path.join(root, f))

total_fixed = 0

for filepath in sorted(all_files):
    # Skip androidMain files (only commonMain and desktopMain)
    if "/androidMain/" in filepath:
        continue

    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.read().splitlines(keepends=True)

    modified = False
    result = []
    i = 0

    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        # Case 1: in multi-line KDoc
        if stripped.startswith("/**") and not stripped.endswith("*/"):
            kdoc_start = i
            result.append(line)
            i += 1
            # Collect KDoc content
            kdoc_lines = []
            while i < len(lines):
                cl = lines[i]
                cs = cl.strip()
                if cs.endswith("*/"):
                    result.append(cl)
                    # Check if next line is (see above)
                    if i + 1 < len(lines) and "see above" in lines[i + 1]:
                        # Extract Chinese from kdoc
                        for ki in range(kdoc_start, i + 1):
                            ksl = lines[ki].strip()
                            cleaned = ksl.replace("/**", "").replace("*/", "").strip()
                            if cleaned.startswith("*"):
                                cleaned = cleaned[1:].strip()
                            if cleaned.startswith("@") or not cleaned:
                                continue
                            if has_chinese(cleaned):
                                kdoc_lines.append(cleaned)
                        translations = []
                        for t in kdoc_lines:
                            en = translate_cn_line(t)
                            if en:
                                translations.append(en)
                        indent = get_indent(cl)
                        if translations:
                            result.append(f"{indent}// EN: {'. '.join(translations)}\n")
                        else:
                            # Fallback: keep the (see above)
                            result.append(lines[i + 1])
                        modified = True
                        i += 1
                    i += 1
                    break
                else:
                    result.append(cl)
                    i += 1
            continue

        # Case 2: single-line comment with Chinese followed by (see above)
        if "see above" in stripped:
            # Look backward for Chinese source
            cn_idx, cn_line = find_chinese_source(lines, i)
            if cn_idx is not None:
                cn_text = extract_chinese(cn_line)
                en = translate_cn_line(cn_text)
                if en:
                    indent = get_indent(lines[i])
                    result.append(f"{indent}// EN: {en}\n")
                    modified = True
                    i += 1
                    continue
            # If no translation found, keep original
            result.append(line)
            i += 1
            continue

        # Case 3: single-line // comment with Chinese that needs EN (not already having one)
        if stripped.startswith("//") and has_chinese(stripped):
            # Check if next line is already EN or this is KDoc end
            result.append(line)
            i += 1
            continue

        result.append(line)
        i += 1

    if modified:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.writelines(result)
        total_fixed += 1
        print(f"  Fixed: {os.path.relpath(filepath, BASE)}")

print(f"\nProcessed files: {total_fixed}")
