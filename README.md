# music-s2-claude-code

本地音乐播放器，纯离线播放。

## 功能特性

- 扫描本地音乐文件
- 音乐播放/暂停/上一首/下一首
- 迷你播放器（显示专辑封面）
- 播放详情页（封面模式与歌词模式）
- 歌词显示与同步
- 日志记录（保存在 app 外部存储目录，自动限制大小）

## 版本历史

### v0.2.4 (2026-05-12)
- 优化 PlayerActivity - 使用协程替代 Handler 实现防抖
- 优化 LogUtils - 添加应用退出时的资源释放
- 优化 MusicService - 改进通知更新逻辑，避免重复更新
- 优化 MusicViewModel - 添加状态保存与恢复功能
- 更新 Constants - 添加状态保存相关键

### v0.2.3 (2026-05-12)
- 修复 GitHub Actions Release 权限问题
- 添加 contents: write 权限配置到 workflow

### v0.2.2 (2026-05-11)
- 修复 LyricParser.kt 类型推断编译错误
- 优化 LogUtils 性能，使用缓冲写入
- 优化 MusicService，添加音频焦点处理
- 优化 PlayerActivity，改进歌词滚动性能

### v0.2.1 (2026-05-11)
- 修复 0.2.0 版本的编译错误
- 修复 MusicViewModel 缺少 withContext import
- 修复 MusicViewModel 处理 nullable currentPosition
- 修复 LyricParser 时间计算表达式
- 修复 PlayerActivity 扩展函数调用方式

### v0.2.0 (2026-05-11)
- 添加播放详情页
- 支持封面模式与歌词模式切换
- 实现歌词解析与同步
- 添加迷你播放器点击跳转功能

### v0.1.3 (2026-05-11)
- 修复 APK 发布到 Release 页面

### v0.1.2 (2026-05-11)
- 代码重构与性能优化
- SongAdapter 使用 DiffUtil 提高列表性能
- LogUtils 添加日志文件大小限制（5MB）
- MainActivity 使用协程替代 Handler
- 迷你播放器显示专辑封面
- 添加常量和扩展函数

### v0.1.1 (2026-05-11)
- 修复 GitHub Actions 构建问题

### v0.1.0 (2026-05-11)
- 初始版本
- 基础播放器框架
- 本地音乐扫描功能
- 基础播放控制 UI
