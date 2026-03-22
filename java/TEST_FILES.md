# MarkItDown-Java 测试文件说明

## 测试文件目录结构

```
test/
├── pdf/                  # PDF 文档测试文件
├── docx/                 # Word 文档测试文件
├── pptx/                # PowerPoint 演示文稿测试文件
├── xlsx/                # Excel 表格测试文件
├── html/                # HTML 网页测试文件
├── image/               # 图片测试文件
├── audio/               # 音频测试文件
├── zip/                 # 压缩包测试文件
└── large/               # 大文件测试文件

test-target/             # 测试输出目录（自动生成）
```

## 完整测试文件清单

| 序号 | 文件路径 | 文件类型 | 测试分类 | 测试命令 | 验证标准 |
|-----|---------|---------|----------|----------|----------|
| 1 | `test/pdf/plain-text.pdf` | 纯文本PDF | PDF-基础功能 | `java -jar target/*.jar test/pdf/plain-text.pdf -o test-target/pdf/test1.pdf.md` | 包含"Test Document"文字 |
| 2 | `test/pdf/scanned.pdf` | 扫描PDF | PDF-OCR功能 | `java -jar target/*.jar test/pdf/scanned.pdf --ocr -o test-target/pdf/test2.pdf.md` | 包含识别的文字内容 |
| 3 | `test/pdf/large-file.pdf` | 大文件PDF | PDF-大文件 | `java -jar target/*.jar test/pdf/large-file.pdf --large-file -o test-target/pdf/test3.pdf.md` | 成功处理大文件 |
| 4 | `test/large/huge-pdf.pdf` | 超大PDF | PDF-性能测试 | `java -jar target/*.jar test/large/huge-pdf.pdf --large-file --stats -o test-target/pdf/test4.pdf.md` | 处理时间<30秒 |
| 5 | `test/docx/basic.docx` | 基础文档 | Word-基础功能 | `java -jar target/*.jar test/docx/basic.docx -o test-target/docx/test5.docx.md` | 包含"Basic DOCX Test" |
| 6 | `test/docx/with-images.docx` | 包含图片 | Word-图片提取 | `java -jar target/*.jar test/docx/with-images.docx -o test-target/docx/test6.docx.md` | 包含`!\[.*\](assets/)`引用 |
| 7 | `test/docx/with-tables.docx` | 包含表格 | Word-表格转换 | `java -jar target/*.jar test/docx/with-tables.docx -o test-target/docx/test7.docx.md` | 包含表格结构 |
| 8 | `test/docx/with-styles.docx` | 格式化文本 | Word-样式保留 | `java -jar target/*.jar test/docx/with-styles.docx -o test-target/docx/test8.docx.md` | 保留格式信息 |
| 9 | `test/docx/with-headers-footers.docx` | 页眉页脚 | Word-复杂结构 | `java -jar target/*.jar test/docx/with-headers-footers.docx -o test-target/docx/test9.docx.md` | 包含页眉页脚信息 |
| 10 | `test/docx/with-charts.docx` | 包含图表 | Word-图表处理 | `java -jar target/*.jar test/docx/with-charts.docx -o test-target/docx/test10.docx.md` | 处理图表元素 |
| 11 | `test/xlsx/basic.xlsx` | 基础表格 | Excel-基础功能 | `java -jar target/*.jar test/xlsx/basic.xlsx -o test-target/xlsx/test11.xlsx.md` | 包含"Employees"表格 |
| 12 | `test/xlsx/large-dataset.xlsx` | 大数据集 | Excel-性能测试 | `java -jar target/*.jar test/xlsx/large-dataset.xlsx --stats -o test-target/xlsx/test12.xlsx.md` | 处理时间<5秒 |
| 13 | `test/large/huge-xlsx.xlsx` | 超大Excel | Excel-内存测试 | `java -jar target/*.jar test/large/huge-xlsx.xlsx --large-file --stats -o test-target/xlsx/test13.xlsx.md` | 内存使用<500MB |
| 14 | `test/image/with-text.png` | 英文文字 | 图片-英文OCR | `java -jar target/*.jar test/image/with-text.png --ocr -o test-target/image/test14.png.md` | 包含"MARKER_12345" |
| 15 | `test/image/with-text-chinese.png` | 中文文字 | 图片-中文OCR | `java -jar target/*.jar test/image/with-text-chinese.png --ocr -l chi_sim -o test-target/image/test15.png.md` | 包含中文字符 |
| 16 | `test/image/screenshot.png` | 界面截图 | 图片-复杂布局 | `java -jar target/*.jar test/image/screenshot.png --ocr -o test-target/image/test16.png.md` | 识别界面文字 |
| 17 | `test/image/sample.jpg` | JPEG格式 | 图片-格式兼容 | `java -jar target/*.jar test/image/sample.jpg --ocr -o test-target/image/test17.jpg.md` | 成功处理JPEG |
| 18 | `test/image/sample.gif` | GIF格式 | 图片-格式兼容 | `java -jar target/*.jar test/image/sample.gif --ocr -o test-target/image/test18.gif.md` | 成功处理GIF |
| 19 | `test/image/sample.bmp` | BMP格式 | 图片-格式兼容 | `java -jar target/*.jar test/image/sample.bmp --ocr -o test-target/image/test19.bmp.md` | 成功处理BMP |
| 20 | `test/image/with-exif.jpg` | EXIF元数据 | 图片-元数据 | `java -jar target/*.jar test/image/with-exif.jpg --ocr -o test-target/image/test20.jpg.md` | 显示EXIF信息 |
| 21 | `test/html/basic.html` | 基础网页 | HTML-基础功能 | `java -jar target/*.jar test/html/basic.html -o test-target/html/test21.html.md` | 转换为Markdown |
| 22 | `test/html/with-tables.html` | 包含表格 | HTML-表格转换 | `java -jar target/*.jar test/html/with-tables.html -o test-target/html/test22.html.md` | 包含表格结构 |
| 23 | `test/html/with-images.html` | 包含图片 | HTML-图片处理 | `java -jar target/*.jar test/html/with-images.html -o test-target/html/test23.html.md` | 处理图片引用 |
| 24 | `test/html/complex.html` | 复杂网页 | HTML-复杂结构 | `java -jar target/*.jar test/html/complex.html -o test-target/html/test24.html.md` | 处理嵌套结构 |
| 25 | `test/audio/speech.wav` | 语音录音 | 音频-语音转录 | `java -jar target/*.jar test/audio/speech.wav -o test-target/audio/test25.wav.md` | 提取音频信息 |
| 26 | `test/audio/music.mp3` | 音乐文件 | 音频-元数据 | `java -jar target/*.jar test/audio/music.mp3 -o test-target/audio/test26.mp3.md` | 显示元数据 |
| 27 | `test/zip/documents.zip` | 文档集合 | 压缩包-递归处理 | `java -jar target/*.jar test/zip/documents.zip -o test-target/zip/test27.zip.md` | 处理压缩内容 |
| 28 | `test/zip/nested.zip` | 嵌套压缩包 | 压缩包-嵌套处理 | `java -jar target/*.jar test/zip/nested.zip -o test-target/zip/test28.zip.md` | 处理嵌套结构 |

## 内容控制测试

| 序号 | 测试项 | 测试命令 | 验证标准 |
|-----|-------|----------|----------|
| 29 | 排除元数据 | `java -jar target/*.jar test/pdf/plain-text.pdf --no-metadata -o test-target/control/test29.pdf.md` | 不包含"Document Information" |
| 30 | 排除图片 | `java -jar target/*.jar test/docx/with-images.docx --no-images -o test-target/control/test30.docx.md` | 不包含图片引用 |
| 31 | 排除表格 | `java -jar target/*.jar test/xlsx/basic.xlsx --no-tables -o test-target/control/test31.xlsx.md` | 显示"表格功能在转换选项中被禁用" |
| 32 | 组合选项测试 | `java -jar target/*.jar test/xlsx/basic.xlsx --no-metadata --no-tables -o test-target/control/test32.xlsx.md` | 既无元数据也无表格 |

## OCR功能测试

| 序号 | 测试项 | 测试命令 | 验证标准 |
|-----|-------|----------|----------|
| 33 | 英文图片OCR | `java -jar target/*.jar test/image/with-text.png --ocr -l eng -o test-target/ocr/test33.png.md` | 识别英文文字准确率≥95% |
| 34 | 中文图片OCR | `java -jar target/*.jar test/image/with-text-chinese.png --ocr -l chi_sim -o test-target/ocr/test34.png.md` | 识别中文文字准确率≥90% |
| 35 | 扫描PDF OCR | `java -jar target/*.jar test/pdf/scanned.pdf --ocr -o test-target/ocr/test35.pdf.md` | 识别扫描页面内容 |
| 36 | 自动语言检测 | `java -jar target/*.jar test/image/screenshot.png --ocr -l auto -o test-target/ocr/test36.png.md` | 自动检测并识别 |
| 37 | JPEG格式OCR | `java -jar target/*.jar test/image/sample.jpg --ocr -o test-target/ocr/test37.jpg.md` | JPEG图片OCR正常 |
| 38 | 多格式图片OCR | `for img in test/image/*.png test/image/*.jpg; do java -jar target/*.jar "$img" --ocr -o test-target/ocr/"$(basename "$img").md"; done` | 批量处理多格式 |

## 性能测试

| 序号 | 测试项 | 测试命令 | 验证标准 |
|-----|-------|----------|----------|
| 39 | 并行处理性能 | `time java -jar target/*.jar test/pdf/*.pdf --parallel -o test-target/perf/serial/` vs `time java -jar target/*.jar test/pdf/*.pdf --parallel -o test-target/perf/parallel/` | 并行处理显著快于串行 |
| 40 | 内存使用测试 | `java -Xmx2g -jar target/*.jar test/large/huge-pdf.pdf --large-file -o test-target/perf/memory.pdf.md` | 内存使用不超限 |
| 41 | 大文件吞吐量 | `java -jar target/*.jar test/xlsx/large-dataset.xlsx --stats -o test-target/perf/throughput.xlsx.md` | 吞吐量>1MB/s |
| 42 | 批量处理稳定性 | `java -jar target/*.jar test/pdf/*.pdf --parallel --threads=8 -o test-target/perf/batch/` | 稳定处理无崩溃 |

