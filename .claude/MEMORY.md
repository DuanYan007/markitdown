# MarkItDown 项目记忆

> 最后更新: 2025-02-28
>
> **重要更新**: 2025-02-28 完成 Web 应用代码架构重构 (见下方架构变更记录)
> 作者: DuanYan (GitHub: DuanYan007)
> 许可证: MIT License

## 项目概述

MarkItDown 是对微软开源项目 [MarkItDown](https://github.com/microsoft/markitdown) 的重写实现，将多种文档格式转换为 Markdown，专为 AI 大模型准备高质量语料。

### 核心定位
- **文档转换**: 将 PDF、Office、图片等多种格式转为 Markdown
- **AI 语料准备**: 优化输出格式，适合大语言模型训练/推理使用
- **多平台支持**: MCP 服务器 / Web 应用 / Java 命令行工具

---

## 项目结构

```
markitdown/
├── markitdown-mcp/          # MCP 服务器 (Python)
│   ├── src/markitdown_mcp_advanced/
│   │   ├── __main__.py      # 入口，支持 STDIO/HTTP 两种模式
│   │   ├── converters/      # 格式转换器
│   │   ├── config.py        # 配置管理
│   │   └── utils.py         # 工具函数
│   ├── pyproject.toml       # Python 项目配置
│   └── README.md
│
├── markitdown-web/          # Web 应用 (Flask) - 已重构为模块化架构
│   └── conveter/
│       ├── app.py                   # 应用入口 (精简为 ~80 行)
│       ├── api/                     # 路由蓝图层
│       │   ├── main.py              # 主页面路由
│       │   ├── conversion.py        # 转换路由
│       │   ├── batch.py             # 批量处理路由
│       │   ├── config.py            # 配置管理路由
│       │   ├── history.py           # 历史记录路由
│       │   └── files.py             # 文件服务路由
│       ├── services/                # 业务逻辑层
│       │   ├── conversion_service.py # 转换服务
│       │   ├── batch_service.py      # 批量转换服务
│       │   ├── history_service.py    # 历史记录服务
│       │   └── format_service.py     # 格式检测服务
│       ├── utils/                   # 工具函数层
│       │   ├── logging_config.py     # 日志配置
│       │   ├── path_helpers.py       # 路径处理
│       │   └── image_path_processor.py # 图片路径处理
│       ├── core/                    # 核心配置
│       │   └── extensions.py         # 扩展初始化
│       ├── middleware/               # 中间件
│       │   └── error_handlers.py     # 错误处理
│       ├── config_manager.py        # 动态配置管理
│       ├── file_migrator.py         # 文件迁移器
│       ├── converters/              # 转换器模块
│       ├── static/                  # 前端资源
│       ├── templates/               # HTML 模板
│       ├── uploads/                 # 上传临时目录
│       └── downloads/               # 转换结果目录
│
├── java/                   # Java 命令行工具
│   └── com/
│       ├── markdown/engine/         # Markdown 构建引擎
│       ├── markitdown/api/          # API 定义
│       ├── markitdown/cli/          # 命令行接口
│       ├── markitdown/converter/    # 各格式转换器
│       ├── markitdown/core/         # 核心引擎
│       └── markitdown/utils/        # 工具类
│
├── pom.xml                # Maven 配置 (Java 11)
├── README.md              # 项目主文档
└── images/                # 文档图片资源
```

---

## 三个子模块详解

### 1. MCP 服务器 (`markitdown-mcp`)

**功能**: 与 Claude Desktop 深度集成的 MCP 协议服务器

**技术栈**:
- MCP Protocol (STDIO/HTTP 双模式)
- PaddleOCR API (远程 OCR 服务)
- Python 标准库 (轻量级设计)
- FastMCP + Starlette + Uvicorn

**已发布 PyPI**: `markitdown-mcp-advanced`

**MCP Tools**:
- `convert_to_markdown(source)` - 转换文件或 URL
- `list_supported_formats()` - 列出支持的格式

**配置环境变量**:
```bash
PADDLE_API_URL     # PaddleOCR API 地址 (必需)
PADDLE_TOKEN       # PaddleOCR Token (必需)
MARKITDOWN_TEMP_DIR # 临时文件目录 (可选)
```

**支持的格式**:
- PDF, 图片 (PNG/JPG/JPEG/GIF/BMP/TIFF/WEBP)
- Word (.docx), PowerPoint (.pptx)
- Excel (.xlsx, .xls - 需额外依赖)
- HTML/HTM, CSV

**启动方式**:
```bash
# STDIO 模式 (Claude Desktop 默认)
markitdown-mcp

# HTTP 模式 (独立服务器)
markitdown-mcp --http --host 127.0.0.1 --port 7566
```

---

### 2. Web 应用 (`markitdown-web/conveter`)

**功能**: 基于 Flask 的在线文档转换服务

**技术栈**:
- Flask + PaddleOCR PP-StructureV3 (本地 OCR)
- 动态配置热更新
- 原子性文件迁移
- 响应式前端设计

**核心功能**:
1. **拖拽上传** - 格式自动识别
2. **实时预览** - 转换结果即时查看
3. **ZIP 批量转换** - 支持压缩包批量处理
4. **转换历史管理** - 历史记录、重新下载
5. **动态配置管理** - 路径、限制等运行时配置

**API 端点**:
| 端点 | 方法 | 功能 |
|------|------|------|
| `/upload/<format_type>` | POST | 文件上传 |
| `/convert/<format_type>` | POST | 文件转换 |
| `/download-md` | GET | 下载结果 |
| `/upload/batch` | POST | 批量上传 |
| `/api/history` | GET | 转换历史 |
| `/api/config` | GET/PUT | 配置管理 |
| `/config` | GET | 配置界面 |

**转换器** (`converters/`):
- `pdf_converter.py` - PDF 转换
- `word_converter.py` - Word 转换
- `img_converter.py` - 图片 OCR
- `audio_converter.py` - 音频元数据
- `video_converter.py` - 视频元数据
- `ppt_native_converter.py` - PowerPoint
- `archive_extractor.py` - ZIP 解压
- `csv_converter.py` - CSV 转换

**启动方式**:
```bash
cd markitdown-web/conveter
python app.py  # 默认端口 5000
```

---

### 3. Java 命令行工具 (`java/`)

**功能**: 轻量级命令行工具，适合服务器环境和批量处理

**技术栈**:
- Java 11+
- Apache POI (Office 文档)
- Apache PDFBox (PDF 处理)
- Jsoup (HTML 解析)
- Tess4J (OCR)
- PicoCLI (命令行界面)

**核心类**:
| 类路径 | 功能 |
|--------|------|
| `MarkItDownApplication` | 主入口 |
| `MarkItDownEngine` | 转换引擎 |
| `ConverterRegistry` | 转换器注册表 |
| `DocumentConverter` | 转换器接口 |
| `MarkItDownCommand` | CLI 命令定义 |

**转换器**:
- `PdfConverter` - PDF
- `DocxConverter` - Word
- `XlsxConverter` - Excel
- `PptxConverter` - PowerPoint
- `HtmlConverter` - HTML
- `ImageConverter` - 图片 OCR
- `AudioConverter` - 音频
- `TextConverter` - 纯文本

**构建运行**:
```bash
cd java
mvn clean package -DskipTests
java -jar target/markitdown-java.jar document.docx -o output.md
```

**命令行选项**:
```bash
-o, --output <FILE>   # 输出文件或目录
--language <LANG>     # OCR 语言 (默认: auto)
-v, --verbose         # 详细输出
```

---

## 支持格式总览

| 格式类别 | 扩展名 | MCP | Web | Java |
|---------|--------|-----|-----|------|
| **PDF** | .pdf | ✅ | ✅ | ⚠️ |
| **Word** | .doc, .docx | ✅ | ✅ | ✅ |
| **Excel** | .xls, .xlsx | ✅ | ✅ | ✅ |
| **PowerPoint** | .ppt, .pptx | ✅ | ✅ | ✅ |
| **图片** | .jpg, .png, .gif, .bmp, .tiff, .webp | ✅ | ✅ | ❌ |
| **HTML** | .html, .htm | ✅ | ✅ | ✅ |
| **CSV** | .csv | ✅ | ✅ | ✅ |
| **音频** | .mp3, .wav, .ogg, .flac, .m4a | ❌ | ✅ | ✅ |
| **视频** | .mp4, .avi, .mov, .mkv | ❌ | ✅ | ❌ |
| **JSON/XML** | .json, .xml | ❌ | ✅ | ✅ |
| **文本** | .txt, .log, .md | ✅ | ✅ | ✅ |
| **ZIP** | .zip | ❌ | ✅ | ❌ |

> ✅ 完整支持 | ⚠️ 基础支持 | ❌ 不支持

---

## 开发规范

### Python 代码规范
- 使用类型注解
- 文档字符串采用 Google 风格
- 异常处理要明确，避免裸 `except`
- PaddleOCR API 调用需要容错重试

### Java 代码规范
- 遵循 Java 命名约定
- 使用 SLF4J 进行日志记录
- 转换器实现 `DocumentConverter` 接口
- 使用 `ConverterRegistry` 注册新转换器

### 前端规范
- 响应式设计，支持移动端
- AJAX 请求要有加载状态提示
- 错误信息要友好展示

---

## 关键配置

### PaddleOCR 配置
MCP 使用远程 API，Web 应用使用本地 PP-StructureV3

获取 API: https://aistudio.baidu.com/paddleocr/

### 文件路径
- `uploads/` - 临时上传目录
- `downloads/` - 转换结果目录
- `converted/` - 已转换文件存档

### 动态配置 (Web)
存储在 `config.json`，运行时可热更新:
- 存储路径
- 文件大小限制
- 历史记录数量

---

## 架构变更记录

### 2025-02-28: Web 应用代码架构重构

**重构原因**: 原 `app.py` 文件过大（1440行），包含路由、业务逻辑、工具函数，难以维护和测试。

**重构内容**:
1. 将单体 `app.py` 拆分为清晰的分层架构
2. 实现 Flask Blueprint 路由模块化
3. 服务层与路由层分离
4. 工具函数独立成模块
5. 保持所有 API 端点兼容

**新增文件结构**:
```
conveter/
├── app.py              # 应用入口 (精简为 ~80 行)
├── api/                # 路由蓝图层 (7个文件)
├── services/           # 业务逻辑层 (4个文件)
├── utils/              # 工具函数层 (4个文件)
├── core/               # 核心配置 (2个文件)
└── middleware/         # 中间件 (2个文件)
```

**已验证**: 应用可以正常启动，所有蓝图已注册。

---

## 待办事项 / 扩展方向

<!-- 此区域记录计划中的功能和改进 -->

### 功能扩展
- [ ] 更多格式支持 (RTF, EPUB, etc.)
- [ ] 并行处理优化
- [ ] 转换质量评估

### 性能优化
- [ ] 大文件流式处理
- [ ] 缓存机制
- [ ] OCR 结果缓存

### 开发体验
- [ ] 单元测试覆盖
- [ ] CI/CD 流程
- [ ] Docker 镜像

---

## 联系方式

- **作者**: DuanYan
- **GitHub**: https://github.com/DuanYan007
- **邮箱**: duanyan2024@gmail.com, 2907762730@qq.com
- **问题反馈**: https://github.com/DuanYan007/markitdown/issues
