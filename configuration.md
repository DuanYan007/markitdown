# 配置

本文中的 `markitdown4j` 是 `java -jar target/markitdown4j-<version>.jar` 的文档简写。当前仓库交付的是 JAR 形式的 CLI，因此如果你没有自定义别名或包装脚本，请直接使用 `java -jar ...`。

## 配置的作用

配置文件用于稳定地定义这个应用默认应当如何处理转换任务，主要包括：

- 输出文件写到哪里
- 是否启用 OCR
- 使用哪一种 OCR 引擎
- 图片和表格以什么形式输出
- 文件大小限制和批量发现行为

如果你希望：

- 每次执行都使用相同的默认选项
- 减少冗长的命令行参数
- 为本地环境和发布环境分别维护不同配置
- 让团队成员共享同一套默认规则

那么应该优先使用配置文件，而不是把所有选项都写在命令行里。

## 如何选择配置方式

当前版本建议按下面的方式选择：

- 只做临时单次转换：直接使用 CLI 参数
- 有稳定的本地默认行为：创建 `markitdown.yml`
- 同一项目中存在个人本地差异：额外使用 `markitdown.local.yml`
- 需要显式切换某一份配置：使用 `--config-path`
- 需要在一次执行中临时覆盖少量项：在配置文件基础上叠加 CLI 参数

## 配置文件

当前版本会识别以下文件：

- `markitdown.yml`
- `markitdown.local.yml`
- `markitdown.example.yml`

说明：

- `markitdown.yml` 适合提交到项目中，作为共享默认配置
- `markitdown.local.yml` 适合保存个人机器路径、凭据引用或本地实验参数
- `markitdown.example.yml` 适合作为模板参考，不一定自动参与项目级配置发现

## 解析顺序

配置的生效顺序如下：

1. 内置默认值
2. `markitdown.yml`
3. `markitdown.local.yml`
4. CLI 覆盖项

这意味着：

- 共享默认值放在 `markitdown.yml`
- 个人机器差异放在 `markitdown.local.yml`
- 当前命令只想临时改一项时，用 CLI 参数覆盖即可

## 从这里开始

第一次配置时建议按这个顺序进行：

1. 先执行 `markitdown4j --generate-config`
2. 生成模板后保留你真正需要的键
3. 先只配置输出和 OCR，不要一次性填写所有项
4. 使用 `markitdown4j --validate-config` 校验语义是否正确
5. 使用 `markitdown4j --show-config` 查看最终生效值和来源

建议把“先校验，再执行转换”作为习惯，这样更容易定位配置问题。

## 起步配置示例

### 1. 不启用 OCR 的本地最小配置

```yaml
output:
  directory: output

content:
  includeMetadata: true
  includeImages: true
  includeTables: true

format:
  imageFormat: markdown
  tableFormat: github
```

适用场景：

- 常规可提取文本 PDF
- DOCX、XLSX、HTML 等常见文档
- 只希望输出落到固定目录

### 2. 本地 Tesseract OCR 配置

```yaml
ocr:
  enabled: true
  engine: tesseract-cli
  language: eng
  tesseractPath: O:/tesserOCR/tesseract.exe
  tessdataPath: O:/tesserOCR/tessdata

output:
  directory: output
```

适用场景：

- 需要本地 OCR
- 希望跨平台保留统一 OCR 入口
- 希望避免引入 JVM 绑定型 OCR 依赖

说明：

- `tesseractPath` 指向可执行文件
- `tessdataPath` 指向语言数据目录
- 如果系统 PATH 已能找到 `tesseract`，是否仍写入 `tesseractPath` 取决于你的环境稳定性要求

### 3. HTTP OCR 服务配置

```yaml
ocr:
  enabled: true
  engine: http
  endpoint: http://127.0.0.1:8080/ocr
  apiKey: your-api-key
  timeoutSeconds: 60
```

适用场景：

- OCR 能力由外部服务统一提供
- CLI 所在机器不希望直接安装 OCR 引擎
- 希望把 OCR 访问逻辑集中在网络服务中

### 4. PaddleOCR 配置

```yaml
ocr:
  enabled: true
  engine: paddleocr
  endpoint: https://your-paddle-service.example/api/jobs
  apiToken: your-api-token
  timeoutSeconds: 120
```

适用场景：

- 使用 PaddleOCR 任务接口
- 需要远程 OCR 统一调度
- 需要和服务端 OCR 基础设施集成

### 5. 大文件处理配置

```yaml
performance:
  maxFileSize: 0

fileDiscovery:
  recursive: true
```

说明：

- `maxFileSize: 0` 表示不限制文件大小
- 也可以在单次命令中使用 `--large-file` 临时开启不限大小模式

适用场景：

- 大型 PDF、超大 XLSX、复杂 ZIP 包
- 需要目录级批量扫描

## 当前版本中“有效配置”和“仅 CLI 生效项”

当前版本应区分两类设置：

### 会进入有效配置视图的项

这些项会在 `--show-config` 中以最终值和来源展示：

- OCR 开关与 OCR 引擎
- 输出目录
- 是否包含 metadata、images、tables
- 表格格式、图片格式
- 最大文件大小
- 文件发现相关选项

### 主要通过 CLI 控制的项

这些项更接近一次性执行行为：

- 具体输入路径
- 单次输出目标路径 `-o`
- 是否执行 `--batch`
- 是否把当前命令作为配置诊断命令运行

理解这个边界很重要，因为并不是所有 CLI 选项都会回写成“长期配置”。

## 完整配置参考

### enginePaths

用于声明运行时二进制或资源路径，例如：

- `tesseractPath`
- `tessdataPath`

当你需要稳定引用本地 OCR 安装路径时，应优先放在这里。

### output

控制输出位置和输出目录相关行为，常见项包括：

- `directory`

建议：

- 批量转换时优先配置输出目录
- 单文件偶发转换时可以直接用 `-o`

### content

控制转换结果中是否保留某些内容：

- `includeMetadata`
- `includeImages`
- `includeTables`

建议：

- 如果下游只消费正文，可关闭 `includeMetadata`
- 如果希望结果更简洁，可按需关闭图片或表格输出

### ocr

OCR 相关配置包括：

- `enabled`
- `engine`
- `language`
- `tesseractPath`
- `tessdataPath`
- `endpoint`
- `apiKey`
- `apiToken`
- `timeoutSeconds`

建议：

- 本地环境优先使用 `tesseract-cli`
- 服务型场景再选择 `http` 或 `paddleocr`
- 不要同时混用互斥的 OCR 路径参数和远程接口参数

### format

用于控制 Markdown 输出风格：

- `imageFormat`
- `tableFormat`
- `pageBreakMode`

建议：

- 需要标准 Markdown 图片时使用 `markdown`
- 需要 HTML `<img>` 时使用 `html`
- 表格输出应根据你的下游渲染器选择

### performance

性能和限制相关项包括：

- `maxFileSize`

建议：

- 生产默认应保留大小限制
- 大文件场景可在受控环境中设为 `0`

### ui

控制命令行展示行为，例如：

- `quiet`

适用场景：

- 自动化脚本中减少非必要输出

### fileDiscovery

控制目录和文件发现行为，例如：

- `recursive`

适用场景：

- 目录转换
- 多级目录批量扫描

### providerExtensions

用于保存特定 OCR 或外部能力的扩展配置键。

当前版本允许提供方特有配置存在，但前提是主结构合法。

## 校验规则

当前版本的配置校验主要覆盖：

- 配置文件必须是 YAML
- YAML 语法必须可解析
- 顶层 section 名称必须合法
- section 下的 key 必须是当前版本支持的键，或被允许的 provider 扩展键
- OCR 引擎名称必须合法
- 互相关联的字段应满足语义要求

建议在以下时机执行校验：

- 新建配置文件后
- 切换 OCR 引擎后
- 修改大文件策略后
- 发布前

## 配置与 CLI 覆盖关系

CLI 参数始终是最后一层覆盖。

例如：

```bash
markitdown4j --show-config --config-path verification/manual/markitdown.release.yml --ocr --ocr-engine http
```

这个命令表示：

- 先加载指定 YAML
- 再把 `--ocr` 和 `--ocr-engine http` 作为本次执行覆盖项
- 在有效配置视图中，这两个值的来源应显示为 CLI

适合用 CLI 覆盖的场景：

- 临时切换 OCR 引擎
- 临时关闭 metadata、tables、images
- 临时放开文件大小限制

不适合长期依赖 CLI 覆盖的场景：

- 每次都写相同的 OCR 路径
- 每次都写相同的输出目录
- 团队内长期共享的默认格式

## 新用户推荐流程

建议按下面的流程完成首次配置：

1. 执行 `markitdown4j --generate-config`
2. 删除自己不需要的键，保留当前真实会用到的部分
3. 填写输出目录和 OCR 配置
4. 执行 `markitdown4j --validate-config`
5. 执行 `markitdown4j --show-config`
6. 用一个小样本文档进行试跑

## 参考模板

可直接参考仓库中的：

- [markitdown.example.yml](markitdown.example.yml)
- [verification/manual/markitdown.release.yml](verification/manual/markitdown.release.yml)
