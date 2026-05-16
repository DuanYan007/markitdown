# markitdown4j

[![version](https://img.shields.io/badge/version-0.0.4-0F766E)](pom.xml)
[![java](https://img.shields.io/badge/java-11-1D4ED8)](pom.xml)
[![packaging](https://img.shields.io/badge/packaging-cli%20%2B%20library-7C3AED)](readme.md)
[![license](https://img.shields.io/badge/license-MIT-15803D)](LICENSE)
[![ocr](https://img.shields.io/badge/ocr-tesseract--cli%20%7C%20http%20%7C%20paddleocr-B45309)](readme.md)

> 一个面向文档转 Markdown 的 Java 实现版本。项目以 CLI 和 Java Library 两种形态交付，定位为 Microsoft MarkItDown 的 Java 重写版本，并用于开源重写比赛场景。

| 项目 | 说明 |
| --- | --- |
| 当前版本 | `0.0.4` |
| 运行语言 | Java 11 |
| 交付形态 | 可执行 Shaded JAR + Java Library |
| OCR 引擎 | `tesseract-cli`、`http`、`paddleocr` |
| 主产物 | `target/markitdown4j-0.0.4.jar` |
| 许可证 | MIT |

## 项目定位

当前仓库只描述和交付当前 Java 版本，项目定位包括：

- Microsoft MarkItDown 的 Java 重写实现
- 面向开源重写比赛的交付项目
- 可直接本地使用的文档转 Markdown CLI
- 可嵌入 Java 系统的转换库

## 项目能力

`markitdown4j` 将多种输入内容转换为 Markdown，并通过两种产品形态暴露同一套核心能力：

- CLI：用于本地批量转换、OCR 控制、配置查看和自动化脚本调用
- Java Library：用于在 Java 服务、工具链和业务系统内直接集成转换能力

### 转换特性

- 支持本地文件、目录、HTTP(S) URL 和标准输入流
- 支持可配置的 Markdown、表格和图片输出方式
- 支持把提取出的图片写入输出目录附近的资源目录
- 支持在图片型文本提取场景下启用 OCR
- 支持基于 YAML 的配置加载、校验和有效配置查看
- 支持单文件转换、目录批量转换和多文件顺序处理

### OCR 选项

| 引擎 | 适用场景 |
| --- | --- |
| `tesseract-cli` | 本地安装 Tesseract 后进行跨平台 OCR |
| `http` | 通过自定义 HTTP OCR 服务执行识别 |
| `paddleocr` | 通过 PaddleOCR 任务接口执行识别 |

## 支持的输入

### 主要格式

- PDF
- DOCX
- XLSX
- HTML / HTM
- TXT / Markdown
- CSV / JSON / XML
- 图片
- ZIP 压缩包

### 具有专门处理逻辑的其他格式

- DOC
- PPTX
- PPT
- XLS
- 音频

## 快速开始

当前仓库交付的 CLI 产物为：

```bash
target/markitdown4j-0.0.4.jar
```

本文中的 `markitdown4j` 只是文档写法，等价于：

```bash
java -jar target/markitdown4j-0.0.4.jar
```

如果你没有自行创建别名或包装脚本，请直接使用 `java -jar ...` 形式执行。

### 构建

```bash
mvn clean package
```

### CLI 基本形式

```bash
markitdown4j [options] <input...>
```

### 常见示例

转换单个 PDF：

```bash
markitdown4j report.pdf -o output.md
```

批量转换目录中的支持文件：

```bash
markitdown4j ./docs --batch -o out/
```

转换远程文件：

```bash
markitdown4j https://example.com/report.pdf -o out/
```

启用 OCR 转换：

```bash
markitdown4j scan.pdf --ocr --ocr-engine tesseract-cli -o output.md
```

生成并检查配置：

```bash
markitdown4j --generate-config
markitdown4j --validate-config
markitdown4j --show-config
```

## 配置模型

当前版本采用 YAML-first 配置模型。

可识别的配置文件包括：

- `markitdown.yml`
- `markitdown.local.yml`
- `markitdown.example.yml`

解析顺序为：

1. 内置默认值
2. `markitdown.yml`
3. `markitdown.local.yml`
4. CLI 覆盖项

完整配置说明、场景配置示例、校验规则和 CLI 覆盖行为请参考 [configuration.md](configuration.md)。

## Java Library 使用

Library 不是附属功能，而是当前产品形态的一部分。

典型使用方式包括：

- 在 Java 服务中处理用户上传文档
- 在 Java 批处理流程中执行文档转换
- 以结构化方式消费 Markdown、metadata 和 warnings
- 在 Java 代码中扩展或控制转换器注册与调用

### 安装

安装到本地仓库：

```bash
mvn clean install
```

Maven 坐标：

```xml
<dependency>
  <groupId>com.markitdown</groupId>
  <artifactId>markitdown4j</artifactId>
  <version>0.0.4</version>
</dependency>
```

### 主要 API 类型

| 类型 | 作用 |
| --- | --- |
| `com.markitdown.core.MarkItDownEngine` | 主转换入口 |
| `com.markitdown.config.ConversionOptions` | 运行时转换选项 |
| `com.markitdown.api.ConversionResult` | 包含 Markdown、metadata、warnings 和状态的结果对象 |
| `com.markitdown.api.DocumentConverter` | 转换器扩展契约 |
| `com.markitdown.exceptions.ConversionException` | 转换失败时的受检异常 |
| `com.markitdown.MarkItDownApplication` | 提供 `createEngine()` 的便捷入口 |

### 最小文件转换示例

```java
MarkItDownEngine engine = MarkItDownApplication.createEngine();
try {
    ConversionOptions options = ConversionOptions.builder().build();
    ConversionResult result = engine.convert(Path.of("report.pdf"), options);

    if (result.isSuccessful()) {
        System.out.println(result.getMarkdown());
    } else {
        System.err.println(result.getWarnings());
    }
} finally {
    engine.shutdown();
}
```

### 流式转换示例

```java
try (InputStream input = /* your stream */) {
    ConversionResult result = engine.convert(input, "text/plain", options);
}
```

### 选项构建示例

```java
ConversionOptions options = ConversionOptions.builder()
        .includeMetadata(true)
        .includeImages(true)
        .includeTables(true)
        .tableFormat("github")
        .imageFormat("markdown")
        .useOcr(true)
        .ocrEngine("tesseract-cli")
        .language("eng")
        .tesseractPath("O:/tesserOCR/tesseract.exe")
        .tessdataPath("O:/tesserOCR/tessdata")
        .maxFileSize(0)
        .build();
```

### 结果读取

常用结果读取接口包括：

- `getMarkdown()`
- `getMetadata()`
- `getWarnings()`
- `isSuccessful()`

### 失败处理

失败场景主要包括：

- 文件不存在或不可读
- MIME 类型无法识别或没有可用转换器
- OCR 引擎不可用或执行失败
- 输入文件超过大小限制
- 远程输入下载失败

这类错误在 CLI 中会转换为面向用户的诊断信息，在 Library 中则可通过结果对象和异常处理逻辑进行控制。

### 异步和批量接口

当前 Library 还提供以下入口：

- `convertAsync(Path, ConversionOptions)`
- `convertAsync(InputStream, String, ConversionOptions)`
- `convertParallel(List<Path>, ConversionOptions)`
- `convertAll(List<Path>, ConversionOptions)`

### 支持检查

```java
boolean supportedPath = engine.isSupported(Path.of("input.pdf"));
boolean supportedMime = engine.isSupported("application/pdf");
```

### 生命周期

Engine 持有运行时资源。转换结束后应调用 `shutdown()` 释放资源，避免长生命周期进程中出现资源残留。

## 文档索引

中文文档：

- [usage.md](usage.md)：CLI 和 Java Library 的使用方法、命令模式、输出规则、OCR 用法与集成示例
- [configuration.md](configuration.md)：配置模型、YAML 键、场景配置示例、校验规则与 CLI 覆盖行为
- [testing.md](testing.md)：当前测试项、测试内容、测试结果范围与手工功能验证命令
- [development.md](development.md)：当前版本的软件设计、目录结构、模块职责、运行流程和开发说明

English documents:

- [readme.en.md](readme.en.md): English project homepage.
- [usage.en.md](usage.en.md): CLI and Java library usage, command patterns, output rules, OCR usage, and integration examples.
- [configuration.en.md](configuration.en.md): configuration model, YAML keys, scenario-based configuration examples, validation rules, and CLI override behavior.
- [testing.en.md](testing.en.md): current test inventory, test scope, and concrete test command references.
- [development.en.md](development.en.md): implementation-oriented design and development documentation for contributors.

## 构建与运行要求

- JDK 11
- Maven 3.8+
- 如果使用 `tesseract-cli` OCR，需要系统中安装 Tesseract 可执行程序

## 当前产品形态

| 形态 | 定位 | 说明 |
| --- | --- | --- |
| CLI | 面向最终使用者 | 用于本地转换、自动化脚本和手工命令执行 |
| Java Library | 面向 Java 集成方 | 用于嵌入业务系统、工具链和后端服务 |

## License

- [LICENSE](LICENSE)
