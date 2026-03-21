# MarkItDown Java 测试文件收集清单

## 📁 目录结构

```
test/
├── pdf/                    # PDF 测试文件
├── docx/                   # Word 测试文件
├── pptx/                   # PowerPoint 测试文件
├── xlsx/                   # Excel 测试文件
├── image/                  # 图片文件
├── audio/                  # 音频文件
├── text/                   # 文本格式 (CSV, JSON, XML, TXT)
├── html/                   # HTML 文件
├── zip/                    # 压缩文件
├── epub/                   # 电子书
├── outlook/                # Outlook 邮件
└── large/                  # 大文件测试
```

---

## 一、PDF 测试文件（8 个）

| 序号 | 文件名 | 大小要求 | 内容要求 | 测试目的 |
|------|--------|---------|---------|---------|
| 1 | `plain-text.pdf` | < 100KB | 纯英文文本，包含标题、段落、列表项 | 基础文本提取 |
| 2 | `plain-text-chinese.pdf` | < 100KB | 纯中文文本，包含标题、段落 | 中文编码测试 |
| 3 | `with-tables.pdf` | < 200KB | 包含 2-3 个表格，表头清晰 | 表格转换测试 |
| 4 | `with-images.pdf` | < 500KB | 包含 2-3 张嵌入式图片 | 图片提取测试 |
| 5 | `multi-page.pdf` | < 1MB | 10 页以上，每页有不同内容 | 分页处理测试 |
| 6 | `scanned.pdf` | < 2MB | 扫描版 PDF（图片格式文字） | OCR 测试 |
| 7 | `encrypted.pdf` | < 100KB | 密码保护的 PDF（密码：test123） | 加密文件处理 |
| 8 | `large-file.pdf` | > 50MB | 100+ 页，包含大量文本和图片 | 大文件内存测试 |

**内容示例（plain-text.pdf）：**

```
# Test Document

This is a sample PDF file for testing markitdown conversion.

## Features to Include:
- Multiple paragraphs
- Different text formatting (bold, italic)
- List items (numbered and bulleted)
- Expected Text: MARKER_12345 (for assertion)
```

---

## 二、Word 测试文件（7 个）

| 序号 | 文件名 | 大小要求 | 内容要求 | 测试目的 |
|------|--------|---------|---------|---------|
| 1 | `basic.docx` | < 50KB | 基础文档：标题、段落、列表 | 基础转换 |
| 2 | `with-styles.docx` | < 100KB | 粗体、斜体、下划线、删除线、颜色 | 样式转换 |
| 3 | `with-tables.docx` | < 100KB | 3 个以上表格，含合并单元格 | 表格处理 |
| 4 | `with-images.docx` | < 1MB | 嵌入 3 张以上图片 | 图片处理 |
| 5 | `with-charts.docx` | < 500KB | 包含图表（柱状图、饼图） | 图表描述 |
| 6 | `with-headers-footers.docx` | < 100KB | 包含页眉、页脚、页码 | 元数据提取 |
| 7 | `old-format.doc` | < 100KB | Word 97-2003 格式 | 旧格式兼容 |

**内容示例（with-styles.docx）：**

```
# Style Test Document

**Bold text example**
*Italic text example*
***Bold and italic***
~~Strikethrough text~~
_Underlined text_

Combined: **bold** and *italic* in same paragraph.
```

---

## 三、Excel 测试文件（5 个）

| 序号 | 文件名 | 大小要求 | 内容要求 | 测试目的 |
|------|--------|---------|---------|---------|
| 1 | `basic.xlsx` | < 50KB | 单工作表，3 列 10 行数据 | 基础表格转换 |
| 2 | `multi-sheet.xlsx` | < 100KB | 3 个工作表，每个不同内容 | 多工作表处理 |
| 3 | `with-formulas.xlsx` | < 50KB | 包含 SUM、AVERAGE、IF 公式 | 公式计算 |
| 4 | `large-dataset.xlsx` | < 5MB | 10000+ 行数据 | 大数据性能 |
| 5 | `old-format.xls` | < 100KB | Excel 97-2003 格式 | 旧格式兼容 |

**内容示例（basic.xlsx）：**

| Name | Age | City | Salary |
|------|-----|------|--------|
| Alice | 28 | Beijing | 15000 |
| Bob | 35 | Shanghai | 18000 |
| Carol | 42 | Guangzhou | 22000 |

---

## 四、PowerPoint 测试文件（6 个）

| 序号 | 文件名 | 大小要求 | 内容要求 | 测试目的 |
|------|--------|---------|---------|---------|
| 1 | `basic.pptx` | < 500KB | 3 张幻灯片，标题+文本 | 基础转换 |
| 2 | `multi-slide.pptx` | < 2MB | 20 张幻灯片 | 多幻灯片处理 |
| 3 | `with-tables.pptx` | < 1MB | 包含表格的幻灯片 | 表格提取 |
| 4 | `with-images.pptx` | < 5MB | 包含多张图片 | 图片处理 |
| 5 | `with-charts.pptx` | < 1MB | 包含图表 | 图表描述 |
| 6 | `old-format.ppt` | < 1MB | PowerPoint 97-2003 格式 | 旧格式兼容 |

**内容示例（basic.pptx）：**

```
幻灯片 1: 标题 "Presentation Title"，副标题 "Subtitle"
幻灯片 2: 标题 "Agenda"，内容为项目符号列表
幻灯片 3: 标题 "Data"，内容为表格
```

---

## 五、图片测试文件（8 个）

| 序号 | 文件名 | 大小要求 | 内容要求 | 测试目的 |
|------|--------|---------|---------|---------|
| 1 | `sample.png` | < 500KB | PNG 格式，包含文字或图形 | PNG 转换 |
| 2 | `sample.jpg` | < 500KB | JPEG 格式，风景/人物照片 | JPEG 转换 |
| 3 | `sample.gif` | < 1MB | GIF 动图（多帧） | GIF 处理 |
| 4 | `sample.bmp` | < 2MB | BMP 格式图片 | BMP 转换 |
| 5 | `with-text.png` | < 500KB | 图片中包含清晰英文文字 | OCR 测试 |
| 6 | `with-text-chinese.png` | < 500KB | 图片中包含清晰中文文字 | 中文 OCR |
| 7 | `with-exif.jpg` | < 500KB | 包含 EXIF 元数据（相机信息） | EXIF 提取 |
| 8 | `screenshot.png` | < 1MB | 网页或软件截图 | 截图 OCR |

**内容要求（with-text.png）：**
- 白色背景
- 黑色文字
- 包含 3-5 行清晰文字
- 字体大小足够大（便于 OCR 识别）

---

## 六、音频测试文件（6 个）

| 序号 | 文件名 | 大小要求 | 内容要求 | 测试目的 |
|------|--------|---------|---------|---------|
| 1 | `sample.mp3` | < 2MB | MP3 格式，包含语音 | MP3 元数据 |
| 2 | `sample.wav` | < 5MB | WAV 格式，清晰语音 | WAV 处理 |
| 3 | `sample.flac` | < 5MB | FLAC 无损格式 | FLAC 支持 |
| 4 | `speech-english.mp3` | < 3MB | 英文语音（约 1 分钟） | 语音转写 |
| 5 | `speech-chinese.mp3` | < 3MB | 中文语音（约 1 分钟） | 中文转写 |
| 6 | `music.mp3` | < 5MB | 音乐文件 | 元数据提取 |

**内容要求：**
- 语音文件需要清晰、无背景噪音
- 包含完整的句子
- 音频质量 128kbps 以上

---

## 七、文本格式文件（8 个）

| 序号 | 文件名 | 大小要求 | 内容要求 | 测试目的 |
|------|--------|---------|---------|---------|
| 1 | `basic.csv` | < 10KB | 标准 CSV，逗号分隔 | CSV 转换 |
| 2 | `with-quotes.csv` | < 10KB | 包含引号和逗号的字段 | 特殊字符 |
| 3 | `large.csv` | < 1MB | 10000+ 行数据 | 大文件性能 |
| 4 | `basic.json` | < 10KB | 嵌套 JSON 对象 | JSON 转换 |
| 5 | `array.json` | < 50KB | JSON 数组 | 数组处理 |
| 6 | `basic.xml` | < 10KB | 标准 XML 文档 | XML 转换 |
| 7 | `with-namespace.xml` | < 10KB | 带 namespace 的 XML | Namespace 处理 |
| 8 | `basic.txt` | < 10KB | 纯文本，多段落 | 纯文本转换 |

**内容示例：**

`basic.csv`:
```csv
Name,Age,City,Email
Alice,28,Beijing,alice@example.com
Bob,35,Shanghai,bob@example.com
Carol,42,Guangzhou,carol@example.com
```

`basic.json`:
```json
{
  "name": "Test User",
  "age": 30,
  "city": "Beijing",
  "skills": ["Java", "Python", "JavaScript"],
  "address": {
    "street": "Main Street",
    "zip": "100000"
  }
}
```

`basic.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<root>
  <person>
    <name>John Doe</name>
    <age>30</age>
    <city>Beijing</city>
  </person>
  <person>
    <name>Jane Smith</name>
    <age>25</age>
    <city>Shanghai</city>
  </person>
</root>
```

---

## 八、HTML 测试文件（6 个）

| 序号 | 文件名 | 大小要求 | 内容要求 | 测试目的 |
|------|--------|---------|---------|---------|
| 1 | `basic.html` | < 10KB | 基础 HTML（h1-h6, p, ul, ol） | 基础 HTML |
| 2 | `with-tables.html` | < 20KB | 包含复杂表格 | 表格转换 |
| 3 | `with-links.html` | < 10KB | 包含多个链接 | 链接提取 |
| 4 | `with-images.html` | < 50KB | 包含 img 标签 | 图片处理 |
| 5 | `with-css.html` | < 20KB | 内联 CSS 样式 | 样式处理 |
| 6 | `html5-semantic.html` | < 20KB | HTML5 语义化标签 | 语义标签 |

**内容示例（basic.html）：**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>Test Page</title>
</head>
<body>
    <h1>Main Heading</h1>
    <h2>Subheading</h2>

    <p>This is a paragraph with <strong>bold</strong> and <em>italic</em> text.</p>

    <h3>Unordered List</h3>
    <ul>
        <li>Item 1</li>
        <li>Item 2</li>
        <li>Item 3</li>
    </ul>

    <h3>Ordered List</h3>
    <ol>
        <li>First item</li>
        <li>Second item</li>
        <li>Third item</li>
    </ol>

    <h3>Table</h3>
    <table>
        <thead>
            <tr>
                <th>Name</th>
                <th>Value</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>Test</td>
                <td>123</td>
            </tr>
            <tr>
                <td>Sample</td>
                <td>456</td>
            </tr>
        </tbody>
    </table>

    <p><a href="https://example.com">Example Link</a></p>
</body>
</html>
```

---

## 九、ZIP 压缩文件（4 个）

| 序号 | 文件名 | 大小要求 | 内容要求 | 测试目的 |
|------|--------|---------|---------|---------|
| 1 | `mixed-documents.zip` | < 2MB | 包含 docx、pdf、txt 各 1 个 | 混合格式 |
| 2 | `only-pdfs.zip` | < 5MB | 包含 5 个 PDF 文件 | 批量 PDF |
| 3 | `nested.zip` | < 2MB | 包含子目录和文件 | 嵌套结构 |
| 4 | `large-archive.zip` | < 50MB | 包含 50+ 文件 | 大量文件 |

---

## 十、EPub 电子书（3 个）

| 序号 | 文件名 | 大小要求 | 内容要求 | 测试目的 |
|------|--------|---------|---------|---------|
| 1 | `basic.epub` | < 500KB | 简单电子书，3 章 | 基础 EPub |
| 2 | `with-images.epub` | < 2MB | 包含插图的电子书 | 图片处理 |
| 3 | `complex.epub` | < 5MB | 多章节、复杂排版 | 复杂结构 |

---

## 十一、Outlook 邮件（3 个）

| 序号 | 文件名 | 大小要求 | 内容要求 | 测试目的 |
|------|--------|---------|---------|---------|
| 1 | `basic-email.msg` | < 50KB | 纯文本邮件 | 基础邮件 |
| 2 | `html-email.msg` | < 100KB | HTML 格式邮件 | HTML 邮件 |
| 3 | `with-attachments.msg` | < 1MB | 包含附件的邮件 | 附件提取 |

---

## 十二、大文件测试（3 个）

| 序号 | 文件名 | 大小要求 | 内容要求 | 测试目的 |
|------|--------|---------|---------|---------|
| 1 | `huge-pdf.pdf` | > 100MB | 500+ 页 PDF | 超大文件内存测试 |
| 2 | `huge-docx.docx` | > 50MB | 1000+ 页文档 | 大文档处理 |
| 3 | `huge-xlsx.xlsx` | > 20MB | 100000+ 行数据 | 大表格性能 |

---

## 📊 汇总统计

| 类别 | 数量 | 预估总大小 | 优先级 |
|------|------|-----------|--------|
| PDF | 8 | ~55MB | 高 |
| Word | 7 | ~2MB | 高 |
| Excel | 5 | ~5MB | 高 |
| PowerPoint | 6 | ~10MB | 高 |
| 图片 | 8 | ~5MB | 中 |
| 音频 | 6 | ~20MB | 中 |
| 文本格式 | 8 | ~1MB | 高 |
| HTML | 6 | ~0.1MB | 高 |
| ZIP | 4 | ~60MB | 中 |
| EPub | 3 | ~8MB | 低 |
| Outlook | 3 | ~1MB | 低 |
| 大文件 | 3 | ~170MB | 高 |
| **总计** | **67** | **~337MB** | - |

---

## 文件质量要求

### 通用要求

1. **文件完整性** - 所有文件必须能正常打开，无损坏
2. **内容可读性** - 文字清晰，无乱码
3. **格式规范** - 符合对应文件格式的标准
4. **无敏感信息** - 不包含真实个人信息、密码、公司机密等

### 特殊要求

**PDF 文件：**
- 使用标准 PDF 1.4+ 格式
- 不使用特殊的嵌入字体（除非测试字体嵌入）
- scanned.pdf 必须是 300dpi 以上的扫描件

**Office 文件：**
- 优先使用 Microsoft Office 生成
- 旧格式文件（.doc/.xls/.ppt）必须真实保存为对应格式

**图片文件：**
- 分辨率至少 1024x768
- OCR 测试图片文字清晰度要高
- EXIF 测试图片必须包含完整元数据（可用手机拍摄）

**音频文件：**
- 语音文件采样率至少 44.1kHz
- 语音清晰，信噪比高
- 背景噪音控制在最低

---

## 交付方式

请将所有文件按以下目录结构打包：

```
test-files-v1.0.zip
├── test/
│   ├── pdf/
│   ├── docx/
│   ├── pptx/
│   ├── xlsx/
│   ├── image/
│   ├── audio/
│   ├── text/
│   ├── html/
│   ├── zip/
│   ├── epub/
│   ├── outlook/
│   └── large/
└── README.md
```

---

## 验收标准

| 检查项 | 要求 |
|--------|------|
| 文件数量 | 67 个（覆盖所有必需测试） |
| 文件完整性 | 100% 可打开，无损坏 |
| 目录结构 | 符合上述规范 |
| 命名规范 | 文件名清晰、无特殊字符 |
| 内容质量 | 符合内容要求 |
| 大小要求 | 符合指定范围 |

---

## 联系方式

如有疑问，请联系项目负责人。

---

**文档版本**: v1.0
**创建日期**: 2026-03-21
