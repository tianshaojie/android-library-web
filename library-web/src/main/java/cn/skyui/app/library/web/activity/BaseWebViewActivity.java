package cn.skyui.app.library.web.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import cn.skyui.app.web.R;

/**
 * 处理通用逻辑（二级界面请使用子类侧滑返回BaseSwipeBackActivity）
 * @author tianshaojie
 * @date 2018/1/27
 */
public abstract class BaseWebViewActivity extends AppCompatActivity {

    // 让5.0之前的系统可以用Vector图标
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    /**
     * 沉浸式状态栏和沉浸式导航栏管理，支持Android 4.4 以上
     */
    protected ImmersionBar mImmersionBar;
    private TextView mToolbarTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 修复系统输入法Bug：在15<=API<=23存在内存泄漏，https://zhuanlan.zhihu.com/p/20828861
//        IMMLeaks.fixFocusedViewLeak(getApplication());
    }

    /**
     * 统一Toolbar：Activity布局内<include layout="@layout/toolbar"/>
     * 如果使用Toolbar，自动开启沉浸状态栏模式
     * 如果布局有toolbar，需要让ImmersionBar接管，否则配合左滑返回，背景不会到顶部
     * 1. 标题
     *  标题居中显示
     *  默认使用AndroidManifest的android:label属性值
     *  可以在onCreate->setContentView之后，调用setToolbarTitle更新Title
     * 2. Icon
     *  app:navigationIcon可以自定义放回按钮，也可代码设置
     * 3. 间距
     *  app:contentInsetStart=0 去掉默认左侧间距
     * @param layoutResID
     */
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            // 带Toolbar使用沉浸式，直接设置titleBar，结合SwipeBack，也需要在setContentView之后初始化ImmersionBar
            mImmersionBar = ImmersionBar.with(this)
                    .titleBar(toolbar)
                    .navigationBarColor(R.color.colorPrimary);
            mImmersionBar.init();

            if(toolbar instanceof Toolbar) {
                setSupportActionBar((Toolbar) toolbar);
                // 显示返回键
                if(getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }

            // 隐藏默认标题
            mToolbarTitle = toolbar.findViewById(R.id.toolbar_title);
            if (mToolbarTitle != null && getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        } else {
            initImmersionBar();
        }
    }

    /**
     * 统一Toolbar返回按钮处理
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 覆盖onTitleChanged：让 title 默认显示到 mToolbarTitle，而且不需要暴露 mToolbarTitle 属性
     * 1. 默认显示：AndroidManifest的android:label属性值
     * 2. 代码设置：直接调用Activity.setTitle()方法动态修改
     * @param title
     * @param color
     */
    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        if (mToolbarTitle != null) {
            mToolbarTitle.setText(title);
        }
    }

    /**
     * 布局没有Toolbar，初始化ImmersionBar
     * 子类可覆盖该方法，设置更多特殊属性，比如透明通栏
     */
    protected void initImmersionBar() {
        // 如果Activity+Fragment，Fragment自带Toolbar，并通过ToolImmersionBar.titleBar(toolbar)管理
        // 则Activity不用fitsSystemWindows(true)
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.init();

        // 如果Activity&子Fragment布局没有Toolbar，可否覆盖该方法设置fitsSystemWindows(true)
//        mImmersionBar = ImmersionBar.with(this)
//                .fitsSystemWindows(true)
//                .statusBarColor(R.color.colorPrimary)
//                .navigationBarColor(R.color.colorPrimary);
//        mImmersionBar.init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImmersionBar != null) {
            mImmersionBar.destroy();
        }
    }
}