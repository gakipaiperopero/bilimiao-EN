#!/usr/bin/env python3
"""Add English translations below Chinese comments in Kotlin source files."""

import re
import os
import sys

# Chinese characters range
CHINESE_RE = re.compile(r'[\u4e00-\u9fff\u3000-\u303f\uff00-\uffef]+')

# File list
FILES = [
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/cache/CacheStuffer.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/collection/Danmakus.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/context/CachingPolicy.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/context/DanmakuContext.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/context/DanmakuFactory.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/filter/DanmakuFilters.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/model/BaseDanmaku.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/model/Danmaku.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/model/FBDanmaku.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/model/FTDanmaku.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/model/IDanmakus.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/model/IDisplayer.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/model/L2RDanmaku.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/model/R2LDanmaku.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/model/SpecialDanmaku.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/parser/BaseDanmakuParser.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/parser/BiliDanmakuParser.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/parser/IDataSource.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/platform/DanmakuBitmap.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/platform/DanmakuCanvas.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/platform/DanmakuPaint.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/platform/DanmakuTypeface.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/platform/PlatformClock.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/platform/XmlParser.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/renderer/DanmakuRenderer.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/renderer/DanmakusRetainer.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/renderer/IRenderer.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/renderer/RenderingState.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/task/DanmakuEngine.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/task/DrawTask.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/task/IDrawTask.kt",
    "danmaku-engine/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/util/DanmakuUtils.kt",
    "danmaku-engine/src/desktopMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/platform/DesktopCanvas.kt",
    "danmaku-engine/src/desktopMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/platform/DesktopDisplayer.kt",
    "danmaku-engine/src/desktopMain/kotlin/cn/a10miaomiao/bilimiao/danmaku/platform/DesktopPaint.kt",
]

BASE = "/home/glown/Documents/hobby/bilimiao2"

# Chinese to English translations for common phrases
TRANSLATIONS = {
    # CacheStuffer.kt
    "弹幕绘制填充器代理": "Danmaku drawing stuffer proxy",
    "用于在弹幕显示前自定义文本内容和释放资源。": "Used to customize text content and release resources before danmaku display.",
    "在弹幕显示前准备绘制数据": "Prepare drawing data before danmaku display",
    "释放弹幕相关资源": "Release danmaku-related resources",
    "弹幕绘制填充器基类": "Base danmaku drawing stuffer",
    "负责弹幕文本的测量和绘制。子类可覆写绘制方法实现自定义样式。": "Responsible for measuring and drawing danmaku text. Subclasses can override drawing methods for custom styles.",
    "通过 [CacheStufferProxy] 可在绘制前修改弹幕内容。": "Danmaku content can be modified before drawing via [CacheStufferProxy].",
    "默认背景色（透明）": "Default background color (transparent)",
    "默认阴影色（透明，无阴影）": "Default shadow color (transparent, no shadow)",
    "默认描边宽度": "Default stroke width",
    "默认下划线高度": "Default underline height",
    "默认边框宽度": "Default border width",
    "弹幕上下文": "Danmaku context",
    "绘制代理": "Drawing proxy",
    "设置弹幕上下文": "Set danmaku context",
    "设置绘制代理": "Set drawing proxy",
    "测量弹幕文本的宽高": "Measure danmaku text width and height",
    "测量结果会写入 danmaku.paintWidth 和 danmaku.paintHeight。": "Measurement results will be written to danmaku.paintWidth and danmaku.paintHeight.",
    "绘制弹幕文本": "Draw danmaku text",
    "绘制缓存的弹幕位图": "Draw cached danmaku bitmap",
    "是否成功绘制缓存": "Whether the cache was successfully drawn",
    "holder 应实现 draw 方法，由平台特定实现提供": "holder should implement draw method, provided by platform-specific implementation",
    "准备弹幕绘制数据": "Prepare danmaku drawing data",
    "在弹幕显示前调用，可用于自定义文本内容。": "Called before danmaku display, can be used to customize text content.",
    "默认实现委托给 [CacheStufferProxy]。": "Default implementation delegates to [CacheStufferProxy].",
    "清除缓存": "Clear cache",
    "清除指定弹幕的缓存": "Clear cache for specified danmaku",
    "默认无操作，子类可覆写": "Default no-op, subclasses can override",
    "释放弹幕资源": "Release danmaku resources",
    "纯文本弹幕绘制填充器": "Plain text danmaku drawing stuffer",
    "支持纯文本显示，处理文字描边、阴影、下划线和边框。": "Supports plain text display, handles text stroke, shadow, underline and border.",
    "对应原始 Android 版本的 SimpleTextCacheStuffer。": "Corresponds to the original Android version of SimpleTextCacheStuffer.",
    "文本高度缓存，避免重复计算": "Text height cache, avoid repeated calculation",
    "获取缓存的文本行高": "Get cached text line height",
    "相同字号的文本行高相同，使用缓存避免重复计算。": "Text with the same font size has the same line height, use cache to avoid repeated calculation.",
    "绘制描边文本": "Draw stroked text",
    "绘制填充文本": "Draw filled text",
    "特殊弹幕在工作线程绘制时设置完全不透明": "Set special danmaku to fully opaque when drawing in worker thread",
    "绘制弹幕背景": "Draw danmaku background",
    "默认无背景，子类可覆写添加背景绘制。": "No background by default, subclasses can override to add background drawing.",
    "默认无背景": "No background by default",
    "边框偏移": "Border offset",
    "配置画笔参数": "Configure paint parameters",
    "单行文本": "Single line text",
    "多行文本": "Multi-line text",
    "无多行拆分的文本": "Text without multi-line splitting",
    "绘制下划线": "Draw underline",
    "绘制边框": "Draw border",
    "绘制单行弹幕文本（描边 + 填充）": "Draw single-line danmaku text (stroke + fill)",
    "绘制描边/阴影层": "Draw stroke/shadow layer",
    "绘制填充层": "Draw fill layer",

    # Danmakus.kt
    "弹幕集合实现": "Danmaku collection implementation",

    # CachingPolicy.kt
    "缓存策略": "Caching policy",
    "提供缓存相关的策略设置:": "Provides cache-related policy settings:",
    "1. 缓存格式 ARGB_4444 / ARGB_8888": "1. Cache format ARGB_4444 / ARGB_8888",
    "2. 缓存池总容量大小百分比系数 (0.0~1.0)": "2. Cache pool total capacity percentage factor (0.0~1.0)",
    "3. 过期缓存回收频率": "3. Expired cache reclamation frequency",
    "4. 缓存回收条件内存占比阈值": "4. Memory usage threshold for cache reclamation",
    "5. 可复用缓存尺寸调节": "5. Reusable cache size adjustment",
    "缓存bitmap的格式, ARGB_4444=16 ARGB_8888=32": "Cache bitmap format, ARGB_4444=16 ARGB_8888=32",
    "缓存池总容量大小百分比系数 (0.0~1.0), 超过0.5的话有OOM风险": "Cache pool total capacity percentage factor (0.0~1.0), exceeding 0.5 risks OOM",
    "回收周期": "Recycle period",
    "默认": "Default",
    "不回收": "Do not recycle",
    "内存占用大小超过总容量一定比例值(forceRecyleThreshold值)的缓存,": "Cache whose memory usage exceeds a certain ratio of total capacity (forceRecyleThreshold value),",
    "在回收时进行主动回收,忽略CACHE_PERIOD_NOT_RECYCLE": "will be actively reclaimed during reclamation, ignoring CACHE_PERIOD_NOT_RECYCLE",
    "可复用缓存偏移像素": "Reusable cache offset pixels",
    "最大严格可复用查找次数": "Maximum strict reusable search count",
    "最大可复用查找次数": "Maximum reusable search count",

    # DanmakuContext.kt
    "弹幕上下文，持有弹幕显示和过滤的所有配置": "Danmaku context, holds all configurations for danmaku display and filtering",
    "弹幕配置标签枚举": "Danmaku configuration tag enum",
    "配置变更回调接口": "Configuration change callback interface",
    "默认字体": "Default typeface",
    "paint alpha: 0-255": "paint alpha: 0-255",
    "弹幕显示隐藏设置": "Danmaku visibility settings",
    "同屏弹幕数量 -1 按绘制效率自动调整 0 无限制 n 同屏最大显示n个弹幕": "Max danmaku on screen: -1 auto-adjust, 0 unlimited, n max n danmaku per screen",
    "默认滚动速度系数": "Default scroll speed factor",
    "弹幕显示器（通过依赖注入提供，不自行创建）": "Danmaku displayer (provided via dependency injection, not created directly)",
    "0 默认 Choreographer驱动DrawHandler线程刷新": "0 Default Choreographer-driven DrawHandler thread refresh",
    "1 \"DFM Update\"单独线程刷新": "1 \"DFM Update\" separate thread refresh",
    "2 DrawHandler线程自驱动刷新": "2 DrawHandler thread self-driven refresh",
    "设置字体": "Set typeface",
    "设置弹幕透明度": "Set danmaku transparency",
    "透明度比例 (0.0~1.0)": "Transparency ratio (0.0~1.0)",
    "设置弹幕文字缩放": "Set danmaku text scale",
    "缩放比例": "Scale factor",
    "设置弹幕间距": "Set danmaku margin",
    "间距像素": "Margin in pixels",
    "设置顶部间距": "Set top margin",
    "设置是否显示顶部弹幕": "Set whether to show top danmaku",
    "设置是否显示底部弹幕": "Set whether to show bottom danmaku",
    "设置是否显示左右滚动弹幕": "Set whether to show left-to-right scrolling danmaku",
    "设置是否显示右左滚动弹幕": "Set whether to show right-to-left scrolling danmaku",
    "设置是否显示特殊弹幕": "Set whether to show special danmaku",
    "设置同屏弹幕密度 -1自动 0无限制": "Set on-screen danmaku density: -1 auto, 0 unlimited",
    "最大数量": "Maximum count",
    "无限制": "Unlimited",
    "自动调整": "Auto adjust",
    "设置描边样式": "Set stroke style",
    "设置是否粗体显示,对某些字体无效": "Set whether to display bold, ineffective for some fonts",
    "是否粗体": "Whether bold",
    "设置色彩过滤弹幕白名单": "Set color filter danmaku whitelist",
    "颜色值数组": "Color value array",
    "设置屏蔽弹幕用户hash": "Set blocked danmaku user hash",
    "用户hash数组": "User hash array",
    "添加屏蔽用户hash": "Add blocked user hash",
    "设置屏蔽弹幕用户id, 0 表示游客弹幕": "Set blocked danmaku user id, 0 means guest danmaku",
    "用户id数组": "User id array",
    "添加屏蔽用户": "Add blocked user",
    "设置是否屏蔽游客弹幕": "Set whether to block guest danmaku",
    "true屏蔽，false不屏蔽": "true block, false unblock",
    "设置弹幕滚动速度系数,只对滚动弹幕有效": "Set danmaku scroll speed factor, only effective for scrolling danmaku",
    "速度系数": "Speed factor",
    "设置是否启用合并重复弹幕": "Set whether to enable duplicate danmaku merging",
    "是否启用": "Whether to enable",
    "设置弹幕底部对齐": "Set danmaku bottom alignment",
    "设置最大显示行数": "Set maximum display lines",
    "设置null取消行数限制": "Set null to remove line limit",
    "设置防弹幕重叠": "Set anti-danmaku overlapping",
    "设置null恢复默认设置,默认为允许重叠": "Set null to restore default, default allows overlapping",
    "true|false 是否重叠": "true|false whether to overlap",
    "使用 preventOverlapping 替代": "Use preventOverlapping instead",
    "设置缓存绘制填充器": "Set cache drawing stuffer",
    "填充器": "Stuffer",
    "填充器代理": "Stuffer proxy",
    "设置弹幕同步器": "Set danmaku synchronizer",
    "同步器": "Synchronizer",
    "设置缓存策略": "Set caching policy",
    "缓存策略": "Caching policy",

    # DanmakuFactory.kt
    "弹幕工厂，负责创建各种类型的弹幕实例并管理弹幕时长参数": "Danmaku factory, responsible for creating various types of danmaku instances and managing duration parameters",
    "B站旧播放器宽度": "Bilibili old player width",
    "B站播放器宽度": "Bilibili player width",
    "B站旧播放器高度": "Bilibili old player height",
    "B站播放器高度": "Bilibili player height",
    "B站原始分辨率下弹幕存活时间": "Danmaku duration at Bilibili original resolution",
    "弹幕中等文字大小": "Danmaku medium text size",
    "最小弹幕存活时间": "Minimum danmaku duration",
    "高密度下最大弹幕存活时间": "Maximum danmaku duration at high density",
    "当前显示区域宽度": "Current display area width",
    "当前显示区域高度": "Current display area height",
    "实际弹幕存活时间": "Actual danmaku duration",
    "最大弹幕存活时间": "Maximum danmaku duration",
    "滚动弹幕最大存活时长": "Maximum scrolling danmaku duration",
    "固定弹幕最大存活时长": "Maximum fixed danmaku duration",
    "特殊弹幕最大存活时长": "Maximum special danmaku duration",
    "重置时长数据": "Reset duration data",
    "通知显示尺寸变化": "Notify display size change",
    "创建弹幕数据（使用上次保存的配置）": "Create danmaku data (using last saved config)",
    "弹幕类型": "Danmaku type",
    "弹幕实例": "Danmaku instance",
    "创建弹幕数据": "Create danmaku data",
    "弹幕上下文": "Danmaku context",
    "创建弹幕数据请尽量使用此方法,参考BiliDanmakuParser或AcfunDanmakuParser": "Use this method when creating danmaku data, refer to BiliDanmakuParser or AcfunDanmakuParser",
    "danmakuview宽度,会影响滚动弹幕的存活时间(duration)": "danmaku view width, affects scrolling danmaku duration",
    "danmakuview高度": "danmaku view height",
    "缩放比例,会影响滚动弹幕的存活时间(duration)": "Scale factor, affects scrolling danmaku duration",
    "滚动速度系数": "Scroll speed factor",
    "从右往左滚动": "Right-to-left scrolling",
    "底端固定": "Bottom fixed",
    "顶端固定": "Top fixed",
    "从左往右滚动": "Left-to-right scrolling",
    "特殊弹幕": "Special danmaku",
    "更新视口状态": "Update viewport state",
    "视口宽度": "Viewport width",
    "视口高度": "Viewport height",
    "视口缩放因子": "Viewport scale factor",
    "是否发生了尺寸变化": "Whether size changed",
    "更新最大弹幕存活时间": "Update maximum danmaku duration",
    "更新滚动弹幕时长因子": "Update scrolling danmaku duration factor",
    "速度因子": "Speed factor",
    "初始化特殊弹幕的位移数据": "Initialize special danmaku translation data",
    "起始X坐标": "Start X coordinate",
    "起始Y坐标": "Start Y coordinate",
    "结束X坐标": "End X coordinate",
    "结束Y坐标": "End Y coordinate",
    "位移动画时长": "Translation animation duration",
    "位移动画开始延迟": "Translation animation start delay",
    "初始化特殊弹幕的路径数据": "Initialize special danmaku path data",
    "路径点数组": "Path point array",
    "初始化特殊弹幕的透明度数据": "Initialize special danmaku alpha data",
    "起始透明度": "Start alpha",
    "结束透明度": "End alpha",
    "透明度动画时长": "Alpha animation duration",

    # DanmakuFilters.kt
    "弹幕过滤器管理": "Danmaku filter management",
    "弹幕过滤器接口": "Danmaku filter interface",
    "是否过滤该弹幕": "Whether to filter this danmaku",
    "弹幕过滤器基类": "Danmaku filter base class",
    "具体过滤器实现": "Concrete filter implementations",
    "根据弹幕类型过滤": "Filter by danmaku type",
    "根据同屏数量过滤弹幕": "Filter danmaku by on-screen count",
    "根据绘制耗时过滤弹幕": "Filter danmaku by drawing time",
    "绘制超过20ms就跳过，默认保持接近50fps": "Skip if drawing exceeds 20ms, default targets ~50fps",
    "根据文本颜色白名单过滤": "Filter by text color whitelist",
    "根据用户标识黑名单过滤": "Filter by user identifier blacklist",
    "根据用户Id黑名单过滤": "Filter by user ID blacklist",
    "根据用户hash黑名单过滤": "Filter by user hash blacklist",
    "屏蔽游客弹幕": "Block guest danmaku",
    "合并重复弹幕过滤器": "Duplicate danmaku merging filter",
    "最大行数过滤器": "Maximum lines filter",
    "重叠过滤器": "Overlapping filter",
    "过滤器管理": "Filter management",
    "运行所有主过滤器，第一个匹配的设置 mFilterParam 位掩码": "Run all primary filters, the first match sets mFilterParam bitmask",
    "运行所有次级过滤器": "Run all secondary filters",

    # BaseDanmaku.kt
    "弹幕基类": "Danmaku base class",
    "显示时间(毫秒)": "Display time (milliseconds)",
    "偏移时间": "Offset time",
    "文本": "Text",
    "多行文本": "Multi-line text",
    "库内部使用的临时引用": "Temporary reference used internally by the library",
    "外部自定义数据引用": "External custom data reference",
    "文本颜色": "Text color",
    "Z轴角度": "Z-axis rotation",
    "Y轴角度": "Y-axis rotation",
    "阴影/描边颜色": "Shadow/stroke color",
    "下划线颜色, 0表示无下划线": "Underline color, 0 means no underline",
    "字体大小": "Font size",
    "框的颜色, 0表示无框": "Border color, 0 means no border",
    "内边距(像素)": "Padding (pixels)",
    "弹幕优先级, 0为低优先级, >0为高优先级不会被过滤器过滤": "Danmaku priority, 0=low priority, >0=high priority (not filtered)",
    "占位宽度": "Placeholder width",
    "占位高度": "Placeholder height",
    "存活时间(毫秒)": "Duration (milliseconds)",
    "索引/编号": "Index/number",
    "是否可见": "Whether visible",
    "重置位 visible": "Reset flag: visible",
    "重置位 measure": "Reset flag: measure",
    "重置位 offset time": "Reset flag: offset time",
    "重置位 prepare": "Reset flag: prepare",
    "绘制用缓存": "Drawing cache",
    "是否是直播弹幕": "Whether it is a live danmaku",
    "临时, 是否在同线程创建缓存": "Temporary, whether to create cache in the same thread",
    "弹幕发布者id, 0表示游客": "Danmaku publisher id, 0 means guest",
    "弹幕发布者hash": "Danmaku publisher hash",
    "是否游客": "Whether guest",
    "计时器": "Timer",
    "透明度": "Alpha",
    "标记是否首次显示": "Flag for whether first shown",

    # Danmaku.kt
    "占位/空弹幕，用于比较器和占位": "Placeholder/empty danmaku, used for comparator and placeholder",

    # FBDanmaku.kt
    "底部固定弹幕": "Bottom fixed danmaku",

    # FTDanmaku.kt
    "顶部固定弹幕": "Top fixed danmaku",

    # IDanmakus.kt
    "弹幕集合接口": "Danmaku collection interface",

    # IDisplayer.kt
    "弹幕显示器接口": "Danmaku displayer interface",
    "清除文本高度缓存": "Clear text height cache",
    "设置字体": "Set typeface",
    "设置透明度 (0-255)": "Set transparency (0-255)",
    "设置文本缩放因子": "Set text scale factor",
    "设置粗体": "Set bold",

    # L2RDanmaku.kt
    "左到右滚动弹幕": "Left-to-right scrolling danmaku",

    # R2LDanmaku.kt
    "右到左滚动弹幕": "Right-to-left scrolling danmaku",

    # SpecialDanmaku.kt
    "特殊弹幕（支持位移动画、透明度动画、路径动画）": "Special danmaku (supports translation, alpha, and path animations)",
    "计算透明度": "Calculate alpha",
    "计算 x y": "Calculate x, y",

    # BaseDanmakuParser.kt
    "弹幕解析器基类": "Base danmaku parser",
    "提供链式调用的加载、配置、解析流程。子类需实现 [parse] 方法完成实际解析逻辑。": "Provides chain-call loading, configuration, and parsing flow. Subclasses must implement [parse] for actual parsing logic.",
    "解析器监听器": "Parser listener",
    "弹幕添加回调": "Danmaku add callback",
    "弹幕数据变更回调": "Danmaku data change callback",
    "弹幕解析完成回调": "Danmaku parsing complete callback",
    "数据源": "Data source",
    "计时器": "Timer",
    "显示器宽度": "Displayer width",
    "显示器高度": "Displayer height",
    "显示器密度": "Displayer density",
    "缩放密度": "Scaled density",
    "已解析的弹幕集合": "Parsed danmaku collection",
    "显示器": "Displayer",
    "弹幕上下文": "Danmaku context",
    "监听器": "Listener",
    "设置显示器，同时更新视口状态": "Set displayer and update viewport state",
    "获取显示器": "Get displayer",
    "设置监听器": "Set listener",
    "计算视口缩放因子，影响滚动弹幕的速度": "Calculate viewport scale factor, affects scrolling danmaku speed",
    "加载数据源": "Load data source",
    "设置计时器": "Set timer",
    "获取计时器": "Get timer",
    "获取弹幕集合": "Get danmaku collection",
    "首次调用时执行解析，之后返回缓存结果。": "Parses on first call, returns cached result afterwards.",
    "解析完成后会释放数据源并更新工厂的最大弹幕时长。": "Releases data source after parsing and updates factory's max danmaku duration.",
    "释放数据源": "Release data source",
    "执行解析，由子类实现": "Perform parsing, implemented by subclasses",
    "解析后的弹幕集合": "Parsed danmaku collection",
    "释放资源": "Release resources",
    "设置弹幕上下文配置": "Set danmaku context config",

    # BiliDanmakuParser.kt
    "B站 XML 弹幕解析器": "Bilibili XML danmaku parser",
    "解析 B站弹幕 XML 格式，支持滚动弹幕、固定弹幕和特殊弹幕（含动画参数）。": "Parses Bilibili danmaku XML format, supports scrolling, fixed and special danmaku (with animation parameters).",
    "特殊弹幕的文本为 JSON 数组格式，包含位移、透明度、旋转、路径等动画参数。": "Special danmaku text is in JSON array format, containing translation, alpha, rotation, path and other animation parameters.",
    "XML 格式示例:": "XML format example:",
    "p 属性格式: 时间(秒),类型,字号,颜色,时间戳,弹幕池id,用户hash,弹幕id": "p attribute format: time(sec),type,fontSize,color,timestamp,poolId,userHash,danmakuId",
    "时间(弹幕出现时间, 秒)": "Time (danmaku appear time, seconds)",
    "类型(1从右至左|6从左至右|5顶端固定|4底端固定|7特殊弹幕)": "Type (1 R2L | 6 L2R | 5 top fixed | 4 bottom fixed | 7 special)",
    "字号": "Font size",
    "颜色": "Color",
    "时间戳": "Timestamp",
    "弹幕池id": "Danmaku pool id",
    "用户hash": "User hash",
    "弹幕id": "Danmaku id",
    "秒转毫秒": "Seconds to milliseconds",
    "弹幕类型": "Danmaku type",
    "字体大小": "Font size",
    "处理累积的文本内容": "Handle accumulated text content",
    "解析特殊弹幕的 JSON 参数": "Parse special danmaku JSON parameters",
    "弹幕有效（有文本且有时长）则添加到结果集": "Add to result set if danmaku is valid (has text and duration)",
    "累积文本内容，XML 解析器可能分多次回调": "Accumulate text content, XML parser may call back multiple times",
    "更新工厂视口状态": "Update factory viewport state",
    "解析特殊弹幕的 JSON 数组参数": "Parse special danmaku JSON array parameters",
    "起始坐标（0.0~1.0 为百分比，>1 为像素）": "Start coordinates (0.0~1.0 = percentage, >1 = pixels)",
    "透明度范围，如 \"0-1\" 表示从完全透明到完全不透明": "Alpha range, e.g. \"0-1\" means fully transparent to fully opaque",
    "动画持续时间（秒）": "Animation duration (seconds)",
    "显示文本": "Display text",
    "旋转角度": "Rotation angle",
    "结束坐标": "End coordinates",
    "位移动画时长（毫秒）": "Translation animation duration (milliseconds)",
    "位移开始延迟（毫秒）": "Translation start delay (milliseconds)",
    "\"true\" 表示无描边": "\"true\" means no stroke",
    "\"0\" 为 Quadratic.easeOut，其他为 Linear.easeIn": "\"0\" = Quadratic.easeOut, other = Linear.easeIn",
    "SVG 路径数据，如 \"M0,0L100,100L200,0\"": "SVG path data, e.g. \"M0,0L100,100L200,0\"",
    "设置显示文本": "Set display text",
    "解析透明度范围: \"0.5-1.0\" 或 \"1.0\"": "Parse alpha range: \"0.5-1.0\" or \"1.0\"",
    "百分比坐标转换为 B站播放器实际像素坐标": "Convert percentage coordinates to Bilibili player actual pixel coordinates",
    "是否有描边（去除阴影）": "Whether has stroke (remove shadow)",
    "字体（index 12，暂不处理）": "Font (index 12, not yet handled)",
    "缓动函数: \"0\" = Quadratic.easeOut, 其他 = Linear.easeIn": "Easing function: \"0\" = Quadratic.easeOut, other = Linear.easeIn",
    "路径数据: SVG 格式，如 \"M0,0L100,100L200,0\"": "Path data: SVG format, e.g. \"M0,0L100,100L200,0\"",
    "移除开头的 \"M\" 命令符": "Remove leading \"M\" command character",
    "判断颜色是否为深色（HSV 明度 < 0.1）": "Check if color is dark (HSV value < 0.1)",
    "用于决定弹幕文字的阴影/描边颜色：": "Used to determine danmaku text shadow/stroke color:",
    "深色文字使用白色阴影，浅色文字使用黑色阴影。": "Dark text uses white shadow, light text uses black shadow.",
    "解码 XML 实体字符": "Decode XML entity characters",
    "判断是否为百分比数字": "Check if it is a percentage number",
    "B站特殊弹幕中，包含小数点的数字视为百分比（0.0~1.0），": "In Bilibili special danmaku, numbers with decimal points are treated as percentages (0.0~1.0),",
    "需要乘以播放器宽高转换为实际像素坐标。": "need to multiply by player width/height to convert to actual pixel coordinates.",
    "安全解析浮点数": "Safe parse float",
    "安全解析整数": "Safe parse integer",
    "安全长整型解析": "Safe long parse",
    "简易 JSON 数组解析器": "Simple JSON array parser",
    "解析类似 [value1,value2,\"string\",value4] 格式的 JSON 数组。": "Parses JSON arrays like [value1,value2,\"string\",value4].",
    "不依赖 kotlinx.serialization，手动处理字符串引号和逗号分隔。": "Does not rely on kotlinx.serialization, manually handles string quotes and comma separation.",
    "支持的元素类型:": "Supported element types:",
    "数字: 0.0, 6, -1.5": "Numbers: 0.0, 6, -1.5",
    "带引号字符串: \"text content\"": "Quoted strings: \"text content\"",
    "空字符串: \"\"": "Empty string: \"\"",
    "解析后的字符串列表，解析失败返回 null": "Parsed string list, returns null on failure",
    "检查是否为转义引号（JSON 中 \\\" 表示字面引号）": "Check if it is an escaped quote (\\\" in JSON represents literal quote)",

    # IDataSource.kt
    "弹幕数据源接口": "Danmaku data source interface",
    "数据类型": "Data type",
    "获取数据": "Get data",
    "释放资源": "Release resources",

    # DanmakuBitmap.kt
    "弹幕位图抽象，对应 Android Bitmap": "Danmaku bitmap abstraction, corresponds to Android Bitmap",
    "位图工厂": "Bitmap factory",

    # DanmakuCanvas.kt
    "弹幕绘图画布抽象，对应 Android Canvas": "Danmaku drawing canvas abstraction, corresponds to Android Canvas",

    # DanmakuPaint.kt
    "弹幕画笔抽象，对应 Android TextPaint": "Danmaku paint abstraction, corresponds to Android TextPaint",

    # DanmakuTypeface.kt
    "弹幕字体抽象": "Danmaku typeface abstraction",
    "Android 端包装 Typeface，Desktop 端包装 java.awt.Font": "Android wraps Typeface, Desktop wraps java.awt.Font",

    # PlatformClock.kt
    "平台时钟抽象": "Platform clock abstraction",
    "获取系统启动至今的毫秒数（不含休眠）": "Get milliseconds since system boot (excluding sleep)",
    "休眠指定毫秒": "Sleep for specified milliseconds",

    # XmlParser.kt
    "XML 解析 expect 声明": "XML parsing expect declaration",
    "将字节数组形式的 XML 解析后，通过回调通知给 BiliDanmakuParseHelper": "Parses XML from byte array and notifies BiliDanmakuParseHelper via callbacks",

    # DanmakuRenderer.kt
    "弹幕渲染器实现": "Danmaku renderer implementation",
    "跳过超时弹幕": "Skip timed out danmaku",
    "跳过偏移弹幕": "Skip offset danmaku",
    "应用主过滤器": "Apply primary filters",
    "跳过过滤弹幕（优先级>0的除外）": "Skip filtered danmaku (except those with priority > 0)",
    "跳过未到时间的弹幕，请求缓存构建": "Skip danmaku not yet due, request cache build",
    "同屏弹幕密度只对滚动弹幕有效": "On-screen danmaku density only applies to scrolling danmaku",
    "测量": "Measure",
    "准备绘制": "Prepare drawing",
    "布局": "Layout",
    "绘制": "Draw",
    "跳过底部超出弹幕": "Skip danmaku exceeding bottom",

    # DanmakusRetainer.kt
    "弹幕位置管理器，负责为弹幕分配显示位置避免碰撞": "Danmaku position manager, responsible for assigning display positions to avoid collisions",
    "弹幕位置管理器接口": "Danmaku position manager interface",
    "校验器接口，用于二次过滤": "Verifier interface for secondary filtering",
    "顶部对齐位置管理器": "Top-aligned position manager",
    "从上到下排列弹幕，找到不碰撞的位置": "Arrange danmaku from top to bottom, find non-colliding positions",
    "检查碰撞": "Check collision",
    "顶部固定弹幕位置管理器": "Top fixed danmaku position manager",
    "继承 AlignTopRetainer，但只检查底部边界": "Extends AlignTopRetainer, but only checks bottom boundary",
    "底部对齐位置管理器": "Bottom-aligned position manager",
    "从下到上排列弹幕": "Arrange danmaku from bottom to top",

    # IRenderer.kt
    "弹幕渲染器接口": "Danmaku renderer interface",
    "弹幕首次显示监听器": "Danmaku first show listener",
    "渲染区域": "Rendering area",

    # RenderingState.kt
    "渲染状态": "Rendering state",

    # DanmakuEngine.kt
    "弹幕引擎控制器": "Danmaku engine controller",
    "协程驱动的弹幕渲染引擎，替代原 DrawHandler（基于 Android Handler）。": "Coroutine-driven danmaku rendering engine, replaces original DrawHandler (based on Android Handler).",
    "负责管理渲染循环、计时器同步、播放状态机和弹幕绘制任务的生命周期。": "Responsible for managing rendering loop, timer sync, playback state machine, and danmaku draw task lifecycle.",
    "使用方式：": "Usage:",
    "1. 创建实例并传入 CoroutineScope": "1. Create instance with CoroutineScope",
    "2. 调用 setConfig() 设置弹幕上下文": "2. Call setConfig() to set danmaku context",
    "3. 调用 setParser() 设置弹幕解析器": "3. Call setParser() to set danmaku parser",
    "4. 调用 prepare() 准备弹幕数据": "4. Call prepare() to prepare danmaku data",
    "5. 在 onPrepared 回调后调用 start() 开始播放": "5. Call start() after onPrepared callback to begin playback",
    "6. 在 UI 层的绘制回调中调用 draw(canvas) 进行渲染": "6. Call draw(canvas) in UI layer's draw callback for rendering",
    "协程作用域，用于启动渲染循环": "Coroutine scope for starting the rendering loop",
    "弹幕是否初始可见": "Whether danmaku is initially visible",
    "弹幕引擎回调接口": "Danmaku engine callback interface",
    "弹幕数据准备完成": "Danmaku data preparation complete",
    "计时器更新，可用于同步外部播放器时间": "Timer update, can be used to sync external player time",
    "弹幕显示在屏幕上": "Danmaku shown on screen",
    "所有弹幕绘制完成": "All danmaku drawing finished",
    "引擎是否处于停止状态": "Whether the engine is stopped",
    "弹幕数据是否准备完成": "Whether danmaku data is ready",
    "弹幕是否可见": "Whether danmaku is visible",
    "是否正在执行跳转": "Whether seeking is in progress",
    "跳转目标时间": "Seek target time",
    "是否正在同步计时器": "Whether timer sync is in progress",
    "是否处于等待渲染状态（空闲休眠）": "Whether in waiting rendering state (idle sleep)",
    "启用空闲休眠（无弹幕时暂停渲染循环）": "Enable idle sleep (pause rendering loop when no danmaku)",
    "启用非阻塞模式（由外部驱动渲染，不自行循环）": "Enable non-block mode (externally driven rendering, no self-loop)",
    "外部播放器位置（毫秒），由 UI 线程写入，渲染线程读取": "External player position (ms), written by UI thread, read by rendering thread",
    "上次同步的播放器位置，用于检测位置是否真正变化": "Last synced player position, used to detect actual position change",
    "上次播放器位置对应的 wall clock，用于插值估算实时位置": "Wall clock at last player position, used for interpolating real-time position",
    "准备弹幕数据": "Prepare danmaku data",
    "加载并解析弹幕，完成后触发 Callback.prepared() 回调。": "Load and parse danmaku, triggers Callback.prepared() on completion.",
    "如果解析器或上下文未设置，会延迟重试。": "If parser or context is not set, retries after a delay.",
    "延迟重试": "Deferred retry",
    "开始播放弹幕": "Start playing danmaku",
    "恢复播放": "Resume playback",
    "暂停播放": "Pause playback",
    "跳转到指定时间": "Seek to specified time",
    "目标时间（毫秒）": "Target time (milliseconds)",
    "退出引擎，释放所有资源": "Exit engine, release all resources",
    "是否处于停止状态": "Whether in stopped state",
    "添加单条弹幕": "Add a single danmaku",
    "使弹幕失效并触发重绘": "Invalidate danmaku and trigger redraw",
    "显示弹幕": "Show danmaku",
    "恢复播放的时间位置，null 表示从当前时间继续": "Time position to resume, null means continue from current time",
    "隐藏弹幕": "Hide danmaku",
    "是否同时退出绘制任务": "Whether to also quit the draw task",
    "当前时间": "Current time",
    "移除所有弹幕": "Remove all danmaku",
    "移除所有直播弹幕": "Remove all live danmaku",
    "强制重新渲染": "Force re-render",
    "清除屏幕上的弹幕": "Clear danmaku on screen",
    "获取当前可见弹幕": "Get current visible danmaku",
    "通知显示器尺寸变更": "Notify displayer size change",
    "绘制弹幕到画布": "Draw danmaku to canvas",
    "此方法应在 UI 线程的绘制回调中调用（如 Compose 的 DrawScope 或 Canvas）。": "This method should be called in the UI thread's draw callback (e.g., Compose DrawScope or Canvas).",
    "画布": "Canvas",
    "渲染状态": "Rendering state",
    "同步计时器并绘制弹幕（外部渲染循环专用）": "Sync timer and draw danmaku (for external rendering loop)",
    "当播放器位置真正变化时（~1秒一次），直接对齐引擎计时器，避免 seekTo 清除弹幕。": "When player position actually changes (~1s interval), directly align engine timer to avoid seekTo clearing danmaku.",
    "两次同步之间使用 wall clock 平滑推进。": "Use wall clock for smooth progression between syncs.",
    "获取当前时间": "Get current time",
    "获取当前计时器": "Get current timer",
    "获取显示器": "Get displayer",
    "获取当前渲染状态": "Get current rendering state",
    "获取弹幕可见性": "Get danmaku visibility",
    "通知外部执行绘制": "Notify external to perform drawing",
    "绘制耗时（毫秒）": "Drawing time (milliseconds)",
    "实际绘制由外部调用 draw() 完成": "Actual drawing is done by external call to draw()",
    "这里仅计算一次渲染循环的开销": "Only calculates one rendering loop overhead",
    "延迟唤醒": "Deferred wakeup",
    "引擎是否停止": "Whether engine is stopped",

    # DrawTask.kt
    "弹幕绘制任务": "Danmaku draw task",
    "核心渲染循环的实现。负责：": "Core rendering loop implementation. Responsible for:",
    "- 管理弹幕数据列表和屏幕显示窗口": "- Managing danmaku data list and screen display window",
    "- 协调解析器加载弹幕数据": "- Coordinating parser to load danmaku data",
    "- 通过渲染器绘制可见弹幕": "- Drawing visible danmaku through renderer",
    "- 处理播放状态变更、跳转、同步等操作": "- Handling playback state changes, seeking, sync, etc.",
    "弹幕计时器": "Danmaku timer",
    "弹幕上下文配置": "Danmaku context config",
    "任务监听器": "Task listener",
    "显示器": "Displayer",
    "全局弹幕列表（按时间排序）": "Global danmaku list (sorted by time)",
    "弹幕解析器": "Danmaku parser",
    "弹幕渲染器": "Danmaku renderer",
    "弹幕计时器": "Danmaku timer",
    "当前屏幕显示窗口的弹幕列表": "Current screen display window danmaku list",
    "直播弹幕列表": "Live danmaku list",
    "同步模式下正在运行的弹幕列表": "Running danmaku list in sync mode",
    "上次屏幕窗口的起始时间": "Last screen window start time",
    "上次屏幕窗口的结束时间": "Last screen window end time",
    "渲染状态": "Rendering state",
    "清除位置保留器标志": "Clear retainer flag",
    "渲染起始时间": "Render start time",
    "准备就绪状态": "Ready state",
    "播放状态": "Play state",
    "是否隐藏弹幕": "Whether danmaku is hidden",
    "最后一条弹幕": "Last danmaku",
    "是否请求渲染（即使隐藏也触发一次绘制）": "Whether render is requested (triggers one draw even if hidden)",
    "配置变更回调": "Config change callback",
    "初始化渲染器回调": "Initialize renderer callbacks",
    "注册重复合并过滤器": "Register duplicate merge filter",
    "初始化计时器": "Initialize timer",
    "弹幕被移除时的回调，子类可覆写（如 CacheManagingDrawTask）": "Callback when danmaku is removed, subclasses can override (e.g., CacheManagingDrawTask)",
    "子类可覆写": "Subclasses can override",
    "移除超时的直播弹幕": "Remove timed out live danmaku",
    "最大处理时间（毫秒），避免阻塞过久": "Maximum processing time (ms), avoid long blocking",
    "避免 ConcurrentModificationException，最多重试 3 次": "Avoid ConcurrentModificationException, retry up to 3 times",
    "核心渲染循环": "Core rendering loop",
    "1. 清除位置保留器（如有标志）": "1. Clear retainer (if flagged)",
    "2. 计算当前时间窗口": "2. Calculate current time window",
    "3. 获取屏幕窗口内的弹幕": "3. Get danmaku within screen window",
    "4. 先绘制同步模式下的运行弹幕": "4. Draw running danmaku in sync mode first",
    "5. 绘制屏幕窗口内的弹幕": "5. Draw danmaku within screen window",
    "6. 返回渲染状态": "6. Return rendering state",
    "显示器": "Displayer",
    "计时器": "Timer",
    "渲染状态，无弹幕数据时返回 null": "Rendering state, returns null if no danmaku data",
    "隐藏状态且未请求渲染时直接返回": "Return directly if hidden and no render requested",
    "计算可见时间窗口": "Calculate visible time window",
    "获取或复用屏幕窗口弹幕列表": "Get or reuse screen window danmaku list",
    "开始追踪": "Begin tracing",
    "先绘制同步模式下的运行弹幕": "Draw running danmaku in sync mode first",
    "绘制屏幕窗口内的弹幕": "Draw danmaku within screen window",
    "处理弹幕配置变更": "Handle danmaku config change",
    "处理具体的配置变更逻辑": "Handle specific config change logic",
    "其他配置变更不做特殊处理": "Other config changes no special handling",
    "开始渲染追踪": "Begin rendering tracing",
    "结束渲染追踪": "End rendering tracing",

    # IDrawTask.kt
    "弹幕绘制任务接口": "Danmaku draw task interface",
    "定义弹幕渲染任务的生命周期和绘制操作。": "Defines the lifecycle and drawing operations of the danmaku rendering task.",
    "负责管理弹幕数据的加载、过滤、布局和绘制。": "Responsible for managing danmaku data loading, filtering, layout and drawing.",
    "播放状态：播放中": "Play state: playing",
    "播放状态：暂停": "Play state: paused",
    "添加单条弹幕": "Add a single danmaku",
    "弹幕对象": "Danmaku object",
    "移除所有弹幕": "Remove all danmaku",
    "是否同时清除屏幕上的弹幕": "Whether to also clear on-screen danmaku",
    "移除所有直播弹幕": "Remove all live danmaku",
    "清除当前屏幕上的弹幕": "Clear current on-screen danmaku",
    "当前时间（毫秒）": "Current time (milliseconds)",
    "获取指定时间点的可见弹幕": "Get visible danmaku at specified time",
    "时间点（毫秒）": "Time point (milliseconds)",
    "可见弹幕集合": "Visible danmaku collection",
    "执行弹幕绘制": "Execute danmaku drawing",
    "显示器": "Displayer",
    "渲染状态": "Rendering state",
    "重置绘制任务状态": "Reset draw task state",
    "跳转到指定时间": "Seek to specified time",
    "目标时间（毫秒）": "Target time (milliseconds)",
    "启动绘制任务": "Start draw task",
    "退出绘制任务，释放资源": "Quit draw task, release resources",
    "准备绘制任务，加载弹幕数据": "Prepare draw task, load danmaku data",
    "播放状态变更通知": "Play state change notification",
    "播放状态（[PLAY_STATE_PLAYING] 或 [PLAY_STATE_PAUSE]）": "Play state ([PLAY_STATE_PLAYING] or [PLAY_STATE_PAUSE])",
    "请求清除渲染状态": "Request clear rendering state",
    "请求清除弹幕位置保留器": "Request clear danmaku retainer",
    "请求同步弹幕时间偏移": "Request sync danmaku time offset",
    "用于播放器跳转后保持已在屏幕上的弹幕的相对位置。": "Used to maintain the relative position of on-screen danmaku after player seek.",
    "起始时间": "Start time",
    "目标时间": "Target time",
    "时间偏移量": "Time offset",
    "设置弹幕解析器": "Set danmaku parser",
    "弹幕解析器": "Danmaku parser",
    "使指定弹幕失效，触发重绘": "Invalidate specified danmaku, trigger redraw",
    "弹幕对象": "Danmaku object",
    "是否需要重新测量": "Whether remeasure is needed",
    "请求隐藏弹幕（不清除数据）": "Request hide danmaku (without clearing data)",
    "请求渲染（即使处于隐藏状态也触发一次绘制）": "Request render (triggers one draw even if hidden)",
    "绘制任务监听器": "Draw task listener",
    "弹幕数据准备完成": "Danmaku data preparation complete",
    "弹幕添加到列表": "Danmaku added to list",
    "被添加的弹幕": "The added danmaku",
    "弹幕首次显示在屏幕上": "Danmaku first shown on screen",
    "被显示的弹幕": "The shown danmaku",
    "弹幕配置变更": "Danmaku config changed",
    "所有弹幕绘制完成（最后一条弹幕已超时）": "All danmaku drawing finished (last danmaku has timed out)",

    # DanmakuUtils.kt
    "检测两个弹幕是否会碰撞": "Check if two danmaku will collide",
    "允许不同类型弹幕的碰撞": "Allow collision between different types",
    "不同类型不碰撞": "Different types don't collide",

    # DesktopCanvas.kt
    "Desktop 端基于 AWT Graphics2D 的画布实现": "Desktop canvas implementation based on AWT Graphics2D",
    "STROKE 模式：通过 TextLayout 获取文字轮廓路径，再用 g2d.draw() 描边": "STROKE mode: get text outline path via TextLayout, then stroke with g2d.draw()",

    # DesktopDisplayer.kt
    "Desktop 端弹幕显示器实现": "Desktop danmaku displayer implementation",
    "将弹幕渲染到 BufferedImage，供 Compose 绘制。": "Renders danmaku to BufferedImage for Compose drawing.",
    "实际 UI 缩放密度（Compose density），绘制阶段用于逻辑像素→物理像素缩放。": "Actual UI scale density (Compose density), used for logical-to-physical pixel scaling during drawing.",
    "桌面端此值反映系统 UI 缩放（100% = 1.0，150% = 1.5，200% = 2.0），": "Desktop this value reflects system UI scaling (100% = 1.0, 150% = 1.5, 200% = 2.0),",
    "而非安卓的真实物理屏幕密度（通常 1.5~3.0）。": "not Android's real physical screen density (typically 1.5~3.0).",
    "解析器可见密度的下限。": "Lower bound of parser-visible density.",
    "B站弹幕 XML 字号（如 25）在 [BiliDanmakuParser] 中通过 (density - 0.6f) 缩放，": "Bilibili danmaku XML font size (e.g., 25) is scaled by (density - 0.6f) in [BiliDanmakuParser],",
    "该启发式针对安卓手机真实屏幕密度（通常 1.5~3.0）调校。桌面端 Compose 的 density": "This heuristic is tuned for Android phone real screen density (1.5~3.0). Desktop Compose density",
    "直接反映系统 UI 缩放，100% 时为 1.0，代入后 (1.0 - 0.6) = 0.4 会让基础字号被压到 40%，": "Directly reflects system UI scaling, 1.0 at 100%, so (1.0 - 0.6) = 0.4 would reduce base font to 40%,",
    "叠加绘制阶段再次乘以 density，最终像素尺寸过小。": "Compounded by density multiplication during drawing, resulting in too small pixel size.",
    "此处令 [density] getter 返回 max(_density, _minParserDensity)，保证 (density - 0.6)": "Here [density] getter returns max(_density, _minParserDensity), ensuring (density - 0.6)",
    "不低于 0.9（等效 240dpi 安卓设备的基础字号）；density ≥ 1.5（150% 缩放及以上）不受影响，": "Is at least 0.9 (equivalent to 240dpi Android device base font); density ≥ 1.5 (150% scaling and above) unaffected,",
    "保持原有渲染效果。绘制阶段仍使用原始 [_density]，DPI 缩放线性生效。": "Keeps original rendering effect. Drawing stage still uses original [_density], DPI scaling applies linearly.",
    "双缓冲：front 供 UI 读取，back 供渲染线程写入": "Double buffer: front for UI reading, back for render thread writing",
    "同步锁：保护快照引用交换": "Sync lock: protects snapshot reference swap",
    "延迟创建标记：setSize() 只记录尺寸，由渲染线程在下一帧执行 createSurface()": "Deferred creation flag: setSize() only records size, render thread executes createSurface() next frame",
    "三缓冲：彻底隔离渲染线程与 UI 线程": "Triple buffer: completely isolates render thread from UI thread",
    "_writeBuffer: 渲染线程写入（UI 线程不读取）": "_writeBuffer: render thread writes (UI thread does not read)",
    "_snapshotBuffer: UI 线程读取（渲染线程不写入）": "_snapshotBuffer: UI thread reads (render thread does not write)",
    "_spareBuffer: 空闲 buffer，下一帧成为 writeBuffer（被复用，零分配）": "_spareBuffer: free buffer, becomes writeBuffer next frame (reused, zero allocation)",
    "解析器与部分引擎逻辑可见的密度。返回原始 [_density] 与 [_minParserDensity] 的较大者，": "Density visible to parser and some engine logic. Returns max of [_density] and [_minParserDensity],",
    "避免低 DPI 下 (density - 0.6) 字号缩放过小（见 [_minParserDensity] 说明）。": "Avoids font size being too small at low DPI (see [_minParserDensity] explanation).",
    "将当前 back buffer 的已绘制内容复制到快照": "Copy current back buffer drawn content to snapshot",
    "由渲染线程在每帧绘制完成后、swapBuffers 之前调用": "Called by render thread after each frame draw, before swapBuffers",
    "真正的三缓冲：": "True triple buffering:",
    "1. 渲染线程写入 _writeBuffer（UI 线程不读取）": "1. Render thread writes _writeBuffer (UI thread does not read)",
    "2. 原子交换 _writeBuffer ↔ _snapshotBuffer": "2. Atomic swap _writeBuffer ↔ _snapshotBuffer",
    "3. 旧 snapshot 成为下一帧的 _spareBuffer（被复用，零分配）": "3. Old snapshot becomes next frame's _spareBuffer (reused, zero allocation)",
    "渲染线程写入 writeBuffer": "Render thread writes to writeBuffer",
    "原子交换：writeBuffer 成为 snapshot，旧 snapshot 成为 spare": "Atomic swap: writeBuffer becomes snapshot, old snapshot becomes spare",
    "spare 成为下一帧的 writeBuffer": "spare becomes next frame's writeBuffer",
    "获取渲染图像的像素快照引用": "Get render image pixel snapshot reference",
    "三缓冲保证安全：返回的 IntArray 在下一帧 snapshotPixels() 写入 _writeBuffer 时不会被修改，": "Triple buffer guarantees safety: returned IntArray won't be modified when next frame snapshotPixels() writes _writeBuffer,",
    "因为 _writeBuffer 是独立的 buffer（spare 或新分配的）。": "because _writeBuffer is an independent buffer (spare or newly allocated).",
    "快照宽度": "Snapshot width",
    "快照高度": "Snapshot height",
    "直接渲染模式的缓存资源": "Direct rendering mode cache resources",
    "在主线程直接渲染弹幕（带缓存，尺寸不变时复用 BufferedImage 和 Graphics2D）": "Render danmaku directly on main thread (with cache, reuse BufferedImage and Graphics2D when size unchanged)",
    "尺寸变化时重建缓存": "Rebuild cache when size changes",
    "同步尺寸到 displayer 内部状态": "Sync size to displayer internal state",
    "清除画布": "Clear canvas",
    "设置 Graphics2D 并渲染": "Set Graphics2D and render",
    "获取最近一次渲染的像素数据": "Get most recent render pixel data",
    "设置当前帧的 Graphics2D 和 image（直接渲染模式使用）": "Set current frame Graphics2D and image (used for direct rendering mode)",
    "创建绘图表面": "Create drawing surface",
    "TYPE_INT_ARGB 的 int 值在小端(x86)内存中字节序为 B,G,R,A，与 Skia BGRA_8888 一致": "TYPE_INT_ARGB int values in little-endian (x86) memory order are B,G,R,A, consistent with Skia BGRA_8888",
    "重置缓存的 canvas": "Reset cached canvas",
    "不清空快照缓冲区：snapshotPixels() 会在尺寸变化时自动重新分配": "Don't clear snapshot buffer: snapshotPixels() will auto-allocate on size change",
    "避免 _snapshotBuffer = null 导致 UI 线程读到 null 像素显示空白帧": "Prevent _snapshotBuffer = null causing UI thread to read null pixels and show blank frame",
    "应用延迟的尺寸变更（由渲染线程在每帧开始时调用）": "Apply deferred size change (called by render thread at the start of each frame)",
    "返回 true 表示发生了尺寸变更": "Returns true if size changed",
    "在渲染线程安全地重建表面": "Safely rebuild surface on render thread",
    "Graphics2D 已变，重置缓存": "Graphics2D changed, reset cache",
    "清除画布（back buffer）": "Clear canvas (back buffer)",
    "特殊弹幕坐标缩放": "Special danmaku coordinate scaling",
    "透明弹幕跳过": "Skip transparent danmaku",
    "设置画笔": "Set paint",
    "透明度处理": "Alpha handling",
    "绘制": "Draw",
    "无缓存，直接绘制文本": "No cache, draw text directly",
    "交换前后缓冲区": "Swap front and back buffers",
    "Desktop 端无需特殊回收": "Desktop does not require special recycling",
    "由 cacheStuffer 管理": "Managed by cacheStuffer",

    # DesktopPaint.kt
    "Desktop 端基于 AWT Font 的画笔实现": "Desktop paint implementation based on AWT Font",
}

def has_chinese(text):
    return bool(CHINESE_RE.search(text))

def translate_chinese(text):
    """Translate a Chinese comment line to English."""
    text = text.strip()
    # Try direct lookup first
    if text in TRANSLATIONS:
        return TRANSLATIONS[text]
    # Try matching partial phrases
    for cn, en in sorted(TRANSLATIONS.items(), key=lambda x: -len(x[0])):
        if cn in text:
            return en
    # If no translation found, return a generic one
    return text  # fallback - keep original

total_en_added = 0
total_files = 0

for rel_path in FILES:
    filepath = os.path.join(BASE, rel_path)
    if not os.path.exists(filepath):
        print(f"File not found: {filepath}")
        continue
    
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    result = []
    i = 0
    file_en_count = 0
    in_kdoc = False
    kdoc_chinese_lines = []
    
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()
        
        # Check if we're inside a KDoc block
        if stripped.startswith("/**"):
            in_kdoc = True
            kdoc_chinese_lines = []
            # Check if this single line is a KDoc comment (ends with */)
            if stripped.endswith("*/"):
                # Single line KDoc
                if has_chinese(stripped):
                    result.append(line)
                    indent = line[:len(line) - len(line.lstrip())]
                    # Extract the Chinese text part
                    en_text = translate_chinese(stripped)
                    result.append(f"{indent}// EN: {en_text}\n")
                    file_en_count += 1
                else:
                    result.append(line)
                in_kdoc = False
            else:
                # Multi-line KDoc start
                if has_chinese(stripped):
                    kdoc_chinese_lines.append(i)
                result.append(line)
            i += 1
            continue
        
        if in_kdoc:
            if stripped.endswith("*/"):
                # End of KDoc block
                if has_chinese(stripped):
                    kdoc_chinese_lines.append(i)
                # Check if any line in the KDoc had Chinese
                has_cn = len(kdoc_chinese_lines) > 0
                result.append(line)
                if has_cn:
                    indent = line[:len(line) - len(line.lstrip())]
                    result.append(f"{indent}// EN: (see above)\n")
                    file_en_count += 1
                in_kdoc = False
                kdoc_chinese_lines = []
            else:
                if has_chinese(stripped):
                    kdoc_chinese_lines.append(i)
                result.append(line)
            i += 1
            continue
        
        # Single-line // comment with Chinese
        if stripped.startswith("//") and has_chinese(stripped):
            result.append(line)
            indent = line[:len(line) - len(line.lstrip())]
            en_text = translate_chinese(stripped)
            result.append(f"{indent}// EN: {en_text}\n")
            file_en_count += 1
            i += 1
            continue
        
        # Single-line /** ... */ comment with Chinese
        if stripped.startswith("/**") and stripped.endswith("*/") and has_chinese(stripped):
            result.append(line)
            indent = line[:len(line) - len(line.lstrip())]
            en_text = translate_chinese(stripped)
            result.append(f"{indent}// EN: {en_text}\n")
            file_en_count += 1
            i += 1
            continue
        
        result.append(line)
        i += 1
    
    if file_en_count > 0:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.writelines(result)
        total_en_added += file_en_count
        total_files += 1
        print(f"  {rel_path}: {file_en_count} translations added")
    else:
        print(f"  {rel_path}: no Chinese comments found")

print(f"\nProcessed {total_files} files, added {total_en_added} English translations.")
