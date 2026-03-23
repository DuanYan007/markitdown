# MarkItDown Java 安装与配置指南

> 📦 markitdown4j.jar 完整安装配置教程

## 📋 系统要求

### 基础要求

- **Java**: JDK 17 或更高版本
- **操作系统**: Windows、Linux、macOS
- **内存**: 最小 512MB，推荐 2GB+
- **磁盘空间**: 最小 100MB 可用空间

### 可选组件

- **Tesseract OCR**: 用于图片文字识别功能
- **语言包**: 根据需要安装对应语言的 OCR 语言包

## 🚀 安装步骤

### 第一步：安装 Java

#### Windows

1. 下载 JDK 17+
   - 官方网站: https://www.oracle.com/java/technologies/downloads/
   - 或使用 OpenJDK: https://adoptium.net/

2. 运行安装程序，按提示完成安装

3. 配置环境变量
   ```bash
   # 设置 JAVA_HOME
   JAVA_HOME=C:\Program Files\Java\jdk-17

   # 添加到 PATH
   Path=%JAVA_HOME%\bin;%Path%
   ```

4. 验证安装
   ```bash
   java -version
   # 应显示: java version "17.x.x"
   ```

#### Linux

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install openjdk-17-jre

# CentOS/RHEL
sudo yum install java-17-openjdk

# 验证安装
java -version
```

#### macOS

```bash
# 使用 Homebrew
brew install openjdk@17

# 配置环境变量
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@17' >> ~/.zshrc
source ~/.zshrc

# 验证安装
java -version
```

### 第二步：获取 markitdown4j.jar

#### 方式一：下载预编译版本（推荐）

```bash
# 1. 下载最新版本 markitdown4j.jar
# 直接下载链接:
# https://github.com/DuanYan007/markitdown/releases/download/v0.0.2/markitdown4j.jar

# 2. 下载测试文件包（可选）
# https://github.com/DuanYan007/markitdown/releases/download/v0.0.2-test/test-files.zip

# 3. 将文件放置到您喜欢的目录，例如：
#    Windows: C:\Tools\markitdown4j.jar
#    Linux/macOS: ~/tools/markitdown4j.jar
```

访问 [GitHub Releases](https://github.com/DuanYan007/markitdown/releases) 查看所有版本。

#### 方式二：从源码构建（开发者）

```bash
# 1. 克隆项目
git clone https://github.com/DuanYan007/markitdown.git
cd markitdown/java

# 2. 构建项目
mvn clean package -DskipTests

# 3. 复制生成的 JAR 文件
cp target/markitdown4j.jar ~/tools/
```

### 第三步：验证安装

```bash
# 测试运行
java -jar markitdown4j.jar --version

# 应显示版本信息
# MarkItDown Java 2.1.1

# 查看帮助信息
java -jar markitdown4j.jar --help
```

## 🔧 OCR 配置（可选）

如果您需要使用图片文字识别功能，需要安装 Tesseract OCR。

### Windows 安装

1. **下载 Tesseract 安装包**
   - 访问: https://github.com/UB-Mannheim/tesseract/wiki
   - 下载最新版本的安装包（例如 `tesseract-ocr-w64-setup-5.x.x.exe`）

2. **运行安装程序**
   - 在安装组件选择界面，勾选 "Chinese (Simplified)" 和 "Chinese (Traditional)" 语言包
   - 完成安装

3. **下载中文语言包（如果安装时未勾选）**
   - 访问: https://github.com/tesseract-ocr/tessdata/raw/main/chi_sim.traineddata
     - 下载 `chi_sim.traineddata`（简体中文）
   - 访问: https://github.com/tesseract-ocr/tessdata/raw/main/chi_tra.traineddata
     - 下载 `chi_tra.traineddata`（繁体中文）

4. **放置语言包文件**
   - 将下载的 `.traineddata` 文件放置到 Tesseract 安装目录的 `tessdata` 文件夹中
   - 例如: `O:\tesserOCR\tessdata\chi_sim.traineddata`
   - tessdata 目录结构示例:
     ```
     O:\tesserOCR\
     ├── tesseract.exe
     └── tessdata\
         ├── eng.traineddata
         ├── chi_sim.traineddata
         └── chi_tra.traineddata
     ```

5. **配置 markitdown4j.jar**
   - 创建或编辑 `.markitdown.properties` 配置文件，指定 Tesseract 路径：
   ```properties
   # Tesseract 引擎路径
   tesseract.path=O:\\tesserOCR
   tessdata.path=O:\\tesserOCR\\tessdata
   ```

6. **验证配置**
   ```bash
   # 查看当前配置
   java -jar markitdown4j.jar --show-config

   # 测试 OCR 功能
   java -jar markitdown4j.jar image.png --ocr -l chi_sim -o result.md
   ```

### Linux 安装

```bash
# Ubuntu/Debian - 安装 Tesseract 和中文语言包
sudo apt-get update
sudo apt-get install tesseract-ocr
sudo apt-get install tesseract-ocr-chi-sim tesseract-ocr-chi-tra

# 验证安装
tesseract --version
tesseract --list-langs
# 应显示: eng, chi_sim, chi_tra 等

# 配置 markitdown4j.jar（如果自动检测失败）
# 编辑 .markitdown.properties:
# tesseract.path=/usr/bin
# tessdata.path=/usr/share/tesseract-ocr/4.00/tessdata
```

### macOS 安装

```bash
# 使用 Homebrew 安装 Tesseract 和所有语言包
brew install tesseract
brew install tesseract-lang

# 验证安装
tesseract --version
tesseract --list-langs
# 应显示: eng, chi_sim, chi_tra, jpn, kor 等

# 配置 markitdown4j.jar（如果自动检测失败）
# 编辑 .markitdown.properties:
# tesseract.path=/opt/homebrew/bin
# tessdata.path=/opt/homebrew/share/tessdata
```

## ⚙️ 高级配置

### 创建配置文件

创建 `.markitdown.properties` 配置文件以自定义默认行为：

```properties
# ===== 引擎路径配置 =====
# Windows 示例（请根据实际安装路径修改）
tesseract.path=O:\\tesserOCR
tessdata.path=O:\\tesserOCR\\tessdata

# Linux 示例
# tesseract.path=/usr/bin
# tessdata.path=/usr/share/tesseract-ocr/4.00/tessdata

# macOS 示例
# tesseract.path=/opt/homebrew/bin
# tessdata.path=/opt/homebrew/share/tessdata

# ===== 输出配置 =====
output.dir=./output
output.image.dir=assets

# ===== 内容选项 =====
content.include.images=true
content.include.tables=true
content.include.metadata=true

# ===== OCR 配置 =====
ocr.enable=false
ocr.language=auto

# ===== 性能配置 =====
performance.parallel=true
performance.threads=0
```

### 配置文件位置

配置文件可以放在以下位置（按优先级排序）：

1. **当前工作目录**: `.markitdown.properties`（推荐）
2. **用户主目录**: `~/.markitdown.properties`
3. **系统配置目录**: （高级用户）

### 使用配置文件

```bash
# 生成默认配置文件
java -jar markitdown4j.jar --generate-config

# 验证配置文件
java -jar markitdown4j.jar --validate-config

# 查看当前配置
java -jar markitdown4j.jar --show-config

# 使用配置文件转换
java -jar markitdown4j.jar document.pdf
```

## 🌍 配置优先级

配置项的优先级从高到低：

1. **命令行参数** (最高优先级)
   ```bash
   java -jar markitdown4j.jar doc.pdf --ocr -l chi_sim
   ```

2. **环境变量**
   ```bash
   export TESSERACT_PATH="/usr/bin"
   export MARKITDOWN_OUTPUT_DIR="./output"
   ```

3. **配置文件** (`.markitdown.properties`)
   ```properties
   ocr.enable=true
   ocr.language=chi_sim
   ```

4. **默认值** (最低优先级)

## 🎯 快速测试

安装完成后，运行以下命令测试各项功能：

### 基础功能测试

```bash
# 测试 PDF 转换
java -jar markitdown4j.jar test.pdf -o output.md

# 测试 Word 转换
java -jar markitdown4j.jar test.docx -o output.md

# 测试 Excel 转换
java -jar markitdown4j.jar test.xlsx -o output.md
```

### OCR 功能测试（需要安装 Tesseract）

```bash
# 测试图片 OCR
java -jar markitdown4j.jar image.png --ocr -o result.md

# 测试中文 OCR
java -jar markitdown4j.jar chinese-image.png --ocr -l chi_sim -o result.md
```

### 性能测试

```bash
# 测试并行处理
java -jar markitdown4j.jar *.pdf --parallel --threads 4 -o output/

# 测试大文件处理
java -jar markitdown4j.jar large.pdf --large-file --stats
```

## 🐛 常见安装问题

### 问题 1: Java 版本不兼容

**错误信息**: `UnsupportedClassVersionError`

**解决方案**:
```bash
# 检查 Java 版本
java -version

# 如果版本低于 17，请升级 Java
# Windows: 下载并安装 JDK 17+
# Linux: sudo apt-get install openjdk-17-jre
# macOS: brew install openjdk@17
```

### 问题 2: 找不到 Java 命令

**错误信息**: `'java' 不是内部或外部命令`

**解决方案**:
```bash
# 1. 确认 Java 已安装
# Windows: dir "C:\Program Files\Java"
# Linux: which java

# 2. 配置 JAVA_HOME 环境变量
# Windows:
#set JAVA_HOME=C:\Program Files\Java\jdk-17
#set PATH=%JAVA_HOME%\bin;%PATH%

# Linux/macOS:
#export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
#export PATH=$JAVA_HOME/bin:$PATH
```

### 问题 3: OCR 不工作

**错误信息**: `OCR功能不可用` 或 `找不到 Tesseract`

**解决方案**:

1. **确认 Tesseract 已安装**
   ```bash
   # Windows: 检查安装目录存在
   dir O:\tesserOCR\
   # 应看到: tesseract.exe 和 tessdata 文件夹

   # Linux/macOS
   tesseract --version
   ```

2. **确认语言包文件存在**
   ```bash
   # Windows
   dir O:\tesserOCR\tessdata\
   # 应看到: chi_sim.traineddata, chi_tra.traineddata 等

   # Linux
   ls /usr/share/tesseract-ocr/4.00/tessdata/
   ```

3. **配置 markitdown4j.jar 路径**
   - 编辑 `.markitdown.properties` 文件：
   ```properties
   # Windows
   tesseract.path=O:\\tesserOCR
   tessdata.path=O:\\tesserOCR\\tessdata

   # Linux
   tesseract.path=/usr/bin
   tessdata.path=/usr/share/tesseract-ocr/4.00/tessdata
   ```

4. **验证配置**
   ```bash
   # 查看当前配置
   java -jar markitdown4j.jar --show-config

   # 测试 OCR 功能
   java -jar markitdown4j.jar image.png --ocr -l chi_sim -o result.md
   ```

### 问题 4: 内存不足

**错误信息**: `java.lang.OutOfMemoryError`

**解决方案**:
```bash
# 增加 JVM 内存限制
java -Xmx2g -jar markitdown4j.jar large.pdf

# 或使用内存优化模式
java -jar markitdown4j.jar large.pdf --optimize-memory
```

### 问题 5: 中文乱码

**解决方案**:
```bash
# 指定正确的 OCR 语言
java -jar markitdown4j.jar chinese.pdf --ocr -l chi_sim

# 或使用自动检测
java -jar markitdown4j.jar chinese.pdf --ocr -l auto
```

## 📚 下一步

安装配置完成后，您可以：

1. 📖 阅读 [COMMAND_REFERENCE.md](COMMAND_REFERENCE.md) 了解所有命令参数
2. 🧪 查看 [TEST_FILES.md](TEST_FILES.md) 了解测试文件
3. 💡 参考 [README.md](README.md) 了解项目概况

## 🆘 获取帮助

- **命令行帮助**: `java -jar markitdown4j.jar --help`
- **使用示例**: `java -jar markitdown4j.jar --examples`
- **查看配置**: `java -jar markitdown4j.jar --show-config`

---

**版本**: 2.1.1 | **最后更新**: 2026-03-23
