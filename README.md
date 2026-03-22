# MarkItDown

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Python Version](https://img.shields.io/badge/python-3.9+-blue)](https://www.python.org/)
[![PyPI](https://img.shields.io/badge/PyPI-markitdown--mcp--advanced-green)](https://pypi.org/project/markitdown-mcp-advanced/)
[![Java Version](https://img.shields.io/badge/java-17+-green)](https://www.oracle.com/java/)

> 将多种文档格式转换为 Markdown，为 AI 大模型准备高质量语料

## 简介

MarkItDown 是对微软开源项目 [MarkItDown](https://github.com/microsoft/markitdown) 的重写实现，提供三种使用方式：

- **MCP 服务器** - 与 Claude Desktop 深度集成
- **Web 应用** - 在线文档转换服务
- **命令行工具** - Java 实现的轻量级工具

## 功能特性

- 📄 支持 PDF、Word、Excel、PPT、图片等 **12+ 种文件格式**
- 🧠 **PaddleOCR** 智能版面分析和高精度 OCR 识别
- 🖼️ 图片文字提取（中英文混合识别）
- 📋 ZIP 批量转换
- 🎯 专为 AI 语料准备优化

---

## 📦 MCP 服务器（推荐）

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

---

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

## ☕ Java 命令行工具

轻量级命令行工具，适合服务器环境和批量处理。

### 快速开始

```bash
# 1. 编译打包
cd java
mvn clean package -DskipTests

# 2. 运行
java -jar target/markitdown-java.jar document.docx -o output.md

# 3. 批量转换
java -jar target/markitdown-java.jar *.docx
```

### 命令行选项

```bash
Usage: markitdown [OPTIONS] INPUT_FILES...

主要选项:
  -o, --output <FILE>      输出文件或目录
  --language <LANG>       OCR 语言 (默认: auto)
  -v, --verbose           详细输出
  -h, --help              显示帮助
```

### 配置文件系统

Java 命令行工具支持 properties 格式的配置文件，可以预设常用参数和引擎路径：

#### 生成配置文件

```bash
java -jar target/markitdown-java.jar --generate-config
```

这将在当前目录创建 `.markitdown.properties` 文件。

#### 配置文件示例

```properties
# 引擎路径配置
tesseract.path=O:\\tesserOCR
tessdata.path=O:\\tesserOCR\\tessdata

# 输出配置
output.dir=./output
output.image.dir=assets

# 处理选项
content.include.images=true
content.include.tables=true
ocr.enable=false
ocr.language=auto

# 性能配置
performance.parallel=true
performance.threads=0
```

#### 使用配置文件

```bash
# 直接使用配置文件中的设置
java -jar target/markitdown-java.jar document.pdf

# 命令行参数会覆盖配置文件设置
java -jar target/markitdown-java.jar document.pdf --ocr --verbose

# 查看当前生效的配置
java -jar target/markitdown-java.jar --show-config

# 验证配置文件
java -jar target/markitdown-java.jar --validate-config
```

#### 配置优先级

1. 命令行参数（最高优先级）
2. 环境变量（如 `TESSERACT_PATH`, `MARKITDOWN_OUTPUT_DIR`）
3. 配置文件
4. 默认值（最低优先级）

#### 详细文档

完整的配置文件说明请参考：[java/CONFIGURATION_GUIDE.md](java/CONFIGURATION_GUIDE.md)

---

## 📚 支持格式总览

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
