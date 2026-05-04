# markitdown

[中文](README.md) | [English](README.en.md)

`markitdown` 是一个面向文档转 Markdown 的仓库。当前主线交付物是 `markitdown4j` Java CLI，它可以把常见办公文档、网页、图片、压缩包以及部分音频元数据转换为 Markdown，并通过统一配置接入不同 OCR / 远程识别后端。

## 这个项目可以做什么

- 将 PDF、Word、Excel、PowerPoint、HTML、图片、文本、ZIP 转换为 Markdown
- 为扫描版 PDF 和图片启用 OCR
- 提供多平台制品：`lite`、`full`、`win32`、`win64`、`linux64`、`mac`
- 通过统一配置切换 OCR 后端，而不需要改转换流程
- 支持远程 OCR Provider，例如 `paddleocr`
- 提供 MCP 形态，方便接入 agent / 工具链

## 仓库结构

- [markitdown-java/README.md](markitdown-java/README.md)：Java CLI 中文文档
- [markitdown-java/README.en.md](markitdown-java/README.en.md)：Java CLI English guide
- [markitdown-java/COMMAND_REFERENCE.md](markitdown-java/COMMAND_REFERENCE.md)：命令与参数参考
- [test/README.md](test/README.md)：测试数据集与验证说明
- [OCR_PROVIDER_ROADMAP.md](OCR_PROVIDER_ROADMAP.md)：OCR / VLM 扩展路线图
- [CONFIGURATION_DESIGN.md](CONFIGURATION_DESIGN.md)：YAML 配置设计

## 快速开始

1. 安装 Java 11 或更高版本
2. 下载适合你平台的 jar
3. 准备配置文件
4. 运行转换命令

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

## 统一配置

项目现在采用 **YAML 优先** 的配置方式。推荐用户通过配置文件初始化，而不是依赖系统级环境配置。

推荐文件：

- `markitdown.yml`：项目主配置文件
- `markitdown.local.yml`：本地私有覆盖，不提交仓库
- [markitdown.example.yml](markitdown.example.yml)：官方模板
- [`.markitdown.properties`](.markitdown.properties)：旧格式，当前仍兼容

统一 OCR 配置示例：

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

统一字段：

- `ocr.enabled`
- `ocr.engine`
- `ocr.endpoint`
- `ocr.api_key`
- `ocr.model`
- `ocr.timeout`
- `ocr.poll_interval`
- `ocr.language`

配置优先级：

1. 命令行参数，例如 `--ocr-engine`
2. `markitdown.local.yml`
3. `markitdown.yml`
4. 程序内置默认值
5. 环境变量，仅作为敏感信息或部署场景补充

常用环境变量：

- `MARKITDOWN_OCR_ENGINE`
- `MARKITDOWN_OCR_ENDPOINT`
- `MARKITDOWN_OCR_API_KEY`
- `MARKITDOWN_OCR_MODEL`
- `MARKITDOWN_OCR_TIMEOUT`
- `MARKITDOWN_OCR_POLL_INTERVAL`

当前公开支持的 OCR 后端：

- `tess4j`：适合 Windows 内嵌 OCR
- `tesseract-cli`：适合 Linux / macOS 本地 OCR
- `paddleocr`：适合远程结构化 OCR
- `http`：适合自定义远程 OCR 服务

## 测试与验证

项目不只是列出功能，也提供了正式测试数据集和可复用的验证路径。

### 自动化测试

执行：

```bash
mvn test
```

当前覆盖：

- Profile 构建与命名检查
- OCR engine factory 选择
- PaddleOCR 响应解析
- 文本流式转换
- ZIP 委托与嵌套转换行为
- YAML / legacy 配置加载

### 测试数据集

[test/test.zip](test/test.zip) 是仓库内正式测试文件包，目前包含约 104 个测试文件，用于：

- 回归测试
- 兼容性验证
- release 前手工验证

解压后的使用说明见 [test/README.md](test/README.md)。

### 已验证路径

- `lite` 基础转换
- `win64 + tess4j`
- `linux64 + tesseract-cli`
- `lite + paddleocr`

## 文档入口

- [Java CLI 中文文档](markitdown-java/README.md)
- [Java CLI English Guide](markitdown-java/README.en.md)
- [命令参考](markitdown-java/COMMAND_REFERENCE.md)
- [测试说明](test/README.md)
- [OCR 路线图](OCR_PROVIDER_ROADMAP.md)
- [YAML 配置设计](CONFIGURATION_DESIGN.md)

## 其他子项目

- `markitdown-mcp`：MCP / agent 集成相关内容

## License

以仓库当前的 License 文件或后续 release 说明为准。
