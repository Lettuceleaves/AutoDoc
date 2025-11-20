# AutoDoc - Java接口文档自动生成工具

[![Build Status](https://img.shields.io/badge/build-passing-green.svg)](https://github.com/yourusername/AutoDoc)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/yourusername/AutoDoc/blob/main/LICENSE)
[![Java](https://img.shields.io/badge/java-17%2B-orange.svg)](https://www.oracle.com/java/technologies/downloads/)

## 项目介绍

AutoDoc 是一款基于静态代码分析的 Java 接口文档自动生成工具，能够通过分析源代码自动提取接口信息、参数结构、返回值类型等内容，并生成结构化的接口文档。

### 核心功能

- **基于静态分析**：无需运行程序，直接分析源代码即可生成文档
- **支持复杂数据结构**：自动解析嵌套的参数类型和返回值类型
- **智能泛型处理**：支持泛型类型参数的正确解析和展示
- **调用链分析**：通过 BFS 算法分析接口调用链，展示完整的接口关系
- **多格式输出**：支持 Markdown 和 JSON 两种输出格式
- **枚举值提取**：自动提取并展示枚举类型的所有可能值
- **依赖注入分析**：支持分析 Spring 依赖注入关系

## 技术栈

- **开发语言**：Java 17+
- **构建工具**：Maven
- **核心库**：
  - JavaParser（代码解析和静态分析）
  - Spring Boot
  - SnakeYAML（配置文件处理）
  - Jackson（JSON处理）
- **辅助工具**：Lombok（简化代码）

## 系统架构

AutoDoc 采用模块化设计，主要包含以下核心模块：

### 1. 入口模块 (`entry`)
- `AutoDocStarter`: 程序的主入口，协调整个文档生成流程
- `DebugResolver`: 调试和解析器支持

### 2. 扫描模块 (`scanner`)
- `ScanControllers`: 扫描控制器类和接口方法
- `ScanFilePaths`: 扫描项目中的所有Java文件
- `ScanDI`: 分析依赖注入关系
- `ScanEnums`: 扫描并提取枚举类型信息
- `ScanUsagePoints`: 分析接口调用链（使用BFS算法）

### 3. 解析模块 (`parser`)
- `ParseInputParams`: 解析接口输入参数
- `ParseOutputParam`: 解析接口输出参数和返回值
- `ParseSingleController`: 解析单个控制器类
- `ParseSingleMethod`: 解析单个接口方法

### 4. 导出模块 (`export`)
- `JSON`: 将文档导出为JSON格式
- `MarkDown`: 将文档导出为Markdown格式

### 5. 工具模块 (`tool`)
- `ASTMap`: 维护抽象语法树的映射
- `ConfigMap`: 配置管理
- `GenericStack`: 泛型处理工具
- `SymbolSolver`: 符号解析器

## 快速开始

### 安装要求

- JDK 17 或更高版本
- Maven 3.6 或更高版本
- 支持Spring Boot项目的源码分析

### 构建项目

```bash
# 克隆仓库
git clone https://github.com/yourusername/AutoDoc.git
cd AutoDoc

# 构建项目
mvn clean install
```

### 使用方法

#### 1. 配置文件设置

在项目根目录创建或修改 `autodoc_config.yaml` 配置文件：

```yaml
# 项目源码路径
source_path: "path/to/your/project/src"

# 导出文档路径
export:
  markdown_path: "autodoc/AutoDoc.md"
  json_path: "autodoc/AutoDoc.json"

# 要扫描的包路径
package_paths:
  - "com.example.controller"
  - "com.example.api"

# 终止递归的类型
recursive_end_points:
  - "java.lang.String"
  - "java.lang.Integer"
  - "java.util.List"
```

#### 2. 运行程序

```bash
# 运行主类
java -cp target/scanner-0.0.1.jar com.letuc.app.entry.AutoDocStarter

# 或指定配置文件路径
java -cp target/scanner-0.0.1.jar com.letuc.app.entry.AutoDocStarter path/to/config
```

#### 3. 查看生成的文档

生成的文档将保存在配置文件指定的路径中：
- Markdown 格式: `autodoc/AutoDoc.md`
- JSON 格式: `autodoc/AutoDoc.json`

## 文档输出示例

### Markdown 输出格式

```markdown
# 接口文档: UserController

**基础路径 (Base URL):** `/user`

---

## 1. register (`POST /user/register`)

**完整方法签名:** `com.example.controller.UserController.register(com.example.model.RegisterRequestDTO)`

### 请求参数 (Request)

| 参数名 | 类型 | 位置 | 描述 |
| :--- | :--- | :--- | :--- |
| `dto` | `com.example.model.RegisterRequestDTO` | `BODY` | |
| &nbsp;&nbsp;&nbsp;↳ `username` | `String` | `BODY` | 用户名 |
| &nbsp;&nbsp;&nbsp;↳ `password` | `String` | `BODY` | 密码 |

### 响应内容 (Response)

**响应类型:** `com.example.model.ResultVO<com.example.model.UserDTO>`

| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `code` | `String` | 响应代码 |
| `message` | `String` | 响应消息 |
| `data` | `UserDTO` | 用户数据 |

## 使用示例

### 在 Spring Boot 项目中使用

1. 将 AutoDoc 作为依赖添加到你的项目中
2. 配置扫描路径和导出选项
3. 在项目构建或部署流程中自动生成文档

```xml
<dependency>
    <groupId>com.letuc.autodoc</groupId>
    <artifactId>scanner</artifactId>
    <version>0.0.1</version>
</dependency>
```

## 自定义和扩展

### 扩展解析器

你可以通过实现自定义解析器来支持更多的注解和特殊类型：

1. 创建新的解析器类
2. 扩展现有的解析逻辑
3. 在 `AutoDocStarter` 中集成你的自定义解析器

### 自定义输出格式

如果你需要其他输出格式，可以：

1. 在 `export` 包下创建新的导出类
2. 实现数据转换和文件保存逻辑
3. 在 `AutoDocStarter` 中调用你的导出方法

## 已知限制

- 不支持动态生成的接口
- 对于复杂的泛型嵌套，可能需要额外的配置
- 不支持非Spring框架的特殊注解解析

## 贡献指南

欢迎提交 Issue 和 Pull Request 来帮助改进项目！

### 提交代码前请确保：

1. 代码通过所有测试
2. 遵循项目的代码风格
3. 添加必要的文档和注释
4. 提供详细的变更说明

## 许可证

[MIT License](https://github.com/yourusername/AutoDoc/blob/main/LICENSE)

## 联系我们

- 作者: letuc
- 邮箱: 2878506229@qq.com

---

**AutoDoc** - 让Java接口文档生成变得简单高效！
