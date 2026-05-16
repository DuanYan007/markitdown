# 使用

## CLI

当前仓库交付的 CLI 产物为 `target/markitdown4j-<version>.jar`。本文中的 `markitdown4j` 是 `java -jar target/markitdown4j-<version>.jar` 的文档简写。如果你没有创建自己的 shell 别名或包装脚本，请直接使用 `java -jar ...`。

基本形式：

```bash
markitdown4j [options] <input...>
```

### 常见命令

转换单个文件：

```bash
markitdown4j report.pdf -o output.md
```

把目录中的支持文件批量转换到输出目录：

```bash
markitdown4j ./docs --batch -o out/
```

递归处理多级目录：

```bash
markitdown4j ./docs --batch --recursive -o out/
```

转换远程文件：

```bash
markitdown4j https://example.com/file.pdf -o out/
```

从标准输入读取文本：

```bash
Get-Content -Raw input.txt | markitdown4j
```

从标准输入读取 JSON 并显式指定 MIME：

```bash
'{"hello":"world"}' | markitdown4j --mime-type application/json
```

启用 OCR：

```bash
markitdown4j scan.pdf --ocr --ocr-engine tesseract-cli -o output.md
```

输出帮助示例：

```bash
markitdown4j --examples
```

列出支持格式：

```bash
markitdown4j --list-formats
```

## 输出规则

当前版本的输出路径规则如下：

- 单文件且 `-o` 指向文件：直接写入该文件
- 单文件且 `-o` 指向目录：输出为 `<source>.md`
- 单文件未指定 `-o`：默认输出到源文件旁边
- 批量目录转换：按源文件名生成 `<source>.md`
- 远程输入未指定输出文件：默认写入当前工作目录

如果路径末尾显式带分隔符，即使目录尚不存在，也按目录处理。

## 配置命令

生成配置模板：

```bash
markitdown4j --generate-config
```

校验配置：

```bash
markitdown4j --validate-config
```

查看当前有效配置：

```bash
markitdown4j --show-config
```

显式加载一份配置：

```bash
markitdown4j --show-config --config-path verification/manual/markitdown.release.yml
```

在 YAML 基础上叠加 CLI 覆盖：

```bash
markitdown4j --show-config --config-path verification/manual/markitdown.release.yml --ocr --ocr-engine http
```

## OCR

### `tesseract-cli`

适用场景：

- 本地 OCR
- 跨平台部署
- 不希望依赖 JVM 内嵌 OCR 绑定

命令示例：

```bash
markitdown4j test/with-text.png --ocr --ocr-engine tesseract-cli -o out/ocr/
```

如果通过配置文件使用：

```yaml
ocr:
  enabled: true
  engine: tesseract-cli
  language: eng
  tesseractPath: O:/tesserOCR/tesseract.exe
  tessdataPath: O:/tesserOCR/tessdata
```

### `http`

适用场景：

- OCR 服务由外部 HTTP 接口统一提供
- CLI 运行机器不直接安装 OCR 引擎

### `paddleocr`

适用场景：

- 接入 PaddleOCR 任务服务
- 需要远程 OCR 管理能力

## Java Library

### 主要 API 类型

| 类型 | 作用 |
| --- | --- |
| `com.markitdown.core.MarkItDownEngine` | 主转换运行时入口 |
| `com.markitdown.config.ConversionOptions` | 类型化转换选项 |
| `com.markitdown.api.ConversionResult` | 转换结果对象 |
| `com.markitdown.api.DocumentConverter` | 转换器扩展接口 |
| `com.markitdown.exceptions.ConversionException` | 转换失败异常 |
| `com.markitdown.MarkItDownApplication` | 创建默认 engine 的便捷入口 |

### 创建 engine

```java
MarkItDownEngine engine = MarkItDownApplication.createEngine();
```

通常建议：

- 长流程中复用一个 engine
- 在生命周期结束时调用 `shutdown()`

### 基于文件的转换

```java
MarkItDownEngine engine = MarkItDownApplication.createEngine();
try {
    ConversionOptions options = ConversionOptions.builder().build();
    ConversionResult result = engine.convert(Path.of("report.pdf"), options);

    if (result.isSuccessful()) {
        System.out.println(result.getMarkdown());
    }
} finally {
    engine.shutdown();
}
```

### 基于流的转换

```java
try (InputStream input = /* your stream */) {
    ConversionResult result = engine.convert(input, "text/plain", options);
}
```

注意事项：

- 流式转换时应尽量提供正确 MIME type
- 并不是所有转换器都支持流输入

### 构建转换选项

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

### 读取结果

常见读取方法：

- `getMarkdown()`
- `getMetadata()`
- `getWarnings()`
- `isSuccessful()`

如果你只关心最终文本内容，最常用的是：

- `isSuccessful()`
- `getMarkdown()`

如果你需要排查转换路径，则还应读取：

- `getWarnings()`
- `getMetadata()`

### 失败处理

常见失败类型包括：

- 文件缺失
- MIME 不支持
- 转换器不支持流输入
- OCR 引擎不可用
- OCR 执行失败
- 文件大小超过限制

CLI 中这类问题会转为用户可读的错误信息。Library 集成中则应结合返回值和异常进行处理。

### 异步与批量 API

当前版本提供：

- `convertAsync(Path, ConversionOptions)`
- `convertAsync(InputStream, String, ConversionOptions)`
- `convertParallel(List<Path>, ConversionOptions)`
- `convertAll(List<Path>, ConversionOptions)`

适用场景：

- 批量文件队列
- 后台任务调度
- 需要并行转换的 Java 服务

### 支持性检查

```java
boolean supportedPath = engine.isSupported(Path.of("input.pdf"));
boolean supportedMime = engine.isSupported("application/pdf");
```

适合在正式转换前做预校验。

### 生命周期

Engine 持有运行资源。无论转换是否成功，完成后都应调用：

```java
engine.shutdown();
```
