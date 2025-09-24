# example 目录说明

本目录包含 JoySafety 项目的各类示例工程，便于用户参考和快速上手。每个子项目均为独立模块，涵盖了安全管理、API服务、技能扩展等典型场景。

## 目录结构

- safety-admin/  
  后台管理系统示例，包含配置文件（conf）、示例数据（data）等。

- safety-api/  
  API 服务示例，包含配置文件（conf）等。

- safety-skills/  
  技能扩展示例，包括：
  - safety-bert/：BERT 模型相关示例
  - safety-fasttext/：FastText 模型相关示例
  - safety-keywords/：关键词检测相关示例
  - safety-knowledge/：知识库相关示例

## 使用方法

1. 进入对应子项目目录，参考 conf/ 下的配置文件进行参数设置。
2. 可使用 data/ 下的 SQL 文件初始化数据库。
3. 各子项目均可独立运行，具体启动方式请参考各自目录下的 README 或脚本文件。

## 适用场景

- 快速体验 JoySafety 各模块功能
- 参考配置与数据格式
- 二次开发或集成时的模板

如需详细说明，请查阅各子项目下的 README 或文档。
