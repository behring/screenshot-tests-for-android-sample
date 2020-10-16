# screenshot-tests-for-android集成指南

[screenshot-tests-for-android](https://github.com/facebook/screenshot-tests-for-android)是一个基于截图的测试库。在android运行instrumentation tests的时候，该库可以快速准确的生成截图并断言和上次截图的差异变化，由facebook开源。

## 集成
1. 在项目根目录的`build.gradle`文件中添加gradle plugin：

    ```groovy
    buildscript {
        ...
        dependencies {
            ...
            classpath 'com.facebook.testing.screenshot:plugin:0.13.0'
        }
    }
    ```

2. 在项目工程(application/android library)目录的`build.gradle`文件中**引用插件**并**添加依赖**。
    
    ```groovy
    // 引用插件
    apply plugin: 'com.facebook.testing.screenshot'
        
    dependencies {
      ...
        // 添加依赖
      androidTestImplementation 'com.facebook.testing.screenshot:layout-hierarchy-common:0.13.0'
      androidTestImplementation 'com.facebook.testing.screenshot:layout-hierarchy-litho:0.13.0'
    }
    ```
    
3. 在`src/androidTest/java`目录下创建自定义`ScreenshotTestRunner`，`ScreenshotTestRunner`根据项目测试需求继承不同类型的TestRunner。`ScreenshotTestRunner`代码如下：

    ```kotlin
    package com.thoughtworks.mp.screenshot_tests_for_android_sample
    
    import android.os.Bundle
    import androidx.test.runner.AndroidJUnitRunner
    import com.facebook.litho.config.ComponentsConfiguration
    import com.facebook.testing.screenshot.ScreenshotRunner
    import com.facebook.testing.screenshot.layouthierarchy.LayoutHierarchyDumper
    import com.facebook.testing.screenshot.layouthierarchy.litho.LithoAttributePlugin
    import com.facebook.testing.screenshot.layouthierarchy.litho.LithoHierarchyPlugin
    
    class ScreenshotTestRunner : AndroidJUnitRunner() {
        companion object {
            init {
                ComponentsConfiguration.isDebugModeEnabled = true
                LayoutHierarchyDumper.addGlobalHierarchyPlugin(LithoHierarchyPlugin.getInstance())
                LayoutHierarchyDumper.addGlobalAttributePlugin(LithoAttributePlugin.getInstance())
            }
        }
    
        override fun onCreate(arguments: Bundle) {
            ScreenshotRunner.onCreate(this, arguments)
            super.onCreate(arguments)
        }
    
        override fun finish(resultCode: Int, results: Bundle) {
            ScreenshotRunner.onDestroy()
            super.finish(resultCode, results)
        }
    }   
    ```

4. 在项目工程(application/android library)目录的`build.gradle`文件中**修改`testInstrumentationRunner`**:
    ```groovy
    android {
      defaultConfig {
        ...
          // 修改testInstrumentationRunner为自定义Runner
          testInstrumentationRunner "com.thoughtworks.mp.screenshot_tests_for_android_sample.ScreenshotTestRunner"
      }
    }
    ```

5. 在`AndroidManifest.xml`中添加sdcard写权限：
    ```xml 
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    ```

## 测试
1. 在`src/androidTest/java`目录下创建测试文件并编写测试代码，例如`MainActivityUITest.kt`代码如下：
    ```kotlin
    package com.thoughtworks.mp.screenshot_tests_for_android_sample
    import android.content.Context
    import android.view.LayoutInflater
    import androidx.test.platform.app.InstrumentationRegistry
    import com.facebook.litho.LithoView
    import com.facebook.testing.screenshot.Screenshot
    import com.facebook.testing.screenshot.ViewHelpers
    import org.junit.Test
    
    class MainActivityUITest {
        @Test
        fun testDefault() {
            val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
            val inflater = LayoutInflater.from(targetContext)
            val view = inflater.inflate(R.layout.activity_main, null, false)
            ViewHelpers.setupView(view).setExactWidthDp(300).layout()
            Screenshot.snap(view).record()
        }
    }
    ```
    
2. 运行测试生成截图。`com.facebook.testing.screenshot`插件为我们提供了如下gradle task:
    - clean<App Variant>AndroidTestScreenshots
        
        > 清除最后一次生成的截图report
        
    - pull<App Variant>AndroidTestScreenshots
        
        > 从设备中获取截图
        
    - record<App Variant>AndroidTestScreenshots
        
        > 安装并运行截图测试，然后为后续验证记录输出结果
        
    - run<App Variant>AndroidTestScreenshots
        
        > 安装并运行截图测试，然后生成report
        
    - verify<App Variant>AndroidTestScreenshots
        > 安装并运行截图测试，然后对比上一次的记录的截图验证它们的输出
        
    
    因此，首次运行截图测试时，应该调用`./gradlew recordDebugAndroidTestScreenshots`生成base截图，作为验证UI样式的基准。之后运行`./gradlew verifyDebugAndroidTestScreenshots`来判断UI是否发生变化。
    
    