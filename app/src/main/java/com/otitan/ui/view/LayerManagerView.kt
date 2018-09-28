package com.otitan.ui.view

import android.app.Activity
import android.util.Log
import android.view.View
import com.esri.arcgisruntime.data.FeatureTable
import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.data.ShapefileFeatureTable
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.otitan.main.view.MapCenterActivity
import com.otitan.model.MyLayer
import com.otitan.ui.adapter.LayerManagerAdapter
import com.otitan.ui.mview.ILayerManager
import com.otitan.ui.mview.ILayerManagerItem
import com.otitan.ui.mview.IMap
import com.otitan.ui.vm.LayerManagerViewModel
import com.otitan.util.ResourcesManager
import com.otitan.util.Utils
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.activity_map_center.*
import kotlinx.android.synthetic.main.share_tckz.*
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileNotFoundException
import kotlin.properties.Delegates

/**
 * 图层控制
 */
class LayerManagerView() : ILayerManager, ILayerManagerItem {
    private var activity: MapCenterActivity by Delegates.notNull()
    private var iMap: IMap by Delegates.notNull()
    var viewModel: LayerManagerViewModel by Delegates.notNull()
    var imgLayer: ArcGISTiledLayer? = null
    var adapter: LayerManagerAdapter? = null
    val checked = HashMap<String, Boolean>()

    constructor(activity: MapCenterActivity, iMap: IMap) : this() {
        this.activity = activity
        this.iMap = iMap
    }

    fun initView() {
        val groups = ResourcesManager.getInstances(activity).getOtmsFolder()
        if (groups.isEmpty()) {
            return
        }
        val childs = ResourcesManager.getInstances(activity).getChildData(groups)
        if (adapter == null) {
            childs.forEach { map ->
                map.forEach { k, list ->
                    list.forEach { file ->
                        checked[file.absolutePath] = false
                    }
                }
            }
            adapter = LayerManagerAdapter(activity, groups, childs, this, checked)
        }
        activity.tckzExplv.setAdapter(adapter)
//        Utils.setExpendHeight(adapter!!, activity.tckzExplv)
    }

    override fun showLayer(type: Int) {
        when (type) {
            1 -> {
                iMap.getOpenStreetLayer()?.isVisible = viewModel.base.get()
            }
            2 -> {
                val list = ResourcesManager.getInstances(activity).getImgTitlePath()
                if (viewModel.img.get()) {
                    if (list.size == 1) {
                        imgLayer = ArcGISTiledLayer(list[0].absolutePath)
                        activity.mapview.map.operationalLayers.add(imgLayer)
                    } else if (list.size > 1) {
                        activity.include_img.visibility = View.VISIBLE
                        activity.imgManager.setData(list)
                    }
                } else {
                    val layers = activity.mapview.map.operationalLayers
                    if (list.size == 1) {
                        if (layers.contains(imgLayer)) {
                            layers.remove(imgLayer)
                        }
                    } else if (list.size > 1) {
                        imgLayer = null
                        activity.include_img.visibility = View.GONE
                        val tempList = ArrayList<Layer>()
                        list.forEach { file ->
                            layers.forEach { layer ->
                                if (layer.name == file.name.split(".")[0]) {
                                    tempList.add(layer)
                                }
                            }
                        }
                        layers.removeAll(tempList)
                    }
                }
            }
            3 -> {
            }
        }
    }

    override fun setExtent(type: Int) {
        when (type) {
            1 -> {
                activity.mapview.setViewpointGeometryAsync(iMap.getOpenStreetLayer()?.fullExtent)
            }
            2 -> {
                if (imgLayer != null) {
                    activity.mapview.setViewpointGeometryAsync(imgLayer?.fullExtent)
                }
            }
            3 -> {
            }
        }
    }

    override fun addLayer(file: File, checked: Boolean) {
        if (checked) {
            addLayers(file)
        } else {
//            val layers = activity.mv_map.map.basemap.baseLayers
            val layers = iMap.getLayers()
            val temp = ArrayList<MyLayer>()
            layers.forEach {
                if (it.cName == file.name.split(".")[0] && file.parent == it.pName) {
                    activity.mapview.map.operationalLayers.remove(MyLayer.layer)
                    temp.add(it)
                }
            }
            layers.removeAll(temp)
        }
    }

    override fun setExtent(file: File) {
        val geometrys = ArrayList<Geometry?>()
        val layers = iMap.getLayers()
        layers.forEach {
            if (it.cName == file.name.split(".")[0] && file.parent == it.pName) {
                geometrys.add(MyLayer.layer?.fullExtent)
            }
        }
        val totalExtent = GeometryEngine.combineExtents(geometrys)
        totalExtent.let {
            activity.mapview.setViewpointGeometryAsync(it)
        }
    }

    override fun close() {
        activity.include_icTckz.visibility = View.GONE
    }

    fun addLayers(file: File) {

        if(file.exists() && file.name.endsWith("geodatabase")){
            addGoedatabase(file)
        }else if(file.exists() && file.name.endsWith("shp")){
            addShp(file)
        }
    }

    fun addGoedatabase(file: File){

        try {
            val gdb = Geodatabase(file.absolutePath) ?: return
            gdb.loadAsync()
            gdb.addDoneLoadingListener {
                val list = gdb.geodatabaseFeatureTables
                list.forEach {
                    val layer = FeatureLayer(it)
                    layer.isVisible = true
                    if (iMap.getOpenStreetLayer() != null) {
//                        val sp1 = iMap.getOpenStreetLayer()?.spatialReference!!.wkid
//                        val sp2 = layer.spatialReference?.wkid
//                        if (sp1 != sp2) {
//                            activity.toast("加载数据与基础底图投影系不同,无法加载")
//                            return@forEach
//                        }
                        activity.mapview.map.operationalLayers.add(layer)
                        val myLayer = MyLayer()
                        myLayer.pName = file.parent
                        myLayer.cName = file.name.split(".")[0]
                        myLayer.lName = layer.name
                        MyLayer.layer = layer
                        myLayer.path = file.absolutePath
                        MyLayer.table = it
                        iMap.getLayers().add(myLayer)
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Log.e("tag", "数据库错误:$e")
        } catch (e: RuntimeException) {
            e.printStackTrace()
            Log.e("tag", "数据库错误:$e")
            activity.toast("数据库错误")
        }
    }

    fun addShp(file: File){
        var path = file.path
        var table = ShapefileFeatureTable(path)
        table.loadAsync()
        table.addDoneLoadingListener {
            var statuts = table.loadStatus
            if(statuts == LoadStatus.LOADING){
                var featureLayer:FeatureLayer = FeatureLayer(table)
                activity.mapview.map.operationalLayers.add(featureLayer)
            }else{
                val error = table.getLoadError().message
                val tip = "Shapefile feature table failed to load: " + table.getLoadError().toString()
                Log.e("加载shp数据", error)
            }
        }
    }
}