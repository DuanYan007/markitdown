# markitdown4j

[中文](README.md) | [English](README.en.md)

`markitdown4j` 是当前仓库的主线交付物。它是一个 Java CLI，用于把常见文档和文件内容转换为 Markdown，并在需要时通过统一配置调用不同 OCR 后端。

## 功能

- 多格式转 Markdown
- 可插拔 OCR 后端
- 平台化制品构建
- 批量、递归、并行处理
- 统一配置和统一命令行入口

## 环境要求

- Java 11+
- Maven 3.8+

## 快速开始

构建：

```bash
mvn package -DskipTests
```

最小示例：

```bash
java -jar target/markitdown4j-0.0.3-lite.jar document.pdf -o output.md
```

## 发布制品

- `lite`
- `full`
- `win32`
- `win64`
- `linux64`
- `mac`

建议选择：

- Windows 64 位：`win64`
- Windows 32 位：`win32`
- Linux：`linux64`
- macOS：`mac`
- 最小体积：`lite`
- 完整 OCR 资源：`full`

## OCR 后端

当前公开支持的 OCR engine：

- `tess4j`
- `tesseract-cli`
- `paddleocr`
- `http`

适用建议：

- `tess4j`：Windows 内嵌 OCR
- `tesseract-cli`：Linux / macOS 本地 OCR
- `paddleocr`：远程结构化 OCR
- `http`：自定义远程 OCR 服务

## 统一 OCR 配置

所有 OCR 后端共用同一套配置字段，用户不需要为不同 OCR 学习不同配置格式。

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

配置文件：

- [`../markitdown.example.yml`](../markitdown.example.yml)
- `../markitdown.local.yml`：本地私有覆盖
- `../markitdown.yml`：项目主配置文件
- [`../.markitdown.properties`](../.markitdown.properties)：旧格式，当前仍兼容

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

## OCR 使用示例

```bash
# Windows embedded OCR
java -jar target/markitdown4j-0.0.3-win64.jar test/with-text.png --ocr --ocr-engine tess4j -o out/ocr.md

# Linux / macOS local OCR
java -jar target/markitdown4j-0.0.3-linux64.jar test/with-text.png --ocr --ocr-engine tesseract-cli -o out/ocr.md

# Remote PaddleOCR
java -jar target/markitdown4j-0.0.3-lite.jar test/with-text.png --ocr --ocr-engine paddleocr -o out/paddle.md
```

## 支持格式

| 类别 | 格式 |
| --- | --- |
| PDF | `.pdf` |
| Word | `.docx`, `.doc` |
| Excel | `.xlsx`, `.xls` |
| PowerPoint | `.pptx`, `.ppt` |
| HTML | `.html`, `.htm` |
| 图片 | `.png`, `.jpg`, `.jpeg`, `.gif`, `.bmp`, `.tiff` |
| 文本 | `.txt`, `.csv`, `.json`, `.xml` |
| 压缩包 | `.zip` |
| 音频元数据 | `.mp3`, `.wav`, `.flac` |

## 测试

自动化测试：

```bash
mvn test
```

当前自动化测试覆盖：

- Profile 构建与命名检查
- OCR engine factory 选择
- PaddleOCR 响应解析
- 文本流式转换
- ZIP 委托与嵌套转换行为
- YAML / legacy 配置加载

测试文件资产：

- [`../test/test.zip`](../test/test.zip) 是正式测试文件包
- 当前包含约 104 个测试文件
- 用于回归、兼容性和 release 前手工验证

已验证的关键路径：

- `lite` 基础转换
- `win64 + tess4j`
- `linux64 + tesseract-cli`
- `lite + paddleocr`

更多测试说明见 [../test/README.md](../test/README.md)。

## 相关文档

- [命令参考](COMMAND_REFERENCE.md)
- [测试说明](../test/README.md)
- [OCR 路线图](../OCR_PROVIDER_ROADMAP.md)
- [YAML 配置设计](../CONFIGURATION_DESIGN.md)
