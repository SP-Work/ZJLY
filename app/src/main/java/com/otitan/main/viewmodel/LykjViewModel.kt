package com.otitan.main.viewmodel

import android.content.Context
import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import com.github.mikephil.charting.data.BarEntry
import com.otitan.TitanApplication
import com.otitan.base.BaseViewModel
import com.otitan.base.BindingAction
import com.otitan.base.BindingCommand
import com.otitan.data.Injection
import com.otitan.data.remote.RemoteDataSource
import com.otitan.model.LykjModel
import com.otitan.model.ResultModel
import com.otitan.ui.mview.ILykj
import com.otitan.util.ToastUtil
import org.jetbrains.anko.toast

class LykjViewModel() : BaseViewModel() {

    var data: ResultModel<LykjModel<Any>>? = null
    //item的数据集合
    var items = ArrayList<Any>()
    val dataRepository = Injection.provideDataRepository()
    var mView: ILykj? = null
    val isFinishRefreshing = ObservableBoolean(false)
    val isFinishLoading = ObservableBoolean(false)
    val hasMore = ObservableBoolean(true)
    var type = ObservableInt(1)
    var page = 1
    var keyWord = ""

    constructor(context: Context?, mView: ILykj) : this() {
        this.mContext = context
        this.mView = mView
    }

    override fun onCreate() {
        super.onCreate()
//        getData(type.get(), page, 1)
        mView?.startRefresh()
    }

    val onRefresh = BindingCommand(object : BindingAction {
        override fun call() {
            hasMore.set(true)
            page = 1
            getData(type.get(), page, 1)
        }
    })

    val onLoadMore = BindingCommand(object : BindingAction {
        override fun call() {
            getData(type.get(), page, 2)
        }
    })

    fun getData(type: Int, page: Int, requestCode: Int) {
        var auth = TitanApplication.loginResult?.access_token
        if (auth == null) {
            mContext?.toast("用户信息验证失败")
            return
        }
        auth = "Bearer $auth"
//        showDialog("加载中...")
        dataRepository.lykj(auth, type, page, 10, keyWord,
                object : RemoteDataSource.mCallback {
                    override fun onFailure(info: String) {
//                        dismissDialog()
                        when (requestCode) {
                            1 -> isFinishRefreshing.set(!isFinishRefreshing.get())
                            2 -> isFinishLoading.set(!isFinishLoading.get())
                        }
                        ToastUtil.setToast(mContext, info)
                    }

                    override fun onSuccess(result: Any) {
//                        dismissDialog()
                        data = result as ResultModel<LykjModel<Any>>
                        when (requestCode) {
                            1 -> {
                                items.clear()
                                isFinishRefreshing.set(!isFinishRefreshing.get())
                            }
                            2 -> {
                                isFinishLoading.set(!isFinishLoading.get())
                            }
                        }
                        if (data?.data == null || data?.data?.rows?.size == 0) {
                            mContext?.toast("没有数据")
                            hasMore.set(false)
                            return
                        }
                        data?.data?.rows?.let {
                            items.addAll(it)
                            mView?.refresh(items)
                            this@LykjViewModel.page++
                        }
                    }
                })
    }

}