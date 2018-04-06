# PicMaster——小巧轻量的手机图片压缩处理工具

继[quickresize](https://github.com/liuzhushaonian/quickresize)后推出的手机版图片压缩工具，从此再也不需要在电脑上才能压缩图片了。

# 下载链接：

[百毒云](https://pan.baidu.com/s/1qiDQmEJrCQnyX-upfmH7zw)

密码: fph5

# 功能列表：
- **图片压缩！** 支持图片大小缩小与质量压缩，减小图片体积再也不成问题！
- **旋转图片！** 45°、90°、180°、360°随你转动，让你的图片秀出花来！
- **翻转图片！** 我就知道旋转图片也无法满足你，所以上下翻转或是左右翻转功能也一并给你,这下你的图片岂不是要翻出花来。
- **添加水印！** 翻出花来的图片都不如你的水印好看！所以我还为你提供了添加水印操作，支持图片水印与自定义文字水印，另外还有9个位置提供你放置水印喔（笑~）
- **格式转换！** 如此简单的功能我就不吹了。

# 未支持功能：

- <S>自定义文字水印大小及颜色</S>
- 自定义输出路径
- 同名文件不覆盖
- 其他……

不支持的原因很简单，这些操作太麻烦了暂时不写。

# 使用方法：

在界面内设置好需要处理的参数，点击选择图片&开始处理即可选择图片并处理。

# 效果

![image](https://github.com/liuzhushaonian/PicMaster/blob/master/screen_short/Screenshot_20180405-194323.png)

![image](https://github.com/liuzhushaonian/PicMaster/blob/master/screen_short/Screenshot_20180405-195034.png)

![image](https://github.com/liuzhushaonian/PicMaster/blob/master/screen_short/dmzj-1522254200969.png)

![image](https://github.com/liuzhushaonian/PicMaster/blob/master/screen_short/Screenshot_20180406-171937.png)

# 吐槽

说实话这是基于Android的Bitmap写出来的压缩处理，相比之前的quickresize，这次真的是连核心算法都要自己写，所以比上次的开发（上次一个周末搞定）要耗费更多的时间。可我并非是想要吐槽这点，算法自己写我也认了，毕竟只是封装Bitmap的各种操作，再搭配一个建造者模式，还不算什么难事。我要吐槽的是UI设计，不知道脑子是不是进屎了，UI界面跟PC版并无太大差别，而UI做起来却比PC版的恶心百倍，我昨天（2018-4-4）真的快要做吐了，心想我连核心算法都弄好了，却要被一个UI给劝退，苦笑的同时，更为自己进了屎一般的设计感到气愤，我也一度认为这是最没有效率的UI设计了，尽管用起来真的很简洁。

# 感谢

[glide](https://github.com/bumptech/glide) 大名鼎鼎的glide我想就不用介绍了吧

[ColorPicker](https://github.com/DingMouRen/ColorPicker) 一款Android颜色选择器

感谢坚持不懈的自己吧，这次连核心算法都是自己写的了……



# 开源协议

[MIT](https://github.com/liuzhushaonian/PicMaster/blob/master/LICENSE)

大概意思是所有代码你都可以任意使用，但是出了事这个锅我不背。