# MarkItDown Java 使用教程

> 🚀 MarkItDown Java - 文档转换工具完整使用指南

## 📦 快速开始

### 1. 获取JAR文件

用户将获得 `markitdown-java.jar` 文件，这是完整的可执行程序，无需额外安装。

### 2. 系统要求

- **Java版本**: Java 11 或更高版本
- **操作系统**: Windows、Linux、macOS
- **内存要求**: 最小 512MB，推荐 2GB+
- **磁盘空间**: 最小 100MB 可用空间

### 3. 验证Java环境

```bash
# 检查Java版本
java -version

# 应该显示类似以下内容：
# java version "11.0.x" 或更高
```

如果未安装Java，请从 [https://www.java.com/download/](https://www.java.com/download/) 下载安装。

## 🚀 基础使用

### 最简单的使用方式

```bash
# 转换单个文件（最简单）
java -jar markitdown-java.jar document.pdf

# 这将自动在相同目录下生成 document.pdf.md 文件
```

### 指定输出文件

```bash
# 指定输出文件名
java -jar markitdown-java.jar document.pdf -o output.md

# 指定输出目录
java -jar markitdown-java.jar document.pdf -o output_dir/

# 转换并输出到不同位置
java -jar markitdown-java.jar input.docx -o converted/readme.md
```

### 查看帮助信息

```bash
# 显示完整帮助
java -jar markitdown-java.jar --help

# 显示版本信息
java -jar markitdown-java.jar --version

# 显示使用示例
java -jar markitdown-java.jar --examples
```

## 📄 支持的文件格式

### 完全支持的格式

| 格式类别 | 支持的扩展名 | 转换质量 |
|---------|-------------|----------|
| **PDF文档** | `.pdf` | ⭐⭐⭐⭐ |
| **Word文档** | `.docx`, `.doc` | ⭐⭐⭐⭐⭐ |
| **Excel表格** | `.xlsx`, `.xls` | ⭐⭐⭐⭐⭐ |
| **PowerPoint** | `.pptx`, `.ppt` | ⭐⭐⭐⭐ |
| **图片文件** | `.png`, `.jpg`, `.jpeg`, `.gif`, `.bmp` | ⭐⭐⭐⭐ |
| **音频文件** | `.mp3`, `.wav`, `.flac` | ⭐⭐⭐ |
| **网页文件** | `.html`, `.htm` | ⭐⭐⭐⭐ |
| **文本文件** | `.txt`, `.json`, `.xml`, `.csv` | ⭐⭐⭐⭐⭐ |
| **电子书** | `.epub` | ⭐⭐⭐⭐ |
| **邮件文件** | `.msg` (Outlook) | ⭐⭐⭐⭐ |
| **压缩文件** | `.zip` | ⭐⭐⭐⭐ |

## 🔥 常用使用场景

### 场景1: 转换PDF文档

```bash
# 基础PDF转换
java -jar markitdown-java.jar report.pdf

# 加密PDF转换
java -jar markitdown-java.jar encrypted.pdf --pdf-password your_password

# 扫描PDF使用OCR识别
java -jar markitdown-java.jar scanned.pdf --ocr

# 中文OCR识别
java -jar markitdown-java.jar chinese-scanned.pdf --ocr -l chi_sim
```

### 场景2: 转换Office文档

```bash
# Word文档转换
java -jar markitdown-java.jar document.docx -o output.md

# Excel表格转换
java -jar markitdown-java.jar data.xlsx -o table.md

# PowerPoint演示文稿转换
java -jar markitdown-java.jar presentation.pptx -o slides.md

# 旧版本Office文件
java -jar markitdown-java.jar old.doc -o converted.md
```

### 场景3: 批量转换文件

```bash
# 转换所有PDF文件
java -jar markitdown-java.jar *.pdf -o output_dir/

# 批量模式转换
java -jar markitdown-java.jar *.pdf --batch

# 递归转换整个目录
java -jar markitdown-java.jar documents/ --recursive -o output/
```

### 场景4: 图片OCR识别

```bash
# 图片文字识别（英文）
java -jar markitdown-java.jar image.png --ocr

# 图片文字识别（中文）
java -jar markitdown-java.jar chinese.png --ocr -l chi_sim

# 自动语言检测
java -jar markitdown-java.jar mixed-text.png --ocr -l auto

# 批量图片OCR
java -jar markitdown-java.jar images/*.png --ocr --batch
```

### 场景5: 内容控制

```bash
# 只提取文字，不要图片
java -jar markitdown-java.jar document.docx --no-images

# 只提取文字，不要表格
java -jar markitdown-java.jar report.xlsx --no-tables

# 只提取文字，不要元数据
java -jar markitdown-java.jar document.pdf --no-metadata

# 组合多个选项
java -jar markitdown-java.jar document.docx --no-images --no-metadata
```

### 场景6: 大文件处理

```bash
# 处理大文件（超过50MB）
java -jar markitdown-java.jar large.pdf --large-file

# 内存优化模式
java -jar markitdown-java.jar huge.pdf --optimize-memory

# 显示性能统计
java -jar markitdown-java.jar large.pdf --stats
```

### 场景7: 性能优化

```bash
# 并行处理多个文件
java -jar markitdown-java.jar *.pdf --parallel --threads 4

# 显示进度条
java -jar markitdown-java.jar large-document.pdf --progress

# 显示详细日志
java -jar markitdown-java.jar document.pdf --verbose

# 静默模式（只显示错误）
java -jar markitdown-java.jar document.pdf --quiet
```

## 🛠️ 高级功能

### 配置文件使用

创建配置文件 `.markitdown.properties`：

```properties
# 引擎路径配置
tesseract.path=/usr/bin
tessdata.path=/usr/share/tesseract-ocr/4.00/tessdata

# 输出配置
output.dir=./output
output.image.dir=assets

# 内容选项
content.include.images=true
content.include.tables=true
content.include.metadata=true

# OCR配置
ocr.enable=false
ocr.language=auto

# 性能配置
performance.parallel=true
performance.threads=4
```

使用配置文件：

```bash
# 生成默认配置文件
java -jar markitdown-java.jar --generate-config

# 验证配置文件
java -jar markitdown-java.jar --validate-config

# 查看当前配置
java -jar markitdown-java.jar --show-config

# 使用配置文件转换
java -jar markitdown-java.jar document.pdf
```

### 管道输入/输出

```bash
# 从管道输入
cat document.pdf | java -jar markitdown-java.jar > output.md

# 从URL转换
curl -s http://example.com/document.pdf | java -jar markitdown-java.jar > output.md

# 组合使用
wget -O - http://example.com/doc.pdf | java -jar markitdown-java.jar | grep "关键词"
```

### ZIP压缩包处理

```bash
# 转换ZIP中的所有文档
java -jar markitdown-java.jar documents.zip

# 混合文档压缩包
java -jar markitdown-java.jar mixed-files.zip -o output/

# 嵌套压缩包
java -jar markitdown-java.jar nested.zip
```

## 📊 实用技巧

### 技巧1: 快速预览

```bash
# 输出到终端快速预览
java -jar markitdown-java.jar document.pdf -o -

# 搜索特定内容
java -jar markitdown-java.jar document.pdf | grep "关键词"
```

### 技巧2: 格式选择

```bash
# 选择表格格式
java -jar markitdown-java.jar table.pdf --table-format github

# 选择图片格式
java -jar markitdown-java.jar doc.docx --image-format markdown
```

### 技巧3: 错误处理

```bash
# 友好的错误提示
java -jar markitdown-java.jar document.pdf --interactive

# 详细错误信息
java -jar markitdown-java.jar document.pdf --verbose
```

### 技巧4: 脚本自动化

创建批量转换脚本 `convert.sh`：

```bash
#!/bin/bash
# 批量转换脚本

for file in *.pdf; do
    echo "正在转换: $file"
    java -jar markitdown-java.jar "$file" --ocr -o "converted/${file%.pdf}.md"
done

echo "转换完成！"
```

使用脚本：

```bash
chmod +x convert.sh
./convert.sh
```

## 🐛 常见问题解决

### 问题1: 找不到Java

**错误信息**: `'java' 不是内部或外部命令`

**解决方案**:
1. 安装Java 11或更高版本
2. 配置JAVA_HOME环境变量
3. 将Java bin目录添加到PATH

### 问题2: 内存不足

**错误信息**: `java.lang.OutOfMemoryError: Java heap space`

**解决方案**:
```bash
# 增加内存限制
java -Xmx2g -jar markitdown-java.jar large.pdf

# 或使用内存优化模式
java -jar markitdown-java.jar large.pdf --optimize-memory
```

### 问题3: OCR不工作

**错误信息**: `OCR功能不可用`

**解决方案**:
1. 安装Tesseract OCR引擎
2. 配置Tesseract路径
3. 下载对应语言包

```bash
# Windows: 下载安装Tesseract
# Linux: sudo apt-get install tesseract-ocr
# macOS: brew install tesseract
```

### 问题4: 中文乱码

**解决方案**:
```bash
# 指定中文OCR语言
java -jar markitdown-java.jar chinese.pdf --ocr -l chi_sim

# 或使用自动检测
java -jar markitdown-java.jar chinese.pdf --ocr -l auto
```

### 问题5: 文件权限问题

**错误信息**: `Permission denied`

**解决方案**:
```bash
# Linux/macOS: 添加执行权限
chmod +x markitdown-java.jar

# 检查文件权限
ls -la markitdown-java.jar
```

## 📚 最佳实践

### 1. 文件组织

```bash
# 推荐的目录结构
project/
├── documents/          # 原始文档
│   ├── report.pdf
│   ├── data.xlsx
│   └── slides.pptx
├── converted/          # 转换结果
└── markitdown-java.jar # 程序文件
```

### 2. 批量处理工作流

```bash
# 创建输出目录
mkdir -p converted

# 批量转换
java -jar markitdown-java.jar documents/* -o converted/ --batch --progress
```

### 3. 质量检查

```bash
# 转换后检查输出
java -jar markitdown-java.jar document.pdf -o test.md
cat test.md | head -20  # 查看前20行
```

### 4. 性能优化

```bash
# 大文件使用并行处理
java -jar markitdown-java.jar *.pdf --parallel --threads 4 --stats
```

## 🔧 环境配置

### Windows系统

1. **安装Java**: 下载并安装Java 11+
2. **配置环境变量**:
   ```
   JAVA_HOME=C:\Program Files\Java\jdk-11
   Path=%JAVA_HOME%\bin;%Path%
   ```
3. **测试安装**: `java -version`

### Linux系统

```bash
# 安装Java
sudo apt-get update
sudo apt-get install openjdk-11-jre

# 验证安装
java -version

# 可选：安装Tesseract OCR
sudo apt-get install tesseract-ocr tesseract-ocr-chi-sim
```

### macOS系统

```bash
# 使用Homebrew安装Java
brew install openjdk@11

# 配置环境变量
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@11' >> ~/.zshrc
source ~/.zshrc

# 可选：安装Tesseract
brew install tesseract tesseract-lang
```

## 📈 性能基准

### 典型文件处理时间

| 文件类型 | 文件大小 | 处理时间 | 内存使用 |
|---------|---------|----------|----------|
| PDF文档 | 1MB | 2-5秒 | 100-200MB |
| Word文档 | 500KB | 1-3秒 | 50-100MB |
| Excel表格 | 2MB | 3-8秒 | 100-200MB |
| 图片OCR | 500KB | 5-15秒 | 200-400MB |
| 大文件PDF | 100MB | 30-60秒 | 500-800MB |

### 性能优化建议

- **小文件**: 使用默认设置即可
- **大文件**: 使用 `--large-file` 和 `--optimize-memory`
- **批量处理**: 使用 `--parallel` 和合适的线程数
- **OCR处理**: 确保Tesseract正确配置

## 🎯 实际应用案例

### 案例1: 文档归档

```bash
# 将Office文档转换为Markdown归档
java -jar markitdown-java.jar archives/*.{docx,xlsx,pptx} -o markdown-archive/ --batch
```

### 案例2: 数据提取

```bash
# 从PDF表格中提取数据
java -jar markitdown-java.jar financial-report.pdf --include-tables -o tables.md
```

### 案例3: 多语言文档处理

```bash
# 处理多语言文档
java -jar markitdown-java.jar multilang.pdf --ocr -l auto -o result.md
```

### 案例4: 自动化工作流

```bash
# 定时批量转换
# 添加到crontab (Linux/macOS)
0 2 * * * cd /path/to/documents && java -jar markitdown-java.jar *.pdf -o converted/ --batch
```

## 📞 技术支持

### 获取帮助

```bash
# 显示帮助信息
java -jar markitdown-java.jar --help

# 显示使用示例
java -jar markitdown-java.jar --examples
```

### 日志调试

```bash
# 详细日志模式
java -jar markitdown-java.jar document.pdf --verbose

# 保存日志到文件
java -jar markitdown-java.jar document.pdf --verbose > conversion.log 2>&1
```

### 版本信息

```bash
# 查看版本
java -jar markitdown-java.jar --version

# 输出示例:
# MarkItDown-java 2.1.0
```

## 📖 附录

### A. 完整命令选项

```bash
用法: markitdown [OPTIONS] INPUT_FILES...

主要选项:
  -o, --output <FILE>           输出文件或目录
  -f, --format <FORMAT>         输出格式 (markdown, plain, json)
  -v, --verbose                 详细输出
  -q, --quiet                   静默模式
  -h, --help                    显示帮助

内容选项:
  --include-images              包含图片
  --no-images                   不包含图片
  --include-tables              包含表格
  --no-tables                   不包含表格
  --include-metadata            包含元数据
  --no-metadata                 不包含元数据

OCR选项:
  --ocr                         启用OCR
  -l, --language <LANG>         OCR语言 (eng, chi_sim, auto等)

性能选项:
  -p, --parallel                并行处理
  --threads <NUM>               线程数量
  --large-file                  允许大文件
  --optimize-memory             内存优化
  --stats                       显示统计信息

配置选项:
  --generate-config             生成配置文件
  --validate-config             验证配置文件
  --show-config                 显示当前配置
```

### B. 支持的语言代码

```
eng        英语
chi_sim    简体中文
chi_tra    繁体中文
jpn        日语
kor        韩语
fra        法语
deu        德语
spa        西班牙语
rus        俄语
ara        阿拉伯语
auto       自动检测
```

### C. 退出代码

```
0  成功
1  转换失败
2  文件不存在
3  不支持的格式
4  配置错误
```

---

**版本**: 2.1.0
**最后更新**: 2026-03-22
**官方文档**: [MarkItDown GitHub](https://github.com/microsoft/markitdown)