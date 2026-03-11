STYLEKIT_STYLE_REFERENCE
style_name: 固定侧边栏布局
style_slug: sidebar-fixed
style_source: /styles/sidebar-fixed

# Hard Prompt

请严格遵守以下风格规则并保持一致性，禁止风格漂移。

## 执行要求
- 优先保证风格一致性，其次再做创意延展。
- 遇到冲突时以禁止项为最高优先级。
- 输出前自检：颜色、排版、间距、交互是否仍属于该风格。

## Style Rules
# Fixed Sidebar (固定侧边栏布局) Design System

> 固定位置的侧边导航栏与可滚动主内容区的应用布局，适合后台管理、文档站点、仪表盘、SaaS 应用。

## 核心理念

Fixed Sidebar（固定侧边栏布局）是应用型界面的经典布局，提供持久可见的导航同时最大化内容展示空间。

核心理念：
- 导航常驻：重要入口始终可及
- 内容优先：主区域最大化利用
- 层级清晰：侧边栏体现信息架构
- 响应适配：小屏幕优雅收起

---

## Token 字典（精确 Class 映射）

### 边框
```
宽度: border
颜色: border-zinc-200
圆角: rounded-lg
```

### 阴影
```
小:   shadow-sm
中:   shadow-sm
大:   shadow-md
悬停: hover:shadow-md
聚焦: focus:shadow-sm
```

### 交互效果
```
悬停位移: undefined
过渡动画: transition-colors duration-200
按下状态: active:scale-95
```

### 字体
```
标题: font-semibold tracking-tight
正文: font-sans
```

### 字号
```
Hero:  text-2xl md:text-3xl
H1:    text-xl md:text-2xl
H2:    text-lg md:text-xl
H3:    text-base md:text-lg
正文:  text-sm
小字:  text-xs
```

### 间距
```
Section: py-6 md:py-8
容器:    px-4 md:px-6 lg:px-8
卡片:    p-4 md:p-6
```

---

## [FORBIDDEN] 绝对禁止

以下 class 在本风格中**绝对禁止使用**，生成时必须检查并避免：

### 禁止的 Class
- `rounded-2xl`
- `rounded-3xl`
- `rounded-full`
- `shadow-2xl`
- `font-black`
- `font-extrabold`
- `text-4xl`
- `text-5xl`
- `text-6xl`

### 禁止的模式
- 匹配 `^rounded-(?:2xl|3xl|full)`
- 匹配 `^shadow-2xl`
- 匹配 `^text-[456]xl`

### 禁止原因
- `rounded-2xl`: Sidebar Fixed uses compact rounded-lg for functional UI elements
- `shadow-2xl`: Sidebar Fixed uses subtle shadows for a clean dashboard feel
- `text-5xl`: Sidebar Fixed uses compact typography appropriate for data-dense layouts

> WARNING: 如果你的代码中包含以上任何 class，必须立即替换。

---

## [REQUIRED] 必须包含

### 按钮必须包含
```
rounded-lg
text-sm font-medium
transition-colors
```

### 卡片必须包含
```
bg-white
rounded-xl
border border-zinc-200
shadow-sm
```

### 输入框必须包含
```
bg-zinc-100
border-0
rounded-lg
text-sm
focus:outline-none focus:ring-2 focus:ring-blue-500/30
```

---

## [COMPARE] 错误 vs 正确对比

### 按钮

[WRONG] **错误示例**（使用了圆角和模糊阴影）：
```html
<button class="rounded-lg shadow-lg bg-blue-500 text-white px-4 py-2 hover:bg-blue-600">
  点击我
</button>
```

[CORRECT] **正确示例**（使用硬边缘、无圆角、位移效果）：
```html
<button class="rounded-lg text-sm font-medium transition-colors bg-[#ff006e] text-white px-4 py-2 md:px-6 md:py-3">
  点击我
</button>
```

### 卡片

[WRONG] **错误示例**（使用了渐变和圆角）：
```html
<div class="rounded-xl shadow-2xl bg-gradient-to-r from-purple-500 to-pink-500 p-6">
  <h3 class="text-xl font-semibold">标题</h3>
</div>
```

[CORRECT] **正确示例**（纯色背景、硬边缘阴影）：
```html
<div class="bg-white rounded-xl border border-zinc-200 shadow-sm p-4 md:p-6">
  <h3 class="font-semibold tracking-tight text-base md:text-lg">标题</h3>
</div>
```

### 输入框

[WRONG] **错误示例**（灰色边框、圆角）：
```html
<input class="rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500" />
```

[CORRECT] **正确示例**（黑色粗边框、聚焦阴影）：
```html
<input class="bg-zinc-100 border-0 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/30 px-3 py-2 md:px-4 md:py-3" placeholder="请输入..." />
```

---

## [TEMPLATES] 页面骨架模板

使用以下模板生成页面，只需替换 `{PLACEHOLDER}` 部分：

### 导航栏骨架
```html
<nav class="bg-white border-b-2 md:border-b-4 border-black px-4 md:px-8 py-3 md:py-4">
  <div class="flex items-center justify-between max-w-6xl mx-auto">
    <a href="/" class="font-black text-xl md:text-2xl tracking-wider">
      {LOGO_TEXT}
    </a>
    <div class="flex gap-4 md:gap-8 font-mono text-sm md:text-base">
      {NAV_LINKS}
    </div>
  </div>
</nav>
```

### Hero 区块骨架
```html
<section class="min-h-[60vh] md:min-h-[80vh] flex items-center px-4 md:px-8 py-12 md:py-0 bg-{ACCENT_COLOR} border-b-2 md:border-b-4 border-black">
  <div class="max-w-4xl mx-auto">
    <h1 class="font-black text-4xl md:text-6xl lg:text-8xl leading-tight tracking-tight mb-4 md:mb-6">
      {HEADLINE}
    </h1>
    <p class="font-mono text-base md:text-xl max-w-xl mb-6 md:mb-8">
      {SUBHEADLINE}
    </p>
    <button class="bg-black text-white font-black px-6 py-3 md:px-8 md:py-4 border-2 md:border-4 border-black shadow-[4px_4px_0px_0px_rgba(255,0,110,1)] md:shadow-[8px_8px_0px_0px_rgba(255,0,110,1)] hover:shadow-none hover:translate-x-[2px] hover:translate-y-[2px] transition-all text-sm md:text-base">
      {CTA_TEXT}
    </button>
  </div>
</section>
```

### 卡片网格骨架
```html
<section class="py-12 md:py-24 px-4 md:px-8">
  <div class="max-w-6xl mx-auto">
    <h2 class="font-black text-2xl md:text-4xl mb-8 md:mb-12">{SECTION_TITLE}</h2>
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 md:gap-6">
      <!-- Card template - repeat for each card -->
      <div class="bg-white border-2 md:border-4 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] md:shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] p-4 md:p-6 hover:shadow-[4px_4px_0px_0px_rgba(255,0,110,1)] md:hover:shadow-[8px_8px_0px_0px_rgba(255,0,110,1)] hover:-translate-y-1 transition-all">
        <h3 class="font-black text-lg md:text-xl mb-2">{CARD_TITLE}</h3>
        <p class="font-mono text-sm md:text-base text-gray-700">{CARD_DESCRIPTION}</p>
      </div>
    </div>
  </div>
</section>
```

### 页脚骨架
```html
<footer class="bg-black text-white py-12 md:py-16 px-4 md:px-8 border-t-2 md:border-t-4 border-black">
  <div class="max-w-6xl mx-auto">
    <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
      <div>
        <span class="font-black text-xl md:text-2xl">{LOGO_TEXT}</span>
        <p class="font-mono text-sm mt-4 text-gray-400">{TAGLINE}</p>
      </div>
      <div>
        <h4 class="font-black text-lg mb-4">{COLUMN_TITLE}</h4>
        <ul class="space-y-2 font-mono text-sm text-gray-400">
          {FOOTER_LINKS}
        </ul>
      </div>
    </div>
  </div>
</footer>
```

---

## [CHECKLIST] 生成后自检清单

**在输出代码前，必须逐项验证以下每一条。如有违反，立即修正后再输出：**

### 1. 圆角检查
- [ ] 搜索代码中的 `rounded-`
- [ ] 确认只有 `rounded-none` 或无圆角
- [ ] 如果发现 `rounded-lg`、`rounded-md` 等，替换为 `rounded-none`

### 2. 阴影检查
- [ ] 搜索代码中的 `shadow-`
- [ ] 确认只使用 `shadow-[Xpx_Xpx_0px_0px_rgba(...)]` 格式
- [ ] 如果发现 `shadow-lg`、`shadow-xl` 等，替换为正确格式

### 3. 边框检查
- [ ] 搜索代码中的 `border-`
- [ ] 确认边框颜色是 `border-black`
- [ ] 如果发现 `border-gray-*`、`border-slate-*`，替换为 `border-black`

### 4. 交互检查
- [ ] 所有按钮都有 `hover:shadow-none hover:translate-x-[2px] hover:translate-y-[2px]`
- [ ] 所有卡片都有 hover 效果（阴影变色或位移）
- [ ] 都包含 `transition-all`

### 5. 响应式检查
- [ ] 边框有 `border-2 md:border-4`
- [ ] 阴影有 `shadow-[4px...] md:shadow-[8px...]`
- [ ] 间距有 `p-4 md:p-6` 或类似的响应式值
- [ ] 字号有 `text-sm md:text-base` 或类似的响应式值

### 6. 字体检查
- [ ] 标题使用 `font-black`
- [ ] 正文使用 `font-mono`

> CRITICAL: **如果任何一项检查不通过，必须修正后重新生成代码。**

---

## [EXAMPLES] 示例 Prompt

### 1. 管理后台

完整的后台管理布局

```
Create an admin dashboard with fixed sidebar:
1. Fixed sidebar with logo, search, navigation, user
2. Navigation groups: Dashboard, Content, Users, Settings
3. Active page highlighted in nav
4. Main area with header and content grid
5. Mobile: hamburger menu, slide-out sidebar
6. Collapsible sidebar option (icons only)
7. User dropdown at bottom of sidebar
Professional look with blue accent color
```

### 2. 文档站点

技术文档的侧边导航布局

```
Create a documentation site with fixed sidebar:
1. Sidebar with doc sections (Getting Started, API, Examples)
2. Nested navigation with accordion expand
3. Search at top of sidebar
4. Main content area with article
5. Right sidebar with table of contents (optional)
6. Previous/Next article navigation at bottom
7. Mobile: slide-out sidebar menu
Clean minimal design focused on readability
```

### 3. SaaS 应用

SaaS 产品的应用框架

```
Create a SaaS application shell with fixed sidebar:
1. Sidebar with workspace switcher at top
2. Main nav: Home, Projects, Team, Reports
3. Secondary nav at bottom: Settings, Help
4. Notification badge on nav item
5. Main content with toolbar and data table
6. Mobile: slide-out navigation
7. Collapsible sidebar with keyboard shortcut hint
Modern SaaS aesthetic with subtle shadows
```