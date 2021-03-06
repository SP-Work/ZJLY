package com.otitan.main.fragment

import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.otitan.base.BaseFragment
import com.otitan.main.adapter.DataManageAdapter
import com.otitan.main.adapter.ResourceManageDataAdapter
import com.otitan.main.viewmodel.LdzzDataViewModel
import com.otitan.main.viewmodel.LycyDataViewModel
import com.otitan.main.viewmodel.SlfhDataViewModel
import com.otitan.main.widgets.AddressPicker
import com.otitan.ui.mview.IDataBase
import com.otitan.util.TitanItemDecoration
import com.otitan.zjly.BR
import com.otitan.zjly.R
import com.otitan.zjly.databinding.FmLdzzDataBinding
import com.otitan.zjly.databinding.FmLycyDataBinding
import com.otitan.zjly.databinding.FmSlfhDataBinding
import org.jetbrains.anko.toast

/**
 * 林业产业 数据管理
 */
class LycyDataFragment : BaseFragment<FmLycyDataBinding, LycyDataViewModel>(), IDataBase {

    var viewmodel: LycyDataViewModel? = null
    var picker: AddressPicker<Any>? = null

    override fun initContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): Int {
        return R.layout.fm_lycy_data
    }

    override fun initVariableId(): Int {
        return BR.viewmodel
    }

    override fun initViewModel(): LycyDataViewModel {
        if (viewmodel == null) {
            viewmodel = LycyDataViewModel(activity, this)
        }
        return viewmodel as LycyDataViewModel
    }

    override fun initViewObservable() {
        viewModel.isFinishRefreshing.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                binding.refreshLayout.finishRefreshing()
            }
        })
        viewModel.isFinishLoading.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                binding.refreshLayout.finishLoadmore()
            }
        })
        viewModel.hasMore.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                binding.refreshLayout.setEnableLoadmore((sender as ObservableBoolean).get())
            }
        })
    }

    override fun initData() {
        super.initData()
        setHasOptionsMenu(true)
        binding.toolbarLycyData.title = "数据查询"
        (activity!! as AppCompatActivity).setSupportActionBar(binding.toolbarLycyData)
        binding.toolbarLycyData.setNavigationOnClickListener { activity?.finish() }

        if (picker == null && context != null) {
            picker = AddressPicker(context!!, "地区选择", object : OnOptionsSelectListener {
                override fun onOptionsSelect(options1: Int, options2: Int, options3: Int, v: View?) {
                    val tx = picker?.options1Items?.get(options1)?.Name + "  " +
                            picker?.options2Items?.get(options1)?.get(options2)?.Name + "  " +
                            picker?.options3Items?.get(options1)?.get(options2)?.get(options3)?.Name
                    binding.tvAddress.text = tx

                    val code1 = picker?.options1Items?.get(options1)?.Code
                    val code2 = picker?.options2Items?.get(options1)?.get(options2)?.Code
                    val code3 = picker?.options3Items?.get(options1)?.get(options2)?.get(options3)?.Code

                    viewmodel?.let {
                        it.dqcode = if (!code3.isNullOrBlank()) {
                            code3!!
                        } else if (!code2.isNullOrBlank()) {
                            code2!!
                        } else {
                            code1!!
                        }
//                        it.onRefresh.execute()
                        startRefresh()
                    }
                }
            })
        }

        binding.spinnerYear.setSelection(0, true)
        binding.spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewmodel?.let {
                    val year = (p0 as Spinner).getItemAtPosition(p2).toString().toInt()
                    it.year = year
//                    it.onRefresh.execute()
                    startRefresh()
                }
            }
        }

        binding.rvDataManage.layoutManager = LinearLayoutManager(activity, OrientationHelper.VERTICAL, false)
        viewmodel?.let {
            val adapter = DataManageAdapter(context, it.items, "林业产业")
            binding.rvDataManage.adapter = adapter
        }
    }

    override fun startRefresh() {
        binding.refreshLayout.startRefresh()
    }

    override fun refresh() {
        val adapter = binding.rvDataManage.adapter
        if (adapter != null && viewmodel != null) {
            (adapter as DataManageAdapter).setData(viewmodel!!.items, null)
        }
    }

    override fun showPicker() {
        if (picker?.isLoaded == true) {
            picker?.picker?.show()
        } else {
            context?.let {
                context?.toast("数据初始化中，请稍候")
            }
        }
    }

}