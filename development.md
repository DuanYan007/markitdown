# 开发文档

## 目的

本文描述 `markitdown4j` 当前版本的软件设计和开发结构，只覆盖当前实现本身，包括：

- 系统目标
- 构建与运行基线
- 仓库结构
- 核心模块职责
- 运行流程
- CLI、配置、转换器和 OCR 设计
- 当前测试与发布验证方式

## 系统概览

`markitdown4j` 是一个文档转 Markdown 的 Java 应用，当前提供两种产品形态：

- CLI：面向本地命令执行、批量转换和自动化脚本
- Java Library：面向 Java 内部集成

系统围绕同一套核心运行时构建：

1. CLI 解析参数或读取配置
2. `ConfigurationManager` 生成有效配置
3. `MarkItDownApplication` 创建默认 `MarkItDownEngine`
4. `ConverterRegistry` 根据文件类型选择 `DocumentConverter`
5. 转换器输出 Markdown、metadata 和 warnings
6. OCR 由 `OcrEngineFactory` 根据配置创建具体 OCR 引擎

## 构建与运行基线

### 构建基线

- Java 11
- Maven 3.8+
- 通过 Maven 生成 Shaded JAR

### 运行基线

- CLI 以 `java -jar target/markitdown4j-<version>.jar` 运行
- Library 以 Maven 依赖方式集成
- OCR 可选，不是所有转换路径都依赖 OCR

### OCR 运行模型

当前 OCR 支持：

- `tesseract-cli`
- `http`
- `paddleocr`

其中 `tesseract-cli` 依赖系统中的 Tesseract 可执行程序；当前版本不依赖 `tess4j`。

## 仓库结构

当前仓库的重要目录如下：

- `markitdown-java/`
- `src/test/java/`
- `test/`
- `verification/`
- `project_tools/`
- `scripts/`
- 根目录文档与 `pom.xml`

### 目录说明

- `markitdown-java/src/main/java/`：主应用代码
- `src/test/java/`：自动化单元测试和集成测试
- `test/`：功能测试输入样例文件
- `verification/`：发布验证配置和批量测试夹具
- `project_tools/`：项目级辅助脚本
- `scripts/`：发布 smoke 和其他辅助脚本

## 包结构

### `com.markitdown`

顶层应用入口与公共 API 所在包，典型类包括：

- `MarkItDownApplication`
- `api.ConversionResult`
- `api.DocumentConverter`
- `exceptions.ConversionException`

### `com.markitdown.cli`

CLI 层负责：

- 命令行参数解析
- 配置诊断命令
- 输入发现
- 输出路径推导
- 远程输入处理
- 标准输入处理
- 批量执行与进度汇总
- 用户消息格式化

当前重要类包括：

- `MarkItDownCommand`
- `ConfigCommands`
- `BatchConversionRunner`
- `CliConfigurationOverrides`
- `InputDiscoveryHelper`
- `OutputPathHelper`
- `PipeInputHelper`
- `RemoteInputHelper`
- `UserMessageHelper`

### `com.markitdown.config`

配置层负责：

- YAML 加载
- 默认值合并
- 本地覆盖合并
- CLI 覆盖合并
- 有效配置视图
- 配置校验
- `ConversionOptions` 构建

关键类：

- `ConfigurationManager`
- `ConversionOptions`

### `com.markitdown.core`

核心运行时负责：

- 转换入口调度
- 转换器注册与查找
- 支持类型判断
- 单文件、流、批量和并行调用

关键类：

- `MarkItDownEngine`
- `ConverterRegistry`

### `com.markitdown.converters`

转换器包负责各类输入格式的具体转换逻辑。当前主要包括：

- `PdfConverter`
- `DocConverter`
- `DocxConverter`
- `PptConverter`
- `PptxConverter`
- `XlsConverter`
- `XlsxConverter`
- `HtmlConverter`
- `TextConverter`
- `ImageConverter`
- `AudioConverter`
- `ZipConverter`

各转换器实现统一的 `DocumentConverter` 契约，并由注册中心按能力进行选择。

### `com.markitdown.ocr`

OCR 模块负责：

- OCR 引擎抽象
- OCR Provider 抽象
- 引擎实例创建
- 不可用 OCR 的降级行为
- 具体 OCR 执行逻辑

关键类包括：

- `OcrEngine`
- `OcrProvider`
- `OcrEngineFactory`
- `TesseractCliOcrEngine`
- `TesseractCliOcrProvider`
- `HttpOcrEngine`
- `PaddleOcrEngine`

### 其他包

- `com.markdown.engine`：Markdown 构建与渲染辅助逻辑
- `com.markitdown.utils`：文件类型识别、图片提取等工具
- `com.markitdown.models`：提取结果等模型对象

## 运行流程

### CLI 流程

CLI 的当前执行流程如下：

1. `MarkItDownCommand` 解析参数
2. 如果是诊断命令，转入 `ConfigCommands`
3. `ConfigurationManager` 加载默认值、YAML、本地 YAML 和 CLI 覆盖
4. `InputDiscoveryHelper` 解析本地、目录、通配符或远程输入
5. `PipeInputHelper` 处理 stdin 模式下的 MIME 判断
6. `RemoteInputHelper` 负责远程输入下载和输出命名辅助
7. `OutputPathHelper` 计算最终输出路径
8. `BatchConversionRunner` 执行顺序或批量转换
9. `UserMessageHelper` 在失败路径中生成面向用户的诊断消息

### Library 流程

Library 使用流程更直接：

1. 通过 `MarkItDownApplication.createEngine()` 创建 engine
2. 构建 `ConversionOptions`
3. 调用 `engine.convert(...)`
4. 读取 `ConversionResult`
5. 调用 `engine.shutdown()`

## CLI 设计

### `MarkItDownCommand`

这是 CLI 主命令入口，负责：

- 解析用户参数
- 区分转换命令与配置诊断命令
- 创建和使用配置对象
- 组织单文件、批量、stdin 和远程输入转换

### 辅助类职责

- `BatchConversionRunner`：处理顺序批量转换、进度和统计信息
- `CliConfigurationOverrides`：把 CLI 选项转成类型化覆盖项
- `InputDiscoveryHelper`：发现目录、通配符、远程 URL 和本地路径输入
- `OutputPathHelper`：统一推导输出路径和目录创建策略
- `PipeInputHelper`：在 stdin 模式下推断 MIME 且不消耗前导字节
- `RemoteInputHelper`：根据 `Content-Disposition` 或 URL 路径确定远程文件名
- `UserMessageHelper`：把内部异常转成用户可读的错误信息

## 配置设计

### 支持的文件

当前识别：

- `markitdown.yml`
- `markitdown.local.yml`
- 通过 `--config-path` 显式指定的 YAML

### 解析顺序

1. 内置默认值
2. `markitdown.yml`
3. `markitdown.local.yml`
4. CLI 覆盖项

### `ConfigurationManager` 行为

`ConfigurationManager` 负责以下行为：

- 发现并加载 YAML 文件
- 忽略非 YAML 文件
- 拒绝非法显式配置文件
- 构建类型化 `ConversionOptions`
- 生成带来源标记的有效配置视图
- 执行语义和结构校验

### `ConversionOptions` 结构

`ConversionOptions` 是运行时类型化配置对象，覆盖的维度包括：

- OCR 开关和引擎
- 输出控制
- metadata、images、tables 开关
- 表格和图片格式
- 文件大小限制
- 路径与语言相关 OCR 参数

## 转换器设计

### `DocumentConverter` 契约

每个转换器通过统一接口暴露：

- 支持的扩展名和 MIME 类型
- 文件转换能力
- 部分转换器的流式转换能力

这使得注册中心可以按能力查找，而不是依赖硬编码条件分支。

### `ConverterRegistry`

注册中心负责：

- 按 MIME 类型维护转换器映射
- 汇总支持类型
- 为 engine 提供查询入口

### `MarkItDownEngine`

核心 engine 负责：

- 校验输入
- 根据 MIME 和文件类型选择转换器
- 调用转换器
- 返回统一 `ConversionResult`
- 提供同步、异步、顺序和并行转换入口
- 对外暴露支持性检查方法

## OCR 设计

### 工厂模型

OCR 使用工厂模型：

1. 从 `ConversionOptions` 读取 OCR 配置
2. `OcrEngineFactory` 判断是否启用 OCR
3. 根据 provider 或 engine 名称选择实现
4. 如果配置不可用，返回不可用 OCR 实现或给出错误

### 当前 OCR 引擎

#### `tesseract-cli`

特点：

- 本地调用系统 Tesseract 可执行程序
- 跨平台
- 不依赖 `tess4j`
- 可显式指定 `tesseractPath` 和 `tessdataPath`

#### `http`

特点：

- 通过 HTTP 接口调用 OCR 服务
- 适合服务端统一 OCR 部署

#### `paddleocr`

特点：

- 通过 PaddleOCR 任务接口执行
- 依赖远程服务配置

## 输出设计

输出设计目标是保证 CLI 行为可预测。

当前规则包括：

- 单文件默认输出到源文件旁
- 批量转换默认输出到指定目录
- 显式文件路径保持不变
- 目录输出统一转为 `<source>.md`
- 远程输入在未指定输出文件时写入当前工作目录
- 写入前自动创建父目录

## 远程输入与管道输入

### 远程输入

远程输入设计包括：

- 下载远程资源
- 处理 `Content-Disposition` 文件名
- 回退到 URL 路径命名
- 清理不安全文件名

### 管道输入

管道输入设计包括：

- 支持显式 `--mime-type`
- 能从输入头部推断纯文本或 JSON
- 在 MIME 识别后仍保留完整输入内容供真正转换使用

## 测试设计

### 自动化测试

当前自动化测试覆盖：

- 构建守卫
- CLI 单元测试与集成测试
- 配置加载与校验测试
- 核心转换流程测试
- 各类转换器测试
- OCR 工厂与 OCR provider 测试
- 工具类测试

详细测试项请直接参考 [testing.md](testing.md)。

### 发布验证

当前发布验证除了 `mvn test` 外，还保留：

- 功能测试命令清单
- 发布配置样例
- smoke 脚本
- OCR 实机验证路径

相关文件包括：

- `verification/manual/markitdown.release.yml`
- `project_tools/run_functional_checks.ps1`
- `scripts/release_smoke.ps1`

## CI

当前 CI 工作流位于：

- `.github/workflows/ci.yml`

当前关注点包括：

- JDK 11 与 JDK 17 的矩阵构建
- Maven 测试执行
- 保证当前版本在主要 Java 版本上可构建

## 常用开发任务

### 运行主测试集

```bash
mvn test
```

### 构建打包 JAR

```bash
mvn -DskipTests package
```

### 运行重点 CLI 测试

```bash
mvn -Dtest=ConfigCommandsTest,MarkItDownCommandFormatsTest test
```

### 运行发布 smoke

```bash
powershell -ExecutionPolicy Bypass -File scripts/release_smoke.ps1
```

### 运行 Tesseract OCR smoke

```bash
markitdown4j test/with-text.png --ocr --config-path ocr.yml -o out/ocr/
```
