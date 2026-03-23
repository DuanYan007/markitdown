# MarkItDown Java 命令行参数完全参考

## 📋 完整命令行参数表

| 参数 | 长参数 | 参数值 | 说明 | 默认值 | 示例 |
|------|--------|--------|------|--------|------|
| **基础选项** |||||||
| `-h` | `--help` | - | 显示帮助信息 | - | `java -jar markitdown4j.jar --help` |
| `-V` | `--version` | - | 显示版本信息 | - | `java -jar markitdown4j.jar --version` |
| `-v` | `--verbose` | - | 详细输出模式 | 关闭 | `java -jar markitdown4j.jar doc.pdf -v` |
| `-q` | `--quiet` | - | 静默模式（仅显示错误） | 关闭 | `java -jar markitdown4j.jar doc.pdf -q` |
| `-o` | `--output` | `<path>` | 指定输出文件或目录 | 标准输出 | `java -jar markitdown4j.jar doc.pdf -o out.md` |
| **内容控制** |||||||
| `--include-metadata` | - | - | 包含文档元数据 | true | `java -jar markitdown4j.jar doc.pdf --include-metadata` |
| `--no-metadata` | - | - | 排除文档元数据 | - | `java -jar markitdown4j.jar doc.pdf --no-metadata` |
| `--include-images` | - | - | 包含图片内容 | true | `java -jar markitdown4j.jar doc.docx --include-images` |
| `--no-images` | - | - | 排除图片内容 | - | `java -jar markitdown4j.jar doc.docx --no-images` |
| `--include-tables` | - | - | 包含表格内容 | true | `java -jar markitdown4j.jar doc.xlsx --include-tables` |
| `--no-tables` | - | - | 排除表格内容 | - | `java -jar markitdown4j.jar doc.xlsx --no-tables` |
| `--image-format` | - | `<format>` | 图片格式 (markdown/html/base64) | markdown | `java -jar markitdown4j.jar doc.docx --image-format html` |
| `--table-format` | - | `<format>` | 表格格式 (github/markdown/pipe) | github | `java -jar markitdown4j.jar doc.pdf --table-format pipe` |
| **OCR 选项** |||||||
| `--ocr` | - | - | 启用 OCR 文字识别 | 关闭 | `java -jar markitdown4j.jar img.png --ocr` |
| `-l` | `--language` | `<lang>` | OCR 语言 (auto/eng/chi_sim/chi_tra/jpn/kor/fra/deu) | auto | `java -jar markitdown4j.jar img.png --ocr -l chi_sim` |
| `--image-output-dir` | - | `<dir>` | 提取图片的输出目录 | assets/ | `java -jar markitdown4j.jar doc.docx --image-output-dir images/` |
| **PDF 选项** |||||||
| `--pdf-password` | - | `<password>` | 加密 PDF 密码 | - | `java -jar markitdown4j.jar secret.pdf --pdf-password pass123` |
| `--large-file` | - | - | 允许处理大文件（>50MB） | 关闭 | `java -jar markitdown4j.jar large.pdf --large-file` |
| `--max-file-size` | - | `<bytes>` | 最大文件大小限制 | 50MB | `java -jar markitdown4j.jar doc.pdf --max-file-size 100000000` |
| **性能选项** |||||||
| `-p` | `--parallel` | - | 启用并行处理 | 关闭 | `java -jar markitdown4j.jar *.pdf --parallel` |
| `--threads` | - | `<num>` | 线程数量 | CPU 核心数 | `java -jar markitdown4j.jar *.pdf --parallel --threads 8` |
| `--progress` | - | - | 显示进度条 | 关闭 | `java -jar markitdown4j.jar doc.pdf --progress` |
| `--stats` | - | - | 显示性能统计 | 关闭 | `java -jar markitdown4j.jar doc.pdf --stats` |
| `--optimize-memory` | - | - | 启用内存优化 | 关闭 | `java -jar markitdown4j.jar large.pdf --optimize-memory` |
| `--temp-dir` | - | `<dir>` | 临时文件目录 | 系统临时目录 | `java -jar markitdown4j.jar doc.pdf --temp-dir /tmp` |
| **目录处理** |||||||
| `-r` | `--recursive` | - | 递归处理子目录 | 关闭 | `java -jar markitdown4j.jar docs/ --recursive` |
| `--batch` | - | - | 批量处理目录中所有支持的文件 | 关闭 | `java -jar markitdown4j.jar docs/ --batch` |
| **用户体验** |||||||
| `-i` | `--interactive` | - | 启用交互模式（详细反馈） | 关闭 | `java -jar markitdown4j.jar doc.pdf --interactive` |
| `--examples` | - | - | 显示使用示例 | - | `java -jar markitdown4j.jar --examples` |
| `--format` | - | `<fmt>` | 输出格式 (markdown/plain/json) | markdown | `java -jar markitdown4j.jar doc.pdf --format json` |

## 🌍 OCR 支持的语言

**📖 详细安装配置指南**: [INSTALLATION.md](INSTALLATION.md#ocr-配置可选) - Tesseract 安装和语言包配置

| 语言代码 | 语言名称 | 语言包文件 | 下载链接 |
|----------|----------|------------|----------|
| `auto` | 自动检测 | - | - |
| `eng` | 英语 | eng.traineddata | [下载](https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata) |
| `chi_sim` | 简体中文 | chi_sim.traineddata | [下载](https://github.com/tesseract-ocr/tessdata/raw/main/chi_sim.traineddata) |
| `chi_tra` | 繁体中文 | chi_tra.traineddata | [下载](https://github.com/tesseract-ocr/tessdata/raw/main/chi_tra.traineddata) |
| `jpn` | 日语 | jpn.traineddata | [下载](https://github.com/tesseract-ocr/tessdata/raw/main/jpn.traineddata) |
| `kor` | 韩语 | kor.traineddata | [下载](https://github.com/tesseract-ocr/tessdata/raw/main/kor.traineddata) |
| `fra` | 法语 | fra.traineddata | [下载](https://github.com/tesseract-ocr/tessdata/raw/main/fra.traineddata) |
| `deu` | 德语 | deu.traineddata | [下载](https://github.com/tesseract-ocr/tessdata/raw/main/deu.traineddata) |
| `spa` | 西班牙语 | spa.traineddata | [下载](https://github.com/tesseract-ocr/tessdata/raw/main/spa.traineddata) |
| `rus` | 俄语 | rus.traineddata | [下载](https://github.com/tesseract-ocr/tessdata/raw/main/rus.traineddata) |
| `ara` | 阿拉伯语 | ara.traineddata | [下载](https://github.com/tesseract-ocr/tessdata/raw/main/ara.traineddata) |

> **注意**: 语言包文件需要放置在 Tesseract 的 `tessdata` 目录中。详细的安装步骤和配置请参考 [INSTALLATION.md](INSTALLATION.md#ocr-配置可选)

## 📊 输出格式选项

### 图片格式 (`--image-format`)

| 格式 | 说明 | 示例输出 |
|------|------|----------|
| `markdown` | 标准 Markdown 图片引用 | `![image](assets/image_0.png)` |
| `html` | HTML img 标签 | `<img src="assets/image_0.png" />` |
| `base64` | Base64 编码内嵌 | `![image](data:image/png;base64,...)` |

### 表格格式 (`--table-format`)

| 格式 | 说明 | 示例 |
|------|------|------|
| `github` | GitHub Flavored Markdown | ` \| Header \| \n \| --- \| \n \| Data \|` |
| `markdown` | 标准 Markdown 表格 | 同 GitHub 格式 |
| `pipe` | 管道符表格 | ` \| Header \| \| Data \|` |

### 输出格式 (`--format`)

| 格式 | 说明 | 用途 |
|------|------|------|
| `markdown` | Markdown 格式（默认） | 标准文档转换 |
| `plain` | 纯文本格式 | 简单文本提取 |
| `json` | JSON 格式 | 程序化处理 |

## 🔧 常用组合示例

| 场景 | 命令示例 |
|------|----------|
| 基础转换 | `java -jar markitdown4j.jar document.pdf -o output.md` |
| 加密 PDF | `java -jar markitdown4j.jar secret.pdf --pdf-password pass123 -o output.md` |
| 图片 OCR | `java -jar markitdown4j.jar image.png --ocr -l chi_sim -o result.md` |
| 批量转换 | `java -jar markitdown4j.jar docs/*.pdf --batch --progress -o output/` |
| 大文件处理 | `java -jar markitdown4j.jar large.pdf --large-file --optimize-memory --stats` |
| 并行处理 | `java -jar markitdown4j.jar *.pdf --parallel --threads 8 -o output/` |
| 清洁输出 | `java -jar markitdown4j.jar doc.docx --no-metadata --no-images -o clean.md` |
| 递归处理 | `java -jar markitdown4j.jar docs/ --recursive --batch --stats` |

## 📝 退出代码

| 代码 | 含义 |
|------|------|
| `0` | 成功 |
| `1` | 转换失败 |
| `2` | 文件不存在 |
| `3` | 不支持的格式 |
| `4` | 配置错误 |

---

**版本**: v0.0.2 | **最后更新**: 2026-03-23 | **下载**: [markitdown4j.jar](https://github.com/DuanYan007/markitdown/releases/download/v0.0.2/markitdown4j.jar)
