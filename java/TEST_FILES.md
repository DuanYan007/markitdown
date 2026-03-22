# MarkItDown Java 测试文件清单

## 📋 测试状态说明

```
✅  - 测试成功
❌  - 测试失败
⏳  - 待测试/未测试
```

## 📊 测试文件清单

| 序号 | 类别 | 文件名 | 测试目的 | 大小要求 | 是否测试 |
|------|------|--------|----------|----------|----------|
| 1 | PDF | `plain-text.pdf` | 基础PDF文本提取 | <100KB | ⏳ |
| 2 | PDF | `plain-text-chinese.pdf` | 中文PDF编码测试 | <100KB | ⏳ |
| 3 | PDF | `scanned.pdf` | 扫描PDF OCR测试 | <2MB | ⏳ |
| 4 | PDF | `encrypted.pdf` | 加密PDF处理 (密码: test123) | <100KB | ⏳ |
| 5 | PDF | `large-file.pdf` | 大文件PDF (>50MB) | >50MB | ⏳ |
| 6 | PDF | `multi-page.pdf` | 多页PDF处理 | <1MB | ⏳ |
| 7 | PDF | `with-tables.pdf` | 包含表格的PDF | <200KB | ⏳ |
| 8 | PDF | `with-images.pdf` | 嵌入图片的PDF | <500KB | ⏳ |
| 9 | PDF | `huge-pdf.pdf` | 超大PDF性能测试 | >100MB | ⏳ |
| 10 | PDF | `multi-column.pdf` | 多栏布局PDF | <500KB | ⏳ |
| 11 | PDF | `with-headers-footers.pdf` | 页眉页脚PDF | <200KB | ⏳ |
| 12 | PDF | `with-bookmarks.pdf` | 书签导航PDF | <500KB | ⏳ |
| 13 | PDF | `with-annotations.pdf` | 批注PDF | <500KB | ⏳ |
| 14 | PDF | `with-forms.pdf` | 表单字段PDF | <300KB | ⏳ |
| 15 | PDF | `mixed-languages.pdf` | 中英混合PDF | <200KB | ⏳ |
| 16 | PDF | `complex-layout.pdf` | 复杂排版PDF | <1MB | ⏳ |
| 17 | PDF | `password-protected.pdf` | 密码保护PDF (密码: test123) | <200KB | ⏳ |
| 18 | PDF | `corrupted.pdf` | 损坏PDF错误处理 | <100KB | ⏳ |
| 19 | PDF | `with-toc.pdf` | 包含目录的PDF | <500KB | ⏳ |
| 20 | Word | `basic.docx` | 基础Word文档转换 | <50KB | ⏳ |
| 21 | Word | `old-format.doc` | 旧格式DOC兼容性 | <100KB | ⏳ |
| 22 | Word | `with-images.docx` | 包含图片的Word | <1MB | ⏳ |
| 23 | Word | `with-tables.docx` | 包含表格的Word | <100KB | ⏳ |
| 24 | Word | `with-styles.docx` | 格式样式Word | <100KB | ⏳ |
| 25 | Word | `with-charts.docx` | 包含图表的Word | <500KB | ⏳ |
| 26 | Word | `with-headers-footers.docx` | 页眉页脚Word | <100KB | ⏳ |
| 27 | Word | `huge-docx.docx` | 超大Word文档性能测试 | >50MB | ⏳ |
| 28 | Word | `with-macros.docx` | 包含宏命令的Word | <200KB | ⏳ |
| 29 | Word | `multi-level-lists.docx` | 多级列表Word | <100KB | ⏳ |
| 30 | Word | `tracked-changes.docx` | 修订痕迹Word | <200KB | ⏳ |
| 31 | Word | `comments.docx` | 批注内容Word | <200KB | ⏳ |
| 32 | Word | `sections.docx` | 分节文档Word | <300KB | ⏳ |
| 33 | Word | `table-of-contents.docx` | 目录结构Word | <200KB | ⏳ |
| 34 | Word | `footnotes-endnotes.docx` | 脚注尾注Word | <300KB | ⏳ |
| 35 | Word | `protected.docx` | 只读保护Word | <100KB | ⏳ |
| 36 | Excel | `basic.xlsx` | 基础Excel表格转换 | <50KB | ⏳ |
| 37 | Excel | `old-format.xls` | 旧格式XLS兼容性 | <100KB | ⏳ |
| 38 | Excel | `with-formulas.xlsx` | 公式计算Excel | <50KB | ⏳ |
| 39 | Excel | `multi-sheet.xlsx` | 多工作表Excel | <100KB | ⏳ |
| 40 | Excel | `large-dataset.xlsx` | 大数据集Excel (10K+行) | <5MB | ⏳ |
| 41 | Excel | `huge-xlsx.xlsx` | 超大Excel性能测试 | >20MB | ⏳ |
| 42 | Excel | `with-pivot-tables.xlsx` | 数据透视表Excel | <500KB | ⏳ |
| 43 | Excel | `with-conditional-format.xlsx` | 条件格式Excel | <200KB | ⏳ |
| 44 | Excel | `with-images.xlsx` | 嵌入图片Excel | <1MB | ⏳ |
| 45 | Excel | `data-validation.xlsx` | 数据验证Excel | <100KB | ⏳ |
| 46 | Excel | `merged-cells.xlsx` | 合并单元格Excel | <100KB | ⏳ |
| 47 | PowerPoint | `basic.pptx` | 基础幻灯片转换 | <500KB | ⏳ |
| 48 | PowerPoint | `old-format.ppt` | 旧格式PPT兼容性 | <1MB | ⏳ |
| 49 | PowerPoint | `multi-slide.pptx` | 多页幻灯片 | <2MB | ⏳ |
| 50 | PowerPoint | `with-images.pptx` | 图片幻灯片 | <5MB | ⏳ |
| 51 | PowerPoint | `with-tables.pptx` | 表格幻灯片 | <1MB | ⏳ |
| 52 | PowerPoint | `with-charts.pptx` | 图表幻灯片 | <1MB | ⏳ |
| 53 | PowerPoint | `with-notes.pptx` | 演讲者备注PPT | <500KB | ⏳ |
| 54 | PowerPoint | `master-slide.pptx` | 母版幻灯片PPT | <1MB | ⏳ |
| 55 | PowerPoint | `animations.pptx` | 动画效果PPT | <2MB | ⏳ |
| 56 | PowerPoint | `multiple-authors.pptx` | 多作者协作PPT | <1MB | ⏳ |
| 57 | PowerPoint | `with-media.pptx` | 嵌入音视频PPT | <10MB | ⏳ |
| 58 | 图片 | `sample.png` | PNG格式图片转换 | <500KB | ⏳ |
| 59 | 图片 | `sample.jpg` | JPEG格式图片转换 | <500KB | ⏳ |
| 60 | 图片 | `sample.bmp` | BMP格式图片转换 | <2MB | ⏳ |
| 61 | 图片 | `sample.gif` | GIF格式图片转换 | <1MB | ⏳ |
| 62 | 图片 | `screenshot.png` | 截图OCR测试 | <1MB | ⏳ |
| 63 | 图片 | `with-text.png` | 英文OCR图片 | <500KB | ⏳ |
| 64 | 图片 | `with-text-chinese.png` | 中文OCR图片 | <500KB | ⏳ |
| 65 | 图片 | `with-exif.jpg` | EXIF信息提取 | <500KB | ⏳ |
| 66 | 图片 | `grayscale.png` | 灰度图像测试 | <500KB | ⏳ |
| 67 | 图片 | `transparent.png` | 透明背景测试 | <500KB | ⏳ |
| 68 | 图片 | `high-resolution.tiff` | 高分辨率TIFF测试 | <5MB | ⏳ |
| 69 | 图片 | `animated.gif` | 动画GIF测试 | <1MB | ⏳ |
| 70 | 音频 | `sample.mp3` | MP3格式音频转换 | <2MB | ⏳ |
| 71 | 音频 | `sample.wav` | WAV格式音频转换 | <5MB | ⏳ |
| 72 | 音频 | `sample.flac` | FLAC无损音频转换 | <5MB | ⏳ |
| 73 | 音频 | `speech-english.mp3` | 英文语音识别 | <3MB | ⏳ |
| 74 | 音频 | `speech-chinese.mp3` | 中文语音识别 | <3MB | ⏳ |
| 75 | 音频 | `music.mp3` | 音乐文件元数据提取 | <5MB | ⏳ |
| 76 | HTML | `basic.html` | 基础HTML转换 | <10KB | ⏳ |
| 77 | HTML | `html5-semantic.html` | HTML5语义化标签 | <20KB | ⏳ |
| 78 | HTML | `with-css.html` | 包含CSS样式 | <20KB | ⏳ |
| 79 | HTML | `with-images.html` | 包含图片标签 | <50KB | ⏳ |
| 80 | HTML | `with-links.html` | 包含链接标签 | <10KB | ⏳ |
| 81 | HTML | `with-tables.html` | 包含表格标签 | <20KB | ⏳ |
| 82 | 文本 | `basic.txt` | 纯文本转换 | <10KB | ⏳ |
| 83 | 文本 | `basic.json` | JSON格式转换 | <10KB | ⏳ |
| 84 | 文本 | `basic.xml` | XML格式转换 | <10KB | ⏳ |
| 85 | 文本 | `basic.csv` | CSV格式转换 | <10KB | ⏳ |
| 86 | 文本 | `with-namespace.xml` | 带命名空间XML | <10KB | ⏳ |
| 87 | 文本 | `with-quotes.csv` | 包含引号的CSV | <10KB | ⏳ |
| 88 | 文本 | `array.json` | JSON数组处理 | <50KB | ⏳ |
| 89 | 文本 | `large.csv` | 大CSV文件性能 | <1MB | ⏳ |
| 90 | EPUB | `basic.epub` | 基础EPUB电子书 | <500KB | ⏳ |
| 91 | EPUB | `complex.epub` | 复杂排版EPUB | <5MB | ⏳ |
| 92 | EPUB | `with-images.epub` | 包含图片EPUB | <2MB | ⏳ |
| 93 | EPUB | `with-drm.epub` | DRM保护EPUB | <1MB | ⏳ |
| 94 | Outlook | `basic-email.msg` | 基础邮件转换 | <50KB | ⏳ |
| 95 | Outlook | `html-email.msg` | HTML格式邮件 | <100KB | ⏳ |
| 96 | Outlook | `with-attachments.msg` | 包含附件的邮件 | <1MB | ⏳ |
| 97 | Outlook | `encrypted-email.msg` | 加密邮件测试 | <100KB | ⏳ |
| 98 | ZIP | `only-pdfs.zip` | 仅PDF压缩包测试 | <5MB | ⏳ |
| 99 | ZIP | `mixed-documents.zip` | 混合文档压缩包 | <2MB | ⏳ |
| 100 | ZIP | `nested.zip` | 嵌套压缩包测试 | <2MB | ⏳ |
| 101 | ZIP | `large-archive.zip` | 大压缩包性能测试 | <50MB | ⏳ |
| 102 | 性能 | `1000-pages.docx` | 超长Word文档性能 | >10MB | ⏳ |
| 103 | 性能 | `10000-rows.xlsx` | 超大Excel表格性能 | >5MB | ⏳ |
| 104 | 性能 | `500-slides.pptx` | 超多幻灯片性能 | >10MB | ⏳ |
| 105 | 性能 | `100-pages.pdf` | 100页PDF性能测试 | <10MB | ⏳ |
| 106 | 性能 | `complex-nested.zip` | 多层嵌套压缩包性能 | >10MB | ⏳ |
| 107 | 性能 | `mixed-formats.zip` | 混合格式大压缩包性能 | >50MB | ⏳ |
| 108 | 性能 | `1000-images.zip` | 1000张图片压缩包性能 | >50MB | ⏳ |
| 109 | 边界 | `empty.pdf` | 空PDF文件错误处理 | <10KB | ⏳ |
| 110 | 边界 | `empty.docx` | 空Word文件错误处理 | <10KB | ⏳ |
| 111 | 边界 | `empty.xlsx` | 空Excel文件错误处理 | <10KB | ⏳ |
| 112 | 边界 | `unicode-only.pdf` | 纯Unicode字符PDF | <100KB | ⏳ |
| 113 | 边界 | `right-to-left.docx` | 阿拉伯文/希伯来文Word | <200KB | ⏳ |
| 114 | 边界 | `emoji-heavy.pptx` | 大量emoji测试 | <500KB | ⏳ |
| 115 | 边界 | `invalid-header.pdf` | 无效文件头错误处理 | <10KB | ⏳ |
| 116 | 边界 | `truncated.docx` | 截断文件错误处理 | <50KB | ⏳ |
| 117 | 边界 | `wrong-extension.txt` | 错误扩展名测试 | <10KB | ⏳ |
| 118 | 边界 | `special-chars.txt` | 特殊字符文本文件 | <10KB | ⏳ |
| 119 | 边界 | `zero-byte.pdf` | 零字节PDF错误处理 | <1KB | ⏳ |

## 📊 测试统计

### 按类别统计
| 类别 | 总数 | 已完成 | 待测试 | 成功率 |
|------|------|--------|--------|--------|
| PDF | 19 | 0 | 19 | 0% |
| Word | 16 | 0 | 16 | 0% |
| Excel | 11 | 0 | 11 | 0% |
| PowerPoint | 11 | 0 | 11 | 0% |
| 图片 | 12 | 0 | 12 | 0% |
| 音频 | 6 | 0 | 6 | 0% |
| HTML | 6 | 0 | 6 | 0% |
| 文本 | 8 | 0 | 8 | 0% |
| EPUB | 4 | 0 | 4 | 0% |
| Outlook | 4 | 0 | 4 | 0% |
| ZIP | 4 | 0 | 4 | 0% |
| 性能 | 7 | 0 | 7 | 0% |
| 边界 | 11 | 0 | 11 | 0% |
| **总计** | **119** | **0** | **119** | **0%** |

### 快速统计命令
```bash
# 统计测试成功数量
grep "✅" TEST_FILES.md | wc -l

# 统计测试失败数量
grep "❌" TEST_FILES.md | wc -l

# 统计待测试数量
grep "⏳" TEST_FILES.md | wc -l
```

## 🧪 测试使用方法

### 基础测试
```bash
# 转换单个文件
java -jar markitdown-java.jar plain-text.pdf

# 测试成功后，在表格中将 ⏳ 改为 ✅
```

### 批量测试
```bash
# 测试所有PDF文件
java -jar markitdown-java.jar *.pdf --batch

# 测试所有Word文件
java -jar markitdown-java.jar *.docx --batch
```

### 功能测试
```bash
# OCR测试
java -jar markitdown-java.jar scanned.pdf --ocr

# 加密PDF测试
java -jar markitdown-java.jar encrypted.pdf --pdf-password test123

# 大文件测试
java -jar markitdown-java.jar huge-pdf.pdf --large-file
```

---

**最后更新**: 2026-03-22
**总测试项**: 119个
**测试进度**: 开始测试