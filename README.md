# markitdown

[中文](README.md) | [English](README.en.md)

`markitdown` 是一个面向文档转 Markdown 的仓库，目前主线交付物是 `markitdown4j` Java CLI。它可以把常见办公文档、网页、图片、压缩包和部分音频元数据转换为 Markdown，并支持通过统一配置切换不同 OCR 后端。

## 这个项目能做什么

- 将 PDF、Word、Excel、PowerPoint、HTML、图片、文本、ZIP 转换为 Markdown
- 支持 OCR 补充识别图片和扫描版 PDF
- 支持多平台制品：`lite`、`full`、`win32`、`win64`、`linux64`、`mac`
- 支持统一 OCR 配置，切换后端时不需要修改转换流程
- 支持远程 OCR Provider，例如 `paddleocr`

## 仓库结构

- [java/README.md](java/README.md)：Java CLI 主项目说明
- [java/COMMAND_REFERENCE.md](java/COMMAND_REFERENCE.md)：命令与参数参考
- [test/README.md](test/README.md)：测试数据集和验证说明
- [OCR_PROVIDER_ROADMAP.md](OCR_PROVIDER_ROADMAP.md)：OCR / VLM 扩展路线图

## 快速开始

1. 安装 Java 11 或更高版本
2. 下载适合你的发布包
3. 运行转换命令

示例：

```bash
java -jar target/markitdown4j-0.0.3-lite.jar document.pdf -o output.md
```

## 下载哪个包

- `win64`：64 位 Windows，内置 Windows OCR native
- `win32`：32 位 Windows，内置 Windows OCR native
- `linux64`：Linux，推荐配合本地或远程 OCR
- `mac`：macOS，推荐配合本地或远程 OCR
- `lite`：最小体积，不内置 `tess4j`
- `full`：完整包，包含完整 OCR 资源

## OCR 配置

项目采用统一 OCR 配置模型。用户不需要为不同 OCR 单独学一套配置，只需要修改同一组字段。

示例：

```properties
ocr.enable=true
ocr.engine=paddleocr
ocr.endpoint=https://paddleocr.aistudio-app.com/api/v2/ocr/jobs
ocr.api.key=YOUR_TOKEN
ocr.model=PaddleOCR-VL-1.5
ocr.timeout=30000
ocr.poll.interval=5000
ocr.language=auto
```

配置文件位置：

- [`.markitdown.properties`](.markitdown.properties)

统一 OCR 配置字段：

- `ocr.enable`
- `ocr.engine`
- `ocr.endpoint`
- `ocr.api.key`
- `ocr.model`
- `ocr.timeout`
- `ocr.poll.interval`
- `ocr.language`

配置优先级：

1. 命令行参数，例如 `--ocr-engine`
2. 环境变量，例如 `MARKITDOWN_OCR_ENGINE`
3. [`.markitdown.properties`](.markitdown.properties)
4. 程序内置默认值

常用环境变量：

- `MARKITDOWN_OCR_ENGINE`
- `MARKITDOWN_OCR_ENDPOINT`
- `MARKITDOWN_OCR_API_KEY`
- `MARKITDOWN_OCR_MODEL`
- `MARKITDOWN_OCR_TIMEOUT`
- `MARKITDOWN_OCR_POLL_INTERVAL`

当前对外支持的 OCR 后端：

- `tess4j`：适合 Windows 内嵌 OCR
- `tesseract-cli`：适合 Linux / macOS 本地 OCR
- `paddleocr`：适合远程结构化 OCR
- `http`：适合接自定义远程 OCR 服务

## 测试与验证

项目不是只写了功能说明，也提供了可复用的测试资产和已执行的验证路径。

### 自动化测试

执行：

```bash
mvn test
```

当前覆盖：

- Profile 构建和命名检查
- OCR engine factory 选择
- PaddleOCR 响应解析
- 文本流式转换
- ZIP 委托和嵌套转换行为

### 测试数据集

仓库中的 [test/test.zip](test/test.zip) 是正式测试文件包，当前包含约 104 个测试文件。它用于：

- 回归测试
- 兼容性验证
- release 前手工检查

解压后的 [test/README.md](test/README.md) 说明了如何使用这些文件进行验证。

### 已验证的关键链路

- `lite` 基础转换
- `win64 + tess4j`
- `linux64 + tesseract-cli`
- `lite + paddleocr`

## 文档入口

- [Java CLI 中文文档](java/README.md)
- [Java CLI English Guide](java/README.en.md)
- [命令参考](java/COMMAND_REFERENCE.md)
- [测试说明](test/README.md)
- [OCR 路线图](OCR_PROVIDER_ROADMAP.md)

## 其他子项目

- `markitdown-mcp`：MCP 相关内容

## License

本仓库按当前仓库中的 License 文件或后续发布说明为准。
