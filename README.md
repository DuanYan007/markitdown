# markitdown4j

A Java CLI and Java library for converting documents into Markdown.

[中文](#中文) | [English](#english)

---

## 中文

### 简介

`markitdown4j` 是一个基于 Java 实现的文档转 Markdown 工具，既可以作为命令行工具使用，也可以作为 Java Library 集成到 JVM 应用中。

项目的目标是把常见文档、网页、图片和归档内容转换为更适合知识库、检索、RAG 预处理和长期归档的 Markdown 文本。

它参考了 Microsoft 开源项目 `markitdown` 的方向，但这是一个面向 Java 生态的非官方实现，不是 Microsoft 官方项目。

### 项目定位

- `CLI 工具`：适合本地批量转换、脚本调用、流水线处理。
- `Java Library`：适合嵌入 Java 服务、数据处理任务和文档平台。
- `配置驱动`：当前项目以 `YAML` 配置文件为中心，支持 `markitdown.yml`、`markitdown.local.yml` 和 `--config-path`。

### 核心特性

- 将多种输入格式转换为 Markdown。
- 支持单文件、多文件、通配符和目录批量处理。
- 支持递归扫描目录中的可转换文件。
- 支持从标准输入读取内容，并在需要时通过 `--mime-type` 指定 MIME 类型。
- 支持 OCR 相关配置，可在同一套配置模型下切换 `tess4j`、`tesseract-cli`、`paddleocr`、`http` 后端。
- 提供 Java API，支持同步、异步和批量并行转换。
- 基于 Maven 构建，适合 Java 11+ 环境。

### 适合谁使用

- 需要把办公文档转成 Markdown 的个人或团队。
- 需要做知识库入库、RAG 数据清洗、全文索引预处理的开发者。
- 需要在 Java 服务中集成文档转换能力的 JVM 团队。
- 需要配置化 OCR 能力，而不希望维护多套后端接入逻辑的项目。

### 支持格式

说明：

- `Supported`：仓库中已有明确转换实现，且已有较直接的代码或测试依据。
- `Experimental`：仓库中已有转换器实现，但验证样例或稳定性证据相对较少。
- `Planned`：从类型识别或规划方向看适合支持，但当前没有稳定转换器。

| 格式 | 扩展名 | 状态 | 说明 |
| ---- | ------ | ---- | ---- |
| PDF | `.pdf` | Supported | 已注册 `PdfConverter`，有 PDF 相关测试。 |
| Word (DOCX) | `.docx` | Supported | 已注册 `DocxConverter`，有单元测试。 |
| Excel (XLSX) | `.xlsx` | Supported | 已注册 `XlsxConverter`，有单元测试。 |
| HTML | `.html`, `.htm` | Supported | 已注册 `HtmlConverter`。 |
| 文本与结构化文本 | `.txt`, `.md`, `.markdown`, `.csv`, `.json`, `.xml` | Supported | 由 `TextConverter` 处理，包含流式转换测试。 |
| 图片 | `.png`, `.jpg`, `.jpeg`, `.gif`, `.bmp`, `.tiff`, `.tif` | Supported | 已注册 `ImageConverter`；可结合 OCR 配置使用。 |
| ZIP 归档 | `.zip` | Supported | 已注册 `ZipConverter`，支持对归档内可识别文件做嵌套转换。 |
| Word (DOC) | `.doc` | Experimental | 已注册 `DocConverter`，但当前验证样例少于 DOCX。 |
| PowerPoint (PPTX) | `.pptx` | Experimental | 已注册 `PptxConverter`，当前测试覆盖有限。 |
| PowerPoint (PPT) | `.ppt` | Experimental | 已注册 `PptConverter`，当前测试覆盖有限。 |
| Excel (XLS) | `.xls` | Experimental | 已注册 `XlsConverter`，当前测试覆盖有限。 |
| 音频 | `.mp3`, `.mp2`, `.wav`, `.ogg`, `.flac`, `.m4a`, `.aac`, `.wma`, `.opus`, `.aiff`, `.au` | Experimental | 已注册 `AudioConverter`；当前更适合作为元数据/转写流程能力看待。 |
| RAR / 7z | `.rar`, `.7z` | Planned | 类型映射存在，但当前默认注册表中没有对应转换器。 |
| EPUB / MOBI | `.epub`, `.mobi` | Planned | 类型映射存在，但当前默认注册表中没有对应转换器。 |

### 构建要求

- Java: `11`
- 构建工具: `Maven`
- 打包方式: `jar`
- 当前仓库未包含 `mvnw`，请使用本地安装的 `mvn`

`pom.xml` 当前坐标：

```xml
<groupId>com.markitdown</groupId>
<artifactId>markitdown4j</artifactId>
<version>0.0.3</version>
```

### 安装

#### 从源码构建

```bash
git clone https://github.com/DuanYan007/markitdown.git
cd markitdown
mvn clean package
```

默认构建产物会输出为：

```bash
target/markitdown4j-0.0.3-lite.jar
```

#### 构建其他打包 Profile

```bash
mvn -Pfull clean package
mvn -Pwin64 clean package
mvn -Plinux64 clean package
mvn -Pmac clean package
```

当前仓库中的主要打包 profile：

| Profile | 说明 |
| ------- | ---- |
| `lite` | 默认 profile，轻量包。 |
| `full` | 包含 `tess4j` 依赖。 |
| `win32` | 32 位 Windows 定向打包。 |
| `win64` | 64 位 Windows 定向打包。 |
| `linux64` | Linux 定向打包。 |
| `mac` | macOS 定向打包。 |

#### 运行 Jar

```bash
java -jar target/markitdown4j-0.0.3-lite.jar input.pdf -o output.md
```

#### 可选：配置简化命令

项目当前的实际分发形式是可执行 Jar；虽然 CLI 内部命令名是 `markitdown`，但仓库当前并没有提供默认安装到系统路径的独立命令。

如果你希望本地使用简化命令，可以自行配置别名，例如：

```bash
alias md4j='java -jar /path/to/markitdown4j-0.0.3-lite.jar'
```

之后可以这样调用：

```bash
md4j input.pdf -o output.md
```

### 快速开始

#### 单文件转换到指定文件

```bash
java -jar target/markitdown4j-0.0.3-lite.jar document.pdf -o output.md
```

#### 单文件直接输出到标准输出

当只传入一个输入文件且未指定 `-o/--output` 时，CLI 会把 Markdown 输出到标准输出：

```bash
java -jar target/markitdown4j-0.0.3-lite.jar document.docx
```

#### 批量转换多个文件

```bash
java -jar target/markitdown4j-0.0.3-lite.jar a.pdf b.docx c.xlsx -o out/
```

#### 并行处理多个文件

```bash
java -jar target/markitdown4j-0.0.3-lite.jar *.pdf --parallel --threads 4 -o out/
```

#### 处理目录中的所有可支持文件

```bash
java -jar target/markitdown4j-0.0.3-lite.jar ./docs --batch -o out/
```

#### 递归处理目录

```bash
java -jar target/markitdown4j-0.0.3-lite.jar ./docs --recursive -o out/
```

#### 从标准输入读取

如果输入来自管道，结果会输出到标准输出。必要时可用 `--mime-type` 显式指定类型：

```bash
curl -s https://example.com/sample.pdf | java -jar target/markitdown4j-0.0.3-lite.jar --mime-type application/pdf
```

### CLI 用法

#### 基本语法

```bash
java -jar target/markitdown4j-0.0.3-lite.jar [options] <input...>
```

说明：

- `<input...>` 支持 0..N 个输入。
- 当使用标准输入时，可以不传文件参数。
- 传入目录时，需要配合 `--batch` 或 `--recursive`。

#### 输入与输出参数

| 参数 | 说明 | 示例 |
| ---- | ---- | ---- |
| `<input...>` | 输入文件列表，也可为通配符或目录 | `docs/a.pdf docs/b.docx` |
| `-o`, `--output` | 输出文件或输出目录 | `-o out/result.md` |
| `-f`, `--format` | 输出格式选项：`markdown`、`plain`、`json` | `--format markdown` |
| `-m`, `--mime-type` | 管道输入时指定 MIME 类型 | `--mime-type application/pdf` |

#### 内容控制参数

| 参数 | 说明 | 示例 |
| ---- | ---- | ---- |
| `--include-images` | 显式启用图片输出 | `--include-images` |
| `--no-images` | 禁用图片输出 | `--no-images` |
| `--include-tables` | 显式启用表格输出 | `--include-tables` |
| `--no-tables` | 禁用表格输出 | `--no-tables` |
| `--include-metadata` | 显式启用元数据输出 | `--include-metadata` |
| `--no-metadata` | 禁用元数据输出 | `--no-metadata` |
| `--table-format` | 表格格式：`github`、`markdown`、`pipe` | `--table-format github` |
| `--image-format` | 图片格式：`markdown`、`html`、`base64` | `--image-format markdown` |
| `--image-output-dir` | 提取图片的相对输出目录 | `--image-output-dir assets` |

#### OCR 参数

| 参数 | 说明 | 示例 |
| ---- | ---- | ---- |
| `--ocr` | 启用 OCR | `--ocr` |
| `-l`, `--language` | OCR 语言，默认 `auto` | `--language eng+chi_sim` |
| `--ocr-engine` | OCR 引擎：`tess4j`、`tesseract-cli`、`paddleocr` | `--ocr-engine paddleocr` |
| `--ocr-endpoint` | 远程 OCR 服务地址 | `--ocr-endpoint https://...` |
| `--ocr-api-key` | 远程 OCR API Key / Token | `--ocr-api-key YOUR_TOKEN` |
| `--ocr-model` | 远程 OCR 模型名 | `--ocr-model PaddleOCR-VL-1.5` |
| `--ocr-timeout` | OCR 超时时间，毫秒 | `--ocr-timeout 30000` |
| `--ocr-poll-interval` | 异步 OCR 轮询间隔，毫秒 | `--ocr-poll-interval 5000` |

#### 文件与性能参数

| 参数 | 说明 | 示例 |
| ---- | ---- | ---- |
| `--max-file-size` | 最大文件大小，字节；`0` 表示不限制 | `--max-file-size 104857600` |
| `--pdf-password` | 加密 PDF 的密码 | `--pdf-password secret` |
| `--large-file` | 允许处理大文件 | `--large-file` |
| `--temp-dir` | 临时目录 | `--temp-dir ./tmp` |
| `-p`, `--parallel` | 多文件并行处理 | `--parallel` |
| `--threads` | 并行线程数，`0` 表示使用默认值 | `--threads 4` |
| `--progress` | 显示进度信息 | `--progress` |
| `--stats` | 显示性能统计 | `--stats` |
| `--memory-limit` | 批处理内存限制，单位 MB | `--memory-limit 1024` |
| `--optimize-memory` | 启用内存优化模式 | `--optimize-memory` |

#### 交互与配置参数

| 参数 | 说明 | 示例 |
| ---- | ---- | ---- |
| `-v`, `--verbose` | 输出更详细日志 | `--verbose` |
| `-q`, `--quiet` | 仅输出错误 | `--quiet` |
| `--examples` | 打印示例并退出 | `--examples` |
| `-i`, `--interactive` | 交互式详细反馈 | `--interactive` |
| `--generate-config` | 生成默认配置文件 | `--generate-config` |
| `--config-path` | 指定配置文件路径 | `--config-path ./config/markitdown.yml` |
| `--validate-config` | 校验配置文件 | `--validate-config` |
| `--show-config` | 显示当前生效配置及来源 | `--show-config` |
| `-r`, `--recursive` | 递归处理目录 | `--recursive` |
| `--batch` | 批量处理目录中所有支持文件 | `--batch` |

### 配置文件

当前项目只保留 `YAML` 配置方式。

支持的配置入口：

- `markitdown.yml`
- `markitdown.local.yml`
- `--config-path /path/to/your.yml`

可用命令：

```bash
java -jar target/markitdown4j-0.0.3-lite.jar --generate-config
java -jar target/markitdown4j-0.0.3-lite.jar --show-config
java -jar target/markitdown4j-0.0.3-lite.jar --validate-config
```

示例配置文件可参考 [markitdown.example.yml](/O:/markitdown/markitdown.example.yml)。

#### 一个最小可用示例

```yaml
app:
  profile: default

ocr:
  enabled: false
  engine: paddleocr
  endpoint: ""
  api_key: ""
  model: ""
  timeout: 30000
  poll_interval: 5000
  language: auto

content:
  include_metadata: true
  include_images: true
  include_tables: true
  page_break_mode: heading

output:
  dir: ./output
  image_dir: assets
  preserve_structure: false
  organize_by_type: false

format:
  image: markdown
  table: github

performance:
  parallel: false
  threads: 0
  optimize_memory: false
  max_file_size: 52428800
  batch_size: 20
```

#### 配置优先级

1. CLI 参数
2. `markitdown.local.yml`
3. `markitdown.yml`
4. 内置默认值

### OCR 后端配置

#### `tess4j`

适合希望在 Java 进程内直接调用 Tesseract 的场景。配置中可提供 `tesseract.path` 和 `tessdata.path`。

```yaml
ocr:
  enabled: true
  engine: tess4j
  language: eng+chi_sim

tesseract:
  path: C:/Program Files/Tesseract-OCR

tessdata:
  path: C:/Program Files/Tesseract-OCR/tessdata
```

说明：

- 当 `tessdata.path` 存在时，优先使用它。
- 当只配置 `tesseract.path` 时，运行时会尝试从该安装目录解析相关资源。
- `tess4j` 依赖仅在 `full`、`win32`、`win64` profile 中打包得更完整；在其他 profile 下请按你的部署环境自行验证。

#### `tesseract-cli`

适合系统已经安装 `tesseract` 命令的场景，尤其是 Linux 和 macOS。

```yaml
ocr:
  enabled: true
  engine: tesseract-cli
  language: eng+chi_sim

tesseract:
  path: /usr/bin/tesseract
```

说明：

- `tesseract.path` 可以直接指向可执行文件。
- 如果未配置，运行时会尝试使用系统路径中的 `tesseract`。

#### `paddleocr`

适合通过远程 OCR 服务处理图片或扫描件。当前代码中默认端点和默认模型如下：

- 默认 endpoint: `https://paddleocr.aistudio-app.com/api/v2/ocr/jobs`
- 默认 model: `PaddleOCR-VL-1.5`

```yaml
ocr:
  enabled: true
  engine: paddleocr
  endpoint: https://paddleocr.aistudio-app.com/api/v2/ocr/jobs
  api_key: YOUR_TOKEN
  model: PaddleOCR-VL-1.5
  timeout: 30000
  poll_interval: 5000
  language: auto
```

说明：

- `api_key` 是必需项，否则后端不可用。
- `endpoint` 和 `model` 都可以留空以使用代码内默认值。
- `poll_interval` 用于异步任务轮询。

### Java API 使用

`markitdown4j` 可以作为 Java Library 使用。当前真实可见的核心 API 包括：

- `com.markitdown.core.MarkItDownEngine`
- `com.markitdown.config.ConversionOptions`
- `com.markitdown.api.ConversionResult`

由于项目仍处于 `0.x` 版本，公共 API 可以使用，但建议把它视为持续演进中的接口。

#### 本地安装到 Maven 仓库

如果你要在其他本地项目中引用，先执行：

```bash
mvn clean install
```

#### Maven 依赖

当前可以依据项目坐标这样引用：

```xml
<dependency>
    <groupId>com.markitdown</groupId>
    <artifactId>markitdown4j</artifactId>
    <version>0.0.3</version>
</dependency>
```

说明：

```text
Currently, markitdown4j is not confirmed as published on Maven Central.
Build and install it locally first if you want to use it as a dependency.
```

#### Java 示例

```java
import com.markitdown.api.ConversionResult;
import com.markitdown.config.ConversionOptions;
import com.markitdown.core.MarkItDownEngine;

import java.nio.file.Path;

public class Example {
    public static void main(String[] args) throws Exception {
        MarkItDownEngine engine = new MarkItDownEngine();

        ConversionOptions options = ConversionOptions.builder()
                .includeImages(true)
                .includeTables(true)
                .includeMetadata(true)
                .useOcr(false)
                .build();

        ConversionResult result = engine.convert(Path.of("document.pdf"), options);

        if (result.isSuccessful()) {
            System.out.println(result.getMarkdown());
        } else {
            System.err.println(result.getWarnings());
        }

        engine.shutdown();
    }
}
```

#### 流式输入示例

```java
import com.markitdown.api.ConversionResult;
import com.markitdown.core.MarkItDownEngine;

import java.io.InputStream;

public class StreamExample {
    public static void main(String[] args) throws Exception {
        MarkItDownEngine engine = new MarkItDownEngine();
        try (InputStream in = StreamExample.class.getResourceAsStream("/sample.pdf")) {
            ConversionResult result = engine.convert(in, "application/pdf");
            System.out.println(result.getMarkdown());
        }
        engine.shutdown();
    }
}
```

### 使用场景

- 把 PDF、Office 文档和网页转换为 Markdown。
- 为知识库或文档中心准备统一文本格式。
- 为 RAG / LLM 应用准备可切分、可索引的数据源。
- 为内部搜索、审计和归档流程准备结构化文本。
- 在 Java 后端、批处理任务或 ETL 流程中嵌入转换能力。

### 与 Microsoft markitdown 的关系

- Microsoft `markitdown` 是本项目的重要灵感来源和参考方向。
- `markitdown4j` 是面向 Java 生态的非官方实现。
- 本项目不是 Microsoft 官方项目，也不由 Microsoft 维护、发布或背书。
- 功能范围、CLI 体验、公共 API 和转换结果都不承诺与原项目完全一致。

### 项目成熟度

从当前仓库结构看，`markitdown4j` 已经具备可运行的 CLI、可复用的 Java API、YAML 配置体系和一批格式转换实现，已经不是纯原型。

但项目仍处于 `0.0.3` 阶段，部分格式和 OCR 组合路径还需要更多验证样例与发布经验，因此更合适的描述是：

- 已可用于实际试用和集成验证
- 核心能力已成形
- 部分输入格式仍在继续补足稳定性和测试覆盖

### Roadmap

- 发布到 Maven Central。
- 提供更方便的 CLI 安装方式，而不仅是 `java -jar`。
- 增加更多稳定格式支持，例如更完整的归档和电子书格式。
- 改进图片、扫描件和复杂版式文档的 Markdown 输出质量。
- 继续强化 OCR 后端接入与验证矩阵。
- 扩展更多集成测试和真实样例集。
- 探索更清晰的插件化转换器机制。
- 增加 Docker 交付方式。

### 本地开发

#### 克隆与构建

```bash
git clone https://github.com/DuanYan007/markitdown.git
cd markitdown
mvn clean package
```

#### 运行测试

```bash
mvn clean test
```

#### 本地安装

```bash
mvn clean install
```

#### 代码与测试位置

- 主源码目录：[markitdown-java/src/main/java](/O:/markitdown/markitdown-java/src/main/java)
- 测试源码目录：[src/test/java](/O:/markitdown/src/test/java)
- 示例配置文件：[markitdown.example.yml](/O:/markitdown/markitdown.example.yml)
- 测试样本包：[test/test.zip](/O:/markitdown/test/test.zip)

### 贡献指南

欢迎通过以下方式参与：

- 提交 Issue 报告 bug 或转换结果问题
- 提交 Pull Request 改进转换器、配置系统或文档
- 补充新的测试样例和回归样本
- 改进 README、使用说明和配置示例
- 为特定文档格式补充更完整的测试覆盖

如果你计划增加新的格式支持，建议同时补充：

- 输入样本
- 期望输出
- 单元测试或集成测试

### FAQ

#### 1. `markitdown4j` 和 Microsoft `markitdown` 是什么关系？

`markitdown4j` 参考了 Microsoft `markitdown` 的方向，但它是 Java 生态下的独立、非官方实现。

#### 2. 这是 Microsoft 官方项目吗？

不是。本项目不是 Microsoft 官方项目，也不是 Microsoft 维护项目。

#### 3. 它是否完全兼容 Microsoft `markitdown`？

不保证。CLI、API、配置模型和输出结果都可能不同。

#### 4. 现在支持哪些文档格式？

当前稳定支持以 PDF、DOCX、XLSX、HTML、文本类格式、常见图片和 ZIP 为主。DOC、PPT、PPTX、XLS、音频等目前更适合视为实验性支持。

#### 5. 可以当作 Java Library 使用吗？

可以。核心入口类是 `MarkItDownEngine`，并可通过 `ConversionOptions` 配置行为。

#### 6. 已经发布到 Maven Central 了吗？

当前仓库内容无法确认这一点。更稳妥的用法是先在本地执行 `mvn clean install`。

#### 7. 为什么转换后的 Markdown 和原文档排版不完全一致？

Markdown 不是面向像素级还原的格式。复杂排版、分页、浮动对象、图表、扫描件和 Office 特性在转换后通常需要一定取舍。

#### 8. 为什么目录输入没有被直接处理？

当前 CLI 需要你显式指定 `--batch` 或 `--recursive` 来处理目录，这样可以避免误扫大量文件。

### License

本项目采用 `MIT License`。详见 [LICENSE](/O:/markitdown/LICENSE)。

### 致谢

感谢以下开源项目为当前实现提供基础能力：

- Microsoft `markitdown`，为本项目提供了重要灵感来源
- [Apache POI](https://poi.apache.org/)
- [Apache PDFBox](https://pdfbox.apache.org/)
- [Apache Tika](https://tika.apache.org/)
- [Jsoup](https://jsoup.org/)
- [Jackson](https://github.com/FasterXML/jackson)
- [Picocli](https://picocli.info/)
- [Tess4J](https://tess4j.sourceforge.net/)

---

## English

### Introduction

`markitdown4j` is a Java implementation of a document-to-Markdown tool. It can be used both as a CLI application and as a Java library inside JVM-based systems.

The project focuses on turning common documents, web pages, images, and archive contents into Markdown for knowledge bases, indexing pipelines, RAG preprocessing, and long-term text-friendly storage.

It is inspired by Microsoft `markitdown`, but it is an unofficial Java ecosystem implementation and is not a Microsoft project.

### Project Positioning

- `CLI tool` for local conversion, scripting, and batch processing
- `Java library` for embedding into Java services and data pipelines
- `YAML-first configuration` using `markitdown.yml`, `markitdown.local.yml`, and `--config-path`

### Features

- Convert multiple input formats into Markdown
- Process single files, multiple files, wildcard inputs, and directories
- Recursively scan supported files in directories
- Read from standard input, with optional `--mime-type` for piped content
- Switch OCR backends under one unified configuration model
- Provide synchronous, asynchronous, and parallel Java APIs
- Build with Maven and run on Java 11+

### Supported Formats

Status guide:

- `Supported`: implemented and backed by clearer code or test evidence
- `Experimental`: implemented, but with lighter validation coverage
- `Planned`: a reasonable future target, but not a stable built-in converter today

| Format | Extensions | Status | Notes |
| ------ | ---------- | ------ | ----- |
| PDF | `.pdf` | Supported | `PdfConverter` is registered and PDF-related tests exist. |
| Word (DOCX) | `.docx` | Supported | `DocxConverter` is registered and tested. |
| Excel (XLSX) | `.xlsx` | Supported | `XlsxConverter` is registered and tested. |
| HTML | `.html`, `.htm` | Supported | `HtmlConverter` is registered. |
| Plain and structured text | `.txt`, `.md`, `.markdown`, `.csv`, `.json`, `.xml` | Supported | Handled by `TextConverter`, with streaming-related tests. |
| Images | `.png`, `.jpg`, `.jpeg`, `.gif`, `.bmp`, `.tiff`, `.tif` | Supported | `ImageConverter` is registered; OCR can be enabled when needed. |
| ZIP archives | `.zip` | Supported | `ZipConverter` can convert supported nested entries. |
| Word (DOC) | `.doc` | Experimental | Implemented, with less validation coverage than DOCX. |
| PowerPoint (PPTX) | `.pptx` | Experimental | Implemented, but still lightly validated. |
| PowerPoint (PPT) | `.ppt` | Experimental | Implemented, but still lightly validated. |
| Excel (XLS) | `.xls` | Experimental | Implemented, but still lightly validated. |
| Audio | `.mp3`, `.mp2`, `.wav`, `.ogg`, `.flac`, `.m4a`, `.aac`, `.wma`, `.opus`, `.aiff`, `.au` | Experimental | `AudioConverter` exists; today it is better viewed as metadata/transcription workflow support. |
| RAR / 7z | `.rar`, `.7z` | Planned | Type mappings exist, but no stable default converter is registered. |
| EPUB / MOBI | `.epub`, `.mobi` | Planned | Type mappings exist, but no stable default converter is registered. |

### Build Requirements

- Java: `11`
- Build tool: `Maven`
- Packaging: `jar`
- No Maven Wrapper (`mvnw`) is currently included in the repository

Current Maven coordinates:

```xml
<groupId>com.markitdown</groupId>
<artifactId>markitdown4j</artifactId>
<version>0.0.3</version>
```

### Installation

#### Build from source

```bash
git clone https://github.com/DuanYan007/markitdown.git
cd markitdown
mvn clean package
```

Default output:

```bash
target/markitdown4j-0.0.3-lite.jar
```

#### Build other packaging profiles

```bash
mvn -Pfull clean package
mvn -Pwin64 clean package
mvn -Plinux64 clean package
mvn -Pmac clean package
```

Main packaging profiles:

| Profile | Description |
| ------- | ----------- |
| `lite` | Default lightweight package |
| `full` | Package with `tess4j` dependency |
| `win32` | Windows 32-bit oriented package |
| `win64` | Windows 64-bit oriented package |
| `linux64` | Linux oriented package |
| `mac` | macOS oriented package |

#### Run the jar

```bash
java -jar target/markitdown4j-0.0.3-lite.jar input.pdf -o output.md
```

#### Optional: add a shorter local command

The internal Picocli command name is `markitdown`, but the current distribution model is still a runnable jar. The repository does not currently install a standalone executable into your system path by default.

If you want a shorter local command, you can add your own alias:

```bash
alias md4j='java -jar /path/to/markitdown4j-0.0.3-lite.jar'
```

Then use:

```bash
md4j input.pdf -o output.md
```

### Quick Start

#### Convert one file to a Markdown file

```bash
java -jar target/markitdown4j-0.0.3-lite.jar document.pdf -o output.md
```

#### Print one converted file to stdout

If you pass exactly one file and do not set `-o/--output`, the CLI prints Markdown to standard output:

```bash
java -jar target/markitdown4j-0.0.3-lite.jar document.docx
```

#### Convert multiple files

```bash
java -jar target/markitdown4j-0.0.3-lite.jar a.pdf b.docx c.xlsx -o out/
```

#### Convert files in parallel

```bash
java -jar target/markitdown4j-0.0.3-lite.jar *.pdf --parallel --threads 4 -o out/
```

#### Process all supported files in a directory

```bash
java -jar target/markitdown4j-0.0.3-lite.jar ./docs --batch -o out/
```

#### Recursively process a directory

```bash
java -jar target/markitdown4j-0.0.3-lite.jar ./docs --recursive -o out/
```

#### Read from stdin

Pipe input is written to stdout. Use `--mime-type` when MIME detection is not obvious:

```bash
curl -s https://example.com/sample.pdf | java -jar target/markitdown4j-0.0.3-lite.jar --mime-type application/pdf
```

### CLI Usage

#### Syntax

```bash
java -jar target/markitdown4j-0.0.3-lite.jar [options] <input...>
```

Notes:

- `<input...>` accepts zero to many inputs
- file arguments are optional when reading from standard input
- directory processing requires `--batch` or `--recursive`

#### Input and output options

| Option | Description | Example |
| ------ | ----------- | ------- |
| `<input...>` | Input files, wildcard patterns, or directories | `docs/a.pdf docs/b.docx` |
| `-o`, `--output` | Output file or output directory | `-o out/result.md` |
| `-f`, `--format` | Output format option: `markdown`, `plain`, `json` | `--format markdown` |
| `-m`, `--mime-type` | MIME type for piped input | `--mime-type application/pdf` |

#### Content options

| Option | Description | Example |
| ------ | ----------- | ------- |
| `--include-images` | Explicitly enable image output | `--include-images` |
| `--no-images` | Disable image output | `--no-images` |
| `--include-tables` | Explicitly enable table output | `--include-tables` |
| `--no-tables` | Disable table output | `--no-tables` |
| `--include-metadata` | Explicitly enable metadata output | `--include-metadata` |
| `--no-metadata` | Disable metadata output | `--no-metadata` |
| `--table-format` | Table format: `github`, `markdown`, `pipe` | `--table-format github` |
| `--image-format` | Image format: `markdown`, `html`, `base64` | `--image-format markdown` |
| `--image-output-dir` | Relative output directory for extracted images | `--image-output-dir assets` |

#### OCR options

| Option | Description | Example |
| ------ | ----------- | ------- |
| `--ocr` | Enable OCR | `--ocr` |
| `-l`, `--language` | OCR language, default `auto` | `--language eng+chi_sim` |
| `--ocr-engine` | OCR engine: `tess4j`, `tesseract-cli`, `paddleocr` | `--ocr-engine paddleocr` |
| `--ocr-endpoint` | Remote OCR endpoint | `--ocr-endpoint https://...` |
| `--ocr-api-key` | Remote OCR API key or token | `--ocr-api-key YOUR_TOKEN` |
| `--ocr-model` | Remote OCR model name | `--ocr-model PaddleOCR-VL-1.5` |
| `--ocr-timeout` | OCR timeout in milliseconds | `--ocr-timeout 30000` |
| `--ocr-poll-interval` | OCR polling interval in milliseconds | `--ocr-poll-interval 5000` |

#### File and performance options

| Option | Description | Example |
| ------ | ----------- | ------- |
| `--max-file-size` | Maximum file size in bytes; `0` means unlimited | `--max-file-size 104857600` |
| `--pdf-password` | Password for encrypted PDFs | `--pdf-password secret` |
| `--large-file` | Allow large file processing | `--large-file` |
| `--temp-dir` | Temporary directory | `--temp-dir ./tmp` |
| `-p`, `--parallel` | Process multiple files in parallel | `--parallel` |
| `--threads` | Number of worker threads; `0` keeps the default | `--threads 4` |
| `--progress` | Show progress information | `--progress` |
| `--stats` | Show performance statistics | `--stats` |
| `--memory-limit` | Memory limit for batch processing, in MB | `--memory-limit 1024` |
| `--optimize-memory` | Enable memory optimization mode | `--optimize-memory` |

#### Interaction and configuration options

| Option | Description | Example |
| ------ | ----------- | ------- |
| `-v`, `--verbose` | Enable verbose output | `--verbose` |
| `-q`, `--quiet` | Suppress non-error output | `--quiet` |
| `--examples` | Print usage examples and exit | `--examples` |
| `-i`, `--interactive` | Enable interactive detailed feedback | `--interactive` |
| `--generate-config` | Generate a default config file | `--generate-config` |
| `--config-path` | Use an explicit config file path | `--config-path ./config/markitdown.yml` |
| `--validate-config` | Validate a config file | `--validate-config` |
| `--show-config` | Print the active effective configuration | `--show-config` |
| `-r`, `--recursive` | Recursively process directory inputs | `--recursive` |
| `--batch` | Process all supported files in a directory | `--batch` |

### Configuration

The current project keeps a YAML-only configuration model.

Supported config entry points:

- `markitdown.yml`
- `markitdown.local.yml`
- `--config-path /path/to/your.yml`

Useful commands:

```bash
java -jar target/markitdown4j-0.0.3-lite.jar --generate-config
java -jar target/markitdown4j-0.0.3-lite.jar --show-config
java -jar target/markitdown4j-0.0.3-lite.jar --validate-config
```

Example config file: [markitdown.example.yml](/O:/markitdown/markitdown.example.yml)

#### Minimal example

```yaml
app:
  profile: default

ocr:
  enabled: false
  engine: paddleocr
  endpoint: ""
  api_key: ""
  model: ""
  timeout: 30000
  poll_interval: 5000
  language: auto

content:
  include_metadata: true
  include_images: true
  include_tables: true
  page_break_mode: heading

output:
  dir: ./output
  image_dir: assets
  preserve_structure: false
  organize_by_type: false

format:
  image: markdown
  table: github

performance:
  parallel: false
  threads: 0
  optimize_memory: false
  max_file_size: 52428800
  batch_size: 20
```

#### Precedence

1. CLI arguments
2. `markitdown.local.yml`
3. `markitdown.yml`
4. built-in defaults

### OCR Backend Setup

#### `tess4j`

Use this when you want in-process Java OCR with a local Tesseract installation.

```yaml
ocr:
  enabled: true
  engine: tess4j
  language: eng+chi_sim

tesseract:
  path: C:/Program Files/Tesseract-OCR

tessdata:
  path: C:/Program Files/Tesseract-OCR/tessdata
```

Notes:

- `tessdata.path` takes precedence when present
- `tesseract.path` can be used when you only know the install directory
- `tess4j` packaging is more complete in the `full`, `win32`, and `win64` profiles

#### `tesseract-cli`

Use this when the `tesseract` executable is already installed on the system.

```yaml
ocr:
  enabled: true
  engine: tesseract-cli
  language: eng+chi_sim

tesseract:
  path: /usr/bin/tesseract
```

Notes:

- `tesseract.path` can point directly to the executable
- if omitted, the runtime tries the system `tesseract`

#### `paddleocr`

Use this for a remote OCR workflow. The current code has these defaults:

- default endpoint: `https://paddleocr.aistudio-app.com/api/v2/ocr/jobs`
- default model: `PaddleOCR-VL-1.5`

```yaml
ocr:
  enabled: true
  engine: paddleocr
  endpoint: https://paddleocr.aistudio-app.com/api/v2/ocr/jobs
  api_key: YOUR_TOKEN
  model: PaddleOCR-VL-1.5
  timeout: 30000
  poll_interval: 5000
  language: auto
```

Notes:

- `api_key` is required
- `endpoint` and `model` can be left empty to use built-in defaults
- `poll_interval` is used for async job polling

### Java API

`markitdown4j` can be used as a Java library. The currently visible core API includes:

- `com.markitdown.core.MarkItDownEngine`
- `com.markitdown.config.ConversionOptions`
- `com.markitdown.api.ConversionResult`

Because the project is still in the `0.x` range, the API is usable but should still be treated as evolving.

#### Install to local Maven repository

```bash
mvn clean install
```

#### Maven dependency

```xml
<dependency>
    <groupId>com.markitdown</groupId>
    <artifactId>markitdown4j</artifactId>
    <version>0.0.3</version>
</dependency>
```

```text
Currently, markitdown4j is not confirmed as published on Maven Central.
Build and install it locally first if you want to use it as a dependency.
```

#### Java example

```java
import com.markitdown.api.ConversionResult;
import com.markitdown.config.ConversionOptions;
import com.markitdown.core.MarkItDownEngine;

import java.nio.file.Path;

public class Example {
    public static void main(String[] args) throws Exception {
        MarkItDownEngine engine = new MarkItDownEngine();

        ConversionOptions options = ConversionOptions.builder()
                .includeImages(true)
                .includeTables(true)
                .includeMetadata(true)
                .useOcr(false)
                .build();

        ConversionResult result = engine.convert(Path.of("document.pdf"), options);

        if (result.isSuccessful()) {
            System.out.println(result.getMarkdown());
        } else {
            System.err.println(result.getWarnings());
        }

        engine.shutdown();
    }
}
```

#### Stream example

```java
import com.markitdown.api.ConversionResult;
import com.markitdown.core.MarkItDownEngine;

import java.io.InputStream;

public class StreamExample {
    public static void main(String[] args) throws Exception {
        MarkItDownEngine engine = new MarkItDownEngine();
        try (InputStream in = StreamExample.class.getResourceAsStream("/sample.pdf")) {
            ConversionResult result = engine.convert(in, "application/pdf");
            System.out.println(result.getMarkdown());
        }
        engine.shutdown();
    }
}
```

### Use Cases

- Convert office documents and PDFs into Markdown
- Prepare content for knowledge bases and internal documentation systems
- Preprocess input data for RAG and LLM pipelines
- Build searchable text indexes from mixed document sources
- Embed document conversion into Java services, jobs, and ETL flows

### Relationship with Microsoft markitdown

- Microsoft `markitdown` is an important inspiration and reference direction for this project
- `markitdown4j` is an unofficial Java ecosystem implementation
- this repository is not an official Microsoft project
- feature coverage, CLI behavior, API shape, and output quality are not guaranteed to match the original project exactly

### Project Status

Based on the current repository, `markitdown4j` already has:

- a working CLI
- reusable Java APIs
- a YAML-based configuration system
- multiple implemented converters

At the same time, the project is still at `0.0.3`, so some formats and OCR combinations still need broader validation. A fair summary today is:

- usable for real evaluation and integration work
- beyond a pure prototype
- still expanding stability and coverage for some converters

### Roadmap

- publish to Maven Central
- provide a simpler installation story than `java -jar`
- add more stable archive and ebook support
- improve Markdown output quality for scanned and layout-heavy inputs
- expand OCR validation across more deployment combinations
- add more integration tests and real-world sample sets
- explore a cleaner plugin-style converter architecture
- provide Docker-based delivery

### Development

#### Clone and build

```bash
git clone https://github.com/DuanYan007/markitdown.git
cd markitdown
mvn clean package
```

#### Run tests

```bash
mvn clean test
```

#### Install locally

```bash
mvn clean install
```

#### Key locations

- source: [markitdown-java/src/main/java](/O:/markitdown/markitdown-java/src/main/java)
- tests: [src/test/java](/O:/markitdown/src/test/java)
- example config: [markitdown.example.yml](/O:/markitdown/markitdown.example.yml)
- sample test archive: [test/test.zip](/O:/markitdown/test/test.zip)

### Contributing

Contributions are welcome through:

- Issues for bugs, regressions, or conversion quality problems
- Pull Requests for converter improvements, configuration work, and documentation
- additional test samples and regression datasets
- README and usage improvements
- stronger validation for specific formats

If you add support for a new format, it is strongly recommended to include:

- sample input files
- expected output behavior
- unit or integration tests

### FAQ

#### 1. What is the relationship between `markitdown4j` and Microsoft `markitdown`?

`markitdown4j` is inspired by Microsoft `markitdown`, but it is an independent Java-focused implementation.

#### 2. Is this an official Microsoft project?

No.

#### 3. Is it fully compatible with Microsoft `markitdown`?

No compatibility guarantee is made for CLI behavior, APIs, configuration shape, or output details.

#### 4. What formats are supported today?

The most stable set currently includes PDF, DOCX, XLSX, HTML, text-like formats, common image formats, and ZIP archives. DOC, PPT, PPTX, XLS, and audio are better treated as experimental for now.

#### 5. Can I use it as a Java library?

Yes. The main entry point is `MarkItDownEngine`, with behavior configured via `ConversionOptions`.

#### 6. Is it published on Maven Central?

That is not confirmed from the current repository state. The safe path today is `mvn clean install` first.

#### 7. Why does the generated Markdown not perfectly match the original layout?

Markdown is not a pixel-perfect preservation format. Complex layout, pagination, floating objects, scanned pages, and Office-specific formatting often require tradeoffs during conversion.

#### 8. Why does passing a directory alone not process it?

The CLI requires `--batch` or `--recursive` for directory inputs so that large scans are explicit rather than accidental.

### License

This project is licensed under the `MIT License`. See [LICENSE](/O:/markitdown/LICENSE).

### Acknowledgements

Thanks to these open source projects that support the current implementation:

- Microsoft `markitdown` for the original inspiration
- [Apache POI](https://poi.apache.org/)
- [Apache PDFBox](https://pdfbox.apache.org/)
- [Apache Tika](https://tika.apache.org/)
- [Jsoup](https://jsoup.org/)
- [Jackson](https://github.com/FasterXML/jackson)
- [Picocli](https://picocli.info/)
- [Tess4J](https://tess4j.sourceforge.net/)
