package com.otitan.main.viewmodel

import android.content.Context
import android.databinding.ObservableBoolean
import com.otitan.TitanApplication
import com.otitan.base.BaseViewModel
import com.otitan.base.BindingAction
import com.otitan.base.BindingCommand
import com.otitan.data.Injection
import com.otitan.data.remote.RemoteDataSource
import com.otitan.model.LdzzModel
import com.otitan.model.LycyModel
import com.otitan.model.ResultModel
import com.otitan.ui.mview.IDataBase
import com.otitan.util.ToastUtil
import org.jetbrains.anko.toast

class LycyDataViewModel() : BaseViewModel() {

    var data: ResultModel<LycyModel<LycyModel.Sjgl>>? = null
    //请求返回列表数据集合
    val items = ArrayList<Any>()
    val dataRepository = Injection.provideDataRepository()
    val isFinishRefreshing = ObservableBoolean(false)
    val isFinishLoading = ObservableBoolean(false)
    val hasMore = ObservableBoolean(true)
    var mView: IDataBase? = null
    var year = 2015
    var page = 1
    var dqcode = "330000"

    constructor(context: Context?, mView: IDataBase) : this() {
        this.mContext = context
        this.mView = mView
    }

    override fun onCreate() {
        super.onCreate()
//        onRefresh.execute()
        mView?.startRefresh()
    }

    val onRefresh = BindingCommand(object : BindingAction {
        override fun call() {
            hasMore.set(true)
            page = 1
            getData(1)
        }
    })

    val onLoadMore = BindingCommand(object : BindingAction {
        override fun call() {
            getData(2)
        }
    })

    fun getData(requestCode: Int) {
        var auth = TitanApplication.loginResult?.access_token
        if (auth == null) {
            mContext?.toast("登录信息验证失败")
            return
        }
        auth = "Bearer $auth"
//        showDialog("加载中...")
        dataRepository.lycyData(auth, 1, dqcode, year, page, 10,
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
                        dismissDialog()
                        data = result as ResultModel<LycyModel<LycyModel.Sjgl>>
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
                            mView?.refresh()
                            this@LycyDataViewModel.page++
                        }
                    }
                })
    }

    fun showPicker() {
        mView?.showPicker()
    }
}