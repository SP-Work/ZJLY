package com.otitan.main.fragment

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import com.bin.david.form.data.column.Column
import com.bin.david.form.data.table.TableData
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.otitan.base.BaseFragment
import com.otitan.main.viewmodel.LycyViewModel
import com.otitan.main.widgets.BarChartInit
import com.otitan.main.widgets.SmartTableStyle
import com.otitan.model.LycyModel
import com.otitan.model.ResourceModel
import com.otitan.model.ResultModel
import com.otitan.ui.mview.ILycy
import com.otitan.util.ScreenTool
import com.otitan.zjly.BR
import com.otitan.zjly.R
import com.otitan.zjly.databinding.FmLycyBinding
import java.lang.Exception

/**
 * 林业产业
 */
class LycyFragment : BaseFragment<FmLycyBinding, LycyViewModel>(), ILycy {

    var viewmodel: LycyViewModel? = null

    override fun initContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): Int {
        return R.layout.fm_lycy
    }

    override fun initVariableId(): Int {
        return BR.viewmodel
    }

    override fun initViewModel(): LycyViewModel {
        if (viewmodel == null) {
            viewmodel = LycyViewModel(activity, this)
        }
        return viewmodel as LycyViewModel
    }

    override fun initData() {
        super.initData()
        setHasOptionsMenu(true)
        binding.toolbarLycy.title = "林业产业"
        (activity!! as AppCompatActivity).setSupportActionBar(binding.toolbarLycy)
        binding.toolbarLycy.setNavigationOnClickListener { activity?.finish() }

        binding.spinnerYear.setSelection(0, true)
        binding.spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewmodel?.let {
                    val year = (p0 as Spinner).getItemAtPosition(p2).toString().toInt()
                    it.getData(it.type, year)
                    it.year = year
                }
            }
        }

        binding.spinnerType.setSelection(0, true)
        binding.spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewmodel?.let {
                    it.getData(p2 + 1, it.year)
                    it.type = p2 + 1
                }
            }
        }

        viewmodel?.let {
            BarChartInit.init(binding.chartLycy, it.keyList)
        }
        SmartTableStyle.setTableStyle(binding.dataTableLycy, context)
    }

    override fun setBarChartData(list: ArrayList<BarEntry>) {
        BarChartInit.setBarChartData(binding.chartLycy, list, activity,
                BarDataSet(list, "单位:亿元"))
    }

    override fun setTableData(tableData: List<Any>) {
        when (viewmodel?.type) {
            1 -> {
                val columnList = ArrayList<Column<LycyModel.Zcy>>()
                val name = Column<LycyModel.Zcy>("地区", "Name")
                val dycy = Column<LycyModel.Zcy>("第一产业", "First")
                val decy = Column<LycyModel.Zcy>("第二产业 ", "Second")
                val dscy = Column<LycyModel.Zcy>("第三产业", "Third")
                columnList.add(name)
                columnList.add(dycy)
                columnList.add(decy)
                columnList.add(dscy)

                binding.dataTableLycy.tableData = TableData("浙江省各地市总产值（单位：亿元）", tableData, columnList as List<Column<Any>>)
            }
            2 -> {
                val columnList = ArrayList<Column<LycyModel.Dycy>>()
                val name = Column<LycyModel.Dycy>("地区", "Name")
                val column1 = Column<LycyModel.Dycy>("林木育种和育苗", "lmyzym")
                val column2 = Column<LycyModel.Dycy>("营造林", "yzl")
                val column3 = Column<LycyModel.Dycy>("木材和竹材采运", "mczccy")
                val column4 = Column<LycyModel.Dycy>("经济林产品的种植与采集", "jjlcpzzcj")
                val column5 = Column<LycyModel.Dycy>("花卉及其他观赏植物种植", "hhjqtgszwzz")
                val column6 = Column<LycyModel.Dycy>("陆生野生动物繁育与利用", "lsysdwfyly")
                val column7 = Column<LycyModel.Dycy>("其他", "qt")
                columnList.add(name)
                columnList.add(column1)
                columnList.add(column2)
                columnList.add(column3)
                columnList.add(column4)
                columnList.add(column5)
                columnList.add(column6)
                columnList.add(column7)
                binding.dataTableLycy.tableData = TableData("浙江省各地市总产值（单位：亿元）", tableData, columnList as List<Column<Any>>)
            }
            3 -> {
                val columnList = ArrayList<Column<LycyModel.Decy>>()
                val name = Column<LycyModel.Decy>("地区", "Name")
                val column1 = Column<LycyModel.Decy>("木质工艺品和木质文教体育用品制造", "mztzwzpzz")
                val column2 = Column<LycyModel.Decy>("木竹藤家具制造", "mztjjzz")
                val column3 = Column<LycyModel.Decy>("木竹苇浆造纸和纸制品", "mzwjzzzp")
                val column4 = Column<LycyModel.Decy>("林产化学产品制造", "lchxcpzz")
                val column5 = Column<LycyModel.Decy>("木材加工和木竹藤棕苇制品制造", "mzgypmzwjtyypzz")
                val column6 = Column<LycyModel.Decy>("非木质林产品加工制造业", "fmzlcpjgzzy")
                val column7 = Column<LycyModel.Decy>("其他", "qt")
                columnList.add(name)
                columnList.add(column1)
                columnList.add(column2)
                columnList.add(column3)
                columnList.add(column4)
                columnList.add(column5)
                columnList.add(column6)
                columnList.add(column7)
                binding.dataTableLycy.tableData = TableData("浙江省各地市总产值（单位：亿元）", tableData, columnList as List<Column<Any>>)
            }
            4 -> {
                val columnList = ArrayList<Column<LycyModel.Dscy>>()
                val name = Column<LycyModel.Dscy>("地区", "Name")
                val column1 = Column<LycyModel.Dscy>("林业生产服务", "lyscfw")
                val column2 = Column<LycyModel.Dscy>("林业旅游与休闲服务", "lylyyxxfw")
                val column3 = Column<LycyModel.Dscy>("林业生态服务", "lystfw")
                val column4 = Column<LycyModel.Dscy>("林业专业技术服务", "lyzyjsfw")
                val column5 = Column<LycyModel.Dscy>("林业公共管理及其他组织服务", "lygggljqtzzfw")
                val column6 = Column<LycyModel.Dscy>("其他", "qt")
                columnList.add(name)
                columnList.add(column1)
                columnList.add(column2)
                columnList.add(column3)
                columnList.add(column4)
                columnList.add(column5)
                columnList.add(column6)
                binding.dataTableLycy.tableData = TableData("浙江省各地市总产值（单位：亿元）", tableData, columnList as List<Column<Any>>)
            }
        }
    }

}