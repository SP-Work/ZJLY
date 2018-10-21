package com.otitan.main.fragment

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.bin.david.form.data.column.Column
import com.bin.david.form.data.table.MapTableData
import com.bin.david.form.data.table.TableData
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.otitan.base.BaseFragment
import com.otitan.main.adapter.LykjAdapter
import com.otitan.main.viewmodel.LykjViewModel
import com.otitan.main.viewmodel.ResourceManageViewModel
import com.otitan.model.ResourceModel
import com.otitan.model.ResultModel
import com.otitan.ui.mview.ILykj
import com.otitan.ui.mview.IResourceManage
import com.otitan.util.TitanItemDecoration
import com.otitan.zjly.BR
import com.otitan.zjly.R
import com.otitan.zjly.databinding.FmLykjBinding
import com.otitan.zjly.databinding.FmResourceManageBinding
import org.jetbrains.anko.collections.forEachWithIndex
import java.lang.Exception

/**
 * 林业科技
 */
class LykjFragment : BaseFragment<FmLykjBinding, LykjViewModel>(), ILykj {

    var viewmodel: LykjViewModel? = null
    var data: ResultModel<Any>? = null
    var barDataset: BarDataSet? = null

    private val colorArray = arrayOf(R.color.colorPrimaryDark, R.color.ccc_red, R.color.ccc_blue, R.color.ccc_beige,
            R.color.problem, R.color.motorway, R.color.trunk, R.color.primary,
            R.color.secondary, R.color.orange, R.color.tertiary, R.color.colorPrimary)

    override fun initContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): Int {
        return R.layout.fm_lykj
    }

    override fun initVariableId(): Int {
        return BR.viewmodel
    }

    override fun initViewModel(): LykjViewModel {
        if (viewmodel == null) {
            viewmodel = LykjViewModel(activity, this)
        }
        return viewmodel as LykjViewModel
    }

    override fun initData() {
        super.initData()
        setHasOptionsMenu(true)
        binding.toolbarLykj.title = "林业科技"
        (activity!! as AppCompatActivity).setSupportActionBar(binding.toolbarLykj)
        binding.toolbarLykj.setNavigationOnClickListener { activity?.finish() }

        binding.spinnerType.setSelection(0, true)
        binding.spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewmodel?.let {
                    it.getData(p2 + 3)
                    it.type = p2 + 3
                }
            }
        }

        binding.spinnerIndex.setSelection(0, true)
        binding.spinnerIndex.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewmodel?.let {
                    val type = (p0 as Spinner).getItemAtPosition(p2).toString()
                    if (type == "科技标准") {
                        it.isKjbz.set(true)
                        it.getData(binding.spinnerType.selectedItemPosition + 3)
                        it.type = binding.spinnerType.selectedItemPosition + 3
                    } else if (type != "") {
                        it.isKjbz.set(false)
                        it.getData(p2 + 1)
                        it.type = p2 + 1
                    }
                }
            }
        }

        val adapter = LykjAdapter(activity, viewmodel?.data?.data?.rows)
        val l = LinearLayoutManager(activity, OrientationHelper.VERTICAL, false)
        binding.rvLykj.layoutManager = l
        binding.rvLykj.addItemDecoration(TitanItemDecoration(activity!!, 0, 10))
        binding.rvLykj.adapter = adapter

        binding
    }

    override fun refresh(items: List<Any>?) {
        (binding.rvLykj.adapter as LykjAdapter).setData(items)
    }
}