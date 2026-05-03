# MarkItDown

## Repository Priority

This repository is organized with the following priority:

1. `markitdown` Java CLI
2. `markitdown-mcp`
3. `markitdown-web`

If you are looking for the main end-user project, start with the Java CLI in [java/README.md](/O:/markitdown/java/README.md).

## Project Memory

For future Codex or contributor sessions, the current project memory lives in:

- [memory/SESSION_BOOT.md](/O:/markitdown/memory/SESSION_BOOT.md)
- [memory/PROJECT_MEMORY.md](/O:/markitdown/memory/PROJECT_MEMORY.md)
- [memory/LONG_TERM_NOTES.md](/O:/markitdown/memory/LONG_TERM_NOTES.md)

These files summarize the current Java CLI direction, OCR/provider architecture,
packaging profiles, smoke-test references, and known environment gotchas.

## 2026-05 Release Notes

The Java CLI now ships as multiple artifacts so users can choose smaller
packages or platform-focused OCR packages.

### Java CLI artifacts

| Profile | Artifact | OCR strategy | Recommended usage |
| --- | --- | --- | --- |
| `lite` | `markitdown4j-<version>-lite.jar` | No embedded `tess4j` native files | Default download, CI, no OCR, external OCR |
| `full` | `markitdown4j-<version>-full.jar` | Includes full `tess4j` resources | Windows users who want embedded OCR |
| `win32` | `markitdown4j-<version>-win32.jar` | Includes only 32-bit Windows OCR native files | 32-bit Windows |
| `win64` | `markitdown4j-<version>-win64.jar` | Includes only 64-bit Windows OCR native files | 64-bit Windows |
| `linux64` | `markitdown4j-<version>-linux64.jar` | No embedded `tess4j`; use `tesseract-cli` | Linux |
| `mac` | `markitdown4j-<version>-mac.jar` | No embedded `tess4j`; use `tesseract-cli` | macOS |

### Recommended downloads

- Windows 64-bit: `win64` or `full`
- Windows 32-bit: `win32`
- Linux: `linux64`
- macOS: `mac`
- No OCR / smallest package: `lite`

### OCR engine guidance

- `tess4j`: best for `full`, `win32`, `win64`
- `tesseract-cli`: best for `linux64`, `mac`, `lite`
- `mock`: tests and debugging
- `http`: optional external integration

### Build commands

```bash
# default lightweight package
mvn package -DskipTests

# full OCR package
mvn package -DskipTests -Pfull

# platform packages
mvn package -DskipTests -Pwin32
mvn package -DskipTests -Pwin64
mvn package -DskipTests -Plinux64
mvn package -DskipTests -Pmac
```

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Python Version](https://img.shields.io/badge/python-3.9+-blue)](https://www.python.org/)
[![PyPI](https://img.shields.io/badge/PyPI-markitdown--mcp--advanced-green)](https://pypi.org/project/markitdown-mcp-advanced/)
[![Java Version](https://img.shields.io/badge/java-17+-green)](https://www.oracle.com/java/)

> 将多种文档格式转换为 Markdown，为 AI 大模型准备高质量语料

## 简介

MarkItDown 是对微软开源项目 [MarkItDown](https://github.com/microsoft/markitdown) 的重写实现，当前以 Java CLI 为主线，同时保留 MCP 和其他子项目目录。

- **☕ Java 命令行工具** - 当前主推的使用方式
- **📦 MCP 服务器** - 面向集成场景的可选子项目
- **其他子项目** - 根据仓库目录独立维护

## 功能特性

- 📄 支持 PDF、Word、Excel、PPT、图片等 **12+ 种文件格式**
- 🔡 支持 `tess4j`、`tesseract-cli` 等可选 OCR 后端
- 🖼️ 图片文字提取（中英文混合识别）
- 📋 ZIP 批量转换
- 🎯 专为 AI 语料准备优化

---

## ☕ Java 命令行工具（推荐）

**最简单直接的使用方式** - 下载即用，无需安装任何依赖，支持 9+ 种文档格式转换。

### 📦 下载发布版本

**最新版本 v0.0.2** - 已通过 103 个测试用例验证，100% 测试通过率

```bash
# 下载主程序
wget https://github.com/DuanYan007/markitdown/releases/download/v0.0.2/markitdown4j.jar

# 或使用 curl
curl -O https://github.com/DuanYan007/markitdown/releases/download/v0.0.2/markitdown4j.jar

# 下载测试文件包（可选，用于功能验证）
wget https://github.com/DuanYan007/markitdown/releases/download/v0.0.2-test/test-files.zip
```

或访问 [GitHub Releases](https://github.com/DuanYan007/markitdown/releases) 查看所有版本。

### 🚀 快速开始（3步上手）

#### 第一步：验证 Java 环境

```bash
# 检查是否已安装 Java（需要 JDK 17 或更高版本）
java -version

# 如果未安装或版本过低，请访问：
# Windows: https://www.oracle.com/java/technologies/downloads/
# Linux: sudo apt-get install openjdk-17-jre (Ubuntu/Debian)
# macOS: brew install openjdk@17
```

#### 第二步：开始转换文档

```bash
# 基础转换 - PDF 转 Markdown
java -jar markitdown4j.jar document.pdf -o output.md

# Word 文档转换
java -jar markitdown4j.jar report.docx -o report.md

# Excel 表格转换
java -jar markitdown4j.jar data.xlsx -o data.md

# 批量转换当前目录所有 PDF
java -jar markitdown4j.jar *.pdf

# 查看帮助信息
java -jar markitdown4j.jar --help
```

#### 第三步：（可选）配置 OCR 功能

如果需要使用图片文字识别功能，需要安装 Tesseract OCR 引擎和语言数据：

**Windows 用户**：

1. **下载 Tesseract**
   - 访问：https://github.com/UB-Mannheim/tesseract/wiki
   - 下载最新版本的安装包（例如 `tesseract-ocr-w64-setup-5.x.x.exe`）

2. **运行安装程序**
   - 建议安装路径：`D:\Tools\Tesseract-OCR\` 或其他自定义路径
   - **注意**：安装程序可以不勾选语言包（我们手动下载）

3. **下载语言数据包**（必须手动操作）
   - 简体中文：https://github.com/tesseract-ocr/tessdata/raw/main/chi_sim.traineddata
   - 繁体中文：https://github.com/tesseract-ocr/tessdata/raw/main/chi_tra.traineddata
   - 英文（通常已包含）：https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata

4. **放置语言数据文件**
   - 在 Tesseract 安装目录下创建 `tessdata` 文件夹（如果不存在）
   - 将下载的 `.traineddata` 文件复制到 `tessdata` 目录中
   - 例如：`D:\Tools\Tesseract-OCR\tessdata\chi_sim.traineddata`

5. **创建配置文件**
   在 markitdown4j.jar 同目录下创建 `.markitdown.properties` 文件：
   ```properties
   # Tesseract 引擎路径（请修改为您的实际安装路径）
   tesseract.path=D:\\Tools\\Tesseract-OCR
   tessdata.path=D:\\Tools\\Tesseract-OCR\\tessdata

   # OCR 配置
   ocr.enable=true
   ocr.language=auto
   ```

6. **验证配置**
   ```bash
   # 查看当前生效的配置
   java -jar markitdown4j.jar --show-config

   # 测试 OCR 功能
   java -jar markitdown4j.jar image.png --ocr -o result.md
   ```

**Linux 用户**：

```bash
# 安装 Tesseract 和中文语言包
sudo apt-get update
sudo apt-get install tesseract-ocr tesseract-ocr-chi-sim tesseract-ocr-chi-tra

# 验证安装和语言包
tesseract --version
tesseract --list-langs
# 应显示：eng, chi_sim, chi_tra 等
```

**macOS 用户**：

```bash
# 使用 Homebrew 安装 Tesseract
brew install tesseract

# 安装语言包
brew install tesseract-lang

# 验证安装
tesseract --version
tesseract --list-langs
```

**语言包下载地址汇总**：

| 语言 | 下载链接 |
|------|----------|
| 简体中文 | [chi_sim.traineddata](https://github.com/tesseract-ocr/tessdata/raw/main/chi_sim.traineddata) |
| 繁体中文 | [chi_tra.traineddata](https://github.com/tesseract-ocr/tessdata/raw/main/chi_tra.traineddata) |
| 英文 | [eng.traineddata](https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata) |
| 日语 | [jpn.traineddata](https://github.com/tesseract-ocr/tessdata/raw/main/jpn.traineddata) |
| 韩语 | [kor.traineddata](https://github.com/tesseract-ocr/tessdata/raw/main/kor.traineddata) |

> **重要提示**：
> - Windows 用户必须手动下载 `.traineddata` 文件并放置到 `tessdata` 目录
> - `tessdata` 目录路径需要在配置文件中正确指定
> - 配置文件中的路径分隔符需要双写：`D:\\Tools\\Tesseract-OCR`

### 📋 支持格式

| 格式类别 | 扩展名 | 转换能力 |
|---------|--------|----------|
| **PDF** | `.pdf` | 文本提取 + 加密支持 |
| **Word** | `.docx`, `.doc` | 表格、图片、样式 |
| **Excel** | `.xlsx`, `.xls` | 公式、多工作表 |
| **PowerPoint** | `.pptx`, `.ppt` | 幻灯片、图表 |
| **HTML** | `.html`, `.htm` | 网页解析、表格 |
| **图片** | `.png`, `.jpg`, `.gif`, `.bmp` | OCR 文字识别 |
| **音频** | `.mp3`, `.wav` | 元数据提取 |
| **文本** | `.txt`, `.csv`, `.json`, `.xml` | 格式转换 |
| **ZIP** | `.zip` | 批量处理 |

### 💡 常用示例

```bash
# 1. 加密 PDF 转换
java -jar markitdown4j.jar secret.pdf --pdf-password your_password -o output.md

# 2. 图片 OCR 文字识别
java -jar markitdown4j.jar image.png --ocr -o result.md

# 3. 中文 OCR 识别
java -jar markitdown4j.jar chinese.png --ocr -l chi_sim -o result.md

# 4. 批量转换并显示进度
java -jar markitdown4j.jar *.pdf --batch --progress -o output/

# 5. 并行处理（加速）
java -jar markitdown4j.jar *.pdf --parallel --threads 4 -o output/

# 6. 只提取文本，不包含图片
java -jar markitdown4j.jar document.docx --no-images -o clean.md
```

### ⚙️ 配置文件系统

Java 命令行工具支持 properties 格式的配置文件，可以预设常用参数和引擎路径：

#### 生成配置文件

```bash
java -jar markitdown4j.jar --generate-config
```

这将在当前目录创建 `.markitdown.properties` 文件。

#### 配置文件示例

```properties
# ==================== 引擎路径配置 ====================
# 请根据您的实际安装路径修改以下路径

# Windows 示例（请使用您的实际安装路径）
tesseract.path=D:\\Tools\\Tesseract-OCR
tessdata.path=D:\\Tools\\Tesseract-OCR\\tessdata

# Linux 示例
# tesseract.path=/usr/bin
# tessdata.path=/usr/share/tesseract-ocr/4.00/tessdata

# macOS 示例
# tesseract.path=/opt/homebrew/bin
# tessdata.path=/opt/homebrew/share/tessdata

# ==================== 输出配置 ====================
output.dir=./output
output.image.dir=assets

# ==================== 处理选项 ====================
content.include.images=true
content.include.tables=true
content.include.metadata=true

# ==================== OCR 配置 ====================
ocr.enable=false
ocr.language=auto

# ==================== 性能配置 ====================
performance.parallel=true
performance.threads=0
```

#### 使用配置文件

```bash
# 直接使用配置文件中的设置
java -jar markitdown4j.jar document.pdf

# 命令行参数会覆盖配置文件设置
java -jar markitdown4j.jar document.pdf --ocr --verbose

# 查看当前生效的配置
java -jar markitdown4j.jar --show-config

# 验证配置文件
java -jar markitdown4j.jar --validate-config
```

#### 配置优先级

1. 命令行参数（最高优先级）
2. 环境变量（如 `TESSERACT_PATH`, `MARKITDOWN_OUTPUT_DIR`）
3. 配置文件
4. 默认值（最低优先级）

### 📚 详细文档

- **安装配置指南**: [java/INSTALLATION.md](java/INSTALLATION.md)
- **命令行参数参考**: [java/COMMAND_REFERENCE.md](java/COMMAND_REFERENCE.md)
- **测试文件清单**: [java/TEST_FILES.md](java/TEST_FILES.md)
- **使用说明**: [java/README.md](java/README.md)

---

## 📦 MCP 服务器

[MCP (Model Context Protocol)](https://modelcontextprotocol.io/) 服务器版本已发布到 PyPI，可直接与 Claude Desktop 集成使用。

### 安装

```bash
pip install markitdown-mcp-advanced
```

### 配置 Claude Desktop

在 Claude Desktop 配置文件中添加：

```json
{
  "mcpServers": {
    "markitdown": {
      "command": "uvx",
      "args": ["--from", "markitdown-mcp-advanced", "markitdown-mcp"],
      "env": {
        "PADDLE_API_URL": "your_paddle_api_url",
        "PADDLE_TOKEN": "your_paddle_token", 
         "MARKITDOWN_TEMP_DIR": "your_temp_dir"
      }
    }
  }
}
```

### 获取 PaddleOCR API 凭证

访问 [PaddleOCR AI Studio](https://aistudio.baidu.com/paddleocr/)，点击 "API" 按钮获取 API URL 和 Token。

### 使用示例

配置完成后，在 Claude Desktop 中直接对话：

```
Convert this PDF to Markdown: /path/to/document.pdf
```

```
Download and convert: https://example.com/article.html
```

### 支持格式

| 类别 | 扩展名 |
|------|--------|
| PDF | `.pdf` |
| 图片 | `.png`, `.jpg`, `.jpeg`, `.gif`, `.bmp`, `.tiff`, `.webp` |
| Word | `.docx` |
| PowerPoint | `.pptx` |
| Excel | `.xlsx`, `.xls` |
| Web | `.html`, `.htm` |
| CSV | `.csv` |

详见 [markitdown-mcp/README.md](markitdown-mcp/README.md)

## 🌐 Web 应用

基于 Flask 的 Web 应用，提供可视化界面和批量处理能力。

### 快速开始

```bash
# 1. 进入目录
cd markitdown-web/conveter

# 2. 创建虚拟环境
python -m venv venv

# Windows:
venv\Scripts\activate
# macOS/Linux:
source venv/bin/activate

# 3. 安装依赖
pip install --upgrade pip
pip install -r requirements.txt

# 4. 启动服务
python app.py
```

访问 http://localhost:5000

### 功能亮点

- 拖拽上传，格式自动识别
- 实时预览转换结果
- ZIP 批量转换
- 转换历史管理
- 动态配置管理

详见 [markitdown-web/readme.md](markitdown-web/readme.md)

---

## 📚 支持格式总览

| 格式类别 | 扩展名 | MCP | Web | Java |
|---------|--------|-----|-----|------|
| **PDF** | .pdf | ✅ | ✅ | ✅ |
| **Word** | .doc, .docx | ✅ | ✅ | ✅ |
| **Excel** | .xls, .xlsx | ✅ | ✅ | ✅ |
| **PowerPoint** | .ppt, .pptx | ✅ | ✅ | ✅ |
| **图片** | .jpg, .png, .gif, .bmp, .tiff, .webp | ✅ | ✅ | ✅ |
| **HTML** | .html, .htm | ✅ | ✅ | ✅ |
| **CSV** | .csv | ✅ | ✅ | ✅ |
| **音频** | .mp3, .wav, .ogg, .flac, .m4a | ❌ | ✅ | ✅ |
| **视频** | .mp4, .avi, .mov, .mkv | ❌ | ✅ | ❌ |
| **JSON/XML** | .json, .xml | ❌ | ✅ | ✅ |
| **文本** | .txt, .log, .md | ✅ | ✅ | ✅ |
| **ZIP** | .zip | ❌ | ✅ | ✅ |

> ✅ 完整支持 | ⚠️ 基础支持 | ❌ 不支持

---

## 🏗️ 技术栈

### MCP 服务器
- MCP Protocol (STDIO/HTTP)
- PaddleOCR API
- Python 标准库（轻量级设计）

### Web 应用
- Flask + PaddleOCR PP-StructureV3
- 动态配置热更新
- 原子性文件迁移

### Java 工具
- Apache POI（Office 文档）
- PicoCLI（命令行界面）
- Jackson（JSON/XML）

---

## 📄 许可证

[MIT License](LICENSE)

---

## 🙏 致谢

- [Microsoft MarkItDown](https://github.com/microsoft/markitdown) - 原始项目
- [PaddleOCR](https://github.com/PaddlePaddle/PaddleOCR) - OCR 框架
- [Flask](https://flask.palletsprojects.com/) - Web 框架
- [Apache POI](https://poi.apache.org/) - Office 文档处理

---

## 👨‍💻 作者

**DuanYan** - [GitHub](https://github.com/DuanYan007)
